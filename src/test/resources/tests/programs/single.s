main:
  lui x5, 0x12345
  lui x6, 0x80000
  lui x7, 0xFFFFF
  lui x5, 0
  lui x0, 0x12345

    auipc x5, 0
    auipc x6, 0x1
    auipc x7, 0x12345
    auipc x8, 0x80000
    auipc x9, 0xFFFFF
    auipc x5, 0
    auipc x0, 0x12345
    addi x5, x0, 7
    addi x6, x0, 7
    addi x7, x0, 9

    beq x5, x6, equal
    addi x10, x0, 99

equal:
  addi x11, x0, 11
  beq x5, x7, finish
  addi x12, x0, 12

finish:
  done
