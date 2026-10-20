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

    val PCIn = Input(UInt(32.W))
    val PCOut = Output(UInt(32.W))

    val op1SelectIn = Input(UInt(1.W))
    val op1SelectOut = Output(UInt(1.W))

    val op2SelectIn = Input(UInt(1.W))
    val op2SelectOut = Output(UInt(1.W))

    val ALUopIn = Input(UInt(4.W))
    val ALUopOut = Output(UInt(4.W))

    val rdIn = Input(UInt(5.W))
    val rdOut = Output(UInt(5.W))

    // which registers have been used
    val rs1In = Input(UInt(5.W))
    val rs1Out = Output(UInt(5.W))

    val rs2In = Input(UInt(5.W))
    val rs2Out = Output(UInt(5.W))

    val stall = Input(Bool())
    val forwardedData1In = Input(UInt(32.W))
    val forwardedData2In = Input(UInt(32.W))

    val controlSignalsIn = Input(new ControlSignals)
    val controlSignalsOut = Output(new ControlSignals)

    val branchTypeIn = Input(UInt(3.W))
    val branchTypeOut = Output(UInt(3.W))

    // used if branch or jump inst in ex
    val bubble = Input(Bool())
  })

  val readData1Register = RegInit(0.U(32.W))
  val readData2Register = RegInit(0.U(32.W))
  val immediateRegister = RegInit(0.S(32.W))
  val PCRegister = RegInit(0.U(32.W))
  val op1SelectRegister = RegInit(0.U(1.W))
  val op2SelectRegister = RegInit(0.U(1.W))
  val ALUopRegister = RegInit(ALUOps.DC)
  val rdRegister = RegInit(0.U(5.W))

  val rs1Register = RegInit(0.U(5.W))
  val rs2Register = RegInit(0.U(5.W))
  val controlSignalsRegister = RegInit(0.U.asTypeOf(new ControlSignals))
  val branchTypeRegister = RegInit(branchType.DC)



  when(io.stall) {
    // The current EX instruction still needs an operand.
    readData1Register := io.forwardedData1In
    readData2Register := io.forwardedData2In
  }.elsewhen(io.bubble) {
    // Let the current EX instruction leave, but admit no new work.
    controlSignalsRegister := ControlSignals.nop
    ALUopRegister := ALUOps.DC
    rdRegister := 0.U
  }.otherwise {
  // accept new instruction data like usual if no stall
    readData1Register := io.readData1In
    readData2Register := io.readData2In
    immediateRegister := io.immediateIn
    PCRegister := io.PCIn
    op1SelectRegister := io.op1SelectIn
    op2SelectRegister := io.op2SelectIn
    ALUopRegister := io.ALUopIn
    rdRegister := io.rdIn
    rs1Register := io.rs1In
    rs2Register := io.rs2In
    controlSignalsRegister := io.controlSignalsIn
    branchTypeRegister := io.branchTypeIn
  }


  io.readData1Out := readData1Register
  io.readData2Out := readData2Register
  io.immediateOut := immediateRegister
  io.PCOut := PCRegister
  io.op1SelectOut := op1SelectRegister
  io.op2SelectOut := op2SelectRegister
  io.ALUopOut := ALUopRegister
  io.rdOut := rdRegister

  io.rs1Out := rs1Register
  io.rs2Out := rs2Register
  io.controlSignalsOut := controlSignalsRegister
  io.branchTypeOut := branchTypeRegister
}