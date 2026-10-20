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

        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val rd = Input(UInt(5.W))
        val controlSignals = Input(new ControlSignals)
        val branchType = Input(UInt(3.W))

        //instruction currently in MEM.
        val memRd = Input(UInt(5.W))
        val memRegWrite = Input(Bool())
        val memRead = Input(Bool())
        val memResult = Input(UInt(32.W))

        //instruction currently in WB.
        val wbRd = Input(UInt(5.W))
        val wbRegWrite = Input(Bool())
        val wbResult = Input(UInt(32.W))

        val aluResult = Output(UInt(32.W))  
        val rdOut = Output(UInt(5.W))
        val controlSignalsOut = Output(new ControlSignals)
        val storeData = Output(UInt(32.W))
        val redirect = Output(Bool())
        val targetPC = Output(UInt(32.W))
        val loadUseHazard = Output(Bool())

        // forwarded data 
        val forwardedData1 = Output(UInt(32.W))
        val forwardedData2 = Output(UInt(32.W))
    })

    val ALU = Module(new ALU)

    val memMatchesRs1 =
      io.memRegWrite &&
      (io.memRd =/= 0.U) &&
      (io.memRd === io.rs1)

    val wbMatchesRs1 =
      io.wbRegWrite &&
      (io.wbRd =/= 0.U) &&
      (io.wbRd === io.rs1)

    val forwardedRs1 = Wire(UInt(32.W))

    // Default: use the value read from the register file.
    forwardedRs1 := io.readData1

    when(memMatchesRs1) {
      //prevents forwarding a lw-instrunctions address as its result
      when(!io.memRead) {
        forwardedRs1 := io.memResult
      }
    }.elsewhen(wbMatchesRs1) {
      forwardedRs1 := io.wbResult
    }

    val memMatchesRs2 =
      io.memRegWrite &&
      (io.memRd =/= 0.U) &&
      (io.memRd === io.rs2)

    val wbMatchesRs2 =
      io.wbRegWrite &&
      (io.wbRd =/= 0.U) &&
      (io.wbRd === io.rs2)

    val forwardedRs2 = Wire(UInt(32.W))

    forwardedRs2 := io.readData2

    when(memMatchesRs2) {
      when(!io.memRead) {
        forwardedRs2 := io.memResult
      }
    }.elsewhen(wbMatchesRs2) {
      forwardedRs2 := io.wbResult
    }

    io.forwardedData1 := forwardedRs1
    io.forwardedData2 := forwardedRs2

    val usesRs1 =
      io.controlSignals.branch ||
      (
        (io.op1Select === Op1Select.rs1) &&
        (io.ALUop =/= ALUOps.COPY_B) &&
        (io.ALUop =/= ALUOps.DC)
      )

    val usesRs2 =
      io.controlSignals.branch ||
      io.controlSignals.memWrite ||
      (
        (io.op2Select === Op2Select.rs2) &&
        (io.ALUop =/= ALUOps.DC)
      )

    io.loadUseHazard :=
      io.memRead &&
      (
        (usesRs1 && memMatchesRs1) ||
        (usesRs2 && memMatchesRs2)
      )
    
    ALU.io.op1 := Mux(
      io.op1Select === Op1Select.PC,
      io.PC,
      forwardedRs1
    )

    ALU.io.op2 := Mux(
      io.op2Select === Op2Select.imm,
      io.immediate.asUInt,
      forwardedRs2
    )


    // branching also supports forwared values
    val equal = forwardedRs1 === forwardedRs2
    val less = forwardedRs1.asSInt < forwardedRs2.asSInt
    val lessU = forwardedRs1 < forwardedRs2

    // Greater-than-or-equal includes equality and is the inverse of less-than.
    val branchCondition = MuxLookup(io.branchType, false.B, Array(
      branchType.beq  -> equal,
      branchType.neq  -> !equal,
      branchType.lt   -> less,
      branchType.gte  -> !less,
      branchType.ltu  -> lessU,
      branchType.gteu -> !lessU
    ))

    io.redirect :=
      !io.loadUseHazard &&
      (
        io.controlSignals.jump ||
        (io.controlSignals.branch && branchCondition)
      )

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

    // now supports forwarded values
    io.storeData := forwardedRs2


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

