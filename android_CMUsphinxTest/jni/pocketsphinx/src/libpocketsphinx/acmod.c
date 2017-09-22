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
 * @file acmod.c Acoustic model structures for PocketSphinx.
 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
 */

/* System headers. */
#include <assert.h>
#include <string.h>

/* SphinxBase headers. */
#include <sphinxbase/prim_type.h>
#include <sphinxbase/err.h>
#include <sphinxbase/cmd_ln.h>
#include <sphinxbase/strfuncs.h>
#include <sphinxbase/byteorder.h>
#include <sphinxbase/feat.h>
#include <sphinxbase/bio.h>

/* Local headers. */
#include "cmdln_macro.h"
#include "acmod.h"
#include "s2_semi_mgau.h"
#include "ptm_mgau.h"
#include "ms_mgau.h"


#include "emu_sys_log.h"
#include "cyVoiceE_model.h"

#include "cyVoiceE_dic.h"
#include "cyVoiceE_fsg.h"
#include "cyVoiceE_jsgf.h"
#include "cyVoiceE_ngram.h"
#include "cyVoiceE_feat_params.h"
#include "cyVoiceE_mdef.h"
#include "cyVoiceE_means.h"
#include "cyVoiceE_noisedict.h"
#include "cyVoiceE_sendump.h"
#include "cyVoiceE_transition_matrices.h"
#include "cyVoiceE_variances.h"
/*
extern const unsigned int u32_g_cyVoiceE_feat_params;
extern const unsigned char u8_g_cyVoiceE_feat_params[];

extern const unsigned int u32_g_cyVoiceE_gram;
extern const unsigned char u8_g_cyVoiceE_gram[];

extern const unsigned int u32_g_cyVoiceE_dic;
extern const unsigned char u8_g_cyVoiceE_dic[];

extern const unsigned int u32_g_cyVoiceE_mdef;
extern const unsigned char u8_g_cyVoiceE_mdef[];

extern const unsigned int u32_g_cyVoiceE_means;
extern const unsigned char u8_g_cyVoiceE_means[];

extern const unsigned int u32_g_cyVoiceE_noisedict;
extern const unsigned char u8_g_cyVoiceE_noisedict[];

extern const unsigned int u32_g_cyVoiceE_sendump;
extern const unsigned char u8_g_cyVoiceE_sendump[];

extern const unsigned int u32_g_cyVoiceE_transition_matrices;
extern const unsigned char u8_g_cyVoiceE_transition_matrices[];

extern const unsigned int u32_g_cyVoiceE_variances;
extern const unsigned char u8_g_cyVoiceE_variances[];
*/

#undef CMU_SPHINX_TEST_PERFORMANCE

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
extern unsigned int tatal_time000;
#endif


/* Feature and front-end parameters that may be in feat.params */
static const arg_t feat_defn[] = {
    waveform_to_cepstral_command_line_macro(),
    cepstral_to_feature_command_line_macro(),
    CMDLN_EMPTY_OPTION
};

#ifndef WORDS_BIGENDIAN
#define WORDS_BIGENDIAN 1
#endif

static int32 acmod_process_mfcbuf(acmod_t *acmod);

static void cyVoiceE_model_init(acmod_t *acmod)
{
	char const *isCyVoiceE = cmd_ln_str_r(acmod->config, "-CyVoiceE");
	if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) )
	{
		acmod->cyVoiceE_dic = (char *)u8_g_cyVoiceE_dic;
		acmod->cyVoiceE_dic_len = u32_g_cyVoiceE_dic;

		if(strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0)
		{
			acmod->cyVoiceE_gram= (char *)u8_g_cyVoiceE_jsgf;
			acmod->cyVoiceE_gram_len = u32_g_cyVoiceE_jsgf;
		}
		else if(strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0)
		{
			acmod->cyVoiceE_gram= (char *)u8_g_cyVoiceE_fsg;
			acmod->cyVoiceE_gram_len = u32_g_cyVoiceE_fsg;
		}
		else if(strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0)
		{
			acmod->cyVoiceE_gram= (char *)u8_g_cyVoiceE_ngram;
			acmod->cyVoiceE_gram_len = u32_g_cyVoiceE_ngram;
		}
		

		acmod->cyVoiceE_feat_params= (char *)u8_g_cyVoiceE_feat_params;
		acmod->cyVoiceE_feat_params_len = u32_g_cyVoiceE_feat_params;

		acmod->cyVoiceE_mdef= (char *)u8_g_cyVoiceE_mdef;
		acmod->cyVoiceE_mdef_len = u32_g_cyVoiceE_mdef;

		acmod->cyVoiceE_means= (char *)u8_g_cyVoiceE_means;
		acmod->cyVoiceE_means_len = u32_g_cyVoiceE_means;

		acmod->cyVoiceE_noisedict= (char *)u8_g_cyVoiceE_noisedict;
		acmod->cyVoiceE_noisedict_len = u32_g_cyVoiceE_noisedict;

		acmod->cyVoiceE_sendump= (char *)u8_g_cyVoiceE_sendump;
		acmod->cyVoiceE_sendump_len = u32_g_cyVoiceE_sendump;

		acmod->cyVoiceE_transition_matrices= (char *)u8_g_cyVoiceE_transition_matrices;
		acmod->cyVoiceE_transition_matrices_len = u32_g_cyVoiceE_transition_matrices;

		acmod->cyVoiceE_variances = (char *)u8_g_cyVoiceE_variances;
		acmod->cyVoiceE_variances_len = u32_g_cyVoiceE_variances;

		acmod->cyVoiceE_mixture_weights = (char *)NULL;
		acmod->cyVoiceE_mixture_weights_len = 0;
	}
}

static int
acmod_init_am(acmod_t *acmod)
{
    char const *mdeffn, *tmatfn, *mllrfn, *hmmdir;
	cyVoiceE_modle *p_cyVoiceE_modle_file = NULL;
	char const *isCyVoiceE = cmd_ln_str_r(acmod->config, "-CyVoiceE");
	unsigned char *p_data = NULL;
	unsigned int count = 0;

//lfl 加载声音模型参数

    

	//CyVoiceE CyVoiceE_H sphinx
	if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
	{
		/* Read model definition. */
		if ((mdeffn = cmd_ln_str_r(acmod->config, "-mdef")) == NULL) 
		{
			if ((hmmdir = cmd_ln_str_r(acmod->config, "-hmm")) == NULL) 
			{
				E_ERROR("Acoustic model definition is not specified neither with -mdef option nor with -hmm\n");
			} else {
				E_ERROR("Folder '%s' does not contain acoustic model definition 'mdef'\n", hmmdir);
			}
			return -1;
		}

		
		p_cyVoiceE_modle_file = cyVoiceE_init((char *)mdeffn);
		p_data = cyVoiceE_decrypt(p_cyVoiceE_modle_file,&count);
		acmod->mdef = cyVoiceE_bin_mdef_read(acmod->config, (char *)p_data,count);
		cyVoiceE_uninit(p_cyVoiceE_modle_file);
		if (acmod->mdef == NULL) 
		{
			E_ERROR("Failed to read acoustic model definition from %s\n", mdeffn);
			return -1;
		}
		

	}
	else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) )
	{
		//从内存初始化 lfl
		p_data = (unsigned char *)acmod->cyVoiceE_mdef;
		count = acmod->cyVoiceE_mdef_len;
		p_data = cyVoiceE_decrypt_memory(p_data,&count);

		acmod->mdef = cyVoiceE_bin_mdef_read(acmod->config, (char *)p_data,count);
		cyVoiceE_decrypt_memory_free(p_data);
		if (acmod->mdef == NULL)
		{
			E_ERROR("Failed to read acoustic model definition from \n");
			return -1;
		}
	}


	/* Read transition matrices. */
	if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
	{
		if ((tmatfn = cmd_ln_str_r(acmod->config, "-tmat")) == NULL) {
			E_ERROR("No tmat file specified\n");
			return -1;
		}

		
		E_INFO("Reading HMM transition probability matrices: %s\n",tmatfn);
		
		p_cyVoiceE_modle_file = cyVoiceE_init((char *)tmatfn);
		p_data = cyVoiceE_decrypt(p_cyVoiceE_modle_file,&count);

		//acmod->mdef = cyVoiceE_bin_mdef_read(acmod->config, (char *)p_data,count);
		acmod->tmat = cyVoiceE_tmat_init((char *)p_data,count, acmod->lmath,
                            cmd_ln_float32_r(acmod->config, "-tmatfloor"),
                            TRUE);

		cyVoiceE_uninit(p_cyVoiceE_modle_file);
		if (acmod->tmat == NULL) 
		{
			E_ERROR("Failed to read acoustic model definition from %s\n", tmatfn);
			return -1;
		}
		

	}
	else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) )
	{
		//从内存初始化 lfl
		p_data = (unsigned char *)acmod->cyVoiceE_transition_matrices;
		count = acmod->cyVoiceE_transition_matrices_len;
		p_data = cyVoiceE_decrypt_memory(p_data,&count);


		acmod->tmat = cyVoiceE_tmat_init((char *)p_data,count, acmod->lmath,
                            cmd_ln_float32_r(acmod->config, "-tmatfloor"),
                            TRUE);
		cyVoiceE_decrypt_memory_free(p_data);
		if (acmod->tmat == NULL) 
		{
			E_ERROR("Failed to read acoustic model definition from \n");
			return -1;
		}
	}
		

    
    
	if((strcmp(isCyVoiceE,"CyVoiceE") == 0) || (strcmp(isCyVoiceE,"sphinx") == 0) )
	{
		 /* Read the acoustic models. */
		if ((cmd_ln_str_r(acmod->config, "-mean") == NULL)
			|| (cmd_ln_str_r(acmod->config, "-var") == NULL)
			|| (cmd_ln_str_r(acmod->config, "-tmat") == NULL)) {
			E_ERROR("No mean/var/tmat files specified\n");
			return -1;
		}
	}
   

    if (cmd_ln_str_r(acmod->config, "-senmgau")) {
        E_INFO("Using general multi-stream GMM computation\n");
        acmod->mgau = ms_mgau_init(acmod, acmod->lmath, acmod->mdef);
        if (acmod->mgau == NULL)
            return -1;
    }
    else {
        E_INFO("Attempting to use SCHMM computation module\n");
        if ((acmod->mgau = s2_semi_mgau_init(acmod)) == NULL) {
            E_INFO("Attempting to use PTHMM computation module\n");
            if ((acmod->mgau = ptm_mgau_init(acmod, acmod->mdef)) == NULL) {
                E_INFO("Falling back to general multi-stream GMM computation\n");
                acmod->mgau = ms_mgau_init(acmod, acmod->lmath, acmod->mdef);
                if (acmod->mgau == NULL)
                    return -1;
            }
        }
    }

    /* If there is an MLLR transform, apply it. */
    if ((mllrfn = cmd_ln_str_r(acmod->config, "-mllr"))) 
	{
        ps_mllr_t *mllr = ps_mllr_read(mllrfn);

		Iava_SysPrintf("acmod_init_am +++++++++++++++++++++++++++++++++ mllrfn = %s",mllrfn);

        if (mllr == NULL)
            return -1;
        acmod_update_mllr(acmod, mllr);
    }

    return 0;
}
static void saveAcmodForRunLoop(acmod_t *acmod)
{
	cmn_t *cmn = acmod->fcb->cmn_struct;
	int32 veclen = cmn->veclen;
	int count = veclen *  sizeof(mfcc_t);

	acmod->cmn_back = (cmn_t *) ckd_calloc(1, sizeof(cmn_t));
    acmod->cmn_back->cmn_mean = (mfcc_t *) ckd_calloc(veclen, sizeof(mfcc_t));
    acmod->cmn_back->cmn_var = (mfcc_t *) ckd_calloc(veclen, sizeof(mfcc_t));
    acmod->cmn_back->sum = (mfcc_t *) ckd_calloc(veclen, sizeof(mfcc_t));

	acmod->cmn_back->veclen = cmn->veclen;
	acmod->cmn_back->nframe = cmn->nframe;
	memcpy(acmod->cmn_back->cmn_mean,cmn->cmn_mean,count);
	memcpy(acmod->cmn_back->cmn_var,cmn->cmn_var,count);
	memcpy(acmod->cmn_back->sum,cmn->sum,count);
}

