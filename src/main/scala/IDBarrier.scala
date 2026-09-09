package FiveStage

import chisel3._

class IDBarrier extends Module {
  val io = IO(new Bundle {
    val readData1In = Input(UInt(32.W))
    val readData1Out = Output(UInt(32.W))

    val readData2In = Input(UInt(32.W))
    val readData2Out = Output(UInt(32.W))

    val immediateIn = Input(SInt(32.W))
    val immediateOut = Output(SInt(32.W))

    val op2SelectIn = Input(UInt(1.W))
    val op2SelectOut = Output(UInt(1.W))

    val ALUopIn = Input(UInt(4.W))
    val ALUopOut = Output(UInt(4.W))

    val rdIn = Input(UInt(5.W))
    val rdOut = Output(UInt(5.W))

    val controlSignalsIn = Input(new ControlSignals)
    val controlSignalsOut = Output(new ControlSignals)
  })

  val readData1Register = RegInit(0.U(32.W))
  val readData2Register = RegInit(0.U(32.W))
  val immediateRegister = RegInit(0.S(32.W))
  val op2SelectRegister = RegInit(0.U(1.W))
  val ALUopRegister = RegInit(ALUOps.DC)
  val rdRegister = RegInit(0.U(5.W))
  val controlSignalsRegister = RegInit(0.U.asTypeOf(new ControlSignals))

  readData1Register := io.readData1In
  readData2Register := io.readData2In
  immediateRegister := io.immediateIn
  op2SelectRegister := io.op2SelectIn
  ALUopRegister := io.ALUopIn
  rdRegister := io.rdIn
  controlSignalsRegister := io.controlSignalsIn

  io.readData1Out := readData1Register
  io.readData2Out := readData2Register
  io.immediateOut := immediateRegister
  io.op2SelectOut := op2SelectRegister
  io.ALUopOut := ALUopRegister
  io.rdOut := rdRegister
  io.controlSignalsOut := controlSignalsRegister
}