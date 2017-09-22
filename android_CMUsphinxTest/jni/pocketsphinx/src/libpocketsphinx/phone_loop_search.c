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
 * @file phone_loop_search.h Fast and rough context-independent phoneme loop search.
 */

#include <sphinxbase/err.h>

#include "phone_loop_search.h"


#include "emu_sys_log.h"

#undef CMU_SPHINX_TEST_PERFORMANCE


static int phone_loop_search_start(ps_search_t *search);
static int phone_loop_search_step(ps_search_t *search, int frame_idx);
static int phone_loop_search_finish(ps_search_t *search);
static int phone_loop_search_reinit(ps_search_t *search, dict_t *dict, dict2pid_t *d2p);
static void phone_loop_search_free(ps_search_t *search);
static char const *phone_loop_search_hyp(ps_search_t *search, int32 *out_score, int32 *out_is_final);
static int32 phone_loop_search_prob(ps_search_t *search);
static ps_seg_t *phone_loop_search_seg_iter(ps_search_t *search, int32 *out_score);

static ps_searchfuncs_t phone_loop_search_funcs = {
    /* name: */   "phone_loop",
    /* start: */  phone_loop_search_start,
    /* step: */   phone_loop_search_step,
    /* finish: */ phone_loop_search_finish,
    /* reinit: */ phone_loop_search_reinit,
    /* free: */   phone_loop_search_free,
    /* lattice: */  NULL,
    /* hyp: */      phone_loop_search_hyp,
    /* prob: */     phone_loop_search_prob,
    /* seg_iter: */ phone_loop_search_seg_iter,
};

static int
phone_loop_search_reinit(ps_search_t *search, dict_t *dict, dict2pid_t *d2p)
{
    phone_loop_search_t *pls = (phone_loop_search_t *)search;
    cmd_ln_t *config = ps_search_config(search);
    acmod_t *acmod = ps_search_acmod(search);
    int i;

    /* Free old dict2pid, dict, if necessary. */
    ps_search_base_reinit(search, dict, d2p);

    /* Initialize HMM context. */
    if (pls->hmmctx)
        hmm_context_free(pls->hmmctx);
    pls->hmmctx = hmm_context_init(bin_mdef_n_emit_state(acmod->mdef),
                                   acmod->tmat->tp, NULL, acmod->mdef->sseq);
    if (pls->hmmctx == NULL)
        return -1;

    /* Initialize phone HMMs. */
    if (pls->phones) {
        for (i = 0; i < pls->n_phones; ++i)
            hmm_deinit((hmm_t *)&pls->phones[i]);
        ckd_free(pls->phones);
    }
    pls->n_phones = bin_mdef_n_ciphone(acmod->mdef);
    pls->phones = ckd_calloc(pls->n_phones, sizeof(*pls->phones));
    for (i = 0; i < pls->n_phones; ++i) {
        pls->phones[i].ciphone = i;
        hmm_init(pls->hmmctx, (hmm_t *)&pls->phones[i],
                 FALSE,
                 bin_mdef_pid2ssid(acmod->mdef, i),
                 bin_mdef_pid2tmatid(acmod->mdef, i));
    }
    pls->beam = logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-pl_beam"));
    pls->pbeam = logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-pl_pbeam"));
    pls->pip = logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-pip"));
    E_INFO("State beam %d Phone exit beam %d Insertion penalty %d\n",
           pls->beam, pls->pbeam, pls->pip);

    return 0;
}

ps_search_t *
phone_loop_search_init(cmd_ln_t *config,
		       acmod_t *acmod,
		       dict_t *dict)
{
    phone_loop_search_t *pls;

	Iava_SysPrintf("phone_loop_search_init +++++++++++++++++++++++++++++++++");

    /* Allocate and initialize. */
    pls = ckd_calloc(1, sizeof(*pls));
    ps_search_init(ps_search_base(pls), &phone_loop_search_funcs,
                   config, acmod, dict, NULL);
    phone_loop_search_reinit(ps_search_base(pls), ps_search_dict(pls),
                             ps_search_dict2pid(pls));

    return ps_search_base(pls);
}

static void
phone_loop_search_free_renorm(phone_loop_search_t *pls)
{
    gnode_t *gn;
    for (gn = pls->renorm; gn; gn = gnode_next(gn))
        ckd_free(gnode_ptr(gn));
    glist_free(pls->renorm);
    pls->renorm = NULL;
}