static void freeAcmodForRunLoop(acmod_t *acmod)
{
	cmn_t *cmn_back = acmod->cmn_back;

	if(cmn_back != NULL)
	{
		if(cmn_back->cmn_mean != NULL)
		{
			ckd_free((void *)cmn_back->cmn_mean);
		}
		if(cmn_back->cmn_var != NULL)
		{
			ckd_free((void *)cmn_back->cmn_var);
		}
		if(cmn_back->sum != NULL)
		{
			ckd_free((void *)cmn_back->sum);
		}

		ckd_free((void *)cmn_back);

		cmn_back = NULL;
	}
}


static void loadAcmodForRunLoop(acmod_t *acmod)
{
	cmn_t *cmn = acmod->fcb->cmn_struct;
	cmn_t *cmn_back = acmod->cmn_back;
	int32 veclen = cmn_back->veclen;

	int count = veclen *  sizeof(mfcc_t);

	cmn->veclen = cmn_back->veclen;
	cmn->nframe = cmn_back->nframe;
	memcpy(cmn->cmn_mean,cmn_back->cmn_mean,count);
	memcpy(cmn->cmn_var,cmn_back->cmn_var,count);
	memcpy(cmn->sum,cmn_back->sum,count);
}

static int
acmod_init_feat(acmod_t *acmod)
{
    acmod->fcb = 
        feat_init(cmd_ln_str_r(acmod->config, "-feat"),
                  cmn_type_from_str(cmd_ln_str_r(acmod->config,"-cmn")),
                  cmd_ln_boolean_r(acmod->config, "-varnorm"),
                  agc_type_from_str(cmd_ln_str_r(acmod->config, "-agc")),
                  1, cmd_ln_int32_r(acmod->config, "-ceplen"));
    if (acmod->fcb == NULL)
        return -1;

    if (cmd_ln_str_r(acmod->config, "-lda")) {
        E_INFO("Reading linear feature transformation from %s\n",
               cmd_ln_str_r(acmod->config, "-lda"));
        if (feat_read_lda(acmod->fcb,
                          cmd_ln_str_r(acmod->config, "-lda"),
                          cmd_ln_int32_r(acmod->config, "-ldadim")) < 0)
            return -1;
    }

    if (cmd_ln_str_r(acmod->config, "-svspec")) {
        int32 **subvecs;
        E_INFO("Using subvector specification %s\n", 
               cmd_ln_str_r(acmod->config, "-svspec"));
        if ((subvecs = parse_subvecs(cmd_ln_str_r(acmod->config, "-svspec"))) == NULL)
            return -1;
        if ((feat_set_subvecs(acmod->fcb, subvecs)) < 0)
            return -1;
    }

    if (cmd_ln_exists_r(acmod->config, "-agcthresh")
        && 0 != strcmp(cmd_ln_str_r(acmod->config, "-agc"), "none")) {
        agc_set_threshold(acmod->fcb->agc_struct,
                          cmd_ln_float32_r(acmod->config, "-agcthresh"));
    }

    if (acmod->fcb->cmn_struct
        && cmd_ln_exists_r(acmod->config, "-cmninit")) {
        char *c, *cc, *vallist;
        int32 nvals;

        vallist = ckd_salloc(cmd_ln_str_r(acmod->config, "-cmninit"));
        c = vallist;
        nvals = 0;
        while (nvals < acmod->fcb->cmn_struct->veclen
               && (cc = strchr(c, ',')) != NULL) {
            *cc = '\0';
            acmod->fcb->cmn_struct->cmn_mean[nvals] = FLOAT2MFCC(atof(c));
            c = cc + 1;
            ++nvals;
        }
        if (nvals < acmod->fcb->cmn_struct->veclen && *c != '\0') {
            acmod->fcb->cmn_struct->cmn_mean[nvals] = FLOAT2MFCC(atof(c));
        }
        ckd_free(vallist);
    }

	//lfl add
	saveAcmodForRunLoop(acmod);

    return 0;
}

static int
acmod_init_feat_test(acmod_t *acmod)
{
	//lfl add
	loadAcmodForRunLoop(acmod);
	return 0;

    acmod->fcb = feat_init_test(acmod->fcb,cmd_ln_str_r(acmod->config, "-feat"),
                  cmn_type_from_str(cmd_ln_str_r(acmod->config,"-cmn")),
                  cmd_ln_boolean_r(acmod->config, "-varnorm"),
                  agc_type_from_str(cmd_ln_str_r(acmod->config, "-agc")),
                  1, cmd_ln_int32_r(acmod->config, "-ceplen"));



    /*if (cmd_ln_str_r(acmod->config, "-lda")) {
        E_INFO("Reading linear feature transformation from %s\n",
               cmd_ln_str_r(acmod->config, "-lda"));
        if (feat_read_lda(acmod->fcb,
                          cmd_ln_str_r(acmod->config, "-lda"),
                          cmd_ln_int32_r(acmod->config, "-ldadim")) < 0)
            return -1;
    }*/

    /*if (cmd_ln_str_r(acmod->config, "-svspec")) {
        int32 **subvecs;
        E_INFO("Using subvector specification %s\n", 
               cmd_ln_str_r(acmod->config, "-svspec"));
        if ((subvecs = parse_subvecs(cmd_ln_str_r(acmod->config, "-svspec"))) == NULL)
            return -1;
        if ((feat_set_subvecs(acmod->fcb, subvecs)) < 0)
            return -1;
    }*/

    /*if (cmd_ln_exists_r(acmod->config, "-agcthresh")
        && 0 != strcmp(cmd_ln_str_r(acmod->config, "-agc"), "none")) {
        agc_set_threshold(acmod->fcb->agc_struct,
                          cmd_ln_float32_r(acmod->config, "-agcthresh"));
    }*/


	//记录如下  acmod->fcb->cmn_struct->cmn_mean[nvals] 的内容， veclen = 13
	//可以在 ps_init 里面记录，然后 复制出来，在 ps_utt_start 里面加入
    if (acmod->fcb->cmn_struct && cmd_ln_exists_r(acmod->config, "-cmninit")) {
        char *c, *cc, *vallist;
        int32 nvals;

        vallist = ckd_salloc(cmd_ln_str_r(acmod->config, "-cmninit"));
        c = vallist;
        nvals = 0;
        while (nvals < acmod->fcb->cmn_struct->veclen
               && (cc = strchr(c, ',')) != NULL) {
            *cc = '\0';
            acmod->fcb->cmn_struct->cmn_mean[nvals] = FLOAT2MFCC(atof(c));
            c = cc + 1;
            ++nvals;
        }
        if (nvals < acmod->fcb->cmn_struct->veclen && *c != '\0') {
            acmod->fcb->cmn_struct->cmn_mean[nvals] = FLOAT2MFCC(atof(c));
        }
        ckd_free(vallist);
    }


    return 0;
}

