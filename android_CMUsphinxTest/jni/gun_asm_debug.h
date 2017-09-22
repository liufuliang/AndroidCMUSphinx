	.include "limits.h"
	.extern asm_debug_data0
	.extern asm_debug_data1
	.extern asm_debug_data2
	
/*
*	用于输出立即数，确定位置
*/
.macro	debug_output_line data
	stmfd	sp!,{r0-r12,lr}
	mov 	r0,\data
	bl	asm_debug_data0
	ldmfd sp!,{r0-r12,lr}
.endm

/*
*	用于输出寄存器值
*/
.macro	debug_output_data data
	stmfd	sp!,{r0-r12,lr}
	mov 	r0,\data
	bl	asm_debug_data1
	ldmfd sp!,{r0-r12,lr}
.endm

/*
*	用于输出 SP  地址
*/
.macro	debug_output_sp_addr data
	stmfd	sp!,{r0-r12,lr}
	mov 	r0,\data
	bl	asm_debug_data2
	ldmfd sp!,{r0-r12,lr}
.endm

/*
*	临时变量区----堆内存----访问栈内存出错，以后在查原因
*/
	.equ ARM_STACK_VAR_0, 0
	.equ ARM_STACK_VAR_1, 4
	.equ ARM_STACK_VAR_2, 8
	.equ ARM_STACK_VAR_3, 12



@	#define INT_MAX 2147483647  	//0x7fffffff
@	#define INT_MIN (-INT_MAX - 1)  

	.equ ARM_INT_MAX, 2147483647
	.equ ARM_INT_MIN, -2147483648
;@ --------------------------- Defines ----------------------------
@	#define DEFAULT_RADIX 12
	.equ DEFAULT_RADIX, 12
@	32 - 12
	.equ DEFAULT_RADIX_RSB_0, 20
	
@	4*32 = 128
	.equ fft_var_k_offset, 128
	
	.equ fft_var_n1_offset, fft_var_k_offset+4	
	.equ fft_var_n2_offset, fft_var_n1_offset+4	
	.equ fft_var_n4_offset, fft_var_n2_offset+4
	
	.equ fft_var_1_lsl_n1_offset, fft_var_n4_offset+4
	.equ fft_var_1_lsl_n2_offset, fft_var_1_lsl_n1_offset+4
	.equ fft_var_1_lsl_n4_offset, fft_var_1_lsl_n2_offset+4
	
	.equ fft_var_i1_offset, fft_var_1_lsl_n4_offset+4
	.equ fft_var_i2_offset, fft_var_i1_offset+4
	.equ fft_var_i3_offset, fft_var_i2_offset+4
	.equ fft_var_i4_offset, fft_var_i3_offset+4
	
	.equ fft_var_m_sub_n1_offset, fft_var_i4_offset+4
	
	.equ fft_var_reserved_offset, fft_var_m_sub_n1_offset+4
	
	
