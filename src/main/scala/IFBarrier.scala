package FiveStage
import chisel3._

class IFBarrier extends Module {
  val io = IO(new Bundle {
    val PCIn = Input(UInt(32.W))
    val PCOut = Output(UInt(32.W))

    val stall = Input(Bool())

    val instructionIn = Input(new Instruction)
    val instructionOut = Output(new Instruction)
  })

  // Save the PC so it lines up with the instruction coming back from memory.
  val PCRegister = RegInit(0.U(32.W))

  // Keep the same PC while ID is waiting.
  when(!io.stall) {
    PCRegister := io.PCIn
  }
  io.PCOut := PCRegister

  // Instruction memory already adds a cycle, so no extra register is needed here.
  io.instructionOut := io.instructionIn
}
