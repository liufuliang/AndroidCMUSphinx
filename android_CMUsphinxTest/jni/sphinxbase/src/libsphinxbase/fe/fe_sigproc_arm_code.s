;@ For commercial use, separate licencing terms must be obtained.
;@ 	2014.03.12
;@ 	liu fu liang

	.include "gun_asm_debug.h"
	
	
	.text
	
	.global swap_int16_arm_code;
	.global swap_int32_arm_code;
	.global fe_pre_emphasis_fixed_point_arm_code;
	.global fe_hamming_window_arm_code
	.global fe_hamming_window_0_arm_code
	.global fe_hamming_window_1_arm_code
	.global fe_hamming_window_2_arm_code
	.global fe_fft_real_arm_code
	.global fe_fft_real_arm_code_test
	.global fe_mel_spec_arm_code
	.global fe_dct2_arm_code
	.global get_sum_arm_code
	.global acmod_activate_hmm_arm_code
	.global vcyber_print_sp_address
	.global get_scores_4b_feat_4_arm_code
	.global	mgau_norm_arm_code
	.global eval_topn_arm_code
	.global eval_cb_arm_code
	.global eval_topn_test
	
	.extern	fe_log_add

	.type swap_int16_arm_code,%function
	.type swap_int32_arm_code,%function
	.type fe_pre_emphasis_fixed_point_arm_code,%function
	.type fe_hamming_window_arm_code,%function
	.type fe_hamming_window_0_arm_code,%function
	.type fe_hamming_window_1_arm_code,%function
	.type fe_hamming_window_2_arm_code,%function
	.type fe_fft_real_arm_code,%function
	.type fe_fft_real_arm_code_test,%function
	.type fe_mel_spec_arm_code,%function
	.type fe_dct2_arm_code,%function
	.type get_sum_arm_code,%function
	.type acmod_activate_hmm_arm_code,%function
	.type vcyber_print_sp_address,%function
	.type get_scores_4b_feat_4_arm_code,%function
	.type mgau_norm_arm_code,%function
	.type eval_topn_arm_code,%function
	.type eval_cb_arm_code,%function
	.type eval_topn_test,%function
	
	
	.align 4;

@	debug_output_line	#0xaa
@	debug_output_data	r0

/*	
*	#define SWAP_INT16(x)	*(x) = ((0x00ff & (*(x))>>8) | (0xff00 & (*(x))<<8))
*/

swap_int16_arm_code:
	stmfd 	sp!,{r1-r12,lr}	
	ldrsh	r1,[r0]
	mov		r4,#0x00ff
	and		r2,r4,r1,lsr #8
	mov		r4,#0xff00
	and		r3,r4,r1,lsl #8
	orrs	r1,r2,r3
	strh	r1,[r0]
	ldmfd 	sp!,{r1-r12,pc}
	





/*	
*	#define SWAP_INT32(x)	*(x) = ((0x000000ff & (*(x))>>24) | \
*				(0x0000ff00 & (*(x))>>8) | \
*				(0x00ff0000 & (*(x))<<8) | \
*				(0xff000000 & (*(x))<<24))
*/
swap_int32_arm_code:
	stmfd 	sp!,{r1-r12,lr}	
	ldr		r1,[r0]
	mov		r4,#0x000000ff
	and		r2,r4,r1,lsr #24
	mov		r4,#0x0000ff00
	and		r3,r4,r1,lsr #8
	orrs	r3,r2,r3
	
	mov		r4,#0x00ff0000
	and		r2,r4,r1,lsl #8
	orrs	r3,r2,r3
	
	mov		r4,#0xff000000
	and		r2,r4,r1,lsl #24
	orrs	r1,r2,r3

	str		r1,[r0]
	ldmfd 	sp!,{r1-r12,pc}
	
	
/*	
*	extern void fe_pre_emphasis_fixed_point_arm_code(short *p_in,int *p_out,int len,int fxd_alpha);
*
*	for (i = 1; i < len; ++i)
*        out[i] = ((fixed32)in[i] << DEFAULT_RADIX)
*            - (fixed32)in[i-1] * fxd_alpha;
*/            
fe_pre_emphasis_fixed_point_arm_code:
	stmfd 	sp!,{r4-r12,lr}
	subs	r2,r2,#1	@	len
	mov		r4,#0		@	i
	cmp		r2,r4		@	len > i
	bls		end
	
	mov		r7,r0		@	src addr
	add		r9,r1,#4
