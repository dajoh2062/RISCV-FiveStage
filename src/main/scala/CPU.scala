package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._

class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  // val IFBarrier  = Module(new IFBarrier).io
  // val IDBarrier  = Module(new IDBarrier).io
  // val EXBarrier  = Module(new EXBarrier).io
  // val MEMBarrier = Module(new MEMBarrier).io

  // Create the stages and the registers between them.
  val ID  = Module(new InstructionDecode)
  val IDBarrier = Module(new IDBarrier).io
  val IF  = Module(new InstructionFetch)
  val IFBarrier = Module(new IFBarrier).io
  // val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)
  val EX = Module(new Execute)
  val EXBarrier = Module(new EXBarrier).io
  val WB = Module(new WriteBack)

  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC := IDBarrier.PCOut

  /**
    TODO: Your code here
    */

  // Only data hazards stall fetch.
  val fetchStall = EX.io.loadUseHazard

  // Check if an older instruction has a newer value for the branch in ID.
  val exWrites =
    IDBarrier.controlSignalsOut.regWrite &&
    (IDBarrier.rdOut =/= 0.U)

  val memWrites =
    EXBarrier.controlSignalsOut.regWrite &&
    (EXBarrier.rdOut =/= 0.U)

  val exMatchesRs1 = exWrites && (IDBarrier.rdOut === ID.io.rs1)
  val exMatchesRs2 = exWrites && (IDBarrier.rdOut === ID.io.rs2)
  val memMatchesRs1 = memWrites && (EXBarrier.rdOut === ID.io.rs1)
  val memMatchesRs2 = memWrites && (EXBarrier.rdOut === ID.io.rs2)

  // EX has the newest result, then MEM. ID already handles the WB bypass.
  val branchOperand1 = Mux(
    exMatchesRs1,
    EX.io.aluResult,
    Mux(memMatchesRs1, EXBarrier.aluResultOut, ID.io.readData1)
  )

  val branchOperand2 = Mux(
    exMatchesRs2,
    EX.io.aluResult,
    Mux(memMatchesRs2, EXBarrier.aluResultOut, ID.io.readData2)
  )

  // If the load value is not ready, let EX handle the branch as before.
  val waitingForLoad =
    (IDBarrier.controlSignalsOut.memRead &&
      (exMatchesRs1 || exMatchesRs2)) ||
    (EXBarrier.controlSignalsOut.memRead &&
      ((!exMatchesRs1 && memMatchesRs1) ||
       (!exMatchesRs2 && memMatchesRs2)))

  // BEQ and BNE can be handled early when both operands are ready.
  val isEqualityBranch =
    ID.io.controlSignals.branch &&
    ((ID.io.branchType === branchType.beq) ||
     (ID.io.branchType === branchType.neq))

  val earlyBranchHandled =
    isEqualityBranch &&
    !waitingForLoad &&
    !fetchStall &&
    !EX.io.redirect

  val operandsEqual = branchOperand1 === branchOperand2
  val earlyBranchTaken = Mux(
    ID.io.branchType === branchType.beq,
    operandsEqual,
    !operandsEqual
  )

  val earlyRedirect = earlyBranchHandled && earlyBranchTaken
  val earlyTargetPC = IFBarrier.PCOut + ID.io.immediate.asUInt

  IFBarrier.PCIn := IF.io.PC
  IFBarrier.instructionIn := IF.io.instruction

  IF.io.stall := fetchStall
  IF.io.stallPC := IFBarrier.PCOut
  IFBarrier.stall := fetchStall

  // Redirect for taken branches and jumps.
  IF.io.redirect := EX.io.redirect || earlyRedirect
  IF.io.targetPC := Mux(EX.io.redirect, EX.io.targetPC, earlyTargetPC)

  // Discard the wrong-path instruction currently in ID.
  IDBarrier.bubble := EX.io.redirect

  ID.io.instruction := IFBarrier.instructionOut

  IDBarrier.readData1In := ID.io.readData1
  IDBarrier.readData2In := ID.io.readData2
  IDBarrier.immediateIn := ID.io.immediate
  IDBarrier.op2SelectIn := ID.io.op2Select
  IDBarrier.ALUopIn := ID.io.ALUop

  IDBarrier.stall := EX.io.loadUseHazard
  IDBarrier.forwardedData1In := EX.io.forwardedData1
  IDBarrier.forwardedData2In := EX.io.forwardedData2
  IDBarrier.rs1In := ID.io.rs1
  IDBarrier.rs2In := ID.io.rs2
  IDBarrier.rdIn := ID.io.rd
  IDBarrier.controlSignalsIn := ID.io.controlSignals
  // This branch is already handled, so EX should not branch again.
  when(earlyBranchHandled) {
    IDBarrier.controlSignalsIn.branch := false.B
  }
  IDBarrier.PCIn := IFBarrier.PCOut
  IDBarrier.op1SelectIn := ID.io.op1Select
  IDBarrier.branchTypeIn := ID.io.branchType

  // Pass the saved instruction from ID into EX.
  EX.io.PC := IDBarrier.PCOut
  EX.io.op1Select := IDBarrier.op1SelectOut
  EX.io.readData1 := IDBarrier.readData1Out
  EX.io.readData2 := IDBarrier.readData2Out
  EX.io.immediate := IDBarrier.immediateOut
  EX.io.op2Select := IDBarrier.op2SelectOut
  EX.io.ALUop := IDBarrier.ALUopOut

  EX.io.rs1 := IDBarrier.rs1Out
  EX.io.rs2 := IDBarrier.rs2Out
  EX.io.rd := IDBarrier.rdOut
  EX.io.controlSignals := IDBarrier.controlSignalsOut
  EX.io.branchType := IDBarrier.branchTypeOut

  //information from the instruction in MEM.
  EX.io.memRd := EXBarrier.rdOut
  EX.io.memRegWrite := EXBarrier.controlSignalsOut.regWrite
  EX.io.memRead := EXBarrier.controlSignalsOut.memRead
  EX.io.memResult := EXBarrier.aluResultOut

  //information from the instruction in WB.
  EX.io.wbRd := WB.io.writeAddress
  EX.io.wbRegWrite := WB.io.writeEnable
  EX.io.wbResult := WB.io.writeData

  // EX supplies the values to capture at the next clock edge.
  EXBarrier.aluResultIn := EX.io.aluResult
  EXBarrier.rdIn := EX.io.rdOut
  EXBarrier.controlSignalsIn := EX.io.controlSignalsOut
  EXBarrier.storeDataIn := EX.io.storeData
  EXBarrier.bubble := EX.io.loadUseHazard

  // MEM uses the values captured at the previous clock edge.
  MEM.io.aluResult := EXBarrier.aluResultOut
  MEM.io.rd := EXBarrier.rdOut
  MEM.io.controlSignals := EXBarrier.controlSignalsOut
  MEM.io.storeData := EXBarrier.storeDataOut

  // Send the final result through WB and back to the register file.
  WB.io.aluResult := MEM.io.aluResultOut
  WB.io.rd := MEM.io.rdOut
  WB.io.regWrite := MEM.io.controlSignalsOut.regWrite

  WB.io.memoryData := MEM.io.memoryDataOut
  WB.io.memRead := MEM.io.controlSignalsOut.memRead

  ID.io.writeData := WB.io.writeData
  ID.io.writeAddress := WB.io.writeAddress
  ID.io.writeEnable := WB.io.writeEnable
}
