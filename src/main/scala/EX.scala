package FiveStage
import chisel3._
import chisel3.util.MuxLookup


class ALU extends Module {
  val io = IO(new Bundle {
    val op1 = Input(UInt(32.W))
    val op2 = Input(UInt(32.W))
    val aluOp = Input(UInt(4.W))

    val aluResult = Output(UInt(32.W))

  })

  val ALUopMap = Array(
    ALUOps.ADD -> (io.op1 + io.op2),
    ALUOps.SUB -> (io.op1 - io.op2),

    ALUOps.SLT  -> (io.op1.asSInt < io.op2.asSInt).asUInt,
    ALUOps.SLTU -> (io.op1 < io.op2).asUInt,

    ALUOps.SLL -> (io.op1 << io.op2(4, 0)),
    ALUOps.SRL -> (io.op1 >> io.op2(4, 0)),
    ALUOps.SRA -> (io.op1.asSInt >> io.op2(4, 0)).asUInt,   

    ALUOps.AND ->(io.op1 & io.op2),
    ALUOps.OR  ->(io.op1 | io.op2), 
    ALUOps.XOR ->(io.op1 ^ io.op2),

    ALUOps.COPY_B -> io.op2
  )
  
  // MuxLookup API: https://github.com/freechipsproject/chisel3/wiki/Muxes-and-Input-Selection#muxlookup
  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)
}

class Execute extends Module{
    val io = IO(new Bundle{
        val readData1 = Input(UInt(32.W))
        val readData2 = Input(UInt(32.W))
        val immediate = Input(SInt(32.W))

        val PC = Input(UInt(32.W))
        val op1Select = Input(UInt(1.W))
        val op2Select = Input(UInt(1.W))
        val ALUop = Input(UInt(4.W))

        val rd = Input(UInt(5.W))
        val controlSignals = Input(new ControlSignals)
        val branchType = Input(UInt(3.W))


        val aluResult = Output(UInt(32.W))
        val rdOut = Output(UInt(5.W))
        val controlSignalsOut = Output(new ControlSignals)
        val storeData = Output(UInt(32.W))
        val redirect = Output(Bool())
        val targetPC = Output(UInt(32.W))
    })

    val ALU = Module(new ALU)

    ALU.io.op1 := Mux(
      io.op1Select === Op1Select.PC,
      io.PC,
      io.readData1
    )

    ALU.io.op2 := Mux(
        io.op2Select === Op2Select.imm,
        io.immediate.asUInt,
        io.readData2
    )

    val equal = io.readData1 === io.readData2
    val less = io.readData1.asSInt < io.readData2.asSInt
    val lessU = io.readData1 < io.readData2

    // Greater-than-or-equal includes equality and is the inverse of less-than.
    val branchCondition = MuxLookup(io.branchType, false.B, Array(
      branchType.beq  -> equal,
      branchType.neq  -> !equal,
      branchType.lt   -> less,
      branchType.gte  -> !less,
      branchType.ltu  -> lessU,
      branchType.gteu -> !lessU
    ))

    io.redirect := io.controlSignals.jump || (io.controlSignals.branch && branchCondition)

    // JAL uses PC as its base; JALR uses rs1 and clears target bit 0.
    val isJalr = io.controlSignals.jump &&
      (io.op1Select === Op1Select.rs1)

    io.targetPC := Mux(
      isJalr,
      ALU.io.aluResult & "hFFFFFFFE".U(32.W),
      ALU.io.aluResult
    )

    ALU.io.aluOp := io.ALUop

    // The target goes to IF, while the return address travels to writeback.
    io.aluResult := Mux(
      io.controlSignals.jump,
      io.PC + 4.U,
      ALU.io.aluResult
    )
    io.rdOut := io.rd
    io.controlSignalsOut := io.controlSignals
    io.storeData := io.readData2


    printf(
        "EX: op1=%d op2=%d alu=%d result=%d rd=%d regWrite=%d\n",
        ALU.io.op1,
        ALU.io.op2,
        io.ALUop,
        io.aluResult,
        io.rd,
        io.controlSignals.regWrite
    )
}

