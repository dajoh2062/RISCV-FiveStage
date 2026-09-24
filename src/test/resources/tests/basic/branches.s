main:
  addi x5, x0, -1
  addi x6, x0, 1
  addi x7, x0, 1
  beq x6, x7, branch_case_0
  addi x10, x0, 1
branch_case_0:
  addi x11, x0, 1
  beq x5, x6, branch_case_1
  addi x10, x0, 2
branch_case_1:
  addi x11, x0, 2
  beq x6, x5, branch_case_2
  addi x10, x0, 3
branch_case_2:
  addi x11, x0, 3
  bne x6, x7, branch_case_3
  addi x10, x0, 4
branch_case_3:
  addi x11, x0, 4
  bne x5, x6, branch_case_4
  addi x10, x0, 5
branch_case_4:
  addi x11, x0, 5
  bne x6, x5, branch_case_5
  addi x10, x0, 6
branch_case_5:
  addi x11, x0, 6
  blt x6, x7, branch_case_6
  addi x10, x0, 7
branch_case_6:
  addi x11, x0, 7
  blt x5, x6, branch_case_7
  addi x10, x0, 8
branch_case_7:
  addi x11, x0, 8
  blt x6, x5, branch_case_8
  addi x10, x0, 9
branch_case_8:
  addi x11, x0, 9
  bge x6, x7, branch_case_9
  addi x10, x0, 10
branch_case_9:
  addi x11, x0, 10
  bge x5, x6, branch_case_10
  addi x10, x0, 11
branch_case_10:
  addi x11, x0, 11
  bge x6, x5, branch_case_11
  addi x10, x0, 12
branch_case_11:
  addi x11, x0, 12
  bltu x6, x7, branch_case_12
  addi x10, x0, 13
branch_case_12:
  addi x11, x0, 13
  bltu x5, x6, branch_case_13
  addi x10, x0, 14
branch_case_13:
  addi x11, x0, 14
  bltu x6, x5, branch_case_14
  addi x10, x0, 15
branch_case_14:
  addi x11, x0, 15
  bgeu x6, x7, branch_case_15
  addi x10, x0, 16
branch_case_15:
  addi x11, x0, 16
  bgeu x5, x6, branch_case_16
  addi x10, x0, 17
branch_case_16:
  addi x11, x0, 17
  bgeu x6, x5, branch_case_17
  addi x10, x0, 18
branch_case_17:
  addi x11, x0, 18
  addi x12, x0, 3
backward:
  addi x12, x12, -1
  bne x12, x0, backward
  done