@/** Structure for the front-end computation. */ 
@struct fe_s {
@    cmd_ln_t *config;				// [r0,#0x00]
	.equ fe_s_offset_config, 				0x00
@    int refcount;					// [r0,#0x04]
	.equ fe_s_offset_refcount, 				0x04
@
@    float32 sampling_rate;			// [r0,#0x08]
	.equ fe_s_offset_sampling_rate,			0x08
@    int16 frame_rate;				// [r0,#0x0C]
	.equ fe_s_offset_frame_rate,			0x0C
@    int16 frame_shift;				// [r0,#0x0E]
	.equ fe_s_offset_frame_shift,			0x0E
@
@    float32 window_length;			// [r0,#0x10]
	.equ fe_s_offset_window_length,			0x10
@    int16 frame_size;				// [r0,#0x14]
	.equ fe_s_offset_frame_size,			0x14
@    int16 fft_size;				// [r0,#0x16]
	.equ fe_s_offset_fft_size, 				0x16
@
@    uint8 fft_order;				// [r0,#0x18]
	.equ fe_s_offset_fft_order,				0x18
@    uint8 feature_dimension;		// [r0,#0x19]
	.equ fe_s_offset_feature_dimension,		0x19
@    uint8 num_cepstra;				// [r0,#0x1A]
	.equ fe_s_offset_num_cepstra,			0x1A
@    uint8 remove_dc;				// [r0,#0x1B]
	.equ fe_s_offset_remove_dc,				0x1B
@    uint8 log_spec;				// [r0,#0x1C]
	.equ fe_s_offset_log_spec, 				0x1C
@    uint8 swap;					// [r0,#0x1D]
	.equ fe_s_offset_swap,	 				0x1D
@    uint8 dither;					// [r0,#0x1E]
	.equ fe_s_offset_dither, 				0x1E
@    uint8 transform;				// [r0,#0x1F]
	.equ fe_s_offset_transform,				0x1F
@
@    float32 pre_emphasis_alpha;	// [r0,#0x20]
	.equ fe_s_offset_pre_emphasis_alpha,	0x20
@    int32 seed;					// [r0,#0x24]
	.equ fe_s_offset_seed,	 				0x24
@
@    int16 frame_counter;			// [r0,#0x28]
	.equ fe_s_offset_frame_counter,			0x28
@    uint8 start_flag;				// [r0,#0x2A]
	.equ fe_s_offset_start_flag,			0x2A
@    uint8 reserved;				// [r0,#0x2B]
	.equ fe_s_offset_reserved, 				0x2B
@
@    /* Twiddle factors for FFT. */
@    frame_t *ccc, *sss;			// [r0,#0x2C]  // [r0,#0x30]
	.equ fe_s_offset_ccc, 					0x2C
	.equ fe_s_offset_sss, 					0x30
@    /* Mel filter parameters. */
@    melfb_t *mel_fb;				// [r0,#0x34]
	.equ fe_s_offset_mel_fb, 				0x34
@    /* Half of a Hamming Window. */
@    window_t *hamming_window;		// [r0,#0x38]
	.equ fe_s_offset_hamming_window,		0x38
@
@    /* Temporary buffers for processing. */
@    /* FIXME: too many of these. */
@    int16 *spch;					// [r0,#0x3C]
	.equ fe_s_offset_spch, 					0x3C
@    frame_t *frame;				// [r0,#0x40]
	.equ fe_s_offset_frame, 				0x40
@    powspec_t *spec, *mfspec;		// [r0,#0x44]  // [r0,#0x48]
	.equ fe_s_offset_spec,	 				0x44
	.equ fe_s_offset_mfspec, 				0x48
@    int16 *overflow_samps;			// [r0,#0x4C]
	.equ fe_s_offset_overflow_samps,		0x4C
@    int16 num_overflow_samps;    	// [r0,#0x50]
	.equ fe_s_offset_num_overflow_samps,	0x50
@    int16 prior;					// [r0,#0x52]
	.equ fe_s_offset_prior, 				0x52
@};








@/** Base Struct to hold all structure for MFCC computation. */
@struct melfb_s {
@    float32 sampling_rate;				// [r0,#0x00]
	.equ melfb_s_offset_sampling_rate, 				0x00
@    int32 num_cepstra;					// [r0,#0x04]
	.equ melfb_s_offset_num_cepstra, 				0x04
@    int32 num_filters;					// [r0,#0x08]
	.equ melfb_s_offset_num_filters, 				0x08
@    int32 fft_size;					// [r0,#0x0c]
	.equ melfb_s_offset_fft_size, 					0x0c
@    float32 lower_filt_freq;			// [r0,#0x10]
	.equ melfb_s_offset_lower_filt_freq, 			0x10
@    float32 upper_filt_freq;			// [r0,#0x14]
	.equ melfb_s_offset_upper_filt_freq, 			0x14
@    /* DCT coefficients. */
@    mfcc_t **mel_cosine;				// [r0,#0x18]
	.equ melfb_s_offset_mel_cosine, 				0x18
@    /* Filter coefficients. */
@    mfcc_t *filt_coeffs;				// [r0,#0x1c]
	.equ melfb_s_offset_filt_coeffs, 				0x1c
@    int16 *spec_start;					// [r0,#0x20]
	.equ melfb_s_offset_spec_start, 				0x20
@    int16 *filt_start;					// [r0,#0x24]
	.equ melfb_s_offset_filt_start, 				0x24
@    int16 *filt_width;					// [r0,#0x28]
	.equ melfb_s_offset_filt_width, 				0x28
@    /* Luxury mobile home. */
@    int32 doublewide;					// [r0,#0x2c]
	.equ melfb_s_offset_doublewide, 				0x2c
@    char const *warp_type;				// [r0,#0x30]
	.equ melfb_s_offset_warp_type, 					0x30
@    char const *warp_params;			// [r0,#0x34]
	.equ melfb_s_offset_warp_params, 				0x34
@    uint32 warp_id;						// [r0,#0x38]
	.equ melfb_s_offset_warp_id, 					0x38
@    /* Precomputed normalization constants for unitary DCT-II/DCT-III */
@    mfcc_t sqrt_inv_n, sqrt_inv_2n;	// [r0,#0x3c]	// [r0,#0x40]
	.equ melfb_s_offset_sqrt_inv_n, 				0x3c
	.equ melfb_s_offset_sqrt_inv_2n, 				0x40
@    /* Value and coefficients for HTK-style liftering */
@    int32 lifter_val;					// [r0,#0x44]
	.equ melfb_s_offset_lifter_val, 				0x44
@    mfcc_t *lifter;						// [r0,#0x48]
	.equ melfb_s_offset_lifter, 					0x48
@    /* Normalize filters to unit area */
@    int32 unit_area;					// [r0,#0x4c]
	.equ melfb_s_offset_unit_area, 					0x4c
@    /* Round filter frequencies to DFT points (hurts accuracy, but is
@       useful for legacy purposes) */
@    int32 round_filters;				// [r0,#0x50]
	.equ melfb_s_offset_round_filters, 				0x50
@};