static void
phone_loop_search_free(ps_search_t *search)
{
    phone_loop_search_t *pls = (phone_loop_search_t *)search;
    int i;

    ps_search_deinit(search);
    for (i = 0; i < pls->n_phones; ++i)
        hmm_deinit((hmm_t *)&pls->phones[i]);
    phone_loop_search_free_renorm(pls);
    ckd_free(pls->phones);
    hmm_context_free(pls->hmmctx);
    ckd_free(pls);
}

static int
phone_loop_search_start(ps_search_t *search)
{
    phone_loop_search_t *pls = (phone_loop_search_t *)search;
    int i;

    /* Reset and enter all phone HMMs. */
    for (i = 0; i < pls->n_phones; ++i) {
        hmm_t *hmm = (hmm_t *)&pls->phones[i];
        hmm_clear(hmm);
        hmm_enter(hmm, 0, -1, 0);
    }
    phone_loop_search_free_renorm(pls);
    pls->best_score = 0;

    return 0;
}

static void
renormalize_hmms(phone_loop_search_t *pls, int frame_idx, int32 norm)
{
    phone_loop_renorm_t *rn = ckd_calloc(1, sizeof(*rn));
    int i;

    pls->renorm = glist_add_ptr(pls->renorm, rn);
    rn->frame_idx = frame_idx;
    rn->norm = norm;

    for (i = 0; i < pls->n_phones; ++i) {
        hmm_normalize((hmm_t *)&pls->phones[i], norm);
    }
}

static int32
evaluate_hmms(phone_loop_search_t *pls, int16 const *senscr, int frame_idx)
{
    int32 bs = WORST_SCORE;
    int i;
	int16 n_phones;
	phone_loop_t *phones;

    //hmm_context_set_senscore(pls->hmmctx, senscr);
    (pls->hmmctx)->senscore = senscr;
	phones = pls->phones;

	n_phones = pls->n_phones;
    for (i = 0; i < n_phones; ++i) 
	{
        hmm_t *hmm = (hmm_t *)&phones[i];
        int32 score;

        if (hmm->frame < frame_idx)
            continue;
        score = hmm_vit_eval(hmm);
        if (score > bs) 
		{
            bs = score;
        }
    }
    pls->best_score = bs;
    return bs;
}

static void
prune_hmms(phone_loop_search_t *pls, int frame_idx)
{
    int32 thresh = pls->best_score + pls->beam;
    int nf = frame_idx + 1;
    int i;
	phone_loop_t *phones = pls->phones;
	int16 n_phones = pls->n_phones;

    /* Check all phones to see if they remain active in the next frame. */
    for (i = 0; i < n_phones; ++i) 
	{
        hmm_t *hmm = (hmm_t *)&phones[i];

        if (hmm->frame < frame_idx)
            continue;
        /* Retain if score better than threshold. */
        if (hmm_bestscore(hmm) > thresh) 
		{
            hmm->frame = nf;
        }
        else
        {
            //hmm_clear_scores(hmm);

			int32 j;
			uint8 n_emit_state = hmm->n_emit_state;
			int32 *p_score = hmm->score;

		    p_score[0] = WORST_SCORE;
		    for (j = 1; j < n_emit_state; j++)
		        p_score[j] = WORST_SCORE;
		    hmm->out_score = WORST_SCORE;

		    hmm->bestscore = WORST_SCORE;
        }
    }
}

static void
phone_transition(phone_loop_search_t *pls, int frame_idx)
{
    int32 thresh = pls->best_score + pls->pbeam;
    int nf = frame_idx + 1;
    int i;
	int16 n_phones = pls->n_phones;
	register phone_loop_t *phones = pls->phones;

//两级循环，先不处理
    /* Now transition out of phones whose last states are inside the
     * phone transition beam. */
    for (i = 0; i < n_phones; ++i) 
	{
        hmm_t *hmm = (hmm_t *)&phones[i];
        int32 newphone_score;
        int j;

        if (hmm_frame(hmm) != nf)
            continue;

        newphone_score = hmm_out_score(hmm) + pls->pip;
        if (newphone_score BETTER_THAN thresh) 
		{
            /* Transition into all phones using the usual Viterbi rule. */
            for (j = 0; j < n_phones; ++j) 
			{
                hmm_t *nhmm = (hmm_t *)&phones[j];

                if (hmm_frame(nhmm) < frame_idx
                    || newphone_score BETTER_THAN hmm_in_score(nhmm)) 
                {
                    //hmm_enter(nhmm, newphone_score, hmm_out_history(hmm), nf);

					hmm_in_score(nhmm) = newphone_score;
		    		hmm_in_history(nhmm) = hmm_out_history(hmm);
		    		hmm_frame(nhmm) = nf;
                }
            }
        }
    }
}

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
extern unsigned int tatal_time000;

