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

    val aluResultOut = Output(UInt(32.W))
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
  DMEM.io.dataIn      := 0.U
  DMEM.io.dataAddress := 0.U
  DMEM.io.writeEnable := false.B

  io.aluResultOut := io.aluResult
  io.rdOut := io.rd
  io.controlSignalsOut := io.controlSignals

  printf(
    "MEM: aluResult=%d rd=%d regWrite=%d\n",
    io.aluResultOut,
    io.rdOut,
    io.controlSignalsOut.regWrite
  )
}