@#define BITVEC_BITS 32
	.equ BITVEC_BITS,32
	
@#define BITVEC_BITS 32
	.equ BITVEC_BITS_SUB_1,31
	
	.equ BITVEC_BITS_SHIFT_BITS,5
	
@/**
@ * Invalid senone sequence ID (limited to 16 bits for PocketSphinx).
@ */
@#define BAD_SSID 0xffff  
	.equ BAD_SSID,0xffff
@/**
@ * Invalid senone ID (limited to 16 bits for PocketSphinx).
@ */
@#define BAD_SENID 0xffff
	.equ BAD_SENID,0xffff

@struct acmod_s {
@    /* Global objects, not retained. */
@    cmd_ln_t *config;         // [r0,#0x00]	 /**< Configuration. */
	.equ acmod_s_offset_config,			0x00
@    logmath_t *lmath;         // [r0,#0x04]	 /**< Log-math computation. */
	.equ acmod_s_offset_lmath,			0x04
@    glist_t strings;          // [r0,#0x08]	 /**< Temporary acoustic model filenames. */
	.equ acmod_s_offset_strings,		0x08
@
@    /* Feature computation: */
@    fe_t *fe;                  // [r0,#0x0c]	/**< Acoustic feature computation. */
	.equ acmod_s_offset_fe,				0x0C
@    feat_t *fcb;               // [r0,#0x10]	/**< Dynamic feature computation. */
	.equ acmod_s_offset_fcb,			0x10
@
@    /* Model parameters: */
@    bin_mdef_t *mdef;          // [r0,#0x14]	/**< Model definition. */
	.equ acmod_s_offset_mdef,			0x14
@    tmat_t *tmat;              // [r0,#0x18]	/**< Transition matrices. */
	.equ acmod_s_offset_tmat,			0x18
@    ps_mgau_t *mgau;           // [r0,#0x1c]	/**< Model parameters. */
	.equ acmod_s_offset_mgau,			0x1C
@    ps_mllr_t *mllr;           // [r0,#0x20]	/**< Speaker transformation. */
	.equ acmod_s_offset_mllr,			0x20
@
@    /* Senone scoring: */
@    int16 *senone_scores;     // [r0,#0x24]	/**< GMM scores for current frame. */
	.equ acmod_s_offset_senone_scores,	0x24
@    bitvec_t *senone_active_vec; // [r0,#0x28]	/**< Active GMMs in current frame. */
	.equ acmod_s_offset_senone_active_vec,	0x28
@    uint8 *senone_active;     // [r0,#0x2C]	/**< Array of deltas to active GMMs. */
	.equ acmod_s_offset_senone_active,	0x2C
@    int senscr_frame;         // [r0,#0x30]	 /**< Frame index for senone_scores. */
	.equ acmod_s_offset_senscr_frame,	0x30
@    int n_senone_active;      // [r0,#0x34]	 /**< Number of active GMMs. */
	.equ acmod_s_offset_n_senone_active,0x34
@    int log_zero;              // [r0,#0x38]	/**< Zero log-probability value. */
	.equ acmod_s_offset_log_zero,		0x38
@
@    /* Utterance processing: */
@    mfcc_t **mfc_buf;   		// [r0,#0x3C]	/**< Temporary buffer of acoustic features. */
	.equ acmod_s_offset_mfc_buf,		0x3C
@    mfcc_t ***feat_buf; 		// [r0,#0x40]	/**< Temporary buffer of dynamic features. */
	.equ acmod_s_offset_feat_buf,		0x40
@    FILE *rawfh;        		// [r0,#0x44]	/**< File for writing raw audio data. */
	.equ acmod_s_offset_rawfh,			0x44
@    FILE *mfcfh;        		// [r0,#0x48]	/**< File for writing acoustic feature data. */
	.equ acmod_s_offset_mfcfh,			0x48
@    FILE *senfh;        		// [r0,#0x4C]	/**< File for writing senone score data. */
	.equ acmod_s_offset_senfh,			0x4C
@    FILE *insenfh;				// [r0,#0x50]	/**< Input senone score file. */
	.equ acmod_s_offset_insenfh,		0x50
@    long *framepos;     		// [r0,#0x54]	/**< File positions of recent frames in senone file. */
	.equ acmod_s_offset_framepos,		0x54
@
@    /* A whole bunch of flags and counters: */
@    uint8 state;        		// [r0,#0x58]	/**< State of utterance processing. */
	.equ acmod_s_offset_state,			0x58
@    uint8 compallsen;   		// [r0,#0x59]	/**< Compute all senones? */
	.equ acmod_s_offset_compallsen,		0x59
@    uint8 grow_feat;    		// [r0,#0x5A]	/**< Whether to grow feat_buf. */
	.equ acmod_s_offset_grow_feat,		0x5A
@    uint8 insen_swap;   		// [r0,#0x5B]	/**< Whether to swap input senone score. */
	.equ acmod_s_offset_insen_swap,		0x5B
@
@    frame_idx_t output_frame; // [r0,#0x5C]	/**< Index of next frame of dynamic features. */
	.equ acmod_s_offset_output_frame,	0x5C
@    frame_idx_t n_mfc_alloc;  // [r0,#0x5E]	/**< Number of frames allocated in mfc_buf */
	.equ acmod_s_offset_n_mfc_alloc,	0x5E
@    frame_idx_t n_mfc_frame;  // [r0,#0x60]	/**< Number of frames active in mfc_buf */
	.equ acmod_s_offset_n_mfc_frame,	0x60
@    frame_idx_t mfc_outidx;   // [r0,#0x62]	/**< Start of active frames in mfc_buf */
	.equ acmod_s_offset_mfc_outidx,		0x62
@    frame_idx_t n_feat_alloc; // [r0,#0x64]	/**< Number of frames allocated in feat_buf */
	.equ acmod_s_offset_n_feat_alloc,	0x64
@    frame_idx_t n_feat_frame; // [r0,#0x66]	/**< Number of frames active in feat_buf */
	.equ acmod_s_offset_n_feat_frame,	0x66
@    frame_idx_t feat_outidx;  // [r0,#0x68]	/**< Start of active frames in feat_buf */
	.equ acmod_s_offset_feat_outidx,	0x68
@};




