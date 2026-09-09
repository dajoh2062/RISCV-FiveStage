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
    ALUOps.SUB -> (io.op1 - io.op2)
  )
  
  // MuxLookup API: https://github.com/freechipsproject/chisel3/wiki/Muxes-and-Input-Selection#muxlookup
  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUopMap)
}

class Execute extends Module{
    val io = IO(new Bundle{
        val readData1 = Input(UInt(32.W))
        val readData2 = Input(UInt(32.W))
        val immediate = Input(SInt(32.W))

        val op2Select = Input(UInt(1.W))
        val ALUop = Input(UInt(4.W))

        val rd = Input(UInt(5.W))
        val controlSignals = Input(new ControlSignals)

        val aluResult = Output(UInt(32.W))
        val rdOut = Output(UInt(5.W))
        val controlSignalsOut = Output(new ControlSignals)
    })

    val ALU = Module(new ALU)

    ALU.io.op1 := io.readData1

    ALU.io.op2 := Mux(
        io.op2Select === Op2Select.imm,
        io.immediate.asUInt,
        io.readData2
    )

    ALU.io.aluOp := io.ALUop

    io.aluResult := ALU.io.aluResult
    io.rdOut := io.rd
    io.controlSignalsOut := io.controlSignals


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