int
acmod_fe_mismatch(acmod_t *acmod, fe_t *fe)
{
    /* Output vector dimension needs to be the same. */
    if (cmd_ln_int32_r(acmod->config, "-ceplen") != fe_get_output_size(fe)) {
	E_ERROR("Configured feature length %d doesn't match feature extraction output size %d\n", 
		cmd_ln_int32_r(acmod->config, "-ceplen"), 
		fe_get_output_size(fe));
        return TRUE;
    }
    /* Feature parameters need to be the same. */
    /* ... */
    return FALSE;
}

int
acmod_feat_mismatch(acmod_t *acmod, feat_t *fcb)
{
    /* Feature type needs to be the same. */
    if (0 != strcmp(cmd_ln_str_r(acmod->config, "-feat"), feat_name(fcb)))
        return TRUE;
    /* Input vector dimension needs to be the same. */
    if (cmd_ln_int32_r(acmod->config, "-ceplen") != feat_cepsize(fcb))
        return TRUE;
    /* FIXME: Need to check LDA and stuff too. */
    return FALSE;
}



acmod_t *
acmod_init(cmd_ln_t *config, logmath_t *lmath, fe_t *fe, feat_t *fcb)
{
    acmod_t *acmod;
    char const *featparams;
	cyVoiceE_modle *p_cyVoiceE_modle_file = NULL;
	char const *isCyVoiceE = cmd_ln_str_r(config, "-CyVoiceE");
	unsigned char *p_data = NULL;
	unsigned int count = 0;

    acmod = ckd_calloc(1, sizeof(*acmod));
    acmod->config = cmd_ln_retain(config);
    acmod->lmath = lmath;
    acmod->state = ACMOD_IDLE;

	//初始化 ROM 模型
	cyVoiceE_model_init(acmod);

	//CyVoiceE CyVoiceE_H sphinx
	if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
	{
		/* Look for feat.params in acoustic model dir. */
		if ((featparams = cmd_ln_str_r(acmod->config, "-featparams"))) 
		{
			
			p_cyVoiceE_modle_file = cyVoiceE_init((char *)featparams);
			p_data = cyVoiceE_decrypt(p_cyVoiceE_modle_file,&count);
			if (cmd_ln_parse_memory_r(acmod->config, feat_defn, (char *)p_data, count,FALSE) != NULL) 
			{
				E_INFO("Parsed model-specific feature parameters from %s\n", featparams);
			}
			cyVoiceE_uninit(p_cyVoiceE_modle_file);
		}
	}
	else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) ) 
	{
		p_data = (unsigned char *)acmod->cyVoiceE_feat_params;
		count = acmod->cyVoiceE_feat_params_len;
		p_data = cyVoiceE_decrypt_memory(p_data,&count);

		//从内存初始化 lfl
		if (cmd_ln_parse_memory_r(acmod->config, feat_defn, (char *)p_data, count,FALSE) != NULL) 
		{
			E_INFO("Parsed model-specific feature parameters from \n");
		}
		cyVoiceE_decrypt_memory_free(p_data);
	}

    /* Initialize feature computation. */
    if (fe) {
        if (acmod_fe_mismatch(acmod, fe))
            goto error_out;
        fe_retain(fe);
        acmod->fe = fe;
    }
    else {

		Iava_SysPrintf("acmod_init +++++++++++++++++++++++++++++++++");
        /* Initialize a new front end. */
        acmod->fe = fe_init_auto_r(config);
        if (acmod->fe == NULL)
            goto error_out;
        if (acmod_fe_mismatch(acmod, acmod->fe))
            goto error_out;

		Iava_SysPrintf("acmod_init +++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
	
    if (fcb) {
		Iava_SysPrintf("acmod_init 00000");
        if (acmod_feat_mismatch(acmod, fcb))
            goto error_out;
        feat_retain(fcb);
        acmod->fcb = fcb;
    }
    else {
		Iava_SysPrintf("acmod_init 00001");
        /* Initialize a new fcb. */
        if (acmod_init_feat(acmod) < 0)
            goto error_out;
    }

	Iava_SysPrintf("acmod_init 00002");
    /* Load acoustic model parameters. */
    if (acmod_init_am(acmod) < 0)
        goto error_out;

	Iava_SysPrintf("acmod_init 00003");
    /* The MFCC buffer needs to be at least as large as the dynamic
     * feature window.  */
    acmod->n_mfc_alloc = acmod->fcb->window_size * 2 + 1;
    acmod->mfc_buf = (mfcc_t **)
        ckd_calloc_2d(acmod->n_mfc_alloc, acmod->fcb->cepsize,
                      sizeof(**acmod->mfc_buf));

    /* Feature buffer has to be at least as large as MFCC buffer. */
    acmod->n_feat_alloc = acmod->n_mfc_alloc + cmd_ln_int32_r(config, "-pl_window");
    acmod->feat_buf = feat_array_alloc(acmod->fcb, acmod->n_feat_alloc);
    acmod->framepos = ckd_calloc(acmod->n_feat_alloc, sizeof(*acmod->framepos));

    /* Senone computation stuff. */
    acmod->senone_scores = ckd_calloc(bin_mdef_n_sen(acmod->mdef),
                                                     sizeof(*acmod->senone_scores));
    acmod->senone_active_vec = bitvec_alloc(bin_mdef_n_sen(acmod->mdef));
    acmod->senone_active = ckd_calloc(bin_mdef_n_sen(acmod->mdef),
                                                     sizeof(*acmod->senone_active));
    acmod->log_zero = logmath_get_zero(acmod->lmath);
    acmod->compallsen = cmd_ln_boolean_r(config, "-compallsen");
    return acmod;

error_out:
    acmod_free(acmod);
    return NULL;
}

acmod_t *
acmod_init_test(acmod_t *acmod,cmd_ln_t *config, logmath_t *lmath, fe_t *fe, feat_t *fcb)
{
    
    char const *featparams;

    //acmod = ckd_calloc(1, sizeof(*acmod));
    //acmod->config = cmd_ln_retain(config);
    //acmod->lmath = lmath;
    //acmod->state = ACMOD_IDLE;

	

    /* Look for feat.params in acoustic model dir. */
    /*if ((featparams = cmd_ln_str_r(acmod->config, "-featparams"))) {
        if (cmd_ln_parse_file_r(acmod->config, feat_defn, featparams, FALSE) != NULL) {
	    E_INFO("Parsed model-specific feature parameters from %s\n", featparams);
        }
    }*/

    /* Initialize feature computation. */
    if (fe) {
        //if (acmod_fe_mismatch(acmod, fe))
        //    goto error_out;
        //fe_retain(fe);
        //acmod->fe = fe;
    }
    else {

		//Iava_SysPrintf("acmod_init +++++++++++++++++++++++++++++++++");
        /* Initialize a new front end. */
        //acmod->fe = fe_init_auto_r(config);
        //if (acmod->fe == NULL)
        //    goto error_out;
        //if (acmod_fe_mismatch(acmod, acmod->fe))
        //    goto error_out;

		//Iava_SysPrintf("acmod_init +++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
	
    if (fcb) {
		/*Iava_SysPrintf("acmod_init 00000");
        if (acmod_feat_mismatch(acmod, fcb))
            goto error_out;
        feat_retain(fcb);
        acmod->fcb = fcb;*/
    }
    else {
		Iava_SysPrintf("acmod_init 00001");
        /* Initialize a new fcb. */
        if (acmod_init_feat_test(acmod) < 0)
            goto error_out;
    }

	//Iava_SysPrintf("acmod_init 00002");
    /* Load acoustic model parameters. */
    //if (acmod_init_am(acmod) < 0)
    //    goto error_out;

	//Iava_SysPrintf("acmod_init 00003");
    /* The MFCC buffer needs to be at least as large as the dynamic
     * feature window.  */
    //acmod->n_mfc_alloc = acmod->fcb->window_size * 2 + 1;
    //acmod->mfc_buf = (mfcc_t **)
    //    ckd_calloc_2d(acmod->n_mfc_alloc, acmod->fcb->cepsize,
    //                  sizeof(**acmod->mfc_buf));

    ///* Feature buffer has to be at least as large as MFCC buffer. */
    //acmod->n_feat_alloc = acmod->n_mfc_alloc + cmd_ln_int32_r(config, "-pl_window");
    //acmod->feat_buf = feat_array_alloc(acmod->fcb, acmod->n_feat_alloc);
    //acmod->framepos = ckd_calloc(acmod->n_feat_alloc, sizeof(*acmod->framepos));

    ///* Senone computation stuff. */
    //acmod->senone_scores = ckd_calloc(bin_mdef_n_sen(acmod->mdef),
    //                                                 sizeof(*acmod->senone_scores));
    //acmod->senone_active_vec = bitvec_alloc(bin_mdef_n_sen(acmod->mdef));
    //acmod->senone_active = ckd_calloc(bin_mdef_n_sen(acmod->mdef),
    //                                                 sizeof(*acmod->senone_active));
    //acmod->log_zero = logmath_get_zero(acmod->lmath);
    //acmod->compallsen = cmd_ln_boolean_r(config, "-compallsen");


    return acmod;

error_out:
    //acmod_free(acmod);
    return NULL;
}

int cyVoiceE_freeChineseWordSegmentationRes(acmod_t *acmod)
{
	cmd_ln_t *config = acmod->config;
	char const *custom_jsgf = cmd_ln_str_r(config, "-Custom_jsgf");
	char const *custom_ngram = cmd_ln_str_r(config, "-Custom_ngram");
	char const *custom_fsg = cmd_ln_str_r(config, "-Custom_fsg");

	if((custom_jsgf != NULL) || (custom_ngram != NULL) || (custom_fsg != NULL))
	{
		//释放哈希表
		if(acmod->cyVoiceE_hash_dic != NULL)
		{
			hash_table_free(acmod->cyVoiceE_hash_dic);
			acmod->cyVoiceE_hash_dic = NULL;
		}

		if(acmod->p_hash_buff != NULL)
		{
			ckd_free(acmod->p_hash_buff);
			acmod->p_hash_buff = NULL;
		}

		acmod->hash_buff_size = 0;

		if(acmod->p_gram_buff != NULL)
		{
			ckd_free(acmod->p_gram_buff);
			acmod->p_gram_buff = NULL;
		}

		acmod->gram_buff_size = 0;
	}



	return 0;
}

void
acmod_free(acmod_t *acmod)
{
    if (acmod == NULL)
        return;

	//lfl add 释放创建词典以及分词使用资源
	cyVoiceE_releaseDicRes(acmod);
	//lfl add 释放分词使用的空间
	cyVoiceE_freeChineseWordSegmentationRes(acmod);
	//lfl add
	freeAcmodForRunLoop(acmod);

    feat_free(acmod->fcb);
    fe_free(acmod->fe);
    cmd_ln_free_r(acmod->config);

    if (acmod->mfc_buf)
        ckd_free_2d((void **)acmod->mfc_buf);
    if (acmod->feat_buf)
        feat_array_free(acmod->feat_buf);

    if (acmod->mfcfh)
        fclose(acmod->mfcfh);
    if (acmod->rawfh)
        fclose(acmod->rawfh);
    if (acmod->senfh)
        fclose(acmod->senfh);

    ckd_free(acmod->framepos);
    ckd_free(acmod->senone_scores);
    ckd_free(acmod->senone_active_vec);
    ckd_free(acmod->senone_active);

    if (acmod->mdef)
        bin_mdef_free(acmod->mdef);
    if (acmod->tmat)
        tmat_free(acmod->tmat);
    if (acmod->mgau)
        ps_mgau_free(acmod->mgau);
    if (acmod->mllr)
        ps_mllr_free(acmod->mllr);
    
    ckd_free(acmod);
}

ps_mllr_t *
acmod_update_mllr(acmod_t *acmod, ps_mllr_t *mllr)
{
    if (acmod->mllr)
        ps_mllr_free(acmod->mllr);
    acmod->mllr = mllr;
    ps_mgau_transform(acmod->mgau, mllr);

    return mllr;
}

int
acmod_write_senfh_header(acmod_t *acmod, FILE *logfh)
{
    char nsenstr[64], logbasestr[64];

    sprintf(nsenstr, "%d", bin_mdef_n_sen(acmod->mdef));
    sprintf(logbasestr, "%f", logmath_get_base(acmod->lmath));
    return bio_writehdr(logfh,
                        "version", "0.1",
                        "mdef_file", cmd_ln_str_r(acmod->config, "-mdef"),
                        "n_sen", nsenstr,
                        "logbase", logbasestr, NULL);
}

int
acmod_set_senfh(acmod_t *acmod, FILE *logfh)
{
    if (acmod->senfh)
        fclose(acmod->senfh);
    acmod->senfh = logfh;
    if (logfh == NULL)
        return 0;
    return acmod_write_senfh_header(acmod, logfh);
}

int
acmod_set_mfcfh(acmod_t *acmod, FILE *logfh)
{
    int rv = 0;

    if (acmod->mfcfh)
        fclose(acmod->mfcfh);
    acmod->mfcfh = logfh;
    fwrite(&rv, 4, 1, acmod->mfcfh);
    return rv;
}

int
acmod_set_rawfh(acmod_t *acmod, FILE *logfh)
{
    if (acmod->rawfh)
        fclose(acmod->rawfh);
    acmod->rawfh = logfh;
    return 0;
}

void
acmod_grow_feat_buf(acmod_t *acmod, int nfr)
{
    if (nfr > MAX_N_FRAMES)
        E_FATAL("Decoder can not process more than %d frames at once, requested %d\n", 
                MAX_N_FRAMES, nfr);

    acmod->feat_buf = feat_array_realloc(acmod->fcb, acmod->feat_buf, acmod->n_feat_alloc, nfr);
    acmod->framepos = ckd_realloc(acmod->framepos,
                                  nfr * sizeof(*acmod->framepos));
    acmod->n_feat_alloc = nfr;
}

int
acmod_set_grow(acmod_t *acmod, int grow_feat)
{
    int tmp = acmod->grow_feat;
    acmod->grow_feat = grow_feat;

    /* Expand feat_buf to a reasonable size to start with. */
    if (grow_feat && acmod->n_feat_alloc < 128)
        acmod_grow_feat_buf(acmod, 128);

    return tmp;
}

int
acmod_start_utt(acmod_t *acmod)
{
	//每次运行修正上一次修改的值，cmn_prior_update
	loadAcmodForRunLoop(acmod);

    fe_start_utt(acmod->fe);
    acmod->state = ACMOD_STARTED;
    acmod->n_mfc_frame = 0;
    acmod->n_feat_frame = 0;
    acmod->mfc_outidx = 0;
    acmod->feat_outidx = 0;
    acmod->output_frame = 0;
    acmod->senscr_frame = -1;
    acmod->n_senone_active = 0;
    acmod->mgau->frame_idx = 0;
    return 0;
}

int
acmod_end_utt(acmod_t *acmod)
{
    int32 nfr = 0;

    acmod->state = ACMOD_ENDED;
    if (acmod->n_mfc_frame < acmod->n_mfc_alloc) {
        int inptr;
        /* Where to start writing them (circular buffer) */
        inptr = (acmod->mfc_outidx + acmod->n_mfc_frame) % acmod->n_mfc_alloc;
        /* nfr is always either zero or one. */
        fe_end_utt(acmod->fe, acmod->mfc_buf[inptr], &nfr);
        acmod->n_mfc_frame += nfr;
        /* Process whatever's left, and any leadout. */
        if (nfr)
            nfr = acmod_process_mfcbuf(acmod);
    }
    if (acmod->mfcfh) {
        int32 outlen, rv;
        outlen = (ftell(acmod->mfcfh) - 4) / 4;
        if (!WORDS_BIGENDIAN)
            SWAP_INT32(&outlen);
        /* Try to seek and write */
        if ((rv = fseek(acmod->mfcfh, 0, SEEK_SET)) == 0) {
            fwrite(&outlen, 4, 1, acmod->mfcfh);
        }
        fclose(acmod->mfcfh);
        acmod->mfcfh = NULL;
    }
    if (acmod->rawfh) {
        fclose(acmod->rawfh);
        acmod->rawfh = NULL;
    }

    if (acmod->senfh) {
        fclose(acmod->senfh);
        acmod->senfh = NULL;
    }

    return nfr;
}

static int
acmod_log_mfc(acmod_t *acmod,
              mfcc_t **cep, int n_frames)
{
    int i, n;
    int32 *ptr = (int32 *)cep[0];

    n = n_frames * feat_cepsize(acmod->fcb);
    /* Swap bytes. */
    if (!WORDS_BIGENDIAN) {
        for (i = 0; i < (n * sizeof(mfcc_t)); ++i) {
            SWAP_INT32(ptr + i);
        }
    }
    /* Write features. */
    if (fwrite(cep[0], sizeof(mfcc_t), n, acmod->mfcfh) != n) {
        E_ERROR_SYSTEM("Failed to write %d values to log file", n);
    }

    /* Swap them back. */
    if (!WORDS_BIGENDIAN) {
        for (i = 0; i < (n * sizeof(mfcc_t)); ++i) {
            SWAP_INT32(ptr + i);
        }
    }
    return 0;
}

static int
acmod_process_full_cep(acmod_t *acmod,
                       mfcc_t ***inout_cep,
                       int *inout_n_frames)
{
    int32 nfr;

    /* Write to log file. */
    if (acmod->mfcfh)
        acmod_log_mfc(acmod, *inout_cep, *inout_n_frames);

    /* Resize feat_buf to fit. */
    if (acmod->n_feat_alloc < *inout_n_frames) {
	    
	if (*inout_n_frames > MAX_N_FRAMES)
	    E_FATAL("Batch processing can not process more than %d frames at once, requested %d\n", 
		    MAX_N_FRAMES, *inout_n_frames);
    
        feat_array_free(acmod->feat_buf);
        acmod->feat_buf = feat_array_alloc(acmod->fcb, *inout_n_frames);
        acmod->n_feat_alloc = *inout_n_frames;
        acmod->n_feat_frame = 0;
        acmod->feat_outidx = 0;
    }
    /* Make dynamic features. */
    nfr = feat_s2mfc2feat_live(acmod->fcb, *inout_cep, inout_n_frames,
                               TRUE, TRUE, acmod->feat_buf);
    acmod->n_feat_frame = nfr;
    assert(acmod->n_feat_frame <= acmod->n_feat_alloc);
    *inout_cep += *inout_n_frames;
    *inout_n_frames = 0;
    return nfr;
}

static int
acmod_process_full_raw(acmod_t *acmod,
                       int16 const **inout_raw,
                       size_t *inout_n_samps)
{
    int32 nfr, ntail;
    mfcc_t **cepptr;

    /* Write to logging file if any. */
    if (acmod->rawfh)
        fwrite(*inout_raw, 2, *inout_n_samps, acmod->rawfh);
    /* Resize mfc_buf to fit. */
    if (fe_process_frames(acmod->fe, NULL, inout_n_samps, NULL, &nfr) < 0)
        return -1;
    if (acmod->n_mfc_alloc < nfr + 1) {
        ckd_free_2d(acmod->mfc_buf);
        acmod->mfc_buf = ckd_calloc_2d(nfr + 1, fe_get_output_size(acmod->fe),
                                       sizeof(**acmod->mfc_buf));
        acmod->n_mfc_alloc = nfr + 1;
    }
    acmod->n_mfc_frame = 0;
    acmod->mfc_outidx = 0;
    fe_start_utt(acmod->fe);
    if (fe_process_frames(acmod->fe, inout_raw, inout_n_samps,
                          acmod->mfc_buf, &nfr) < 0)
        return -1;
    fe_end_utt(acmod->fe, acmod->mfc_buf[nfr], &ntail);
    nfr += ntail;

    cepptr = acmod->mfc_buf;
    nfr = acmod_process_full_cep(acmod, &cepptr, &nfr);
    acmod->n_mfc_frame = 0;
    return nfr;
}

/**
 * Process MFCCs that are in the internal buffer into features.
 */
static int32
acmod_process_mfcbuf(acmod_t *acmod)
{
    mfcc_t **mfcptr;
    int32 ncep;

    ncep = acmod->n_mfc_frame;
    /* Also do this in two parts because of the circular mfc_buf. */
    if (acmod->mfc_outidx + ncep > acmod->n_mfc_alloc) {
        int32 ncep1 = acmod->n_mfc_alloc - acmod->mfc_outidx;
        int saved_state = acmod->state;

        /* Make sure we don't end the utterance here. */
        if (acmod->state == ACMOD_ENDED)
            acmod->state = ACMOD_PROCESSING;
        mfcptr = acmod->mfc_buf + acmod->mfc_outidx;
        ncep1 = acmod_process_cep(acmod, &mfcptr, &ncep1, FALSE);
        /* It's possible that not all available frames were filled. */
        ncep -= ncep1;
        acmod->n_mfc_frame -= ncep1;
        acmod->mfc_outidx += ncep1;
        acmod->mfc_outidx %= acmod->n_mfc_alloc;
        /* Restore original state (could this really be the end) */
        acmod->state = saved_state;
    }
    mfcptr = acmod->mfc_buf + acmod->mfc_outidx;
    ncep = acmod_process_cep(acmod, &mfcptr, &ncep, FALSE);
    acmod->n_mfc_frame -= ncep;
    acmod->mfc_outidx += ncep;
    acmod->mfc_outidx %= acmod->n_mfc_alloc;
    return ncep;
}


#include "emu_sys_log.h"
//#undef CMU_SPHINX_TEST_PERFORMANCE

int
acmod_process_raw(acmod_t *acmod,
		  int16 const **inout_raw,
		  size_t *inout_n_samps,
		  int full_utt)
{
    int32 ncep;
	int ret;

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		unsigned int begin,end;
	
		begin = Iava_SysGetUS();
#endif

    /* If this is a full utterance, process it all at once. */
    if (full_utt)
        return acmod_process_full_raw(acmod, inout_raw, inout_n_samps);

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		end = Iava_SysGetUS();
		Iava_SysPrintf("----acmod_process_raw 000 use %d us    full_utt = %d!!!",end-begin,full_utt);
#endif


    /* Append MFCCs to the end of any that are previously in there
     * (in practice, there will probably be none) */
    if (inout_n_samps && *inout_n_samps) 
	{
        int16 const *prev_audio_inptr = *inout_raw;
        int inptr;

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		unsigned int begin0,end0,count;
	
		begin0 = Iava_SysGetUS();
#endif

        /* Total number of frames available. */
        ncep = acmod->n_mfc_alloc - acmod->n_mfc_frame;
        /* Where to start writing them (circular buffer) */
        inptr = (acmod->mfc_outidx + acmod->n_mfc_frame) % acmod->n_mfc_alloc;

		
#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		end0 = Iava_SysGetUS();
		Iava_SysPrintf("----acmod_process_raw 11111 use %d us!!!",end0-begin0);
		count = 0;
#endif

        /* Write them in two (or more) parts if there is wraparound. */
        while (inptr + ncep > acmod->n_mfc_alloc) 
		{
#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		count++;
#endif

            int32 ncep1 = acmod->n_mfc_alloc - inptr;
            if (fe_process_frames(acmod->fe, inout_raw, inout_n_samps,
                                  acmod->mfc_buf + inptr, &ncep1) < 0)
                return -1;
            /* Write to logging file if any. */
            if (acmod->rawfh) 
			{
                fwrite(prev_audio_inptr, 2,
                       *inout_raw - prev_audio_inptr,
                       acmod->rawfh);
                prev_audio_inptr = *inout_raw;
            }
            /* ncep1 now contains the number of frames actually
             * processed.  This is a good thing, but it means we
             * actually still might have some room left at the end of
             * the buffer, hence the while loop.  Unfortunately it
             * also means that in the case where we are really
             * actually done, we need to get out totally, hence the
             * goto. */
            acmod->n_mfc_frame += ncep1;
            ncep -= ncep1;
            inptr += ncep1;
            inptr %= acmod->n_mfc_alloc;
            if (ncep1 == 0)
                goto alldone;
        }

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		begin0 = Iava_SysGetUS();
		Iava_SysPrintf("----while(%d) acmod_process_raw 22222 use %d us!!!",count,begin0-end0);
#endif

		
        assert(inptr + ncep <= acmod->n_mfc_alloc);
        if (fe_process_frames(acmod->fe, inout_raw, inout_n_samps,
                              acmod->mfc_buf + inptr, &ncep) < 0)
            return -1;
        /* Write to logging file if any. */
        if (acmod->rawfh) 
		{
            fwrite(prev_audio_inptr, 2,
                   *inout_raw - prev_audio_inptr, acmod->rawfh);
            prev_audio_inptr = *inout_raw;
        }
        acmod->n_mfc_frame += ncep;

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		end0 = Iava_SysGetUS();
		Iava_SysPrintf("----inout_n_samps=%d acmod_process_raw 33333 use %d us!!!",*inout_n_samps,end0-begin0);
#endif

    alldone:
        ;
    }

#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	begin = Iava_SysGetUS();
	Iava_SysPrintf("----acmod_process_raw 001 use %d us!!!",begin-end);
#endif


    /* Hand things off to acmod_process_cep. */
	ret = acmod_process_mfcbuf(acmod);

#if 0
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	end = Iava_SysGetUS();
	Iava_SysPrintf("----acmod_process_raw 002 use %d us!!!",end-begin);
#endif


    return ret;
}




int
acmod_process_cep(acmod_t *acmod,
		  mfcc_t ***inout_cep,
		  int *inout_n_frames,
		  int full_utt)
{
    int32 nfeat, ncep, inptr;
    int orig_n_frames;

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	unsigned int begin,end;
		
	begin = Iava_SysGetUS();
#endif

    /* If this is a full utterance, process it all at once. */
    if (full_utt)
        return acmod_process_full_cep(acmod, inout_cep, inout_n_frames);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	end = Iava_SysGetUS();
	Iava_SysPrintf("----acmod_process_cep 000 use %d us    full_utt = %d!!!",end-begin,full_utt);
#endif

    /* Write to log file. */
    if (acmod->mfcfh)
        acmod_log_mfc(acmod, *inout_cep, *inout_n_frames);

    /* Maximum number of frames we're going to generate. */
    orig_n_frames = ncep = nfeat = *inout_n_frames;

    /* FIXME: This behaviour isn't guaranteed... */
    if (acmod->state == ACMOD_ENDED)
        nfeat += feat_window_size(acmod->fcb);
    else if (acmod->state == ACMOD_STARTED)
        nfeat -= feat_window_size(acmod->fcb);

    /* Clamp number of features to fit available space. */
    if (nfeat > acmod->n_feat_alloc - acmod->n_feat_frame) {
        /* Grow it as needed - we have to grow it at the end of an
         * utterance because we can't return a short read there. */
        if (acmod->grow_feat || acmod->state == ACMOD_ENDED)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc + nfeat);
        else
            ncep -= (nfeat - (acmod->n_feat_alloc - acmod->n_feat_frame));
    }

    /* Where to start writing in the feature buffer. */
    if (acmod->grow_feat) {
        /* Grow to avoid wraparound if grow_feat == TRUE. */
        inptr = acmod->feat_outidx + acmod->n_feat_frame;
        while (inptr + nfeat >= acmod->n_feat_alloc)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc * 2);
    }
    else {
        inptr = (acmod->feat_outidx + acmod->n_feat_frame) % acmod->n_feat_alloc;
    }


    /* FIXME: we can't split the last frame drop properly to be on the bounary, so just return */
    if (inptr + nfeat > acmod->n_feat_alloc && acmod->state == ACMOD_ENDED) {
	*inout_n_frames -= ncep;
	*inout_cep += ncep;
	return 0;
    }

    /* Write them in two parts if there is wraparound. */
    if (inptr + nfeat > acmod->n_feat_alloc) {
        int32 ncep1 = acmod->n_feat_alloc - inptr;

        /* Make sure we don't end the utterance here. */
        nfeat = feat_s2mfc2feat_live(acmod->fcb, *inout_cep,
                                     &ncep1,
                                     (acmod->state == ACMOD_STARTED),
                                     FALSE,
                                     acmod->feat_buf + inptr);
        if (nfeat < 0)
            return -1;
        /* Move the output feature pointer forward. */
        acmod->n_feat_frame += nfeat;
        assert(acmod->n_feat_frame <= acmod->n_feat_alloc);
        inptr += nfeat;
        inptr %= acmod->n_feat_alloc;
        /* Move the input feature pointers forward. */
        *inout_n_frames -= ncep1;
        *inout_cep += ncep1;
        ncep -= ncep1;
    }

    nfeat = feat_s2mfc2feat_live(acmod->fcb, *inout_cep,
                                 &ncep,
                                 (acmod->state == ACMOD_STARTED),
                                 (acmod->state == ACMOD_ENDED),
                                 acmod->feat_buf + inptr);
    if (nfeat < 0)
        return -1;
    acmod->n_feat_frame += nfeat;
    assert(acmod->n_feat_frame <= acmod->n_feat_alloc);
    /* Move the input feature pointers forward. */
    *inout_n_frames -= ncep;
    *inout_cep += ncep;
    if (acmod->state == ACMOD_STARTED)
        acmod->state = ACMOD_PROCESSING;
    return orig_n_frames - *inout_n_frames;
}

