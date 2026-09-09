package FiveStage
import chisel3._

class IFBarrier extends Module {
    val io = IO(new Bundle {
        val PCIn = Input(UInt(32.W))
        val PCOut = Output(UInt(32.W))

        val instructionIn = Input(new Instruction)
        val instructionOut = Output(new Instruction)
  })

    val PCRegister = RegInit(0.U(32.W))

    PCRegister := io.PCIn
    io.PCOut := PCRegister

    io.instructionOut := io.instructionIn

}