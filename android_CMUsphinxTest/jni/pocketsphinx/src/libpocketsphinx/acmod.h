/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
/* ====================================================================
 * Copyright (c) 2008 Carnegie Mellon University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * This work was supported in part by funding from the Defense Advanced 
 * Research Projects Agency and the National Science Foundation of the 
 * United States of America, and the CMU Sphinx Speech Consortium.
 *
 * THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND 
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 */

/**
 * @file acmod.h Acoustic model structures for PocketSphinx.
 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
 */

#ifndef __ACMOD_H__
#define __ACMOD_H__

/* System headers. */
#include <stdio.h>

/* SphinxBase headers. */
#include <sphinxbase/cmd_ln.h>
#include <sphinxbase/logmath.h>
#include <sphinxbase/fe.h>
#include <sphinxbase/feat.h>
#include <sphinxbase/bitvec.h>
#include <sphinxbase/err.h>
#include <sphinxbase/prim_type.h>

/* Local headers. */
#include "ps_mllr.h"
#include "bin_mdef.h"
#include "tmat.h"
#include "hmm.h"

#include "cyVoiceE_model.h"
/**
 * States in utterance processing.
 */
typedef enum acmod_state_e {
    ACMOD_IDLE,		/**< Not in an utterance. */
    ACMOD_STARTED,      /**< Utterance started, no data yet. */
    ACMOD_PROCESSING,   /**< Utterance in progress. */
    ACMOD_ENDED         /**< Utterance ended, still buffering. */
} acmod_state_t;

/**
 * Dummy senone score value for unintentionally active states.
 */
#define SENSCR_DUMMY 0x7fff

/**
 * Feature space linear transform structure.
 */
struct ps_mllr_s {
    int refcnt;     /**< Reference count. */
    int n_class;    /**< Number of MLLR classes. */
    int n_feat;     /**< Number of feature streams. */
    int *veclen;    /**< Length of input vectors for each stream. */
    float32 ****A;  /**< Rotation part of mean transformations. */
    float32 ***b;   /**< Bias part of mean transformations. */
    float32 ***h;   /**< Diagonal transformation of variances. */
    int32 *cb2mllr; /**< Mapping from codebooks to transformations. */
};

/**
 * Acoustic model parameter structure. 
 */
typedef struct ps_mgau_s ps_mgau_t;

typedef struct ps_mgaufuncs_s {
    char const *name;

    int (*frame_eval)(ps_mgau_t *mgau,
                      int16 *senscr,
                      uint8 *senone_active,
                      int32 n_senone_active,
                      mfcc_t ** feat,
                      int32 frame,
                      int32 compallsen);
    int (*transform)(ps_mgau_t *mgau,
                     ps_mllr_t *mllr);
    void (*free)(ps_mgau_t *mgau);
} ps_mgaufuncs_t;    

typedef struct word_phone_s {
	char *p_phone_1;
	char *p_phone_2;
	char *p_phone_3;
	char *p_phone_4;
	char *p_phone_5;
//	int len;	//统计链表长度

	struct word_phone_s *next;
}word_phone_t;

struct ps_mgau_s {
    ps_mgaufuncs_t *vt;  /**< vtable of mgau functions. */
    int frame_idx;       /**< frame counter. */
};

#define ps_mgau_base(mg) ((ps_mgau_t *)(mg))
#define ps_mgau_frame_eval(mg,senscr,senone_active,n_senone_active,feat,frame,compallsen) \
    (*ps_mgau_base(mg)->vt->frame_eval)                                 \
    (mg, senscr, senone_active, n_senone_active, feat, frame, compallsen)
#define ps_mgau_transform(mg, mllr)                                  \
    (*ps_mgau_base(mg)->vt->transform)(mg, mllr)
#define ps_mgau_free(mg)                                  \
    (*ps_mgau_base(mg)->vt->free)(mg)

/**
 * Acoustic model structure.
 *
 * This object encapsulates all stages of acoustic processing, from
 * raw audio input to acoustic score output.  The reason for grouping
 * all of these modules together is that they all have to "agree" in
 * their parameterizations, and the configuration of the acoustic and
 * dynamic feature computation is completely dependent on the
 * parameters used to build the original acoustic model (which should
 * by now always be specified in a feat.params file).
 *
 * Because there is not a one-to-one correspondence from blocks of
 * input audio or frames of input features to frames of acoustic
 * scores (due to dynamic feature calculation), results may not be
 * immediately available after input, and the output results will not
 * correspond to the last piece of data input.
 *
 * TODO: In addition, this structure serves the purpose of queueing
 * frames of features (and potentially also scores in the future) for
 * asynchronous passes of recognition operating in parallel.
 */