int
acmod_process_feat(acmod_t *acmod,
		   mfcc_t **feat)
{
    int i, inptr;

    if (acmod->n_feat_frame == acmod->n_feat_alloc) {
        if (acmod->grow_feat)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc * 2);
        else
            return 0;
    }

    if (acmod->grow_feat) {
        /* Grow to avoid wraparound if grow_feat == TRUE. */
        inptr = acmod->feat_outidx + acmod->n_feat_frame;
        while (inptr + 1 >= acmod->n_feat_alloc)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc * 2);
    }
    else {
        inptr = (acmod->feat_outidx + acmod->n_feat_frame) % acmod->n_feat_alloc;
    }
    for (i = 0; i < feat_dimension1(acmod->fcb); ++i)
        memcpy(acmod->feat_buf[inptr][i],
               feat[i], feat_dimension2(acmod->fcb, i) * sizeof(**feat));
    ++acmod->n_feat_frame;
    assert(acmod->n_feat_frame <= acmod->n_feat_alloc);

    return 1;
}

static int
acmod_read_senfh_header(acmod_t *acmod)
{
    char **name, **val;
    int32 swap;
    int i;

    if (bio_readhdr(acmod->insenfh, &name, &val, &swap) < 0)
        goto error_out;
    for (i = 0; name[i] != NULL; ++i) {
        if (!strcmp(name[i], "n_sen")) {
            if (atoi(val[i]) != bin_mdef_n_sen(acmod->mdef)) {
                E_ERROR("Number of senones in senone file (%d) does not match mdef (%d)\n",
                        atoi(val[i]), bin_mdef_n_sen(acmod->mdef));
                goto error_out;
            }
        }
        if (!strcmp(name[i], "logbase")) {
            if (abs(atof(val[i]) - logmath_get_base(acmod->lmath)) > 0.001) {
                E_ERROR("Logbase in senone file (%f) does not match acmod (%f)\n",
                        atof(val[i]), logmath_get_base(acmod->lmath));
                goto error_out;
            }
        }
    }
    acmod->insen_swap = swap;
    bio_hdrarg_free(name, val);
    return 0;
error_out:
    bio_hdrarg_free(name, val);
    return -1;
}

