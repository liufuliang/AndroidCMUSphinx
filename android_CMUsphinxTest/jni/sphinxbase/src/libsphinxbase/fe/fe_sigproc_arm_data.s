@定义变量区，
	.globl fft_order_shift_table
	.global get_scores_4b_feat_var
	.global eval_topn_var
	.data
	.align 4
	.type fft_order_shift_table, %object	
	.size fft_order_shift_table, 180
	.type get_scores_4b_feat_var, %object	
	.size get_scores_4b_feat_var, 80
	.type eval_topn_var, %object	
	.size eval_topn_var, 16
	
	

@ 因为 fft_order 是 8 BIT 的，所以最大是 256 个有效数据
fft_order_shift_table:
	.word 0x00000001,0x00000002,0x00000004,0x00000008,0x00000010,0x00000020,0x00000040,0x00000080
	.word 0x00000100,0x00000200,0x00000400,0x00000800,0x00001000,0x00002000,0x00004000,0x00008000
	.word 0x00010000,0x00020000,0x00040000,0x00080000,0x00100000,0x00200000,0x00400000,0x00800000
	.word 0x01000000,0x02000000,0x04000000,0x08000000,0x10000000,0x20000000,0x40000000,0x80000000
	
@变量区 4*32 = 128
fft_var_k:
	.word	0
	
@  分别存储 n1,n2,n4
@	33,34,35
fft_var_n1:
	.word	0
fft_var_n2:
	.word	0
fft_var_n4:
	.word	0
	
@  分别存储 (1<<n1),(1<<n2),(1<<n4)
@	36,37,38
fft_var_1_lsl_n1:
	.word	0
fft_var_1_lsl_n2:
	.word	0
fft_var_1_lsl_n4:
	.word	0
	
	
@  分别存储 i1,i2,i3,i4
@	39,40,41,42
fft_var_i1:
	.word	0
fft_var_i2:
	.word	0
fft_var_i3:
	.word	0
fft_var_i4:
	.word	0
	
@	43
fft_var_m_sub_n1:
	.word	0
	
@	44
fft_var_reserved:
	.word	0
	
@变量区 4*13 = 52

@0--44  == 45 *4 =180


get_scores_4b_feat_var:
@	uint8 w_den[4 * 16];
	.space 64
@ 	uint8 *pid_cw0, *pid_cw1, *pid_cw2, *pid_cw3;
	.word	0,0,0,0
	
	
eval_topn_var:
	.word	0,0,0,0