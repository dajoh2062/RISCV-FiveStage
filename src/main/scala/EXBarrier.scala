package FiveStage

import chisel3._

class EXBarrier extends Module {
  val io = IO(new Bundle {
    val aluResultIn = Input(UInt(32.W))
    val aluResultOut = Output(UInt(32.W))

    val storeDataIn = Input(UInt(32.W))
    val storeDataOut = Output(UInt(32.W))

    val rdIn = Input(UInt(5.W))
    val rdOut = Output(UInt(5.W))

    val controlSignalsIn = Input(new ControlSignals)
    val controlSignalsOut = Output(new ControlSignals)


    val bubble = Input(Bool())

  })

  val aluResultRegister = RegInit(0.U(32.W))
  val storeDataRegister = RegInit(0.U(32.W))
  val rdRegister = RegInit(0.U(5.W))
  val controlSignalsRegister = RegInit(0.U.asTypeOf(new ControlSignals))

  aluResultRegister := io.aluResultIn
  storeDataRegister := io.storeDataIn
  rdRegister := io.rdIn
  controlSignalsRegister := Mux(
      io.bubble,
      ControlSignals.nop,
      io.controlSignalsIn
    )

  io.aluResultOut := aluResultRegister
  io.storeDataOut := storeDataRegister
  io.rdOut := rdRegister
  io.controlSignalsOut := controlSignalsRegister
}