int
acmod_set_insenfh(acmod_t *acmod, FILE *senfh)
{
    acmod->insenfh = senfh;
    if (senfh == NULL) {
        acmod->n_feat_frame = 0;
        acmod->compallsen = cmd_ln_boolean_r(acmod->config, "-compallsen");
        return 0;
    }
    acmod->compallsen = TRUE;
    return acmod_read_senfh_header(acmod);
}

int
acmod_rewind(acmod_t *acmod)
{
    /* If the feature buffer is circular, this is not possible. */
    if (acmod->output_frame > acmod->n_feat_alloc) {
        E_ERROR("Circular feature buffer cannot be rewound (output frame %d, alloc %d)\n",
               acmod->output_frame, acmod->n_feat_alloc);
        return -1;
    }

    /* Frames consumed + frames available */
    acmod->n_feat_frame = acmod->output_frame + acmod->n_feat_frame;

    /* Reset output pointers. */
    acmod->feat_outidx = 0;
    acmod->output_frame = 0;
    acmod->senscr_frame = -1;
    acmod->mgau->frame_idx = 0;

    return 0;
}

int
acmod_advance(acmod_t *acmod)
{
    /* Advance the output pointers. */
    if (++acmod->feat_outidx == acmod->n_feat_alloc)
        acmod->feat_outidx = 0;
    --acmod->n_feat_frame;
    ++acmod->mgau->frame_idx;

    return ++acmod->output_frame;
}

