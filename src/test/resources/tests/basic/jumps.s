main:
  jal x0, start
  addi x10, x0, 99
function:
  addi x11, x0, 11
  addi x5, x5, 1
  jalr x5, x5, zero
  addi x12, x0, 99
start:
  jal x5, function
  addi x13, x5, 0
  jalr x6, x0, finish
  addi x14, x0, 99
finish:
  addi x15, x6, 0
  done
