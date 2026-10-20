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
  testHarness.currentPC  := IF.testHarness.PC


  /**
    TODO: Your code here
    */

    // used for stalling instructions after a branch/jump
    val controlInID =
      ID.io.controlSignals.branch ||
      ID.io.controlSignals.jump

    val controlInEX =
      IDBarrier.controlSignalsOut.branch ||
      IDBarrier.controlSignalsOut.jump

    // EX can resolve the branch/jump once its operands are available.
    val controlResolved =
      controlInEX && !EX.io.loadUseHazard

    // Wait for a load, or stop fetching ahead of a branch/jump.
    val fetchStall =
      (EX.io.loadUseHazard || controlInID) && !controlResolved

    IFBarrier.PCIn := IF.io.PC
    IFBarrier.instructionIn := IF.io.instruction

    IF.io.stall := fetchStall
    IF.io.stallPC := IFBarrier.PCOut
    IFBarrier.stall := fetchStall

    // Resume fetching for both taken and untaken branches.
    IF.io.redirect := controlResolved
    IF.io.targetPC := Mux(
      EX.io.redirect,
      EX.io.targetPC,
      IDBarrier.PCOut + 4.U
    )

    IDBarrier.bubble := controlInEX
      

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
    IDBarrier.PCIn := IFBarrier.PCOut
    IDBarrier.op1SelectIn := ID.io.op1Select
    IDBarrier.branchTypeIn := ID.io.branchType




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

    WB.io.aluResult := MEM.io.aluResultOut
    WB.io.rd := MEM.io.rdOut
    WB.io.regWrite := MEM.io.controlSignalsOut.regWrite

    WB.io.memoryData := MEM.io.memoryDataOut
    WB.io.memRead := MEM.io.controlSignalsOut.memRead

    ID.io.writeData := WB.io.writeData
    ID.io.writeAddress := WB.io.writeAddress
    ID.io.writeEnable := WB.io.writeEnable

    
}