struct acmod_s {
    /* Global objects, not retained. */
    cmd_ln_t *config;         // [r0,#0x00]	 /**< Configuration. */
    logmath_t *lmath;         // [r0,#0x04]	 /**< Log-math computation. */
    glist_t strings;          // [r0,#0x08]	 /**< Temporary acoustic model filenames. */

    /* Feature computation: */
    fe_t *fe;                  // [r0,#0x0c]	/**< Acoustic feature computation. */
    feat_t *fcb;               // [r0,#0x10]	/**< Dynamic feature computation. */

    /* Model parameters: */
    bin_mdef_t *mdef;          // [r0,#0x14]	/**< Model definition. */
    tmat_t *tmat;              // [r0,#0x18]	/**< Transition matrices. */
    ps_mgau_t *mgau;           // [r0,#0x1c]	/**< Model parameters. */
    ps_mllr_t *mllr;           // [r0,#0x20]	/**< Speaker transformation. */

    /* Senone scoring: */
    int16 *senone_scores;     // [r0,#0x24]	/**< GMM scores for current frame. */
    bitvec_t *senone_active_vec; // [r0,#0x28]	/**< Active GMMs in current frame. */
    uint8 *senone_active;     // [r0,#0x2C]	/**< Array of deltas to active GMMs. */
    int senscr_frame;         // [r0,#0x30]	 /**< Frame index for senone_scores. */
    int n_senone_active;      // [r0,#0x34]	 /**< Number of active GMMs. */
    int log_zero;              // [r0,#0x38]	/**< Zero log-probability value. */

    /* Utterance processing: */
    mfcc_t **mfc_buf;   		// [r0,#0x3C]	/**< Temporary buffer of acoustic features. */
    mfcc_t ***feat_buf; 		// [r0,#0x40]	/**< Temporary buffer of dynamic features. */
    FILE *rawfh;        		// [r0,#0x44]	/**< File for writing raw audio data. */
    FILE *mfcfh;        		// [r0,#0x48]	/**< File for writing acoustic feature data. */
    FILE *senfh;        		// [r0,#0x4C]	/**< File for writing senone score data. */
    FILE *insenfh;				// [r0,#0x50]	/**< Input senone score file. */
    long *framepos;     		// [r0,#0x54]	/**< File positions of recent frames in senone file. */

    /* A whole bunch of flags and counters: */
    uint8 state;        		// [r0,#0x58]	/**< State of utterance processing. */
    uint8 compallsen;   		// [r0,#0x59]	/**< Compute all senones? */
    uint8 grow_feat;    		// [r0,#0x5A]	/**< Whether to grow feat_buf. */
    uint8 insen_swap;   		// [r0,#0x5B]	/**< Whether to swap input senone score. */

    frame_idx_t output_frame; // [r0,#0x5C]	/**< Index of next frame of dynamic features. */
    frame_idx_t n_mfc_alloc;  // [r0,#0x5E]	/**< Number of frames allocated in mfc_buf */
    frame_idx_t n_mfc_frame;  // [r0,#0x60]	/**< Number of frames active in mfc_buf */
    frame_idx_t mfc_outidx;   // [r0,#0x62]	/**< Start of active frames in mfc_buf */
    frame_idx_t n_feat_alloc; // [r0,#0x64]	/**< Number of frames allocated in feat_buf */
    frame_idx_t n_feat_frame; // [r0,#0x66]	/**< Number of frames active in feat_buf */
    frame_idx_t feat_outidx;  // [r0,#0x68]	/**< Start of active frames in feat_buf */


	cmn_t *cmn_back;//lfl add

	//词典
	char *cyVoiceE_dic;	// 内容指针
	unsigned int cyVoiceE_dic_len;	//对应的内容大小

	//语言模型  FSG  JSGF  NGRAM
	char *cyVoiceE_gram;	// 内容指针
	unsigned int cyVoiceE_gram_len;	//对应的内容大小

	//声音特征参数
	char *cyVoiceE_feat_params;	// 内容指针
	unsigned int cyVoiceE_feat_params_len;	//对应的内容大小

	//音素表
	char *cyVoiceE_mdef;	// 内容指针
	unsigned int cyVoiceE_mdef_len;	//对应的内容大小

	//
	char *cyVoiceE_means;	// 内容指针
	unsigned int cyVoiceE_means_len;	//对应的内容大小

	// 
	char *cyVoiceE_noisedict;	// 内容指针
	unsigned int cyVoiceE_noisedict_len;	//对应的内容大小

	// 
	char *cyVoiceE_sendump;	// 内容指针
	unsigned int cyVoiceE_sendump_len;	//对应的内容大小

	// 
	char *cyVoiceE_transition_matrices;	// 内容指针
	unsigned int cyVoiceE_transition_matrices_len;	//对应的内容大小

