#ifndef _CMU_SPHINX_DEBUG_SWITCH_H_
#define _CMU_SPHINX_DEBUG_SWITCH_H_

/*
*	以下开关如果打开，代表使用汇编代码
*	验证OK代表开关打开，速度会有提升
*/
//

//验证OK
#define	SWAP_INT16_USE_ARM_CODE		0
//验证OK
#define	SWAP_INT32_USE_ARM_CODE		0
//验证OK
#define FE_PRE_EMPHASIS_ARM_CODE		0

//验证OK,但是速度提升有限，可能得话继续优化
#define	FE_HAMMING_WINDOW_ARM_CODE	0


//验证OK,但是速度提升有限，可能得话继续优化
#define	FE_FFT_REAL_ARM_CODE			0


//验证OK,但是速度提升有限，可能得话继续优化
#define FE_MEL_SPEC_ARM_CODE			0


//验证OK,但是速度提升有限，可能得话继续优化
#define FE_DCT2_ARM_CODE				0


//经测试速度提升基本忽略。。。。。。
#define ACMOD_ACTIVATE_HMM_ARM_CODE	0

//经测试速度提升基本忽略。。。。。。
#define GET_SCORES_4B_FEAT_4_ARM_CODE	1


//验证OK,但是速度提升有限，可能得话继续优化
#define MGAU_NORM_ARM_CODE	1

// 验证OK，
#define EVAL_TOPN_ARM_CODE	1


// 验证OK，
#define EVAL_CB_ARM_CODE	1




#ifdef __cplusplus
extern "C" {
#endif

extern void vcyber_print_sp_address(void);



extern int fft_order_shift_table[];


extern void swap_int16_arm_code(short *p_in);
extern void swap_int32_arm_code(int *p_in);

extern void fe_pre_emphasis_fixed_point_arm_code(short *p_in,int *p_out,int len,int fxd_alpha);

//extern void fe_hamming_window_arm_code(int * in,int in_len, int * window,  int remove_dc);
extern int fe_hamming_window_0_arm_code(int * in,int in_len);
extern void fe_hamming_window_1_arm_code(int * in,int in_len, int sum);
extern void fe_hamming_window_2_arm_code(int * in,int in_len, int * window);

extern int fe_fft_real_arm_code(void *fe);
extern void fe_fft_real_arm_code_test(int *fe,int i);

extern void fe_mel_spec_arm_code(void  *fe);

extern void fe_dct2_arm_code(void *fe, void *mflogspec, void *mfcep, int htk);


extern int get_sum_arm_code(int *p_in,int len);

extern void acmod_activate_hmm_arm_code(void *acmod, void *hmm);

extern int get_scores_4b_feat_4_arm_code(void *s, int i,short *senone_scores, unsigned char *senone_active,int n_senone_active);

extern int mgau_norm_arm_code(void *s, int feat);

extern void eval_topn_arm_code(void *s, int32 feat, void *z);
extern void eval_topn_test(int *p);


extern void eval_cb_arm_code(void *s, int32 feat, void *z);

#ifdef __cplusplus
} // End of extern "C"
#endif

#endif//_CMU_SPHINX_DEBUG_SWITCH_H_

