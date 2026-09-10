package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class WriteBack extends Module{

    val io = IO(new Bundle{
        // Inputs from mem
        val aluResult = Input(UInt(32.W))
        val rd = Input(UInt(5.W))
        val regWrite = Input(Bool())
        val memoryData = Input(UInt(32.W))
        val memRead = Input(Bool())

        //outputs to register file
        val writeData = Output(UInt(32.W))
        val writeAddress = Output(UInt(5.W))
        val writeEnable = Output(Bool())


       
    })

    io.writeData := Mux(
        io.memRead,
        io.memoryData,
        io.aluResult
    )
    io.writeAddress := io.rd
    io.writeEnable := io.regWrite



}