loop:
@	i + 0
	ldrsh	r8,[r7,#2]
	
	ldrsh	r5,[r7],#2
	mul		r6,r3,r5						@ (fixed32)in[i-1] * fxd_alpha;

	rsbs	r5,r6,r8,lsl #DEFAULT_RADIX
	str		r5,[r9],#4
	
@	i + 1
	ldrsh	r8,[r7,#2]
	
	ldrsh	r5,[r7],#2
	mul		r6,r3,r5						@ (fixed32)in[i-1] * fxd_alpha;

	rsbs	r5,r6,r8,lsl #DEFAULT_RADIX
	str		r5,[r9],#4
	
@	i + 2
	ldrsh	r8,[r7,#2]
	
	ldrsh	r5,[r7],#2
	mul		r6,r3,r5						@ (fixed32)in[i-1] * fxd_alpha;

	rsbs	r5,r6,r8,lsl #DEFAULT_RADIX
	str		r5,[r9],#4	
	
@	i + 3
	ldrsh	r8,[r7,#2]
	
	ldrsh	r5,[r7],#2
	mul		r6,r3,r5						@ (fixed32)in[i-1] * fxd_alpha;

	rsbs	r5,r6,r8,lsl #DEFAULT_RADIX
	str		r5,[r9],#4			
	
	
	
	add		r4,r4,#4	@	i++
	cmp		r2,r4		@	len > i
	bhi		loop
end:
	ldmfd 	sp!,{r4-r12,pc}
	
	
	
	
/*	
*	extern void fe_hamming_window_arm_code(int * in,int in_len, int * window,  int remove_dc);
*          
*	#define FIXMUL_ANY(a,b,r) ({				\
*      int cl, ch, _a = a, _b = b;			\
*      __asm__ ("smull %0, %1, %2, %3\n"			\
*	   "mov %0, %0, lsr %4\n"			\
*	   "orr %0, %0, %1, lsl %5\n"			\
*	   : "=&r" (cl), "=&r" (ch)			\
*	   : "r" (_a), "r" (_b), "i" (r), "i" (32-(r)));\
*      cl; })
*
*	
*	#define COSMUL(x,y) FIXMUL_ANY(x,y,30)
*	
*	if (remove_dc) {
*		frame_t mean = 0;
*
*
*		for (i = 0; i < in_len; i++)
*			mean += in[i];
*		mean /= in_len;
*		for (i = 0; i < in_len; i++)
*			in[i] -= (frame_t)mean;
*	}
*
*    for (i = 0; i < in_len/2; i++) {
*        in[i] = COSMUL(in[i], window[i]);
*        in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);
*    }
*/

@fe_hamming_window_arm_code:
@	stmfd 	sp!,{r4-r12,lr}
@	b		debug
@@	debug_output_line	#0xa1
@@	debug_output_data	r2
@@	debug_output_data	r3
@	tst		r3,#0xff		@ if (remove_dc)
@	beq		loop3
@	
@@	debug_output_line	#0xa2
@	mov 	r4,#0		@ frame_t mean = 0;
@	mov		r5,#0		@ i = 0
@	cmp		r5,r1		@ i < in_len
@	bge		loop1
@loop0:
@	ldr		r7,[r0,r5,lsl #2]
@	adds	r4,r4,r7
@	
@	add		r5,r5,#1
@	cmp		r5,r1
@	blt		loop0	
@
@@	debug_output_line	#0xa3
@@	debug_output_data	r4
@@	debug_output_data	r2
@@	debug_output_data	r1
@loop1:
@	mov		r5,r0
@	mov		r6,r2
@	
@	mov		r0,r4
@	bl		get_mean
@	mov		r4,r0
@	mov		r2,r6
@	
@@	debug_output_line	#0xa4
@@	debug_output_data	r4
@@	debug_output_data	r2
@@	debug_output_data	r1
@	
@	mov		r0,r5
@	
@	
@	mov		r5,#0		@ i = 0
@	cmp		r5,r1		@ i < in_len
@	bge		loop3
@loop2:
@	ldr		r7,[r0,r5,lsl #2]
@	subs	r7,r7,r4			@ in[i] - (frame_t)mean;
@	
@	str		r7,[r0,r5,lsl #2]
@	
@	add		r5,r5,#1
@	cmp		r5,r1
@	blt		loop2
@	
@@	ldr		r5,[r0,#0]
@@	debug_output_data	r5
@@	ldr		r5,[r0,#4]
@@	debug_output_data	r5
@@	ldr		r5,[r0,#8]
@@	debug_output_data	r5
@@	ldr		r5,[r0,#12]
@@	debug_output_data	r5
@	
@loop3:
@	ldmfd 	sp!,{r4-r12,pc}
@	
@debug:	
@	
@	
@	
@@	debug_output_line	#0xa5
@	mov		r5,#0			@ i = 0
@	mov		r6,r1,lsr #1	@ in_len/2
@	cmp		r5,r6			@ i < in_len/2
@	bge		end0
@	
@@	debug_output_line	#0xa6
@	sub		r11,r1,#1		@ in_len-1
@loop31:
@@     in[i] = COSMUL(in[i], window[i]);
@
@@	debug_output_line	#0xa7
@@	debug_output_data	r0	
@@	debug_output_data	r5
@	ldr		r7,[r0,r5,lsl #2]		@	in[i]
@@	debug_output_data	r2
@	ldr		r8,[r2,r5,lsl #2]		@	window[i]
@	
@@	debug_output_data	r7
@@	debug_output_data	r8
@	
@	smull	r4,r9,r7,r8		@ COSMUL
@	
@@	debug_output_data	r4
@@	debug_output_data	r9
@	
@	
@	mov 	r4,r4,lsr #30
@	orr 	r4,r4,r9,lsl #2	@ 32 - 30
@	
@	str		r4,[r0,r5,lsl #2]
@	
@	
@@	debug_output_line	#0xa8
@	
@@   in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);		
@	
@	sub		r12,r11,r5
@	ldr		r7,[r0,r12,lsl #2]	@in[in_len-1-i]
@	
@	smull	r4,r9,r7,r8		@ COSMUL
@	mov 	r4,r4,lsr #30
@	orr 	r4,r4,r9,lsl #2	@ 32 - 30
@
@	str		r4,[r0,r12,lsl #2]
@	
@@	debug_output_line	#0xa9
@	
@	add		r5,r5,#1
@	cmp		r5,r6
@	blt		loop31
@end0:	
@	ldmfd 	sp!,{r4-r12,pc}

	
	
/*
*		for (i = 0; i < in_len; i++)
*			mean += in[i];
*/
fe_hamming_window_0_arm_code:
	stmfd 	sp!,{r2-r12,lr}
	mov		r5,#0
	
	subs	r3,r1,#1
	bmi		fe_hamming_window_0_end
		
fe_hamming_window_0:
	ldr		r4,[r0,r3,lsl #2]
	adds	r5,r5,r4
	
	subs	r3,r3,#1
	bpl		fe_hamming_window_0
	
fe_hamming_window_0_end:
	mov		r0,r5
	ldmfd 	sp!,{r2-r12,pc}
	
	
/*
*		for (i = 0; i < in_len; i++)
*			in[i] -= (frame_t)mean;
*/	
fe_hamming_window_1_arm_code:
	stmfd 	sp!,{r3-r12,lr}
	subs	r3,r1,#1
	beq		fe_hamming_window_1_end
	
fe_hamming_window_1:
	ldr		r4,[r0,r3,lsl #2]
	subs	r4,r4,r2
	str		r4,[r0,r3,lsl #2]
	
	subs	r3,r3,#1
	bne		fe_hamming_window_1
	
	ldr		r4,[r0,r3,lsl #2]
	subs	r4,r4,r2
	str		r4,[r0,r3,lsl #2]
	
fe_hamming_window_1_end:
	ldmfd 	sp!,{r3-r12,pc}
	
	
	
/*
*    for (i = 0; i < in_len/2; i++) {
*        in[i] = COSMUL(in[i], window[i]);
*        in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);
*    }
*/	
fe_hamming_window_2_arm_code:
	stmfd 	sp!,{r3-r12,lr}
	mov		r5,#0			@ i = 0
	mov		r6,r1,lsr #1	@ in_len/2
	cmp		r5,r6			@ i < in_len/2
	bge		end0
	
	sub		r11,r1,#1		@ in_len-1
loop31:
@     in[i] = COSMUL(in[i], window[i]);

	ldr		r7,[r0,r5,lsl #2]		@	in[i]
	ldr		r8,[r2,r5,lsl #2]		@	window[i]
	
	
	smull	r4,r9,r7,r8		@ COSMUL
	mov 	r4,r4,lsr #30
	orr 	r4,r4,r9,lsl #2	@ 32 - 30
	
	str		r4,[r0,r5,lsl #2]
	
	
@   in[in_len-1-i] = COSMUL(in[in_len-1-i], window[i]);		
	
	sub		r12,r11,r5
	ldr		r7,[r0,r12,lsl #2]	@in[in_len-1-i]
	
	smull	r4,r9,r7,r8		@ COSMUL
	mov 	r4,r4,lsr #30
	orr 	r4,r4,r9,lsl #2	@ 32 - 30

	str		r4,[r0,r12,lsl #2]
	
	
	add		r5,r5,#1
	cmp		r5,r6
	blt		loop31
end0:	
	ldmfd 	sp!,{r3-r12,pc}
	
	
	
	
	
	
	
fe_fft_real_arm_code:
	stmfd 	sp!,{r1-r12,lr}
	
@	 int i, j, k, m, n;
@    frame_t *x, xt;
@
@    x = fe->frame;
@    m = fe->fft_order;
@    n = fe->fft_size;
@
@    /* Bit-reverse the input. */
@    j = 0;
@    for (i = 0; i < n - 1; ++i) {
@        if (i < j) {
@            xt = x[j];
@            x[j] = x[i];
@            x[i] = xt;
@        }
@        k = n / 2;
@        while (k <= j) {
@            j -= k;
@            k /= 2;
@        }
@        j += k;
@    }
	ldr		r1,[r0,#fe_s_offset_frame]		@	x = fe->frame;
	ldrb	r12,[r0,#fe_s_offset_fft_order]	@	m = fe->fft_order;
	ldrh	r2,[r0,#fe_s_offset_fft_size]	@	n = fe->fft_size;
	
	mov		r3,#0		@	j = 0;
	mov		r4,#0		@	i = 0;
	subs	r5,r2,#1	@	i < n - 1;
	beq		fe_fft_real_1

fe_fft_real_0:
	cmp		r4,r3
	bge		fe_fft_real_01	@ if (i < j)
	ldr		r6,[r1,r3,lsl #2]		@ xt = x[j];
	ldr		r7,[r1,r4,lsl #2]		@ x[j] = x[i];
	str		r7,[r1,r3,lsl #2]
	str		r6,[r1,r4,lsl #2]		@ x[i] = xt;
fe_fft_real_01:
	mov		r6,r2,lsr #1	@ k = n / 2;
fe_fft_real_02:
	cmp		r6,r3			@ while (k <= j)
	bhi		fe_fft_real_03

	sub		r3,r3,r6		@ j -= k;
	mov		r6,r6,lsr #1	@ k /= 2;
	b		fe_fft_real_02
fe_fft_real_03:
	add		r3,r3,r6		@ j += k;
	
	add		r4,r4,#1		@ ++i
	cmp		r4,r5
	blt		fe_fft_real_0
	

@    /* Basic butterflies (2-point FFT, real twiddle factors):
@     * x[i]   = x[i] +  1 * x[i+1]
@     * x[i+1] = x[i] + -1 * x[i+1]
@     */
@    for (i = 0; i < n; i += 2) {
@        xt = x[i];
@        x[i]     = (xt + x[i + 1]);
@        x[i + 1] = (xt - x[i + 1]);
@    }

fe_fft_real_1:
	subs	r3,r2,#2	@	i < n - 1;
	beq		fe_fft_real_2
fe_fft_real_10:
	add		r4,r3,#1	@	i + 1
	ldr		r5,[r1,r3,lsl #2]	@	xt = x[i];
	ldr		r6,[r1,r4,lsl #2]   @ 	x[i + 1]
	adds	r7,r5,r6			@ 	x[i]     = (xt + x[i + 1]);
	str		r7,[r1,r3,lsl #2]
	subs	r7,r5,r6			@ 	x[i + 1] = (xt - x[i + 1]);
	str		r7,[r1,r4,lsl #2]

	subs	r3,r3,#2	@	i < n - 1;
	bne		fe_fft_real_10
	

	add		r4,r3,#1	@	i + 1
	ldr		r5,[r1,r3,lsl #2]	@	xt = x[i];
	ldr		r6,[r1,r4,lsl #2]   @ 	x[i + 1]
	adds	r7,r5,r6			@ 	x[i]     = (xt + x[i + 1]);
	str		r7,[r1,r3,lsl #2]
	subs	r7,r5,r6			@ 	x[i + 1] = (xt - x[i + 1]);
	str		r7,[r1,r4,lsl #2]

@	ldmfd 	sp!,{r1-r12,pc}
@
@    /* The rest of the butterflies, in stages from 1..m */
@    for (k = 1; k < m; ++k) {
@        int n1, n2, n4;
@
@        n4 = k - 1;
@        n2 = k;
@        n1 = k + 1;
@        /* Stride over each (1 << (k+1)) points */
@        for (i = 0; i < n; i += (1 << n1)) {
@            /* Basic butterfly with real twiddle factors:
@             * x[i]          = x[i] +  1 * x[i + (1<<k)]
@             * x[i + (1<<k)] = x[i] + -1 * x[i + (1<<k)]
@             */
@            xt = x[i];
@            x[i]             = (xt + x[i + (1 << n2)]);
@            x[i + (1 << n2)] = (xt - x[i + (1 << n2)]);
@
@            /* The other ones with real twiddle factors:
@             * x[i + (1<<k) + (1<<(k-1))]
@             *   = 0 * x[i + (1<<k-1)] + -1 * x[i + (1<<k) + (1<<k-1)]
@             * x[i + (1<<(k-1))]
@             *   = 1 * x[i + (1<<k-1)] +  0 * x[i + (1<<k) + (1<<k-1)]
@             */
@            x[i + (1 << n2) + (1 << n4)] = -x[i + (1 << n2) + (1 << n4)];
@            x[i + (1 << n4)]             =  x[i + (1 << n4)];
@            
@            /* Butterflies with complex twiddle factors.
@             * There are (1<<k-1) of them.
@             */
@            for (j = 1; j < (1 << n4); ++j) {
@                frame_t cc, ss, t1, t2;
@                int i1, i2, i3, i4;
@
@                i1 = i + j;
@                i2 = i + (1 << n2) - j;
@                i3 = i + (1 << n2) + j;
@                i4 = i + (1 << n2) + (1 << n2) - j;
@
@                /*
@                 * cc = real(W[j * n / (1<<(k+1))])
@                 * ss = imag(W[j * n / (1<<(k+1))])
@                 */
@                cc = fe->ccc[j << (m - n1)];
@                ss = fe->sss[j << (m - n1)];
@
@                /* There are some symmetry properties which allow us
@                 * to get away with only four multiplications here. */
@                t1 = COSMUL(x[i3], cc) + COSMUL(x[i4], ss);
@                t2 = COSMUL(x[i3], ss) - COSMUL(x[i4], cc);
@
@                x[i4] = (x[i2] - t2);
@                x[i3] = (-x[i2] - t2);
@                x[i2] = (x[i1] - t1);
@                x[i1] = (x[i1] + t1);
@            }
@        }
@    }
@
@    /* This isn't used, but return it for completeness. */
@    return m;
    

fe_fft_real_2:
	ldr		r3,=fft_order_shift_table

	
	mov		r11,#1						@ 	k = 1; n2 = k;
	str		r11,[r3,#fft_var_k_offset]
	
	
fe_fft_real_20:
	cmp		r11,r12						@	k < m;
	bge		fe_fft_real_end
	
	
	
@	保存相关变量值  n1,n2,n4  1<<n1  1<<n2  1<<n4
	adds	r4,r11,#1					@	n1 = k + 1;
	str		r4,[r3,#fft_var_n1_offset]
	ldr		r5,[r3,r4,lsl #2]			@	1 << n1
	str		r5,[r3,#fft_var_1_lsl_n1_offset]
	
	
	subs	r5,r12,r4					@	(m - n1)
	str		r5,[r3,#fft_var_m_sub_n1_offset]
	
	
	str		r11,[r3,#fft_var_n2_offset]
	ldr		r5,[r3,r11,lsl #2]			@	1 << n2
	str		r5,[r3,#fft_var_1_lsl_n2_offset]
	
	subs	r4,r11,#1					@	n4 = k - 1;
	str		r4,[r3,#fft_var_n4_offset]
	
	
	ldr		r5,[r3,r4,lsl #2]			@	1 << n1
	str		r5,[r3,#fft_var_1_lsl_n4_offset]
	

	mov		r4,#0						@	i = 0;
fe_fft_real_21:	
	cmp		r4,r2						@	i < n;
	bge		fe_fft_real_20_end
	
@	xt = x[i];
	ldr		r2,[r1,r4,lsl #2]			@	xt = x[i];

	
@ x[i]             = (xt + x[i + (1 << n2)]);
	ldr		r5,[r3,#fft_var_1_lsl_n2_offset]			@	1 << n2
	add		r6,r5,r4					@ 	i + (1 << n2)
	ldr		r7,[r1,r6,lsl #2]			@	x[i + (1 << n2)]
	adds	r8,r2,r7					@	(xt + x[i + (1 << n2)])
	str		r8,[r1,r4,lsl #2]			@	x[i] = (xt + x[i + (1 << n2)]);

	
@ x[i + (1 << n2)] = (xt - x[i + (1 << n2)]);
	subs	r8,r2,r7
	str		r8,[r1,r6,lsl #2]
	
@ x[i + (1 << n2) + (1 << n4)] = -x[i + (1 << n2) + (1 << n4)];
	ldr		r5,[r3,#fft_var_1_lsl_n4_offset] @	1 << n4  (n = k-1) 为了减少指令数，
	adds	r5,r6,r5			@	i + (1 << n2) + (1 << n4)
	ldr		r6,[r1,r5,lsl #2]	@	x[i + (1 << n2) + (1 << n4)]
	rsbs	r6,r6,#0			@	-x[i + (1 << n2) + (1 << n4)]
	str		r6,[r1,r5,lsl #2]	@	x[i + (1 << n2) + (1 << n4)] = -x[i + (1 << n2) + (1 << n4)];
@ x[i + (1 << n4)]             =  x[i + (1 << n4)];
	

@	for (j = 1; j < (1 << n4); ++j)
	mov		r2,#1						@	j = 1;
	
fe_fft_real_22:
	ldr		r6,[r3,#fft_var_1_lsl_n4_offset]	@(1 << n4)
	cmp		r2,r6	@	j < (1 << n4)
	bge		fe_fft_real_21_end
	

@可以用r11,5,6,7,8,9,12,       r11如果实在不够用，就用R11
	ldr		r5,[r3,#fft_var_m_sub_n1_offset]
	mov		r6,r2
	lsl		r6,r5		@	j << (m - n1)
	
	
	ldr		r8,[r0,#fe_s_offset_ccc]
	ldr		r7,[r8,r6,lsl #2]		@cc = fe->ccc[j << (m - n1)];
	ldr		r8,[r0,#fe_s_offset_sss]
	ldr		r6,[r8,r6,lsl #2]		@ss = fe->sss[j << (m - n1)];
	

	ldr		r8,[r3,#fft_var_1_lsl_n2_offset]	@(1 << n2)
	adds	r9,r8,r4
	adds	r11,r9,r2			@	i3 = i + (1 << n2) + j;
	str		r11,[r3,#fft_var_i3_offset]
	ldr		r9,[r1,r11,lsl #2]	@	x[i3]
	
	
	adds	r11,r4,r8,lsl #1	
	subs	r12,r11,r2			@	i4 = i + (1 << n2) + (1 << n2) - j;
	str		r12,[r3,#fft_var_i4_offset]
	ldr		r8,[r1,r12,lsl #2]	@	x[i4]
	
@	COSMUL(x[i3], cc)
	smull	r5,r12,r9,r7		@ COSMUL
	mov 	r5,r5,lsr #30
	orr 	r5,r5,r12,lsl #2	@ 32 - 30

@	COSMUL(x[i4], ss)
	smull	r11,r12,r8,r6		@ COSMUL
	mov 	r11,r11,lsr #30
	orr 	r11,r11,r12,lsl #2	@ 32 - 30
	
	adds	r5,r5,r11		@	t1 = COSMUL(x[i3], cc) + COSMUL(x[i4], ss);
	
	
@	COSMUL(x[i3], ss)
	smull	r11,r12,r9,r6		@ COSMUL
	mov 	r11,r11,lsr #30
	orr 	r11,r11,r12,lsl #2	@ 32 - 30
	
@	COSMUL(x[i4], cc)
	smull	r6,r12,r8,r7		@ COSMUL
	mov 	r6,r6,lsr #30
	orr 	r6,r6,r12,lsl #2	@ 32 - 30
	
	subs	r6,r11,r6			@ t2 = COSMUL(x[i3], ss) - COSMUL(x[i4], cc);
	
	
	ldr		r7,[r3,#fft_var_i3_offset]
	subs	r8,r7,r2,lsl #1		@	i2 = i + (1 << n2) - j;
	ldr		r9,[r1,r8,lsl #2]	@	x[i2]
	subs	r11,r9,r6			@	x[i4] = (x[i2] - t2);
	ldr		r12,[r3,#fft_var_i4_offset]		@	=i4
	str		r11,[r1,r12,lsl #2]	@	x[i4] = (x[i2] - t2);
	
	rsbs	r11,r9,#0			@	-x[i2]
	subs	r9,r11,r6			@	(-x[i2] - t2)
	str		r9,[r1,r7,lsl #2]	@	x[i3] = (-x[i2] - t2);
	

@	i2 = r8,t1 = r5
	adds	r6,r2,r4			@	i1 = i + j;
	ldr		r7,[r1,r6,lsl #2]	@	x[i1]
	subs	r9,r7,r5			@	(x[i1] - t1)
	str		r9,[r1,r8,lsl #2]	@	x[i2] = (x[i1] - t1);
	
	adds	r9,r7,r5			@	(x[i1] + t1)
	str		r9,[r1,r6,lsl #2]	@	x[i1] = (x[i1] + t1);
	
	
fe_fft_real_22_end:
	add		r2,r2,#1		@	 ++j
	b		fe_fft_real_22
	

fe_fft_real_21_end:
	ldrh	r2,[r0,#fe_s_offset_fft_size]	@	n = fe->fft_size;
	ldr		r5,[r3,#fft_var_1_lsl_n1_offset]		@	(1 << n1)  n1 = k + 1    大概增加256条指令
	add		r4,r4,r5		@	i += (1 << n1)
	b		fe_fft_real_21
	
	
fe_fft_real_20_end:
	ldr		r11,[r3,#fft_var_k_offset]	
	add		r11,r11,#1	@	++k
	str		r11,[r3,#fft_var_k_offset]
	ldrb	r12,[r0,#fe_s_offset_fft_order]	@	m = fe->fft_order;
	b		fe_fft_real_20
	
fe_fft_real_end:
    mov		r0,r12
	ldmfd 	sp!,{r1-r12,pc}
	
	
	
	
	
fe_fft_real_arm_code_test:
	stmfd 	sp!,{r2-r12,lr}
	
	mov 	r4,r1
	
	ldr		r1,[r0,#fe_s_offset_frame]		@	x = fe->frame;

	
	ldr		r3,=fft_order_shift_table
	
	
@	for (j = 1; j < (1 << n4); ++j)
	mov		r2,#1						@	j = 1;
	
fe_fft_real_test_22:
@	debug_output_line	#0xa4
	ldr		r6,[r3,#fft_var_1_lsl_n4_offset]	@(1 << n4)
	cmp		r2,r6	@	j < (1 << n4)
	bge		fe_fft_real_test_21_end
	
@	debug_output_line	#0xa5
@	debug_output_data 	r2
@	debug_output_data 	r6
@可以用r11,5,6,7,8,9,12,       r11如果实在不够用，就用R11
	ldr		r5,[r3,#fft_var_m_sub_n1_offset]
	mov		r6,r2
	lsl		r6,r5		@	j << (m - n1)
	
	debug_output_line	#0xa6
	debug_output_data 	r5
	debug_output_data 	r6
	
	ldr		r8,[r0,#fe_s_offset_ccc]
	ldr		r7,[r8,r6,lsl #2]		@cc = fe->ccc[j << (m - n1)];
	ldr		r8,[r0,#fe_s_offset_sss]
	ldr		r6,[r8,r6,lsl #2]		@ss = fe->sss[j << (m - n1)];
	
	debug_output_data 	r7
	debug_output_data 	r6
	
	debug_output_line	#0xa7
@	debug_output_data 	r4
	ldr		r8,[r3,#fft_var_1_lsl_n2_offset]	@(1 << n2)
	debug_output_data 	r8
	adds	r9,r8,r4
	adds	r11,r9,r2			@	i3 = i + (1 << n2) + j;
	str		r11,[r3,#fft_var_i3_offset]
	ldr		r9,[r1,r11,lsl #2]	@	x[i3]
	
	debug_output_data 	r11
	
@	debug_output_line	#0xa8
	adds	r11,r4,r8,lsl #1	
	subs	r12,r11,r2			@	i4 = i + (1 << n2) + (1 << n2) - j;
	str		r12,[r3,#fft_var_i4_offset]
	ldr		r8,[r1,r12,lsl #2]	@	x[i4]
	
	debug_output_line	#0xa9
	debug_output_data 	r9
	debug_output_data 	r8
	
@	COSMUL(x[i3], cc)
	smull	r5,r12,r9,r7		@ COSMUL
	mov 	r5,r5,lsr #30
	orr 	r5,r5,r12,lsl #2	@ 32 - 30

@	COSMUL(x[i4], ss)
	smull	r11,r12,r8,r6		@ COSMUL
	mov 	r11,r11,lsr #30
	orr 	r11,r11,r12,lsl #2	@ 32 - 30
	
	adds	r5,r5,r11		@	t1 = COSMUL(x[i3], cc) + COSMUL(x[i4], ss);
	
@	debug_output_line	#0xaa
	
@	COSMUL(x[i3], ss)
	smull	r11,r12,r9,r6		@ COSMUL
	mov 	r11,r11,lsr #30
	orr 	r11,r11,r12,lsl #2	@ 32 - 30
	
@	COSMUL(x[i4], cc)
	smull	r6,r12,r8,r7		@ COSMUL
	mov 	r6,r6,lsr #30
	orr 	r6,r6,r12,lsl #2	@ 32 - 30
	
	subs	r6,r11,r6			@ t2 = COSMUL(x[i3], ss) - COSMUL(x[i4], cc);
	
	debug_output_line	#0xab
	debug_output_data 	r5
	debug_output_data 	r6
	
	ldr		r7,[r3,#fft_var_i3_offset]
	subs	r8,r7,r2,lsl #1		@	i2 = i + (1 << n2) - j;
	debug_output_data 	r7
	debug_output_data 	r8
	ldr		r9,[r1,r8,lsl #2]	@	x[i2]
	subs	r11,r9,r6			@	x[i4] = (x[i2] - t2);
	ldr		r12,[r3,#fft_var_i4_offset]		@	=i4
	str		r11,[r1,r12,lsl #2]	@	x[i4] = (x[i2] - t2);
	
	debug_output_line	#0xac
	debug_output_data 	r12
	debug_output_data 	r11
	
	rsbs	r11,r9,#0			@	-x[i2]
	subs	r9,r11,r6			@	(-x[i2] - t2)
	str		r9,[r1,r7,lsl #2]	@	x[i3] = (-x[i2] - t2);
	
	
	debug_output_data 	r7
	debug_output_data 	r9
	
@	i2 = r8,t1 = r5
	adds	r6,r2,r4			@	i1 = i + j;
	ldr		r7,[r1,r6,lsl #2]	@	x[i1]
	subs	r9,r7,r5			@	(x[i1] - t1)
	str		r9,[r1,r8,lsl #2]	@	x[i2] = (x[i1] - t1);
	
	debug_output_line	#0xad
	debug_output_data 	r8
	debug_output_data 	r9
	
	adds	r9,r7,r5			@	(x[i1] + t1)
	str		r9,[r1,r6,lsl #2]	@	x[i1] = (x[i1] + t1);
	
	debug_output_data 	r6
	debug_output_data 	r9
	

	
fe_fft_real_test_22_end:
	add		r2,r2,#1		@	 ++j
	b		fe_fft_real_test_22
	
fe_fft_real_test_21_end:
	mov		r1,r4
	ldmfd 	sp!,{r2-r12,pc}
	

	
	
	
	
	
	
	
	
fe_mel_spec_arm_code:
	stmfd 	sp!,{r1-r12,lr}
	
	ldr		r2,[r0,#fe_s_offset_spec]		@	spec = fe->spec;
	ldr		r3,[r0,#fe_s_offset_mfspec]		@	mfspec = fe->mfspec;
	
	ldr		r12,[r0,#fe_s_offset_mel_fb]	@	p_mel_fb = fe->mel_fb;	
	ldr		r4,[r12,#melfb_s_offset_filt_coeffs]	@	filt_coeffs = p_mel_fb->filt_coeffs;
	
	
	mov		r12,#0			@ whichfilt = 0;
fe_mel_spec_00:
	ldr		r11,[r0,#fe_s_offset_mel_fb]			@	p_mel_fb = fe->mel_fb;	
	ldr		r5,[r11,#melfb_s_offset_num_filters]	@	num_filters = p_mel_fb->num_filters;
	cmp		r12,r5			@	whichfilt < num_filters;
	bge		fe_mel_spec_end
	

	ldr		r1,[r11,#melfb_s_offset_spec_start]		@	p_spec_start = p_mel_fb->spec_start;
	mov		r9,r12,lsl #1
	ldrsh	r7,[r1,r9]	@	spec_start = p_spec_start[whichfilt];
	ldr		r1,[r11,#melfb_s_offset_filt_start]		@	p_filt_start = p_mel_fb->filt_start;
	ldrsh	r8,[r1,r9]	@	filt_start = p_filt_start[whichfilt];
	

	ldr		r1,[r2,r7,lsl #2]		@	spec[spec_start]
	ldr		r5,[r4,r8,lsl #2]		@	filt_coeffs[filt_start];
	adds	r6,r1,r5				@	spec[spec_start] + filt_coeffs[filt_start];
	str		r6,[r3,r12,lsl #2]		@	mfspec[whichfilt] = spec[spec_start] + filt_coeffs[filt_start];
	
	ldr		r5,[r11,#melfb_s_offset_filt_width]		@	p_filt_width = p_mel_fb->filt_width;
	ldrsh	r6,[r5,r9]		@	x = p_filt_width[whichfilt];
	

	mov		r11,#1		@	i = 1;
fe_mel_spec_10:
	cmp		r11,r6		@	i < x;
	bge		fe_mel_spec_01
	

	adds	r5,r7,r11			@	spec_start + i
	ldr		r9,[r2,r5,lsl #2]	@	spec[spec_start + i]
	adds	r5,r8,r11			@	filt_start + i
	ldr		r1,[r4,r5,lsl #2]	@	filt_coeffs[filt_start + i]
	
	adds	r1,r1,r9			@	spec[spec_start + i] + filt_coeffs[filt_start + i]
	

	ldr		r5,[r3,r12,lsl #2]	@	mfspec[whichfilt]
	mov		r9,r0
	mov		r0,r5
	stmfd 	sp!,{r1-r12}
	bl		fe_log_add
	ldmfd 	sp!,{r1-r12}
	str		r0,[r3,r12,lsl #2]	@	mfspec[whichfilt] = fe_log_add
	mov		r0,r9
	
	adds	r11,r11,#1
	b		fe_mel_spec_10
	
fe_mel_spec_01:
	adds	r12,r12,#1		@	whichfilt++
	b		fe_mel_spec_00
fe_mel_spec_end:	
	ldmfd 	sp!,{r1-r12,pc}
	
	
	
	
	
	
	
@void fe_dct2(fe_t * fe, const powspec_t * mflogspec, mfcc_t * mfcep, int htk)
@{
@    int32 i, j;
@
@    /* Compute C0 separately (its basis vector is 1) to avoid
@     * costly multiplications. */
@    mfcep[0] = mflogspec[0];
@    for (j = 1; j < fe->mel_fb->num_filters; j++)
@	mfcep[0] += mflogspec[j];
@    if (htk)
@        mfcep[0] = COSMUL(mfcep[0], fe->mel_fb->sqrt_inv_2n);
@    else /* sqrt(1/N) = sqrt(2/N) * 1/sqrt(2) */
@        mfcep[0] = COSMUL(mfcep[0], fe->mel_fb->sqrt_inv_n);
@
@    for (i = 1; i < fe->num_cepstra; ++i) {
@        mfcep[i] = 0;
@        for (j = 0; j < fe->mel_fb->num_filters; j++) {
@	    mfcep[i] += COSMUL(mflogspec[j],
@				fe->mel_fb->mel_cosine[i][j]);
@        }
@        mfcep[i] = COSMUL(mfcep[i], fe->mel_fb->sqrt_inv_2n);
@    }
@}
fe_dct2_arm_code:
	stmfd 	sp!,{r4-r12,lr}
	ldr		r4,[r1]			@	mflogspec[0]
	str		r4,[r2]			@	mfcep[0] = mflogspec[0];
	
	ldr		r12,[r0,#fe_s_offset_mel_fb]			@	p_mel_fb = fe->mel_fb;
	ldr		r11,[r12,#melfb_s_offset_num_filters]	@	num_filters = p_mel_fb->num_filters;
	
	subs	r9,r11,#1
	beq		fe_dct2_00_end
fe_dct2_00:
	ldr		r8,[r1,r9,lsl #2]	@	mflogspec[j];
	adds	r4,r4,r8			@	sum += mflogspec[j];
	
	
	subs	r9,r9,#1
	bne		fe_dct2_00
	
fe_dct2_00_end:
@	str		r4,[r2]		@	mfcep[0] = mflogspec[j];
	
	
	
	tst		r3,#0xff		@ if (htk)
	ldreq	r5,[r12,#melfb_s_offset_sqrt_inv_n]		@	p_mel_fb->sqrt_inv_n
	ldrne	r5,[r12,#melfb_s_offset_sqrt_inv_2n]	@	p_mel_fb->sqrt_inv_2n
	
	
@	COSMUL(mfcep[0], cc)
	smull	r6,r7,r4,r5		@ COSMUL
	mov 	r6,r6,lsr #30
	orr 	r6,r6,r7,lsl #2	@ 32 - 30
	
	str		r6,[r2]			@mfcep[0] =
	
	
	
	

	
	ldr		r5,[r12,#melfb_s_offset_sqrt_inv_2n]	@	p_mel_fb->sqrt_inv_2n	
	ldr		r6,[r12,#melfb_s_offset_mel_cosine]		@	p_temp1 = p_mel_fb->mel_cosine;
	ldrb	r7,[r0,#fe_s_offset_num_cepstra]		@	num_cepstra = fe->num_cepstra;
	subs	r4,r7,#1		@ if(num_cepstra == 1) goto fe_dct2_end
	beq		fe_dct2_end
fe_dct2_10:	
	mov		r9,#0
	
	ldr		r12,[r6,r4,lsl #2]	@	p_temp = p_temp1[i];
	
	
	
	stmfd 	sp!,{r4-r8}
	subs	r4,r11,#1
	bmi		fe_dct2_20_end		@	if(j < 0) goto fe_dct2_20_end
fe_dct2_20:
	ldr		r5,[r1,r4,lsl #2]	@	mflogspec[j]
	ldr		r6,[r12,r4,lsl #2]	@	p_temp[j]
	
@	COSMUL(mfcep[0], cc)
	smull	r7,r8,r5,r6		@ COSMUL
	mov 	r7,r7,lsr #30
	orr 	r7,r7,r8,lsl #2	@ 32 - 30
	
	adds	r9,r9,r7	@	sum += COSMUL(mflogspec[j],p_temp[j]);

	subs	r4,r4,#1
	bpl		fe_dct2_20	@ if(j <= 0)
fe_dct2_20_end:	
	ldmfd 	sp!,{r4-r8}
	
	
	
	smull	r12,r8,r9,r5		@ COSMUL
	mov 	r12,r12,lsr #30
	orr 	r12,r12,r8,lsl #2	@ 32 - 30
	
	str		r12,[r2,r4,lsl #2]	@mfcep[i] =
fe_dct2_10_end:
	subs	r4,r4,#1	@	++i;
	bne		fe_dct2_10
fe_dct2_end:
	ldmfd 	sp!,{r4-r12,pc}
	
	
	
	
	
	
	
	
	
acmod_activate_hmm_arm_code:
	stmfd 	sp!,{r2-r12,lr}
@	debug_output_line	#0xff
	ldrb	r2,[r0,#acmod_s_offset_compallsen]	
@	debug_output_data	r2
	tst		r2,#0xff		@if (acmod->compallsen)
	bne		acmod_activate_hmm_end
	
@	debug_output_line	#0xa0
	ldr		r2,[r1,#hmm_s_offset_ctx]			@	hmm->ctx
	ldr		r12,[r2,#hmm_context_s_offset_sseq]	@	hmm->ctx->sseq
	ldrb	r2,[r1,#hmm_s_offset_n_emit_state]	@n_emit_state = hmm->n_emit_state;
	add		r3,r1,#hmm_s_offset_senid			@p_senid = hmm->senid;
	ldr		r4,[r0,#acmod_s_offset_senone_active_vec] @senone_active_vec = acmod->senone_active_vec;
	
@	debug_output_data	r12
@	debug_output_data	r2
@	debug_output_data	r3
@	debug_output_data	r4
	
	ldrb	r5,[r1,#hmm_s_offset_mpx]	@	if (hmm->mpx) 
	tst		r5,#0xff	
	beq		acmod_activate_hmm_10		@	else
	
@	debug_output_line	#0xa1
	subs	r6,r2,#1		@	i = (n_emit_state - 1);
	bmi		acmod_activate_hmm_end	
acmod_activate_hmm_0:
@	debug_output_line	#0xa2
	mov		r11,r6,lsl #1
	ldrh	r7,[r3,r11]		@ senid = p_senid[i];
@	debug_output_data	r7
	
	ldr		r8,=BAD_SSID
	cmp		r7,r8
	beq		acmod_activate_hmm_01
	
@	debug_output_line	#0xa3
	ldr		r8,[r12,r7,lsl #2]	@ &hmm->ctx->sseq[senid]
	ldrh	r9,[r8,r11]	@ temp = hmm->ctx->sseq[senid][i];
	
@	debug_output_data	r9
	
	lsr		r11,r9,#BITVEC_BITS_SHIFT_BITS	@	temp >> 5
	ldr		r8,[r4,r11,lsl #2]	@	senone_active_vec[temp >> 5]
	
@	debug_output_data	r8
	and		r9,#BITVEC_BITS_SUB_1
	mov		r7,#1
	lsl		r7,r9
	orr		r7,r8	@	senone_active_vec[temp >> 5] | (1UL << (temp & mask))
	str		r7,[r4,r11,lsl #2]
@	debug_output_data	r7
acmod_activate_hmm_01:
	subs	r6,r6,#1
	bpl		acmod_activate_hmm_0
	
	b		acmod_activate_hmm_end
acmod_activate_hmm_10:
@	debug_output_line	#0xaa
	
	subs	r6,r2,#1		@	i = (n_emit_state - 1);
	bmi		acmod_activate_hmm_end	
acmod_activate_hmm_11:
@	debug_output_line	#0xab
	mov		r11,r6,lsl #1
	ldrh	r7,[r3,r11]		@ temp = p_senid[i];

@	debug_output_data	r7
	
	lsr		r11,r7,#BITVEC_BITS_SHIFT_BITS	@	temp >> 5
	ldr		r8,[r4,r11,lsl #2]	@	senone_active_vec[temp >> 5]
	
@	debug_output_data	r8
	
	and		r7,#BITVEC_BITS_SUB_1
	mov		r9,#1
	lsl		r9,r7
	orr		r9,r8	@	senone_active_vec[temp >> 5] | (1UL << (temp & mask))
	str		r9,[r4,r11,lsl #2]	@	senone_active_vec[temp >> 5] =
	
@	debug_output_data	r9
	subs	r6,r6,#1
	bpl		acmod_activate_hmm_11
	
acmod_activate_hmm_end:
	ldmfd 	sp!,{r2-r12,pc}
	
	
	
	
	
	
	
@	ARM	指令参数传递规则，默认使用R0--R3这4个寄存器传递小于等于4个的参数，
@	如果参数个数为4个以上，则第5个开始使用栈传递，如下例子
@	extern int get_scores_4b_feat_4_arm_code(void *s, int i,short *senone_scores, unsigned char *senone_active,int n_senone_active);
@
@get_scores_4b_feat_4_arm_code:
@	ldr		r12,[sp,#0x0]
@	debug_output_data r12
@	debug_output_sp_addr	sp
@	
@	stmfd 	sp!,{r4-r12,lr}
@	debug_output_sp_addr	sp
@
@	ldr		r4,[sp,#0x28]
@	debug_output_data r4
@	ldmfd 	sp!,{r4-r12,pc}


get_scores_4b_feat_4_arm_code:
	stmfd 	sp!,{r4-r12,lr}
@	ldr		r4,[sp,#0x28]
@	debug_output_data r4		@	取参数 int32 n_senone_active
	
	ldr		r5,[r0,#s2_semi_mgau_s_offset_f] 	@	vqFeature_t *f = s->f
	ldr		r6,[r5,r1,lsl #2]		@ f = s->f[i];
	
@	ldr		r8,[r6,#vqFeature_t_f_0_score_offset]	@	f0_score = f[0].score;
@	ldr		r9,[r6,#vqFeature_t_f_1_score_offset]
	ldr		r11,[r6,#vqFeature_t_f_2_score_offset]
	ldr		r12,[r6,#vqFeature_t_f_3_score_offset]
	
	ldr		r7,[r0,#s2_semi_mgau_s_offset_mixw_cb]	@	uint8 *mixw_cb = s->mixw_cb;
	
	ldr		r10,=get_scores_4b_feat_var
	mov 	r5,#15
get_scores_4b_feat_4_00:
	ldrb	r8,[r7,r5]			@	uint8 temp = mixw_cb[j];
	ldr		r9,[r6,#vqFeature_t_f_0_score_offset]
	add		r4,r8,r9
	strb	r4,[r10,r5]			@	w_den[j] = temp + f0_score;
	
	ldr		r9,[r6,#vqFeature_t_f_1_score_offset]
	add		r4,r8,r9
	add		r9,r5,#16
	strb	r4,[r10,r9]			@	w_den[j] = temp + f1_score;
	
	add		r4,r8,r11
	add		r9,r5,#32
	strb	r4,[r10,r9]			@	w_den[j] = temp + f2_score;

	add		r4,r8,r12
	add		r9,r5,#48
	strb	r4,[r10,r9]			@	w_den[j] = temp + f2_score;


	subs	r5,r5,#1
	bpl		get_scores_4b_feat_4_00
	


	ldr		r8,[r6,#vqFeature_t_f_0_codeword_offset]	@	f[0].codeword
	ldr		r5,[r0,#s2_semi_mgau_s_offset_mixw]
	ldr		r9,[r5,r1,lsl #2]		@	s->mixw[i]
	ldr		r11,[r9,r8,lsl #2]
	str		r11,[r10,#pid_cw0_offset]	@	pid_cw0 = s->mixw[i][f[0].codeword];
	
	ldr		r8,[r6,#vqFeature_t_f_1_codeword_offset]	@	f[1].codeword
	ldr		r11,[r9,r8,lsl #2]
	str		r11,[r10,#pid_cw1_offset]	@	pid_cw1 = s->mixw[i][f[1].codeword];
	
	ldr		r8,[r6,#vqFeature_t_f_2_codeword_offset]	@	f[2].codeword
	ldr		r11,[r9,r8,lsl #2]
	str		r11,[r10,#pid_cw2_offset]	@	pid_cw1 = s->mixw[i][f[2].codeword];
	
	ldr		r8,[r6,#vqFeature_t_f_3_codeword_offset]	@	f[3].codeword
	ldr		r11,[r9,r8,lsl #2]
	str		r11,[r10,#pid_cw3_offset]	@	pid_cw1 = s->mixw[i][f[3].codeword];
	
@	ldmfd 	sp!,{r4-r12,pc}

	ldr		r4,[r0,#s2_semi_mgau_s_offset_lmath_8b]	@	logmath_t *lmath_8b = s->lmath_8b;
	ldr		r12,[r4,#logadd_s_offset_table]			@	uint8 *p_table = (uint8 *)(((logadd_t *)lmath_8b)->table);
	
	
	ldr		r4,[sp,#0x28]	@	n_senone_active
	mov		r5,#0			@	j = 0
	mov		r6,#0			@	l = 0
get_scores_4b_feat_4_01:
	cmp		r5,r4
	bge		get_scores_4b_feat_4_end

	ldrb	r7,[r3,r5]
	add		r7,r7,r6		@	int n = senone_active[j] + l;
	
	lsr		r8,r7,#1		@	int n_lsr_1 = n >> 1;
	
	
	stmfd 	sp!,{r4-r7}
	tst		r7,#0x1
	beq		get_scores_4b_feat_4_02
@if(n & 1)
	ldr		r7,[r10,#pid_cw0_offset]
	ldrb	r9,[r7,r8]		@	pid_cw0[n_lsr_1]
	lsr		r7,r9,#4		@	cw = pid_cw0[n_lsr_1] >> 4;
	
	ldrb	r4,[r10,r7]		@	tmp = w_den[cw];
	
	ldr		r7,[r10,#pid_cw1_offset]
	ldrb	r9,[r7,r8]		@	pid_cw1[n_lsr_1]
	lsr		r7,r9,#4		@	cw = pid_cw0=1[n_lsr_1] >> 4;
	
	add		r7,r7,#16
	ldrb	r5,[r10,r7]	@	mly = w_den[16+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r4,r7,r6		@tmp =  r - p_table[d];
	
	
	
	ldr		r7,[r10,#pid_cw2_offset]
	ldrb	r9,[r7,r8]		@	pid_cw1[n_lsr_1]
	lsr		r7,r9,#4		@	cw = pid_cw2=1[n_lsr_1] >> 4;
	
	add		r7,r7,#32
	ldrb	r5,[r10,r7]	@	mly = w_den[32+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r4,r7,r6		@tmp =  r - p_table[d];
	
	
	
	ldr		r7,[r10,#pid_cw3_offset]
	ldrb	r9,[r7,r8]		@	pid_cw1[n_lsr_1]
	lsr		r7,r9,#4		@	cw = pid_cw3=1[n_lsr_1] >> 4;
	
	add		r7,r7,#48
	ldrb	r5,[r10,r7]	@	mly = w_den[48+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r11,r7,r6		@tmp =  r - p_table[d];
	
	b		get_scores_4b_feat_4_01_end
get_scores_4b_feat_4_02:
	mov		r11,#0x0f
	ldr		r7,[r10,#pid_cw0_offset]
	ldrb	r9,[r7,r8]		@	pid_cw0[n_lsr_1]
	and		r9,r11			@	cw = pid_cw0[n_lsr_1] & 0x0f;
	
	ldrb	r4,[r10,r9]		@	tmp = w_den[cw];
	
	ldr		r7,[r10,#pid_cw1_offset]
	ldrb	r9,[r7,r8]		@	pid_cw1[n_lsr_1]
	and		r9,r11			@	cw = pid_cw1[n_lsr_1] & 0x0f;
	
	add		r7,r9,#16
	ldrb	r5,[r10,r7]	@	mly = w_den[16+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r4,r7,r6		@tmp =  r - p_table[d];
	
	
	
	ldr		r7,[r10,#pid_cw2_offset]
	ldrb	r9,[r7,r8]		@	pid_cw2[n_lsr_1]
	and		r9,r11			@	cw = pid_cw2[n_lsr_1] & 0x0f;
	
	add		r7,r9,#32
	ldrb	r5,[r10,r7]	@	mly = w_den[32+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r4,r7,r6		@tmp =  r - p_table[d];
	
	
	
	ldr		r7,[r10,#pid_cw3_offset]
	ldrb	r9,[r7,r8]		@	pid_cw2[n_lsr_1]
	and		r9,r11			@	cw = pid_cw3[n_lsr_1] & 0x0f;
	
	add		r7,r9,#48
	ldrb	r5,[r10,r7]	@	mly = w_den[48+cw];
	
	cmp		r4,r5			@	if (tmp > mly)
	movhi	r7,r5			@	r = mly;
	subhi	r9,r4,r5		@	d = (tmp - mly);
	movls	r7,r4			@	r = tmp;
	subls	r9,r5,r4		@	d = (mly - tmp);
	
	ldrb	r6,[r12,r9]
	sub		r11,r7,r6		@tmp =  r - p_table[d];
	
	
	
get_scores_4b_feat_4_01_end:
	ldmfd 	sp!,{r4-r7}
	
@	senone_scores[n] += tmp;
	lsl		r9,r7,#1
	ldrh	r8,[r2,r9]
	add		r8,r8,r11
	strh	r8,[r2,r9]		@	senone_scores[n] += tmp;
	
	mov 	r6,r7		@ l = n;
	
	add		r5,r5,#1
	b		get_scores_4b_feat_4_01
	
get_scores_4b_feat_4_end:
	mov 	r0,#0			@	return 0;
	ldmfd 	sp!,{r4-r12,pc}
	
	
	
	
	
mgau_norm_arm_code:
	stmfd 	sp!,{r2-r12,lr}
	ldr		r2,[r0,#s2_semi_mgau_s_offset_f]	@
	ldr		r3,[r2,r1,lsl #2]	@	vqFeature_t *p_f = s->f[feat];
	
	ldr		r4,[r3,#vqFeature_t_f_0_score_offset]
	lsr		r5,r4,#SENSCR_SHIFT	@	norm = p_f[0].score >> SENSCR_SHIFT;
	
	ldr		r11,[r0,#s2_semi_mgau_s_offset_topn_beam]@
	ldrb	r12,[r11,r1]	@	s->topn_beam[feat]
	
	ldrsh	r2,[r0,#s2_semi_mgau_s_offset_max_topn]	@s->max_topn
	mov 	r4,#0
mgau_norm_0:
	cmp		r4,r2
	bge		mgau_norm_end
	
	ldr		r6,[r3,r4,lsl #3]	@	p_f[j].score
	lsr		r7,r6,#SENSCR_SHIFT
	subs	r8,r5,r7
	str		r8,[r3,r4,lsl #3]	@	p_f[j].score = -((p_f[j].score >> SENSCR_SHIFT) - norm);
	
	mov		r9,#MAX_NEG_ASCR
	cmp		r8,r9				@	if (p_f[j].score > MAX_NEG_ASCR)
	strhi	r9,[r3,r4,lsl #3]	@	p_f[j].score = MAX_NEG_ASCR;
	movhi	r8,r9
	
	tst		r12,#0xff
	cmpne	r8,r12
	bhi		mgau_norm_end
	
	
	add		r4,r4,#1
	b		mgau_norm_0
	
mgau_norm_end:
	mov		r0,r4	@	return j;
	ldmfd 	sp!,{r2-r12,pc}
	
	
	
	
	
	
	
	
	
	
eval_topn_arm_code:
	stmfd 	sp!,{r3-r12,lr}
	ldr		r3,[r0,#s2_semi_mgau_s_offset_f]
	ldr		r12,[r3,r1,lsl #2]		@	topn = s->f[feat];
	
	ldr		r3,[r0,#s2_semi_mgau_s_offset_veclen]
	ldr		r11,[r3,r1,lsl #2]		@	ceplen = s->veclen[feat];
	
	ldrh	r3,[r0,#s2_semi_mgau_s_offset_max_topn]	@	int16 max_topn = s->max_topn;
	
@	debug_output_line	#0xa1
@	debug_output_data	r12
@	debug_output_data	r11
@	debug_output_data	r3
	
	ldr		r9,=eval_topn_var
	
	ldr		r7,[r0,#s2_semi_mgau_s_offset_means]
	ldr		r8,[r7,r1,lsl #2]
	ldr		r7,[r8]		@	s->means[feat][0]
	str		r7,[r9,#ARM_STACK_VAR_1]
	
	ldr		r7,[r0,#s2_semi_mgau_s_offset_vars]
	ldr		r8,[r7,r1,lsl #2]
	ldr		r7,[r8]
	str		r7,[r9,#ARM_STACK_VAR_2]
	
	ldr		r7,[r0,#s2_semi_mgau_s_offset_dets]		@	
	ldr		r8,[r7,r1,lsl #2]
	str		r8,[r9,#ARM_STACK_VAR_3]
	
	mov		r4,#0x0		@	i = 0;
eval_topn_00:
	cmp		r4,r3
	bge		eval_topn_end
	
@	debug_output_line	#0xa2
	add		r5,r12,r4,lsl #3	
	ldr		r6,[r5,#vqFeature_s_offset_codeword]	@	cw = topn[i].codeword;
	

@	debug_output_data	r6
	
	mul		r5,r6,r11		@	cw * ceplen;
	
@	debug_output_data	r5
	

	ldr		r9,=eval_topn_var
	
	ldr		r7,[r9,#ARM_STACK_VAR_1]
@	debug_output_data	r7
	add		r8,r7,r5,lsl #2	@	mean = s->means[feat][0] + cw * ceplen;
	
	ldr		r7,[r9,#ARM_STACK_VAR_2]
	add		r5,r7,r5,lsl #2	@	var = s->vars[feat][0] + cw * ceplen;
	
	ldr		r9,[r9,#ARM_STACK_VAR_3]
	ldr		r7,[r9,r6,lsl #2]	@	d = s->dets[feat][cw];
	
	mov 	r6,r2	@	obs = z;
	
	@debug_output_data	r8
	@debug_output_data	r5
@	debug_output_data	r7
	@debug_output_data	r6
	
	
	stmfd 	sp!,{r0-r4,r12}
	mov		r12,#0x0
eval_topn_01:

	cmp		r12,r11
	bge		eval_topn_01_end
	

	ldr		r1,[r6],#4
	ldr		r2,[r8],#4
	
@	debug_output_line	#0xb0
@	debug_output_data	r1
@	debug_output_data	r2
	
	subs	r1,r1,r2	@	diff = *obs++ - *mean++;
	
@	debug_output_line	#0xb1
@	debug_output_data	r1

@	MFCCMUL
@	#define FIXMUL(a,b) FIXMUL_ANY(a,b,DEFAULT_RADIX)
	smull	r4,r3,r1,r1		@ COSMUL
	mov 	r4,r4,lsr #DEFAULT_RADIX
	orr 	r4,r4,r3,lsl #20	@ 32 - 12
	
@	debug_output_line	#0xb2
@	debug_output_data	r4
	
	ldr		r3,[r5]		@	*var
	
@	debug_output_data	r3

	smull	r1,r2,r3,r4		@ COSMUL
	mov 	r1,r1,lsr #DEFAULT_RADIX
	orr 	r1,r1,r2,lsl #20	@ 32 - 12
	
@	debug_output_line	#0xb3
@	debug_output_data	r1
	
	
@	d = GMMSUB(d, compl);
@#define GMMSUB(a,b) 	(((a)-(b) > a) ? (INT_MIN) : ((a)-(b)))
	sub		r2,r7,r1
	cmp		r2,r7
	movgt	r7,#ARM_INT_MIN
	movle	r7,r2
	
@	debug_output_line	#0xb4
@	debug_output_data	r7

	add		r5,r5,#4
	
	add		r12,r12,#1
	b		eval_topn_01
	

eval_topn_01_end:
	ldmfd 	sp!,{r0-r4,r12}
	

	str		r7,[r12,r4,lsl #3]	@	topn[i].score = (int32)d;
	
@	debug_output_line	#0xa3
@	debug_output_data	r7
	
	cmp		r4,#0x0		@	if (i == 0)  continue;
	beq		eval_topn_00_end
	
	add		r5,r12,r4,lsl #3	
@	ldr		r6,[r5,#vqFeature_s_offset_codeword]	@	vtmp = topn[i];

@5.6.7.8.9
	ldmia	r5!,{r6,r7}		@	vtmp = topn[i];
	
@	debug_output_line	#0xa4
@	debug_output_data	r6
@	debug_output_data	r7
	
@	mov		r5,r4			@	j = i;
	add		r5,r12,#8
	add		r8,r12,r4,lsl #3
eval_topn_02:
	cmp		r8,r5
	blt		eval_topn_02_end
	ldr		r9,[r8,#-8]		@	topn[j].score;
	cmp		r6,r9			@	(int32)d > topn[j -1].score;
	ble		eval_topn_02_end
	
	
	str		r9,[r8],#4
	
	ldr		r9,[r8,#-8]
	str		r9,[r8],#-12
	
	
@	subs	r5,r5,#1
	b		eval_topn_02
	
eval_topn_02_end:
@	add		r8,r12,r5,lsl #3
	stmia	r8!,{r6,r7}
	
@	debug_output_line	#0xa5
@	debug_output_data	r5
eval_topn_00_end:
	add		r4,r4,#1
	b		eval_topn_00
	
eval_topn_end:
	ldmfd 	sp!,{r3-r12,pc}
	
	
	
	



eval_cb_arm_code:
	stmfd 	sp!,{r3-r12,lr}
@	ldr		r3,=eval_topn_var
@	str		r0,[r3,#ARM_STACK_VAR_0]
@	str		r1,[r3,#ARM_STACK_VAR_1]

@	debug_output_line	#0xa0

	
	ldr		r4,[r0,#s2_semi_mgau_s_offset_veclen]
	ldr		r12,[r4,r1,lsl #2]	@	ceplen = s->veclen[feat];
	
	ldr		r4,[r0,#s2_semi_mgau_s_offset_means]
	ldr		r5,[r4,r1,lsl #2]
	ldr		r11,[r5]		@	mean = s->means[feat][0];
	
	ldr		r4,[r0,#s2_semi_mgau_s_offset_vars]
	ldr		r5,[r4,r1,lsl #2]
	ldr		r9,[r5]		@	var = s->vars[feat][0];
	
	ldrh	r7,[r0,#s2_semi_mgau_s_offset_max_topn]	@	int16 max_topn = s->max_topn;

	ldr		r4,[r0,#s2_semi_mgau_s_offset_f]
	ldr		r6,[r4,r1,lsl #2]		@	topn = s->f[feat];
@	debug_output_data	r6
	
	sub		r8,r7,#1
	add		r8,r6,r8,lsl #3			@	worst = topn + (max_topn - 1);
	
	
	ldr		r4,[r0,#s2_semi_mgau_s_offset_dets]	@
	ldr		r5,[r4,r1,lsl #2]		@	det = s->dets[feat];
	
	
	ldrh	r4,[r0,#s2_semi_mgau_s_offset_n_density]	@ s->n_density;
	add		r1,r5,r4,lsl #2		@	detE = det + s->n_density;
	
	
	mov		r4,r5					@	detP = det;
	
eval_cb_00:
@	debug_output_line	#0xa1
	cmp		r4,r1		@	detP < detE;
	bge		eval_cb_end
	
	
	ldr		r0,[r4]		@	d = *detP;
	mov		r3,r2		@	obs = z;
	
	ldr		r10,[r8,#vqFeature_s_offset_score]	@	int32 worst_score = worst->score;
	
	
@r1,r4,r5,r6,r7
	stmfd 	sp!,{r1,r4-r7}
	
@	debug_output_line	#0xa2
@	debug_output_data	r12
@	debug_output_data	r0
@	debug_output_data	r10
	mov 	r1,#0		@	j = 0;
eval_cb_01:
	cmp		r1,r12		@	(j < ceplen)
	bge		eval_cb_01_end
	cmp		r0,r10		@	(d >= worst_score)
	blt		eval_cb_01_end
	
	ldr		r4,[r3],#4	@	*obs++
	ldr		r5,[r11],#4	@	*mean++
	
@	debug_output_line	#0xb0
@	debug_output_data	r4
@	debug_output_data	r5
	
	subs	r4,r4,r5	@	diff = *obs++ - *mean++;

@	debug_output_line	#0xb2
@	debug_output_data	r1
@	debug_output_data	r4
	
@	MFCCMUL
@	#define FIXMUL(a,b) FIXMUL_ANY(a,b,DEFAULT_RADIX)
	smull	r5,r6,r4,r4		@ COSMUL
	mov 	r5,r5,lsr #DEFAULT_RADIX
	orr 	r5,r5,r6,lsl #20	@ 32 - 12
	
	ldr		r4,[r9],#4			@	*var
	
@	debug_output_data	r4
	
	smull	r7,r6,r5,r4		@ COSMUL
	mov 	r7,r7,lsr #DEFAULT_RADIX
	orr 	r7,r7,r6,lsl #20	@ 32 - 12
	
@	d = GMMSUB(d, compl);
@#define GMMSUB(a,b) 	(((a)-(b) > a) ? (INT_MIN) : ((a)-(b)))
	sub		r4,r0,r7
	cmp		r4,r0
	movgt	r0,#ARM_INT_MIN
	movle	r0,r4
	
	add		r1,r1,#1
	b		eval_cb_01
	
eval_cb_01_end:
	mov		r3,r1	@	用R3吧 J 的	值传出去
	ldmfd 	sp!,{r1,r4-r7}
	
@	debug_output_line	#0xa3
@	debug_output_data	r3
@	debug_output_data	r0
	
	cmp		r12,r3
	subgt	r3,r12,r3			@	if (j < ceplen)  == (ceplen > j)
	addgt	r11,r11,r3,lsl #2	@	mean += (ceplen - j);
	addgt	r9,r9,r3,lsl #2		@	var += (ceplen - j);
	bgt		eval_cb_00_end
	
	
@	debug_output_line	#0xaf
	cmp		r0,r10		@	if ((int32)d < worst_score) 
	blt		eval_cb_00_end
	
@	debug_output_line	#0xa4
	@debug_output_data	r4
	@debug_output_data	r5
	sub		r3,r4,r5	@	cw = detP - det;
	lsr		r3,r3,#2
@r0,r1,r3,r4,r5,
	stmfd 	sp!,{r0-r1,r4-r5}
@	debug_output_line	#0xa5
@	debug_output_data	r3
	mov		r0,#0	@	i = 0;
eval_cb_02:
@	debug_output_line	#0xb0
	cmp		r0,r7	@	i < max_topn;
	bge		eval_cb_02_end
	
@	debug_output_line	#0xb1
@	@debug_output_data	r6
@	@debug_output_data	r0
@	@debug_output_data	r3
	add		r1,r6,r0,lsl #3
@	@debug_output_data	r1
	ldr		r4,[r1,#vqFeature_s_offset_codeword]	@topn[i].codeword
@	debug_output_data	r4
	cmp		r4,r3	@	if (topn[i].codeword == cw)
	beq		eval_cb_02_end
	
	add		r0,r0,#1	@	i++
	b		eval_cb_02
	
eval_cb_02_end:
@	@debug_output_line	#0xbf
@	@debug_output_data	r0
	mov		r10,r0
	ldmfd 	sp!,{r0-r1,r4-r5}
	
@	debug_output_line	#0xa6
@	debug_output_data	r10
@	debug_output_data	r7
	cmp		r10,r7		@	if (i < max_topn)
	blt		eval_cb_00_end
	
	
	
@r0 = d 	r3 = cw		r6 = topn	r8 = worst	
@	r1,r4,r5,r7,r9,r11,r12
	stmfd 	sp!,{r1,r4-r5,r7,r9,r11-r12}
@	debug_output_line	#0xa7
	mov		r1,r8
	add		r12,r6,#8	@	(topn + 1)
eval_cb_03:
	cmp		r1,r12
	blt		eval_cb_03_end
	ldr		r4,[r1,#-8]
	cmp		r0,r4
	blt		eval_cb_03_end
	
	str		r4,[r1],#4
	
	ldr		r4,[r1,#-8]
	str		r4,[r1],#-12
	
	b		eval_cb_03
eval_cb_03_end:
	stmia	r1!,{r0,r3}
	ldmfd 	sp!,{r1,r4-r5,r7,r9,r11-r12}
	
@	debug_output_line	#0xa8
	
	
eval_cb_00_end:
	add		r4,r4,#4	@	++detP
	b		eval_cb_00
	
eval_cb_end:
@	debug_output_line	#0xa9
@	ldr		r3,=eval_topn_var
@	ldr		r0,[r3,#ARM_STACK_VAR_0]
@	ldr		r1,[r3,#ARM_STACK_VAR_1]
	ldmfd 	sp!,{r3-r12,pc}
	
	
	
	
	
	
	
	
	
@	debug_output_line	#0xaa
@	debug_output_data	r0	
	
eval_topn_test:
	stmfd 	sp!,{r1-r12,lr}
	debug_output_data r0
	mov		r12,r0
	ldmia	r0!,{r1-r6}
	debug_output_data r0
	debug_output_data r1
	debug_output_data r2
	debug_output_data r3
	debug_output_data r4
	debug_output_data r5
	debug_output_data r6
@	str		r6,[r12]
@	str		r5,[r12],#4
@	str		r4,[r12],#4
	add		r1,r1,#0x10
	add		r2,r2,#0x10
	add		r3,r3,#0x10
	add		r4,r4,#0x10
	add		r5,r5,#0x10
	add		r6,r6,#0x10
	stmia	r12!,{r6,r1-r5}
	ldmfd 	sp!,{r1-r12,pc}
	
	
	
@ SP堆栈指针 逆向增长
vcyber_print_sp_address:
	stmfd 	sp!,{r0-r1,lr}
	debug_output_sp_addr	sp
	mov		r0,#0xaa
	str		r0,[sp,#-0x10]
	str		r0,[sp,#-0x14]
	str		r0,[sp,#-0x18]
	str		r0,[sp,#-0x1c]
	ldmfd 	sp!,{r0-r1,pc}
	
	

get_sum_arm_code:
	stmfd 	sp!,{r2-r12,lr}
	mov		r5,#0
	
	subs	r3,r1,#1
	bmi		get_sum_end	
get_sum_00:
	ldr		r4,[r0,r3,lsl #2]
	adds	r5,r5,r4
	
	subs	r3,r3,#1
	bpl		get_sum_00

get_sum_end:
	mov		r0,r5
	ldmfd 	sp!,{r2-r12,pc}
	