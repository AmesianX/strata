  .text
  .globl target
  .type target, @function

#! file-offset 0
#! rip-offset  0
#! capacity    3 bytes

# Text                 #  Line  RIP   Bytes  Opcode               
.target:               #        0     0      OPC=<label>          
  movb %ah, %al        #  1     0     2      OPC=movb_r8_rh       
  movsbq %al, %rdx     #  2     0x2   4      OPC=movsbq_r64_r8    
  cwtd                 #  3     0x6   2      OPC=cwtd             
  popcntl %edx, %r15d  #  4     0x8   5      OPC=popcntl_r32_r32  
  cwtl                 #  5     0xd   1      OPC=cwtl             
  movq %rax, %rdx      #  6     0xe   3      OPC=movq_r64_r64     
  adcw %dx, %ax        #  7     0x11  3      OPC=adcw_r16_r16     
  retq                 #  8     0x14  1      OPC=retq             
                                                                  
.size target, .-target
