  .text
  .globl target
  .type target, @function

#! file-offset 0
#! rip-offset  0
#! capacity    3 bytes

# Text               #  Line  RIP  Bytes  Opcode             
.target:             #        0    0      OPC=<label>        
  movsbl %ah, %ecx   #  1     0    3      OPC=movsbl_r32_rh  
  movsbq %bl, %r11   #  2     0x3  4      OPC=movsbq_r64_r8  
  testl %r11d, %ecx  #  3     0x7  3      OPC=testl_r32_r32  
  retq               #  4     0xa  1      OPC=retq           
                                                             
.size target, .-target