@/**
@ * @struct hmm_t
@ * @brief An individual HMM among the HMM search space.
@ *
@ * An individual HMM among the HMM search space.  An HMM with N
@ * emitting states consists of N+1 internal states including the
@ * non-emitting exit (out) state.
@ */
@typedef struct hmm_s {
@    hmm_context_t *ctx;            // [r0,#0x00]	/**< Shared context data for this HMM. */
	.equ hmm_s_offset_ctx,					0x00
@    int32 score[HMM_MAX_NSTATE];   // [r0,#0x04]	/**< State scores for emitting states. */
	.equ hmm_s_offset_score,				0x04
@    								// [r0,#0x08]	// [r0,#0x0C] // [r0,#0x10] // [r0,#0x14]
@    int32 history[HMM_MAX_NSTATE]; // [r0,#0x18]	/**< History indices for emitting states. */
	.equ hmm_s_offset_history,				0x18
@    								// [r0,#0x1C]	// [r0,#0x20] // [r0,#0x24] // [r0,#0x28]
@    int32 out_score;               // [r0,#0x2C]	/**< Score for non-emitting exit state. */
	.equ hmm_s_offset_out_score,			0x2C
@    int32 out_history;             // [r0,#0x30]	/**< History index for non-emitting exit state. */
	.equ hmm_s_offset_out_history,			0x30
@    uint16 ssid;                   // [r0,#0x34]	/**< Senone sequence ID (for non-MPX) */
	.equ hmm_s_offset_ssid,					0x34
@    uint16 senid[HMM_MAX_NSTATE];  // [r0,#0x36]/**< Senone IDs (non-MPX) or sequence IDs (MPX) */
	.equ hmm_s_offset_senid,				0x36
@    								// [r0,#0x38]	// [r0,#0x3A] // [r0,#0x3C] // [r0,#0x3E]
@    int32 bestscore;				// [r0,#0x40] 	/**< Best [emitting] state score in current frame (for pruning). */
	.equ hmm_s_offset_bestscore,			0x40
@    int16 tmatid;       			// [r0,#0x44]	/**< Transition matrix ID (see hmm_context_t). */
	.equ hmm_s_offset_tmatid,				0x44
@    frame_idx_t frame;  			// [r0,#0x46]	/**< Frame in which this HMM was last active; <0 if inactive */
	.equ hmm_s_offset_frame,				0x46
@    uint8 mpx;          			// [r0,#0x48]	/**< Is this HMM multiplex? (hoisted for speed) */
	.equ hmm_s_offset_mpx,					0x48
@    uint8 n_emit_state; 			// [r0,#0x49] 	/**< Number of emitting states (hoisted for speed) */
	.equ hmm_s_offset_n_emit_state,			0x49
@} hmm_t;




