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
  val IF  = Module(new InstructionFetch)
  // val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)

  val IFBarrier = Module(new IFBarrier).io

  val EX = Module(new Execute)
  
  val IDBarrier = Module(new IDBarrier).io

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

    // connect ID to IF
    IFBarrier.PCIn := IF.io.PC
    IFBarrier.instructionIn := IF.io.instruction

    ID.io.instruction := IFBarrier.instructionOut


    IDBarrier.readData1In := ID.io.readData1
    IDBarrier.readData2In := ID.io.readData2
    IDBarrier.immediateIn := ID.io.immediate
    IDBarrier.op2SelectIn := ID.io.op2Select
    IDBarrier.ALUopIn := ID.io.ALUop
    IDBarrier.rdIn := ID.io.rd
    IDBarrier.controlSignalsIn := ID.io.controlSignals

    EX.io.readData1 := IDBarrier.readData1Out
    EX.io.readData2 := IDBarrier.readData2Out
    EX.io.immediate := IDBarrier.immediateOut
    EX.io.op2Select := IDBarrier.op2SelectOut
    EX.io.ALUop := IDBarrier.ALUopOut
    EX.io.rd := IDBarrier.rdOut
    EX.io.controlSignals := IDBarrier.controlSignalsOut


    MEM.io.aluResult := EX.io.aluResult
    MEM.io.rd := EX.io.rdOut
    MEM.io.controlSignals := EX.io.controlSignalsOut
    MEM.io.storeData := EX.io.storeData

    WB.io.aluResult := MEM.io.aluResultOut
    WB.io.rd := MEM.io.rdOut
    WB.io.regWrite := MEM.io.controlSignalsOut.regWrite

    WB.io.memoryData := MEM.io.memoryDataOut
    WB.io.memRead := MEM.io.controlSignalsOut.memRead

    ID.io.writeData := WB.io.writeData
    ID.io.writeAddress := WB.io.writeAddress
    ID.io.writeEnable := WB.io.writeEnable

    
}