	// 
	char *cyVoiceE_variances;	// 内容指针
	unsigned int cyVoiceE_variances_len;	//对应的内容大小

	//
	char *cyVoiceE_mixture_weights;	// 内容指针
	unsigned int cyVoiceE_mixture_weights_len;	//对应的内容大小
	

	cyVoiceE_modle *p_cyVoiceE_modle_dict;	//词典句柄
	cyVoiceE_modle *p_cyVoiceE_modle_fdict;	//过滤词典句柄

	char *p_data_dict;	//词典数据
	unsigned int dict_count;
	char *p_data_fdict;	//过滤词典
	unsigned int fdict_count;


	/* Chinese Word Segmentation */
	hash_table_t *cyVoiceE_hash_dic;	//词典的哈希表句柄
//	unsigned int cyVoiceE_dic_count;	//词典的单词量
	char *p_hash_buff;		//词典数据存储空间
	unsigned int hash_buff_size;	//词典数据的大小

	char *p_gram_buff;		//语法数据存储空间
	unsigned int gram_buff_size;	//语法数据的大小

	char *p_data_new_dict;	//新建词典数据的缓冲区
	unsigned int new_dict_count;//新建词典的实际大小，非缓冲区大小

	//词的音素链表
	word_phone_t *p_cyVoiceE_word_phone;

};
typedef struct acmod_s acmod_t;

/**
 * Initialize an acoustic model.
 *
 * @param config a command-line object containing parameters.  This
 *               pointer is not retained by this object.
 * @param lmath global log-math parameters.
 * @param fe a previously-initialized acoustic feature module to use,
 *           or NULL to create one automatically.  If this is supplied
 *           and its parameters do not match those in the acoustic
 *           model, this function will fail.  This pointer is not retained.
 * @param fe a previously-initialized dynamic feature module to use,
 *           or NULL to create one automatically.  If this is supplied
 *           and its parameters do not match those in the acoustic
 *           model, this function will fail.  This pointer is not retained.
 * @return a newly initialized acmod_t, or NULL on failure.
 */
acmod_t *acmod_init(cmd_ln_t *config, logmath_t *lmath, fe_t *fe, feat_t *fcb);

/**
 * Adapt acoustic model using a linear transform.
 *
 * @param mllr The new transform to use, or NULL to update the existing
 *              transform.  The decoder retains ownership of this pointer,
 *              so you should not attempt to free it manually.  Use
 *              ps_mllr_retain() if you wish to reuse it
 *              elsewhere.
 * @return The updated transform object for this decoder, or
 *         NULL on failure.
 */
ps_mllr_t *acmod_update_mllr(acmod_t *acmod, ps_mllr_t *mllr);

/**
 * Start logging senone scores to a filehandle.
 *
 * @param acmod Acoustic model object.
 * @param logfh Filehandle to log to.
 * @return 0 for success, <0 on error.
 */
int acmod_set_senfh(acmod_t *acmod, FILE *senfh);

/**
 * Start logging MFCCs to a filehandle.
 *
 * @param acmod Acoustic model object.
 * @param logfh Filehandle to log to.
 * @return 0 for success, <0 on error.
 */
int acmod_set_mfcfh(acmod_t *acmod, FILE *logfh);

/**
 * Start logging raw audio to a filehandle.
 *
 * @param acmod Acoustic model object.
 * @param logfh Filehandle to log to.
 * @return 0 for success, <0 on error.
 */
int acmod_set_rawfh(acmod_t *acmod, FILE *logfh);

/**
 * Finalize an acoustic model.
 */
void acmod_free(acmod_t *acmod);

/**
 * Mark the start of an utterance.
 */
int acmod_start_utt(acmod_t *acmod);

/**
 * Mark the end of an utterance.
 */
int acmod_end_utt(acmod_t *acmod);

/**
 * Rewind the current utterance, allowing it to be rescored.
 *
 * After calling this function, the internal frame index is reset, and
 * acmod_score() will return scores starting at the first frame of the
 * current utterance.  Currently, acmod_set_grow() must have been
 * called to enable growing the feature buffer in order for this to
 * work.  In the future, senone scores may be cached instead.
 *
 * @return 0 for success, <0 for failure (if the utterance can't be
 *         rewound due to no feature or score data available)
 */
int acmod_rewind(acmod_t *acmod);

/**
 * Advance the frame index.
 *
 * This function moves to the next frame of input data.  Subsequent
 * calls to acmod_score() will return scores for that frame, until the
 * next call to acmod_advance().
 *
 * @return New frame index.
 */
int acmod_advance(acmod_t *acmod);

