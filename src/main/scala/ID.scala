package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      //added input to id for
      val instruction = Input(new Instruction)
      // +
      val writeEnable = Input(Bool())
      val writeAddress = Input(UInt(5.W))
      val writeData = Input(UInt(32.W))


      // added outputs for alu
      val readData1 = Output(UInt(32.W))
      val readData2 = Output(UInt(32.W))
      val rd = Output(UInt(5.W))
      val immediate = Output(SInt(32.W))

      val controlSignals = Output(new ControlSignals)
      val op1Select = Output(UInt(1.W))
      val op2Select = Output(UInt(1.W))
      val ALUop = Output(UInt(4.W))
      }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  //registers.io.readAddress1 := 0.U
  //registers.io.readAddress2 := 0.U
  // register fields from instruction
  registers.io.readAddress1 := io.instruction.registerRs1
  registers.io.readAddress2 := io.instruction.registerRs2

  //registers.io.writeEnable  := false.B
  //registers.io.writeAddress := 0.U
  //registers.io.writeData    := 0.U
  // + 
  registers.io.writeEnable := io.writeEnable
  registers.io.writeAddress := io.writeAddress
  registers.io.writeData := io.writeData

  //decoder.instruction := 0.U.asTypeOf(new Instruction)
  // added 
  // ID prepares the values and control signals that the execute stage will need
  decoder.instruction := io.instruction

  io.readData1 := registers.io.readData1
  io.readData2 := registers.io.readData2
  io.rd := io.instruction.registerRd
  io.immediate := Mux(
    decoder.immType === ImmFormat.STYPE,
    io.instruction.immediateSType,
    io.instruction.immediateIType
  )

  io.controlSignals := decoder.controlSignals
  io.op1Select := decoder.op1Select
  io.op2Select := decoder.op2Select
  io.ALUop := decoder.ALUop
  when(!testHarness.registerSetup.setup) {
  printf(
    "ID: instr=%x rs1=%d rs2=%d rd=%d imm=%d regWrite=%d op2=%d alu=%d\n",
    io.instruction.instruction,
    io.instruction.registerRs1,
    io.instruction.registerRs2,
    io.instruction.registerRd,
    io.immediate,
    io.controlSignals.regWrite,
    io.op2Select,
    io.ALUop
  )
}
}
