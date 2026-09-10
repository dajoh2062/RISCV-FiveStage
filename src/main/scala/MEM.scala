package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
    val aluResult = Input(UInt(32.W))
    val rd = Input(UInt(5.W))
    val controlSignals = Input(new ControlSignals)
    val storeData = Input(UInt(32.W))

    val aluResultOut = Output(UInt(32.W))
    val memoryDataOut = Output(UInt(32.W))
    val rdOut = Output(UInt(5.W))
    val controlSignalsOut = Output(new ControlSignals)
      
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  //DMEM.io.dataIn      := 0.U
  DMEM.io.dataAddress := io.aluResult
  //DMEM.io.writeEnable := false.B
  DMEM.io.dataIn := io.storeData
  DMEM.io.writeEnable := io.controlSignals.memWrite

  //io.aluResultOut := io.aluResult
  //io.rdOut := io.rd
  //io.controlSignalsOut := io.controlSignals
  io.memoryDataOut := DMEM.io.dataOut

  val aluResultRegister = RegInit(0.U(32.W))
  val rdRegister = RegInit(0.U(5.W))
  val controlSignalsRegister = RegInit(0.U.asTypeOf(new ControlSignals))

  aluResultRegister := io.aluResult
  rdRegister := io.rd
  controlSignalsRegister := io.controlSignals

  io.aluResultOut := aluResultRegister
  io.rdOut := rdRegister
  io.controlSignalsOut := controlSignalsRegister


  printf(
    "MEM: aluResult=%d rd=%d regWrite=%d\n",
    io.aluResultOut,
    io.rdOut,
    io.controlSignalsOut.regWrite
  )
}