/**
 * Set memory allocation policy for utterance processing.
 *
 * @param grow_feat If non-zero, the internal dynamic feature buffer
 * will expand as necessary to encompass any amount of data fed to the
 * model.
 * @return previous allocation policy.
 */
int acmod_set_grow(acmod_t *acmod, int grow_feat);

/**
 * TODO: Set queue length for utterance processing.
 *
 * This function allows multiple concurrent passes of search to
 * operate on different parts of the utterance.
 */

/**
 * Feed raw audio data to the acoustic model for scoring.
 *
 * @param inout_raw In: Pointer to buffer of raw samples
 *                  Out: Pointer to next sample to be read
 * @param inout_n_samps In: Number of samples available
 *                      Out: Number of samples remaining
 * @param full_utt If non-zero, this block represents a full
 *                 utterance and should be processed as such.
 * @return Number of frames of data processed.
 */
int acmod_process_raw(acmod_t *acmod,
                      int16 const **inout_raw,
                      size_t *inout_n_samps,
                      int full_utt);

/**
 * Feed acoustic feature data into the acoustic model for scoring.
 *
 * @param inout_cep In: Pointer to buffer of features
 *                  Out: Pointer to next frame to be read
 * @param inout_n_frames In: Number of frames available
 *                      Out: Number of frames remaining
 * @param full_utt If non-zero, this block represents a full
 *                 utterance and should be processed as such.
 * @return Number of frames of data processed.
 */
int acmod_process_cep(acmod_t *acmod,
                      mfcc_t ***inout_cep,
                      int *inout_n_frames,
                      int full_utt);

/**
 * Feed dynamic feature data into the acoustic model for scoring.
 *
 * Unlike acmod_process_raw() and acmod_process_cep(), this function
 * accepts a single frame at a time.  This is because there is no need
 * to do buffering when using dynamic features as input.  However, if
 * the dynamic feature buffer is full, this function will fail, so you
 * should either always check the return value, or always pair a call
 * to it with a call to acmod_score().
 *
 * @param feat Pointer to one frame of dynamic features.
 * @return Number of frames processed (either 0 or 1).
 */
int acmod_process_feat(acmod_t *acmod,
                       mfcc_t **feat);

/**
 * Set up a senone score dump file for input.
 *
 * @param insenfh File handle of dump file
 * @return 0 for success, <0 for failure
 */
int acmod_set_insenfh(acmod_t *acmod, FILE *insenfh);

/**
 * Read one frame of scores from senone score dump file.
 *
 * @return Number of frames read or <0 on error.
 */
int acmod_read_scores(acmod_t *acmod);

/**
 * Get a frame of dynamic feature data.
 *
 * @param inout_frame_idx Input: frame index to get, or NULL
 *                        to obtain features for the most recent frame.
 *                        Output: frame index corresponding to this
 *                        set of features.
 * @return Feature array, or NULL if requested frame is not available.
 */
mfcc_t **acmod_get_frame(acmod_t *acmod, int *inout_frame_idx);

/**
 * Score one frame of data.
 *
 * @param inout_frame_idx Input: frame index to score, or NULL
 *                        to obtain scores for the most recent frame.
 *                        Output: frame index corresponding to this
 *                        set of scores.
 * @return Array of senone scores for this frame, or NULL if no frame
 *         is available for scoring (such as if a frame index is
 *         requested that is not yet or no longer available).  The
 *         data pointed to persists only until the next call to
 *         acmod_score() or acmod_advance().
 */
int16 const *acmod_score(acmod_t *acmod,
                         int *inout_frame_idx);

/**
 * Write senone dump file header.
 */
int acmod_write_senfh_header(acmod_t *acmod, FILE *logfh);

/**
 * Write a frame of senone scores to a dump file.
 */
int acmod_write_scores(acmod_t *acmod, int n_active, uint8 const *active,
                       int16 const *senscr, FILE *senfh);


/**
 * Get best score and senone index for current frame.
 */
int acmod_best_score(acmod_t *acmod, int *out_best_senid);

/**
 * Clear set of active senones.
 */
void acmod_clear_active(acmod_t *acmod);

/**
 * Activate senones associated with an HMM.
 */
void acmod_activate_hmm(acmod_t *acmod, hmm_t *hmm);

/**
 * Activate a single senone.
 */
#define acmod_activate_sen(acmod, sen) bitvec_set((acmod)->senone_active_vec, sen)

/**
 * Build active list from 
 */
int32 acmod_flags2list(acmod_t *acmod);


/**
*	free Chinese Word Segmentation Resource
*/
int cyVoiceE_freeChineseWordSegmentationRes(acmod_t *acmod);


int cyVoiceE_releaseDicRes(acmod_t *acmod);
#endif /* __ACMOD_H__ */
