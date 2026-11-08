package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )

  /**
    * TODO: Add input signals for handling events such as jumps

    * TODO: Add output signal for the instruction.
    * The instruction is of type Bundle, which means that you must
    * use the same syntax used in the testHarness for IMEM setup signals
    * further up.
    */
  val io = IO(
    new Bundle {
      val PC = Output(UInt())                   // To IFBarrier.PCIn: address requested from instruction memory.
      val instruction = Output(new Instruction) // To IFBarrier.instructionIn, then ID.io.instruction.

      val stall = Input(Bool())                 // From EX.io.loadUseHazard via fetchStall in CPU.
      val stallPC = Input(UInt(32.W))           // From IFBarrier.PCOut: PC of the instruction held in ID.

      val redirect = Input(Bool())              // From EX.io.redirect: taken branch or jump.
      val targetPC = Input(UInt(32.W))          // From EX.io.targetPC: branch or jump destination.
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)

  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress

  /**
    * TODO: Your code here.
    *
    * You should expand on or rewrite the code below.
    */
  // CPU can redirect from ID or EX; a stall repeats the held instruction.
  val fetchAddress = Mux(
    io.redirect,
    io.targetPC,
    Mux(io.stall, io.stallPC, PC)
  )

  // The PC sent to IFBarrier must match the requested instruction.
  io.PC := fetchAddress
  IMEM.io.instructionAddress := fetchAddress

  when(io.redirect) {
    // We are already requesting targetPC during this cycle.
    PC := io.targetPC + 4.U
  }.elsewhen(!io.stall) {
    PC := PC + 4.U
  }

  // The memory returns the instruction requested on the previous cycle.
  val instruction = Wire(new Instruction)
  instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  // added to expose instruction from IF
  io.instruction := instruction
  // when(!testHarness.IMEMsetup.setup) {
  // printf("IF: PC=%x instr=%x\n", PC, instruction.instruction)
  // }

  /**
    * Setup. You should not change this code.
    */
  // Keep the CPU at the start while the test program is being loaded.
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }
}
