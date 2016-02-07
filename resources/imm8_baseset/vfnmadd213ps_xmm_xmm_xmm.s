  .text
  .globl target
  .type target, @function

#! file-offset 0
#! rip-offset  0
#! capacity    6 bytes

# Text                              #  Line  RIP   Bytes  Opcode                        
.target:                            #        0     0      OPC=<label>                   
  vmovdqu %xmm1, %xmm6              #  1     0     4      OPC=vmovdqu_xmm_xmm           
  vmovaps %xmm2, %xmm7              #  2     0x4   4      OPC=vmovaps_xmm_xmm           
  vmovaps %xmm3, %xmm1              #  3     0x8   4      OPC=vmovaps_xmm_xmm           
  vfnmadd132ps %ymm6, %ymm1, %ymm7  #  4     0xc   5      OPC=vfnmadd132ps_ymm_ymm_ymm  
  vmovaps %xmm7, %xmm1              #  5     0x11  4      OPC=vmovaps_xmm_xmm           
  retq                              #  6     0x15  1      OPC=retq                      
                                                                                        
.size target, .-target