#endif

static int
phone_loop_search_step(ps_search_t *search, int frame_idx)
{
    phone_loop_search_t *pls = (phone_loop_search_t *)search;
    acmod_t *acmod = ps_search_acmod(search);
    int16 const *senscr;
	int16 n_phones;
	phone_loop_t *phones;
    int i;
	
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	unsigned int begin,end;
	begin = Iava_SysGetUS();
#endif

#if 1
//(CMU_SPHINX_TEST_PERFORMANCE == 1)
	//if(tatal_time000 == 0)
		Iava_SysPrintf("ps_search_t		phone_loop_search_step frame_idx = %d!!!",frame_idx);
#endif

    /* All CI senones are active all the time. */
    if (!acmod->compallsen)
    {
#if 0//(CMU_SPHINX_TEST_PERFORMANCE == 1)
		Iava_SysPrintf("phone_loop_search_step pls->n_phones = %d!!!",pls->n_phones);
#endif
		n_phones = pls->n_phones;
		phones = pls->phones;
        for (i = 0; i < n_phones; ++i)
            acmod_activate_hmm(acmod, (hmm_t *)&phones[i]);
    }

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	end = Iava_SysGetUS();
	if(tatal_time000 == 0)
		Iava_SysPrintf("phone_loop_search_step 00000 use = %d us",end-begin);
#endif

    /* Calculate senone scores for current frame. */
    senscr = acmod_score(acmod, &frame_idx);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	begin = Iava_SysGetUS();
	if(tatal_time000 == 0)
		Iava_SysPrintf("phone_loop_search_step 00001 use = %d us",begin - end);
#endif


    /* Renormalize, if necessary. */
    if (pls->best_score + (2 * pls->beam) WORSE_THAN WORST_SCORE) 
	{
	
#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
			Iava_SysPrintf("Renormalizing Scores at frame %d, best score %d\n",frame_idx, pls->best_score);
#endif
        E_INFO("Renormalizing Scores at frame %d, best score %d\n",
               frame_idx, pls->best_score);
        renormalize_hmms(pls, frame_idx, pls->best_score);
    }

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		end = Iava_SysGetUS();
		if(tatal_time000 == 0)
			Iava_SysPrintf("phone_loop_search_step 00002 use = %d us",end-begin);
#endif


    /* Evaluate phone HMMs for current frame.  评估帧   模型 */
    pls->best_score = evaluate_hmms(pls, senscr, frame_idx);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	begin = Iava_SysGetUS();
	if(tatal_time000 == 0)
		Iava_SysPrintf("phone_loop_search_step 00003 use = %d us",begin - end);
#endif

    /* Prune phone HMMs.  消减模型*/
    prune_hmms(pls, frame_idx);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
		end = Iava_SysGetUS();
		if(tatal_time000 == 0)
			Iava_SysPrintf("phone_loop_search_step 00004 use = %d us",end-begin);
#endif

    /* Do phone transitions. */
    phone_transition(pls, frame_idx);

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	begin = Iava_SysGetUS();
	if(tatal_time000 == 0)
		Iava_SysPrintf("phone_loop_search_step 00005 use = %d us",begin - end);
#endif

    return 0;
}

static int
phone_loop_search_finish(ps_search_t *search)
{
    /* Actually nothing to do here really. */
    return 0;
}

static char const *
phone_loop_search_hyp(ps_search_t *search, int32 *out_score, int32 *out_is_final)
{
    E_WARN("Hypotheses are not returned from phone loop search");
    return NULL;
}

static int32
phone_loop_search_prob(ps_search_t *search)
{
    /* FIXME: Actually... they ought to be. */
    E_WARN("Posterior probabilities are not returned from phone loop search");
    return 0;
}

static ps_seg_t *
phone_loop_search_seg_iter(ps_search_t *search, int32 *out_score)
{
    E_WARN("Hypotheses are not returned from phone loop search");
    return NULL;
}