@/**
@ * @struct hmm_context_t
@ * @brief Shared information between a set of HMMs.
@ *
@ * We assume that the initial state is emitting and that the
@ * transition matrix is n_emit_state x (n_emit_state+1), where the
@ * extra destination dimension correponds to the non-emitting final or
@ * exit state.
@ */
@typedef struct hmm_context_s {
@    int32 n_emit_state;     // [r0,#0x00]		/**< Number of emitting states in this set of HMMs. */
	.equ hmm_context_s_offset_n_emit_state,	0x00
@    uint8 ** const *tp;	     // [r0,#0x04]		/**< State transition scores tp[id][from][to] (logs3 values). */
	.equ hmm_context_s_offset_tp,		0x04
@    int16 const *senscore;  	// [r0,#0x08]		/**< State emission scores senscore[senid]
@                               (negated scaled logs3 values). */
	.equ hmm_context_s_offset_senscore,	0x08
@    uint16 * const *sseq;    	// [r0,#0x0C]		/**< Senone sequence mapping. */
	.equ hmm_context_s_offset_sseq,		0x0C
@    int32 *st_sen_scr;      	// [r0,#0x10]/**< Temporary array of senone scores (for some topologies). */
	.equ hmm_context_s_offset_st_sen_scr,0x10
@    listelem_alloc_t *mpx_ssid_alloc; 	// [r0,#0x14]	/**< Allocator for senone sequence ID arrays. */
	.equ hmm_context_s_offset_mpx_ssid_alloc,	0x14
@    void *udata;            			// [r0,#0x18]		/**< Whatever you feel like, gosh. */
	.equ hmm_context_s_offset_mpx_ssid_alloc,	0x18
@} hmm_context_t;
;@ --------------------------- Defines ----------------------------	
	
	
	
	
@	uint8 w_den[4 * 16];	
	.equ w_den_size,		0x40
	
	.equ pid_cw0_offset,		0x44
	.equ pid_cw1_offset,		0x48
	.equ pid_cw2_offset,		0x4c
	.equ pid_cw3_offset,		0x50
	
	.equ vqFeature_t_f_0_score_offset,	0x00
	.equ vqFeature_t_f_1_score_offset,	0x08 
	.equ vqFeature_t_f_2_score_offset,	0x10 
	.equ vqFeature_t_f_3_score_offset,	0x18
	
	.equ vqFeature_t_f_0_codeword_offset,	0x04
	.equ vqFeature_t_f_1_codeword_offset,	0x0C
	.equ vqFeature_t_f_2_codeword_offset,	0x14
	.equ vqFeature_t_f_3_codeword_offset,	0x1C
	
@struct vqFeature_s {
@    int32 score; 			// [r0,#0x00]		/* score or distance */
	.equ vqFeature_s_offset_score,		0x00
@    int32 codeword; 		// [r0,#0x04]		/* codeword (vector index) */
	.equ vqFeature_s_offset_codeword,	0x04
@};	
	
	
@struct logadd_s {
@    /** Table, in unsigned integers of (width) bytes. */
@    void *table	;		// [r0,#0x0]
	.equ logadd_s_offset_table,		0x00
@    /** Number of elements in (table).  This is never smaller than 256 (important!) */
@    uint32 table_size;	// [r0,#0x4]
	.equ logadd_s_offset_table_size,		0x04
@    /** Width of elements of (table). */
@    uint8 width;		// [r0,#0x8]
	.equ logadd_s_offset_width,		0x08
@    /** Right shift applied to elements in (table). */
@    int8 shift;			// [r0,#0x9]
	.equ logadd_s_offset_shift,		0x09		
@};
	