int
acmod_write_scores(acmod_t *acmod, int n_active, uint8 const *active,
                   int16 const *senscr, FILE *senfh)
{
    int16 n_active2;

    /* Uncompressed frame format:
     *
     * (2 bytes) n_active: Number of active senones
     * If all senones active:
     * (n_active * 2 bytes) scores of active senones
     *
     * Otherwise:
     * (2 bytes) n_active: Number of active senones
     * (n_active bytes) deltas to active senones
     * (n_active * 2 bytes) scores of active senones
     */
    n_active2 = n_active;
    if (fwrite(&n_active2, 2, 1, senfh) != 1)
        goto error_out;
    if (n_active == bin_mdef_n_sen(acmod->mdef)) {
        if (fwrite(senscr, 2, n_active, senfh) != n_active)
            goto error_out;
    }
    else {
        int i, n;
        if (fwrite(active, 1, n_active, senfh) != n_active)
            goto error_out;
        for (i = n = 0; i < n_active; ++i) {
            n += active[i];
            if (fwrite(senscr + n, 2, 1, senfh) != 1)
                goto error_out;
        }
    }
    return 0;
error_out:
    E_ERROR_SYSTEM("Failed to write frame to senone file");
    return -1;
}

/**
 * Internal version, used for reading previous frames in acmod_score()
 */
static int
acmod_read_scores_internal(acmod_t *acmod)
{
    FILE *senfh = acmod->insenfh;
    int16 n_active;
    int rv;

    if (acmod->n_feat_frame == acmod->n_feat_alloc) {
        if (acmod->grow_feat)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc * 2);
        else
            return 0;
    }

    if (senfh == NULL)
        return -1;
    if ((rv = fread(&n_active, 2, 1, senfh)) < 0)
        goto error_out;
    else if (rv == 0)
        return 0;

    acmod->n_senone_active = n_active;
    if (acmod->n_senone_active == bin_mdef_n_sen(acmod->mdef)) {
        if ((rv = fread(acmod->senone_scores, 2,
                        acmod->n_senone_active, senfh)) < 0)
            goto error_out;
        else if (rv != acmod->n_senone_active)
            return 0;
    }
    else {
        int i, n;
        if ((rv = fread(acmod->senone_active, 1,
                        acmod->n_senone_active, senfh)) < 0)
            goto error_out;
        else if (rv != acmod->n_senone_active)
            return 0;
        for (i = 0, n = 0; i < acmod->n_senone_active; ++i) {
            int j, sen = n + acmod->senone_active[i];
            for (j = n + 1; j < sen; ++j)
                acmod->senone_scores[j] = SENSCR_DUMMY;
            if ((rv = fread(acmod->senone_scores + sen, 2, 1, senfh)) < 0)
                goto error_out;
            else if (rv == 0)
                return 0;
            n = sen;
        }
        ++n;
        while (n < bin_mdef_n_sen(acmod->mdef))
            acmod->senone_scores[n++] = SENSCR_DUMMY;
    }
    return 1;