@struct s2_semi_mgau_s {
@    ps_mgau_t base;     // [r0,#0x00]	/**< base structure. */
	.equ s2_semi_mgau_s_offset_base,	0x00
@    cmd_ln_t *config;   // [r0,#0x08]	/* configuration parameters */
	.equ s2_semi_mgau_s_offset_config,	0x08
@
@    gauden_t *g;        // [r0,#0x0C]	/* Set of Gaussians (pointers below point in here and will go away soon) */
	.equ s2_semi_mgau_s_offset_g,		0x0C
@    mfcc_t  ***means;	// [r0,#0x10]	/* mean vectors foreach feature, density */
	.equ s2_semi_mgau_s_offset_means,	0x10
@    mfcc_t  ***vars;	// [r0,#0x14]	/* inverse var vectors foreach feature, density */
	.equ s2_semi_mgau_s_offset_vars,	0x14
@    mfcc_t  **dets;		// [r0,#0x18]	/* det values foreach cb, feature */
	.equ s2_semi_mgau_s_offset_dets,	0x18
@
@    uint8 ***mixw;     // [r0,#0x1C]	/* mixture weight distributions */
	.equ s2_semi_mgau_s_offset_mixw,	0x1C
@    mmio_file_t *sendump_mmap;	// [r0,#0x20]	/* memory map for mixw (or NULL if not mmap) */
	.equ s2_semi_mgau_s_offset_sendump_mmap,	0x20
@
@    uint8 *mixw_cb;    // [r0,#0x24]	/* mixture weight codebook, if any (assume it contains 16 values) */
	.equ s2_semi_mgau_s_offset_mixw_cb,	0x24
@    int32 *veclen;		// [r0,#0x28]	/* Length of feature streams */
	.equ s2_semi_mgau_s_offset_veclen,	0x28
@    int16 n_feat;		// [r0,#0x2C]	/* Number of feature streams */
	.equ s2_semi_mgau_s_offset_n_feat,	0x2C
@    int16 n_density;	// [r0,#0x2E]	/* Number of mixtures per codebook */
	.equ s2_semi_mgau_s_offset_n_density,	0x2E
@    int32 n_sen;		// [r0,#0x30]	/* Number of senones */
	.equ s2_semi_mgau_s_offset_n_sen,	0x30
@    uint8 *topn_beam;  // [r0,#0x34]	 /* Beam for determining per-frame top-N densities */
	.equ s2_semi_mgau_s_offset_topn_beam,	0x34
@    int16 max_topn;	// [r0,#0x38]
	.equ s2_semi_mgau_s_offset_max_topn,	0x38
@    int16 ds_ratio;	// [r0,#0x3A]
	.equ s2_semi_mgau_s_offset_ds_ratio,	0x3A
@
@    vqFeature_t ***topn_hist; // [r0,#0x3C]	/**< Top-N scores and codewords for past frames. */
	.equ s2_semi_mgau_s_offset_topn_hist,	0x3C
@    uint8 **topn_hist_n;      // [r0,#0x40]	/**< Variable top-N for past frames. */
	.equ s2_semi_mgau_s_offset_topn_hist_n,	0x40
@    vqFeature_t **f;          	// [r0,#0x44]	/**< Topn-N for currently scoring frame. */
	.equ s2_semi_mgau_s_offset_f,			0x44
@    int n_topn_hist;          // [r0,#0x48]	/**< Number of past frames tracked. */
	.equ s2_semi_mgau_s_offset_n_topn_hist,	0x48
@
@    /* Log-add table for compressed values. */
@    logmath_t *lmath_8b;		// [r0,#0x4C]
	.equ s2_semi_mgau_s_offset_lmath_8b,	0x4C
@    /* Log-add object for reloading means/variances. */
@    logmath_t *lmath;			// [r0,#0x50]
	.equ s2_semi_mgau_s_offset_lmath,		0x50
@};	

@#define SENSCR_SHIFT 10
	.equ SENSCR_SHIFT,		10
@#define MAX_NEG_ASCR 96
	.equ MAX_NEG_ASCR,		96