error_out:
    E_ERROR_SYSTEM("Failed to read frame from senone file");
    return -1;
}

int
acmod_read_scores(acmod_t *acmod)
{
    int inptr, rv;

    if (acmod->grow_feat) {
        /* Grow to avoid wraparound if grow_feat == TRUE. */
        inptr = acmod->feat_outidx + acmod->n_feat_frame;
        /* Has to be +1, otherwise, next time acmod_advance() is
         * called, this will wrap around. */
        while (inptr + 1 >= acmod->n_feat_alloc)
            acmod_grow_feat_buf(acmod, acmod->n_feat_alloc * 2);
    }
    else {
        inptr = (acmod->feat_outidx + acmod->n_feat_frame) % acmod->n_feat_alloc;
    }

    if ((rv = acmod_read_scores_internal(acmod)) != 1)
        return rv;

    /* Set acmod->senscr_frame appropriately so that these scores
       get reused below in acmod_score(). */
    acmod->senscr_frame = acmod->output_frame + acmod->n_feat_frame;

    E_DEBUG(1,("Frame %d has %d active states\n",
               acmod->senscr_frame, acmod->n_senone_active));

    /* Increment the "feature frame counter" and record the file
     * position for the relevant frame in the (possibly circular)
     * buffer. */
    ++acmod->n_feat_frame;
    acmod->framepos[inptr] = ftell(acmod->insenfh);

    return 1;
}

static int
calc_frame_idx(acmod_t *acmod, int *inout_frame_idx)
{
    int frame_idx;

    /* Calculate the absolute frame index to be scored. */
    if (inout_frame_idx == NULL)
        frame_idx = acmod->output_frame;
    else if (*inout_frame_idx < 0)
        frame_idx = acmod->output_frame + 1 + *inout_frame_idx;
    else
        frame_idx = *inout_frame_idx;

    return frame_idx;
}

static int
calc_feat_idx(acmod_t *acmod, int frame_idx)
{
    int n_backfr, feat_idx;

    n_backfr = acmod->n_feat_alloc - acmod->n_feat_frame;
	
    if (frame_idx < 0 || acmod->output_frame - frame_idx > n_backfr) {
        E_ERROR("Frame %d outside queue of %d frames, %d alloc (%d > %d), cannot score\n",
                frame_idx, acmod->n_feat_frame, acmod->n_feat_alloc,
                acmod->output_frame - frame_idx, n_backfr);
        return -1;
    }

    /* Get the index in feat_buf/framepos of the frame to be scored. */
    feat_idx = ((acmod->feat_outidx + frame_idx - acmod->output_frame)
                % acmod->n_feat_alloc);
    if (feat_idx < 0) feat_idx += acmod->n_feat_alloc;

    return feat_idx;
}

mfcc_t **
acmod_get_frame(acmod_t *acmod, int *inout_frame_idx)
{
    int frame_idx, feat_idx;

    /* Calculate the absolute frame index requested. */
    frame_idx = calc_frame_idx(acmod, inout_frame_idx);

    /* Calculate position of requested frame in circular buffer. */
    if ((feat_idx = calc_feat_idx(acmod, frame_idx)) < 0)
        return NULL;

    if (inout_frame_idx)
        *inout_frame_idx = frame_idx;

    return acmod->feat_buf[feat_idx];
}

int16 const *
acmod_score(acmod_t *acmod, int *inout_frame_idx)
{
    int frame_idx, feat_idx;
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		unsigned int begin,end;
		begin = Iava_SysGetUS();
#endif


    /* Calculate the absolute frame index to be scored. */
    frame_idx = calc_frame_idx(acmod, inout_frame_idx);

    /* If all senones are being computed, or we are using a senone file,
       then we can reuse existing scores. */
    if ((acmod->compallsen || acmod->insenfh)
        && frame_idx == acmod->senscr_frame) 
    {
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		Iava_SysPrintf("----acmod_score acmod->senscr_frame = %d",acmod->senscr_frame);
#endif
        if (inout_frame_idx)
            *inout_frame_idx = frame_idx;
        return acmod->senone_scores;
    }

    /* Calculate position of requested frame in circular buffer. */
    if ((feat_idx = calc_feat_idx(acmod, frame_idx)) < 0)
        return NULL;
	
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	end = Iava_SysGetUS();
	if(tatal_time000 == 0)
			Iava_SysPrintf("acmod_score 00000 use = %d us",end-begin);
	begin = Iava_SysGetUS();
#endif

    /* If there is an input senone file locate the appropriate frame and read it. */
    if (acmod->insenfh) 
	{
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		Iava_SysPrintf("----acmod_score acmod->insenfh = %d",acmod->insenfh);
#endif
        fseek(acmod->insenfh, acmod->framepos[feat_idx], SEEK_SET);
        if (acmod_read_scores_internal(acmod) < 0)
            return NULL;
    }
    else {

        /* Build active senone list. */
        acmod_flags2list(acmod);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		end = Iava_SysGetUS();
		if(tatal_time000 == 0)
				Iava_SysPrintf("acmod_score 00001 use = %d us",end-begin);
		begin = Iava_SysGetUS();
#endif

        /* Generate scores for the next available frame */
        ps_mgau_frame_eval(acmod->mgau,
                           acmod->senone_scores,
                           acmod->senone_active,
                           acmod->n_senone_active,
                           acmod->feat_buf[feat_idx],
                           frame_idx,
                           acmod->compallsen);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		end = Iava_SysGetUS();
		if(tatal_time000 == 0)
			Iava_SysPrintf("acmod_score 00002 use = %d us  feat_idx = %d",end-begin,feat_idx);
		begin = Iava_SysGetUS();
#endif

    }

    if (inout_frame_idx)
        *inout_frame_idx = frame_idx;
    acmod->senscr_frame = frame_idx;

    /* Dump scores to the senone dump file if one exists. */
    if (acmod->senfh) 
	{
	
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		Iava_SysPrintf("----acmod_score acmod->senfh = %d",acmod->senfh);
#endif

        if (acmod_write_scores(acmod, acmod->n_senone_active,
                               acmod->senone_active,
                               acmod->senone_scores,
                               acmod->senfh) < 0)
            return NULL;
        E_DEBUG(1,("Frame %d has %d active states\n", frame_idx, acmod->n_senone_active));
    }

#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	Iava_SysPrintf("----acmod_score acmod->senone_scores = %d",acmod->senone_scores);
#endif
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
			end = Iava_SysGetUS();
			if(tatal_time000 == 0)
				Iava_SysPrintf("acmod_score 00003 use = %d us",end-begin);
#endif

    return acmod->senone_scores;
}

int
acmod_best_score(acmod_t *acmod, int *out_best_senid)
{
    int i, best;

    best = SENSCR_DUMMY;
    if (acmod->compallsen) {
        for (i = 0; i < bin_mdef_n_sen(acmod->mdef); ++i) {
            if (acmod->senone_scores[i] < best) {
                best = acmod->senone_scores[i];
                *out_best_senid = i;
            }
        }
    }
    else {
        int16 *senscr;
        senscr = acmod->senone_scores;
        for (i = 0; i < acmod->n_senone_active; ++i) {
            senscr += acmod->senone_active[i];
            if (*senscr < best) {
                best = *senscr;
                *out_best_senid = i;
            }
        }
    }
    return best;
}


void
acmod_clear_active(acmod_t *acmod)
{
    if (acmod->compallsen)
        return;
    bitvec_clear_all(acmod->senone_active_vec, bin_mdef_n_sen(acmod->mdef));
    acmod->n_senone_active = 0;
}

#define MPX_BITVEC_SET(a,h,i)                                   \
    if (hmm_mpx_ssid(h,i) != BAD_SSID)                          \
        bitvec_set((a)->senone_active_vec, hmm_mpx_senid(h,i))
        
#define NONMPX_BITVEC_SET(a,h,i)                                        \
    bitvec_set((a)->senone_active_vec,                                  \
               hmm_nonmpx_senid(h,i))

/*
#define hmm_mpx_ssid(h,st) (h)->senid[st]

#define hmm_mpx_senid(h,st) (hmm_mpx_ssid(h,st) == BAD_SENID \
                             ? BAD_SENID : (h)->ctx->sseq[hmm_mpx_ssid(h,st)][st])

#define bitvec_set(v,b)		(v[(b)/BITVEC_BITS] |= (1UL << ((b) & (BITVEC_BITS-1))))



bitvec_set((a)->senone_active_vec, hmm->senid[i])

#define hmm_nonmpx_senid(h,st) ((h)->senid[st])
*/

void
acmod_activate_hmm(acmod_t *acmod, hmm_t *hmm)
{

#if(ACMOD_ACTIVATE_HMM_ARM_CODE == 1)
	acmod_activate_hmm_arm_code(acmod,hmm);
#else
    int i,mask = BITVEC_BITS-1;
	uint8 n_emit_state,x;
	uint16 *p_senid;
	uint16 temp,senid;
	bitvec_t *senone_active_vec;
	uint16 **sseq;

//	Iava_SysPrintf("ASM DEBUG = ff");
//	Iava_SysPrintf("DEBUG = %x",acmod->compallsen);
    if (acmod->compallsen)
        return;
//	Iava_SysPrintf("ASM DEBUG = a0");
	sseq = hmm->ctx->sseq;
	n_emit_state = hmm->n_emit_state;
	p_senid = hmm->senid;
	senone_active_vec = acmod->senone_active_vec;
/*
	Iava_SysPrintf("DEBUG = %x",sseq);
	Iava_SysPrintf("DEBUG = %x",n_emit_state);
	Iava_SysPrintf("DEBUG = %x",p_senid);
	Iava_SysPrintf("DEBUG = %x",senone_active_vec);
*/
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		//Iava_SysPrintf("++----acmod_activate_hmm n_emit_state = %d",n_emit_state);
#endif

    if (hmm->mpx) 
	{

		//Iava_SysPrintf("ASM DEBUG = a1");
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	//Iava_SysPrintf("----acmod_activate_hmm use hmm_is_mpx(hmm)!!!");
#endif

		    for (i = (n_emit_state - 1); i >= 0; i--) 
			{
				//Iava_SysPrintf("ASM DEBUG = a2");

				senid = p_senid[i];
				//Iava_SysPrintf("DEBUG = %x",senid);
				if (senid != BAD_SSID)
				{
					//Iava_SysPrintf("ASM DEBUG = a3");
	        		temp = sseq[senid][i];
					//Iava_SysPrintf("DEBUG = %x",temp);
					//Iava_SysPrintf("DEBUG = %x",senone_active_vec[temp >> 5]);
					senone_active_vec[temp >> 5] |= (1UL << (temp & mask));

					//Iava_SysPrintf("DEBUG = %x",senone_active_vec[temp >> 5]);
				}
		    }
/*	
        switch (n_emit_state) {
        case 5:

			x = 4;
			senid = p_senid[x];
			if (senid != BAD_SSID)
			{
				temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][x]);
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			}


			x = 3;
			senid = p_senid[x];
			if (senid != BAD_SSID)
			{
				temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][x]);
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			}

			
            //MPX_BITVEC_SET(acmod, hmm, 4);
            //MPX_BITVEC_SET(acmod, hmm, 3);
        case 3:

			x = 2;
			senid = p_senid[x];
			if (senid != BAD_SSID)
			{
				temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][x]);
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			}

			x = 1;
			senid = p_senid[x];
			if (senid != BAD_SSID)
			{
        		temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][x]);
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			}

			x = 0;
			senid = p_senid[x];
			if (senid != BAD_SSID)
			{
        		temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][x]);
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			}

			
            //MPX_BITVEC_SET(acmod, hmm, 2);
            //MPX_BITVEC_SET(acmod, hmm, 1);
            //MPX_BITVEC_SET(acmod, hmm, 0);
            break;
        default:

            for (i = 0; i < n_emit_state; ++i) 
			{

				senid = p_senid[i];
				if (senid != BAD_SSID)
				{
	        		temp = (senid == BAD_SENID ? BAD_SENID : hmm->ctx->sseq[senid][i]);
					senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
				}

				
                //MPX_BITVEC_SET(acmod, hmm, i);
            }
        }
*/
    }
    else 
	{

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
			//Iava_SysPrintf("----acmod_activate_hmm 00001!!!");
#endif
			//Iava_SysPrintf("ASM DEBUG = aa");

		   for (i = (n_emit_state - 1); i >= 0; i--) 
		   {
		   		//Iava_SysPrintf("ASM DEBUG = ab");
			   temp = p_senid[i];
			   //Iava_SysPrintf("DEBUG = %x",temp);
			   //Iava_SysPrintf("DEBUG = %x",senone_active_vec[temp >> 5]);
					
			   senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			   //Iava_SysPrintf("DEBUG = %x",senone_active_vec[temp >> 5]);
			   //NONMPX_BITVEC_SET(acmod, hmm, i);
		   }

/*
        switch (n_emit_state) 
		{
        case 5:
			temp = p_senid[4];
			senone_active_vec[temp >> 5] |= (1UL << (temp & mask));

			temp = p_senid[3];
			senone_active_vec[temp >> 5] |= (1UL << (temp & mask));

			
            //NONMPX_BITVEC_SET(acmod, hmm, 4);
            //NONMPX_BITVEC_SET(acmod, hmm, 3);
        case 3:

			temp = p_senid[2];
			senone_active_vec[temp >> 5] |= (1UL << (temp & mask));

			temp = p_senid[1];
			senone_active_vec[temp >> 5] |= (1UL << (temp & mask));

			temp = p_senid[0];
			senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
			
            //NONMPX_BITVEC_SET(acmod, hmm, 2);
            //NONMPX_BITVEC_SET(acmod, hmm, 1);
            //NONMPX_BITVEC_SET(acmod, hmm, 0);
            break;
        default:
            for (i = 0; i < n_emit_state; ++i) 
			{
				temp = p_senid[i];
				senone_active_vec[temp >> 5] |= (1UL << (temp & mask));
                //NONMPX_BITVEC_SET(acmod, hmm, i);
            }
        }
*/
    }

#endif
}

int32
acmod_flags2list(acmod_t *acmod)
{
#if 1
	int32 w, l, n, b, total_dists, total_words, extra_bits;
    bitvec_t *flagptr;
	register uint8 *senone_active;
	
    total_dists = bin_mdef_n_sen(acmod->mdef);
    if (acmod->compallsen) {
        acmod->n_senone_active = total_dists;
        return total_dists;
    }

    //total_words = total_dists / BITVEC_BITS;
    total_words = total_dists >> 5;
    //extra_bits = total_dists % BITVEC_BITS;
    extra_bits = total_dists & 0x1f;
	senone_active = acmod->senone_active;
		
    w = n = l = 0;
    for (flagptr = acmod->senone_active_vec; w < total_words; ++w, ++flagptr) 
	{
        if (*flagptr == 0)
            continue;
        for (b = 0; b < BITVEC_BITS; ++b) 
		{
            if (*flagptr & (1UL << b)) 
			{
                //int32 sen = w * BITVEC_BITS + b;
                int32 sen = (w << 5) + b;
                int32 delta = sen - l;
                /* Handle excessive deltas "lossily" by adding a few
                   extra senones to bridge the gap. */
                while (delta > 255) 
				{
                    senone_active[n++] = 255;
                    delta -= 255;
                }
                senone_active[n++] = delta;
                l = sen;
            }
        }
    }

    for (b = 0; b < extra_bits; ++b) 
	{
        if (*flagptr & (1UL << b)) 
		{
            //int32 sen = w * BITVEC_BITS + b;
			int32 sen = (w << 5) + b;
            int32 delta = sen - l;
            /* Handle excessive deltas "lossily" by adding a few
               extra senones to bridge the gap. */
            while (delta > 255) {
                senone_active[n++] = 255;
                delta -= 255;
            }
            senone_active[n++] = delta;
            l = sen;
        }
    }

    acmod->n_senone_active = n;
    E_DEBUG(1, ("acmod_flags2list: %d active in frame %d\n",
                acmod->n_senone_active, acmod->output_frame));
    return n;

#else
    int32 w, l, n, b, total_dists, total_words, extra_bits;
    bitvec_t *flagptr;

    total_dists = bin_mdef_n_sen(acmod->mdef);
    if (acmod->compallsen) {
        acmod->n_senone_active = total_dists;
        return total_dists;
    }
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
//	Iava_SysPrintf("acmod_flags2list total_dists = %d",total_dists);
#endif
    total_words = total_dists / BITVEC_BITS;
    extra_bits = total_dists % BITVEC_BITS;
    w = n = l = 0;
    for (flagptr = acmod->senone_active_vec; w < total_words; ++w, ++flagptr) {
        if (*flagptr == 0)
            continue;
        for (b = 0; b < BITVEC_BITS; ++b) {
            if (*flagptr & (1UL << b)) {
                int32 sen = w * BITVEC_BITS + b;
                int32 delta = sen - l;
                /* Handle excessive deltas "lossily" by adding a few
                   extra senones to bridge the gap. */
                while (delta > 255) {
                    acmod->senone_active[n++] = 255;
                    delta -= 255;
                }
                acmod->senone_active[n++] = delta;
                l = sen;
            }
        }
    }

    for (b = 0; b < extra_bits; ++b) {
        if (*flagptr & (1UL << b)) {
            int32 sen = w * BITVEC_BITS + b;
            int32 delta = sen - l;
            /* Handle excessive deltas "lossily" by adding a few
               extra senones to bridge the gap. */
            while (delta > 255) {
                acmod->senone_active[n++] = 255;
                delta -= 255;
            }
            acmod->senone_active[n++] = delta;
            l = sen;
        }
    }

    acmod->n_senone_active = n;
    E_DEBUG(1, ("acmod_flags2list: %d active in frame %d\n",
                acmod->n_senone_active, acmod->output_frame));
    return n;

#endif
}


