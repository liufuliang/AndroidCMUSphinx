/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
/* ====================================================================
 * Copyright (c) 1999-2004 Carnegie Mellon University.  All rights
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

/*
 * fsg_search.c -- Search structures for FSM decoding.
 * 
 * **********************************************
 * CMU ARPA Speech Project
 *
 * Copyright (c) 2004 Carnegie Mellon University.
 * ALL RIGHTS RESERVED.
 * **********************************************
 * 
 * HISTORY
 *
 * 18-Feb-2004	M K Ravishankar (rkm@cs.cmu.edu) at Carnegie Mellon
 * 		Started.
 */

/* System headers. */
#include <stdio.h>
#include <string.h>
#include <assert.h>

/* SphinxBase headers. */
#include <sphinxbase/err.h>
#include <sphinxbase/ckd_alloc.h>
#include <sphinxbase/strfuncs.h>
#include <sphinxbase/cmd_ln.h>

/* Local headers. */
#include "pocketsphinx_internal.h"
#include "ps_lattice_internal.h"
#include "fsg_search_internal.h"
#include "fsg_history.h"
#include "fsg_lextree.h"

#include "emu_sys_log.h"

#include "cyVoiceE_model.h"

//#undef CMU_SPHINX_TEST_PERFORMANCE



/* Turn this on for detailed debugging dump */
#define __FSG_DBG__		0
#define __FSG_DBG_CHAN__	0

static ps_seg_t *fsg_search_seg_iter(ps_search_t *search, int32 *out_score);
static ps_lattice_t *fsg_search_lattice(ps_search_t *search);
static int fsg_search_prob(ps_search_t *search);

static ps_searchfuncs_t fsg_funcs = {
    /* name: */   "fsg",
    /* start: */  fsg_search_start,
    /* step: */   fsg_search_step,
    /* finish: */ fsg_search_finish,
    /* reinit: */ fsg_search_reinit,
    /* free: */   fsg_search_free,
    /* lattice: */  fsg_search_lattice,
    /* hyp: */      fsg_search_hyp,
    /* prob: */     fsg_search_prob,
    /* seg_iter: */ fsg_search_seg_iter,
};

#define CYVOICEE_BUFF_SIZE	(1024)
static int cyVoiceE_find_word_from_left_to_right(hash_table_t *cyVoiceE_hash_dic,unsigned char *p_in,unsigned char *p_out,unsigned int count)
{
	int len = strlen((char *)p_in);
	int ret,i,word_count = 0;
	unsigned char line_1[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	char result[CYVOICEE_BUFF_SIZE] = {0};
	char *value = NULL;
	char *p_key = NULL;
	int index = 0;

	//初始化
	memset(p_out,0,count);

	memcpy(line,p_in,len);
	memcpy(line_1,line,len);
	while(1)
	{
		len = strlen((char *)line_1);
		if(len == 0)
			break;
		//分词

		//查找匹配的词 由大到小
		for(i = len;i > 0 ;i--)
		{
			line_1[i] = 0;
			p_key = (char *)&(line_1[0]);
			ret = hash_table_lookup(cyVoiceE_hash_dic,p_key,(void **)&value);
			if(ret == 0)
			{
				word_count++;
				break;

			}
		}

		if(i == 0)
		{
			Iava_SysPrintf("\n cyVoiceE_find_word_from_left_to_right error = %s",p_in);
			return -1;
		}
		else
		{


			memcpy(&(result[index]),p_key,i);
			index = index + i;
			result[index++] = 0x20;//0x0a
		}

		memset(line_1,0,sizeof(line_1));
		memcpy(line_1,&line[i],strlen((char *)(&line[i])));
		memset(line,0,sizeof(line));
		memcpy(line,line_1,sizeof(line_1));
		
	}

	//分词结果
	ret = strlen(result) - 1;
	result[ret] = 0;

	memcpy(p_out,result,ret);

	return word_count;
}

static int cyVoiceE_find_word_from_right_to_left(hash_table_t *cyVoiceE_hash_dic,unsigned char *p_in,unsigned char *p_out,unsigned int count)
{
	int len = strlen((char *)p_in);
	int ret,i,word_count = 0;
	unsigned char line_1[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	char result[CYVOICEE_BUFF_SIZE] = {0};
	char *value = NULL;
	char *p_key = NULL;
	int index = sizeof(result) - 1;

	//初始化
	memset(p_out,0,count);

	memcpy(line,p_in,len);
	memcpy(line_1,line,len);
	while(1)
	{
		len = strlen((char *)line_1);
		if(len == 0)
			break;
		//分词

		//查找匹配的词 由大到小
		for(i = 0;i < len;i++)
		{
			p_key = (char *)&(line_1[i]);
			ret = hash_table_lookup(cyVoiceE_hash_dic,p_key,(void **)&value);
			if(ret == 0)
			{
				word_count++;
				break;

			}
		}

		if(i >= len)
		{
			Iava_SysPrintf("\n cyVoiceE_find_word_from_right_to_left error = %s",p_in);
			return -1;
		}
		else
		{
			int x = len - i;
			index = index - x - 1;
			result[index] = 0x20;//0x0a

			memcpy(&(result[index + 1]),p_key,x);
		}

		memcpy(line_1,line,len);
		line_1[i] = 0;
		
	}

	//分词结果
	ret = sizeof(result) - 1 - index -1;
	memcpy(p_out,&(result[index + 1]),ret);

	return word_count;
}

static int cyVoiceE_find_word_two_way(hash_table_t *cyVoiceE_hash_dic,unsigned char *p_in,unsigned char *p_out,unsigned int count)
{
	int count_l,count_r,ret;
	unsigned char result_l[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char result_r[CYVOICEE_BUFF_SIZE] = {0};

	

	count_l = cyVoiceE_find_word_from_left_to_right(cyVoiceE_hash_dic,p_in,result_l,count);
	count_r = cyVoiceE_find_word_from_right_to_left(cyVoiceE_hash_dic,p_in,result_r,count);

	if((count_l <= 0) && (count_r <= 0))
	{
		Iava_SysPrintf("\n cyVoiceE_find_word_two_way error!!!  count_l = %d  count_r = %d",count_l,count_r);
		return -1;
	}

	//解决双向的冲突问题 ---- 如果分词数一样，有限使用逆向最大匹配分词
	if(count_l >= count_r)
	{
		memcpy(p_out,result_r,count);
		ret = count_r;
	}
	else
	{
		memcpy(p_out,result_l,count);
		ret = count_l;
	}



	return ret;

}

static int cyVoiceE_ChineseWordSegmentation(hash_table_t *cyVoiceE_hash_dic,
											unsigned char *p_in,unsigned int in_count,
											unsigned char *p_out,unsigned int *out_count)
{
	int i = 0,j = 0,len = 0,x= 0;
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line1[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char result1[CYVOICEE_BUFF_SIZE] = {0};
	char *p_start = NULL,*p_end = NULL;
	char *p_out_temp = (char *)p_out;
	int ret;
	int flag = 0;

	
	while(1)
	{
		//读取一行的内容，对其内容做分词
		memset(line,0,sizeof(line));
		memset(line1,0,sizeof(line1));
		x= 0;
		while ((unsigned char)(p_in[i]) != 0x0a)
		{
			line[x++] = p_in[i++];

			if(i >= in_count)
				break;
		}
		if(i >= in_count)
			break;
		
		
		p_start = strstr((char *)line,"(");
		p_end = strstr((char *)line,")");
		if((p_start != NULL) && (p_end != NULL))
		{
			p_start++;

			len = p_end- p_start;
			x = 0;
			for(j = 0;j < len;j++)
			{
				if(isspace((unsigned char)(p_start[j])) == NULL)
					line1[x++] = p_start[j];
			}

		}
		else if((p_start == NULL) && (p_end == NULL))
		{
			memcpy(line1,line,x);
		}
		else
		{
			break;
		}


		
		//双向搜索
		//memset(result0,0,sizeof(result0));
		//cyVoiceE_find_word_from_left_to_right(cyVoiceE_hash_dic,line,result0,sizeof(result0));

		memset(result1,0,sizeof(result1));
		//cyVoiceE_find_word_from_right_to_left(cyVoiceE_hash_dic,line1,result1,sizeof(result1));
		ret = cyVoiceE_find_word_two_way(cyVoiceE_hash_dic,line1,result1,sizeof(result1));
		if(ret <= 0)
		{
			Iava_SysPrintf("\n cyVoiceE_ChineseWordSegmentation return = %d",ret);
			return -1;
		}

		if(flag == 0)
		{
			flag = 1;
		}
		else if(flag == 1)
		{
			strcat(p_out_temp," | ");
		}
		strcat(p_out_temp,"(");
		strcat(p_out_temp,(char *)result1);
		if(p_end == NULL)
		{
			strcat(p_out_temp,")");
		}
		else
		{
			strcat(p_out_temp,p_end);
		}

		//p_out_temp = p_out_temp + strlen(p_out_temp);

		while ((unsigned char)(p_in[i]) == 0x0a)
		{
			i++;
		}

	}


	*out_count = strlen((char *)p_out);
	return 0;
}



static int cyVoiceE_create_jsgf(hash_table_t *cyVoiceE_hash_dic,
											unsigned char *p_in,unsigned int in_count,
											unsigned char *p_out,unsigned int *out_count)

{
	//
	int i = 0,j = 0,len = 0,x= 0;
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line1[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char result1[CYVOICEE_BUFF_SIZE] = {0};
	char *p_start = NULL,*p_end = NULL;
	char *p_out_temp = (char *)p_out;
	int ret;
	int flag = 0;

	
	while(1)
	{
		//读取一行的内容，对其内容做分词
		memset(line,0,sizeof(line));
		memset(line1,0,sizeof(line1));
		x= 0;
		while ((unsigned char)(p_in[i]) != 0x0a)
		{
			line[x++] = p_in[i++];

			if(i >= in_count)
				break;
		}
		if(i >= in_count)
			break;
		
		
		p_start = strstr((char *)line,"(");
		p_end = strstr((char *)line,")");
		len = p_end - p_start -1;
		if((p_start != NULL) && (p_end != NULL))
		{
			p_start = p_start + 1;
			memcpy(line1,p_start,len);

		}
		else if((p_start == NULL) && (p_end == NULL))
		{
			memcpy(line1,line,x);
		}
		else
		{
			break;
		}



		if(flag == 0)
		{
			flag = 1;
		}
		else if(flag == 1)
		{
			strcat(p_out_temp," | ");
		}
		strcat(p_out_temp,"(");
		strcat(p_out_temp,(char *)line1);
		if(p_end == NULL)
		{
			strcat(p_out_temp,")");
		}
		else
		{
			strcat(p_out_temp,p_end);
		}

		//p_out_temp = p_out_temp + strlen(p_out_temp);

		while ((unsigned char)(p_in[i]) == 0x0a)
		{
			i++;
		}

	}


	*out_count = strlen((char *)p_out);
	return 0;

	return 0;
}
#ifdef WIN32

void cyVoiceE_saveChineseWordSegmentation(char *fileName,unsigned char *p_in,unsigned intlen)
{
	char *p_start,*p_end;
	char *p_temp = (char *)p_in;
	int count = 0;

	FILE *fh_log = fopen(fileName, "wb");
	if(fh_log == NULL)
		return;
	
	while(1)
	{
		p_start = strstr(p_temp,"(");
		p_end = strstr(p_temp,")");

		if((p_start != NULL) && (p_end != NULL))
		{
			p_start++;
			count = p_end - p_start;
			fwrite("<s> ", strlen("<s> "), 1, fh_log);
			fwrite(p_start, count, 1, fh_log);
			fwrite(" </s>", strlen(" </s>"), 1, fh_log);
			fwrite("\n", strlen("\n"), 1, fh_log);

			p_temp = p_end + 1;
		}
		else
		{
			break;
		}
	}


	if(fh_log)
		fclose(fh_log);
	
}


void cyVoiceE_saveBuffToFile(char *fileName,unsigned char *p_in,unsigned int len)
{
	char *p_start,*p_end;
	char *p_temp = (char *)p_in;
	int count = 0;

	FILE *fh_log = fopen(fileName, "wb");
	if(fh_log == NULL)
		return;
	
	fwrite(p_in, len, 1, fh_log);
	

	if(fh_log)
		fclose(fh_log);
	
}
#endif

static jsgf_t *cyVoiceE_jsgf_parse_memory(cmd_ln_t *config,acmod_t *acmod,unsigned char *jsgf_buff,unsigned int count, jsgf_t *parent)
{
	unsigned char *custom_jsgf = (unsigned char *)cmd_ln_str_r(config, "-Custom_jsgf");
	int custom_flag = (int)cmd_ln_int_r(config, "-Custom_flag");
	int len = 0,jsgf_len,x;
	unsigned char *p_custom_jsgf_buff;
	int custom_jsgf_buff_real_len = 0;
	char *custom_rule_start_flag = "<CyvoiceCustom> =";
	int custom_rule_start_flag_len;
	char *custom_rule_end_flag = ";";
	unsigned int max = 0;
	char *p_temp = NULL;
	int ret = 0;
	
	//Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00000");

	if(custom_jsgf != NULL)
	{
		//0 = default 1 = CyVoiceE_Chinese Word Segmentation  2 = CyVoiceE_create dictionary
		if((custom_flag == 1) || (custom_flag == 0) || (custom_flag == 2))
		{
			int space_size = 0;

			//Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00001");
			//添加用户定义语法
			len = strlen((char *)custom_jsgf);

			space_size = len / 3 + 1;
			max = len + count + space_size;

			acmod->p_gram_buff = (char *)ckd_malloc(max);
			memset(acmod->p_gram_buff,0,max);

			custom_jsgf_buff_real_len = len + space_size;
			p_custom_jsgf_buff = (char *)ckd_malloc(custom_jsgf_buff_real_len);
			memset(p_custom_jsgf_buff,0,(custom_jsgf_buff_real_len));
			// custom_jsgf = "打开车门\n关闭车门\n打开电灯\n关闭电灯\n";
			//机械分词--双向最大
			if((custom_flag == 1) || (custom_flag == 2))
			{
				ret = cyVoiceE_ChineseWordSegmentation(acmod->cyVoiceE_hash_dic,custom_jsgf,len,p_custom_jsgf_buff,&custom_jsgf_buff_real_len);
			}
			else if(custom_flag == 0)
			{
				ret = cyVoiceE_create_jsgf(acmod->cyVoiceE_hash_dic,custom_jsgf,len,p_custom_jsgf_buff,&custom_jsgf_buff_real_len);
			}
			if(ret < 0)
			{
				Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00002");
				if(acmod->p_gram_buff != NULL)
				{
					ckd_free(acmod->p_gram_buff);
					acmod->p_gram_buff = NULL;
				}
				if(p_custom_jsgf_buff != NULL)
				{
					ckd_free(p_custom_jsgf_buff);
					p_custom_jsgf_buff = NULL;
				}

				 goto error;
			}

	#ifdef WIN32
			//cyVoiceE_saveChineseWordSegmentation(".//lm//jsgf_100_command.txt",p_custom_jsgf_buff,custom_jsgf_buff_real_len);
			//cyVoiceE_saveChineseWordSegmentation(".//lm//jsgf_200.txt",p_custom_jsgf_buff,custom_jsgf_buff_real_len);
			//cyVoiceE_saveChineseWordSegmentation(".//lm//jsgf_500.txt",p_custom_jsgf_buff,custom_jsgf_buff_real_len);
	#endif

			p_temp = strstr((char *)jsgf_buff,custom_rule_start_flag);
			if(p_temp == NULL)
			{
				Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00003");
				if(acmod->p_gram_buff != NULL)
				{
					ckd_free(acmod->p_gram_buff);
					acmod->p_gram_buff = NULL;
				}
				if(p_custom_jsgf_buff != NULL)
				{
					ckd_free(p_custom_jsgf_buff);
					p_custom_jsgf_buff = NULL;
				}

				 goto error;
			}

			jsgf_len = p_temp - (char *)jsgf_buff;
			memcpy(acmod->p_gram_buff,jsgf_buff,jsgf_len);

			p_temp = acmod->p_gram_buff + jsgf_len;
			custom_rule_start_flag_len = strlen(custom_rule_start_flag);
			memcpy(p_temp,custom_rule_start_flag,custom_rule_start_flag_len);

			p_temp = p_temp + custom_rule_start_flag_len;
			memcpy(p_temp,p_custom_jsgf_buff,custom_jsgf_buff_real_len);

			p_temp = p_temp + custom_jsgf_buff_real_len;
			x = strlen(custom_rule_end_flag);
			memcpy(p_temp,custom_rule_end_flag,x);

			p_temp[x] = 0xa;
			p_temp[x+1] = 0xff;

			if(p_custom_jsgf_buff != NULL)
				ckd_free(p_custom_jsgf_buff);

			if((jsgf_len + custom_rule_start_flag_len + custom_jsgf_buff_real_len + x + 2) > max)
			{
				Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00004");
				if(acmod->p_gram_buff != NULL)
					ckd_free(acmod->p_gram_buff);			

				 goto error;
			}

			//Iava_SysPrintf("cyVoiceE_jsgf_parse_memory 00005");
			acmod->gram_buff_size = jsgf_len + custom_rule_start_flag_len + custom_jsgf_buff_real_len + x +2;

#ifdef WIN32
			cyVoiceE_saveBuffToFile(".//lm//command_100.jsgf",acmod->p_gram_buff,acmod->gram_buff_size);
#endif

			goto end;
		}
		
	}
	/*
	else
	{
		acmod->p_gram_buff = (char *)jsgf_buff;
		acmod->gram_buff_size = count;
	}
	*/

error:
	//Iava_SysPrintf("cyVoiceE_jsgf_parse_memory  error");
	return NULL;
	acmod->p_gram_buff = (char *)jsgf_buff;
	acmod->gram_buff_size = count;
	
	
end:
	//Iava_SysPrintf("cyVoiceE_jsgf_parse_memory  end");
	return jsgf_parse_memory(acmod->p_gram_buff,acmod->gram_buff_size,parent);
}
ps_search_t *
fsg_search_init(cmd_ln_t *config,
                acmod_t *acmod,
                dict_t *dict,
                dict2pid_t *d2p)
{
    fsg_search_t *fsgs;
    char const *path;
	char const *isCyVoiceE = cmd_ln_str_r(config, "-CyVoiceE");

	Iava_SysPrintf("fsg_search_init +++++++++++++++++++++++++++++++++");
	

    fsgs = ckd_calloc(1, sizeof(*fsgs));
    ps_search_init(ps_search_base(fsgs), &fsg_funcs, config, acmod, dict, d2p);

    /* Initialize HMM context. */
    fsgs->hmmctx = hmm_context_init(bin_mdef_n_emit_state(acmod->mdef),
                                    acmod->tmat->tp, NULL, acmod->mdef->sseq);
    if (fsgs->hmmctx == NULL) {
        ps_search_free(ps_search_base(fsgs));
        return NULL;
    }

    /* Intialize the search history object */
    fsgs->history = fsg_history_init(NULL, dict);
    fsgs->frame = -1;

    /* Initialize FSG table. */
    fsgs->fsgs = hash_table_new(5, HASH_CASE_YES);

    /* Get search pruning parameters */
    fsgs->beam_factor = 1.0f;
    fsgs->beam = fsgs->beam_orig
        = (int32) logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-beam"))
        >> SENSCR_SHIFT;
    fsgs->pbeam = fsgs->pbeam_orig
        = (int32) logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-pbeam"))
        >> SENSCR_SHIFT;
    fsgs->wbeam = fsgs->wbeam_orig
        = (int32) logmath_log(acmod->lmath, cmd_ln_float64_r(config, "-wbeam"))
        >> SENSCR_SHIFT;

    /* LM related weights/penalties */
    fsgs->lw = cmd_ln_float32_r(config, "-lw");
    fsgs->pip = (int32) (logmath_log(acmod->lmath, cmd_ln_float32_r(config, "-pip"))
                           * fsgs->lw)
        >> SENSCR_SHIFT;
    fsgs->wip = (int32) (logmath_log(acmod->lmath, cmd_ln_float32_r(config, "-wip"))
                           * fsgs->lw)
        >> SENSCR_SHIFT;

    /* Best path search (and confidence annotation)? */
    if (cmd_ln_boolean_r(config, "-bestpath"))
        fsgs->bestpath = TRUE;

    /* Acoustic score scale for posterior probabilities. */
    fsgs->ascale = 1.0 / cmd_ln_float32_r(config, "-ascale");

    E_INFO("FSG(beam: %d, pbeam: %d, wbeam: %d; wip: %d, pip: %d)\n",
           fsgs->beam_orig, fsgs->pbeam_orig, fsgs->wbeam_orig,
           fsgs->wip, fsgs->pip);

    /* Load an FSG if one was specified in config */
    if ((path = cmd_ln_str_r(config, "-fsg"))) {
        fsg_model_t *fsg;
		unsigned char *p_data = NULL;
		unsigned int count = 0;

		//CyVoiceE CyVoiceE_H sphinx
		if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
		{
			cyVoiceE_modle * p_cyVoiceE_modle = cyVoiceE_init((char *)path);
			p_data = cyVoiceE_decrypt(p_cyVoiceE_modle,&count);
			fsg = cyVoiceE_fsg_model_readfile(p_data,count, acmod->lmath, fsgs->lw);
			cyVoiceE_uninit(p_cyVoiceE_modle);
			if (fsg == NULL)
			{
				goto error_out;
			}
		}
		else if((strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) /*|| (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0)*/ )
		{
			//从内存初始化 lfl
			p_data = (unsigned char *)acmod->cyVoiceE_gram;
			count = acmod->cyVoiceE_gram_len;
			p_data = cyVoiceE_decrypt_memory(p_data,&count);

			fsg = cyVoiceE_fsg_model_readfile(p_data,count, acmod->lmath, fsgs->lw);
			cyVoiceE_decrypt_memory_free((unsigned char *)p_data);
			if (fsg == NULL)
			{
				goto error_out;
			}
		}



        if (fsg_set_add(fsgs, fsg_model_name(fsg), fsg) != fsg) {
            fsg_model_free(fsg);
            goto error_out;
        }
        if (fsg_set_select(fsgs, fsg_model_name(fsg)) == NULL)
            goto error_out;
        if (fsg_search_reinit(ps_search_base(fsgs),
                              ps_search_dict(fsgs),
                              ps_search_dict2pid(fsgs)) < 0)
            goto error_out;
    }
    /* Or load a JSGF grammar */
    else if ((path = cmd_ln_str_r(config, "-jsgf"))) {
        fsg_model_t *fsg;
        jsgf_rule_t *rule;
        char const *toprule;
		unsigned char *p_data = NULL;
		unsigned int count = 0;

		//CyVoiceE CyVoiceE_H sphinx
		if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
		{
			cyVoiceE_modle * p_cyVoiceE_modle = cyVoiceE_init((char *)path);
			p_data = cyVoiceE_decrypt(p_cyVoiceE_modle,&count);
			fsgs->jsgf = cyVoiceE_jsgf_parse_memory(config,acmod,p_data, count,NULL);
			cyVoiceE_uninit(p_cyVoiceE_modle);
			if (fsgs->jsgf == NULL)
			{
				goto error_out;
			}
		}
		else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) /*|| (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0)*/ )
		{
			//从内存初始化 lfl
			p_data = (unsigned char *)acmod->cyVoiceE_gram;
			count = acmod->cyVoiceE_gram_len;
			p_data = cyVoiceE_decrypt_memory(p_data,&count);

			fsgs->jsgf = cyVoiceE_jsgf_parse_memory(config,acmod,p_data, count,NULL);
			cyVoiceE_decrypt_memory_free((unsigned char *)p_data);
			if (fsgs->jsgf == NULL)
			{
				goto error_out;
			}
		}

		

		//if ((fsgs->jsgf = jsgf_parse_file(path, NULL)) == NULL)
        //    goto error_out;

        rule = NULL;
        /* Take the -toprule if specified. */
        if ((toprule = cmd_ln_str_r(config, "-toprule"))) {
            char *anglerule;
            anglerule = string_join("<", toprule, ">", NULL);
            rule = jsgf_get_rule(fsgs->jsgf, anglerule);
            ckd_free(anglerule);
            if (rule == NULL) {
                E_ERROR("Start rule %s not found\n", toprule);
                goto error_out;
            }
        }
        /* Otherwise, take the first public rule. */
        else {
            jsgf_rule_iter_t *itor;

            for (itor = jsgf_rule_iter(fsgs->jsgf); itor;
                 itor = jsgf_rule_iter_next(itor)) {
                rule = jsgf_rule_iter_rule(itor);
                if (jsgf_rule_public(rule)) {
            	    jsgf_rule_iter_free(itor);
                    break;
                }
            }
            if (rule == NULL) {
                E_ERROR("No public rules found in %s\n", path);
                goto error_out;
            }
        }
        fsg = jsgf_build_fsg(fsgs->jsgf, rule, acmod->lmath, fsgs->lw);
        if (fsg_set_add(fsgs, fsg_model_name(fsg), fsg) != fsg) {
            fsg_model_free(fsg);
            goto error_out;
        }
        if (fsg_set_select(fsgs, fsg_model_name(fsg)) == NULL)
            goto error_out;
        if (fsg_search_reinit(ps_search_base(fsgs),
                              ps_search_dict(fsgs),
                              ps_search_dict2pid(fsgs)) < 0)
            goto error_out;
    }

    return ps_search_base(fsgs);

error_out:
    fsg_search_free(ps_search_base(fsgs));
    return NULL;
}

void
fsg_search_free(ps_search_t *search)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    hash_iter_t *itor;

    ps_search_deinit(search);
    if (fsgs->jsgf)
        jsgf_grammar_free(fsgs->jsgf);
    fsg_lextree_free(fsgs->lextree);
    if (fsgs->history) {
        fsg_history_reset(fsgs->history);
        fsg_history_set_fsg(fsgs->history, NULL, NULL);
        fsg_history_free(fsgs->history);
    }
    if (fsgs->fsgs) {
        for (itor = hash_table_iter(fsgs->fsgs);
             itor; itor = hash_table_iter_next(itor)) {
            fsg_model_t *fsg = (fsg_model_t *) hash_entry_val(itor->ent);
            fsg_model_free(fsg);
        }
        hash_table_free(fsgs->fsgs);
    }
    hmm_context_free(fsgs->hmmctx);
    ckd_free(fsgs);
}

int
fsg_search_reinit(ps_search_t *search, dict_t *dict, dict2pid_t *d2p)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;

    /* Free the old lextree */
    if (fsgs->lextree)
        fsg_lextree_free(fsgs->lextree);

    /* Free old dict2pid, dict */
    ps_search_base_reinit(search, dict, d2p);
    
    /* Nothing to update */
    if (fsgs->fsg == NULL)
	return 0;

    /* Update the number of words (not used by this module though). */
    search->n_words = dict_size(dict);

    /* Allocate new lextree for the given FSG */
    fsgs->lextree = fsg_lextree_init(fsgs->fsg, dict, d2p,
                                     ps_search_acmod(fsgs)->mdef,
                                     fsgs->hmmctx, fsgs->wip, fsgs->pip);

    /* Inform the history module of the new fsg */
    fsg_history_set_fsg(fsgs->history, fsgs->fsg, dict);

    return 0;
}


static int
fsg_search_add_silences(fsg_search_t *fsgs, fsg_model_t *fsg)
{
    dict_t *dict;
    int32 wid;
    int n_sil;

    dict = ps_search_dict(fsgs);
    /*
     * NOTE: Unlike N-Gram search, we do not use explicit start and
     * end symbols.  This is because the start and end nodes are
     * defined in the grammar.  We do add silence/filler self-loops to
     * all states in order to allow for silence between words and at
     * the beginning and end of utterances.
     *
     * This has some implications for word graph generation, namely,
     * that there can (and usually will) be multiple start and end
     * states in the word graph.  We therefore do add explicit start
     * and end nodes to the graph.
     */
    /* Add silence self-loops to all states. */
    fsg_model_add_silence(fsg, "<sil>", -1,
                          cmd_ln_float32_r(ps_search_config(fsgs), "-silprob"));
    n_sil = 0;
    /* Add self-loops for all other fillers. */
    for (wid = dict_filler_start(dict); wid < dict_filler_end(dict); ++wid) {
        char const *word = dict_wordstr(dict, wid);
        if (wid == dict_startwid(dict) || wid == dict_finishwid(dict))
            continue;
        fsg_model_add_silence(fsg, word, -1,
                              cmd_ln_float32_r(ps_search_config(fsgs), "-fillprob"));
        ++n_sil;
    }

    return n_sil;
}

/* Scans the dictionary and check if all words are present. */
static int
fsg_search_check_dict(fsg_search_t *fsgs, fsg_model_t *fsg)
{
    dict_t *dict;
    int i;

    dict = ps_search_dict(fsgs);
    for (i = 0; i < fsg_model_n_word(fsg); ++i) {
        char const *word;
        int32 wid;

        word = fsg_model_word_str(fsg, i);
        wid = dict_wordid(dict, word);
        if (wid == BAD_S3WID) {
    	    E_ERROR("The word '%s' is missing in the dictionary\n", word);
    	    return FALSE;
    	}
    }

    return TRUE;
}

static int
fsg_search_add_altpron(fsg_search_t *fsgs, fsg_model_t *fsg)
{
    dict_t *dict;
    int n_alt, n_word;
    int i;

    dict = ps_search_dict(fsgs);
    /* Scan FSG's vocabulary for words that have alternate pronunciations. */
    n_alt = 0;
    n_word = fsg_model_n_word(fsg);
    for (i = 0; i < n_word; ++i) {
        char const *word;
        int32 wid;

        word = fsg_model_word_str(fsg, i);
        wid = dict_wordid(dict, word);
        if (wid != BAD_S3WID) {
            while ((wid = dict_nextalt(dict, wid)) != BAD_S3WID) {
	        n_alt += fsg_model_add_alt(fsg, word, dict_wordstr(dict, wid));
    	    }
    	}
    }

    E_INFO("Added %d alternate word transitions\n", n_alt);
    return n_alt;
}

fsg_model_t *
fsg_set_get_fsg(fsg_search_t *fsgs, const char *name)
{
    void *val;

    if (hash_table_lookup(fsgs->fsgs, name, &val) < 0)
        return NULL;
    return (fsg_model_t *)val;
}

fsg_model_t *
fsg_set_add(fsg_search_t *fsgs, char const *name, fsg_model_t *fsg)
{
    if (name == NULL)
        name = fsg_model_name(fsg);

    if (!fsg_search_check_dict(fsgs, fsg))
	return NULL;

    /* Add silence transitions and alternate words. */
    if (cmd_ln_boolean_r(ps_search_config(fsgs), "-fsgusefiller")
        && !fsg_model_has_sil(fsg))
        fsg_search_add_silences(fsgs, fsg);
    if (cmd_ln_boolean_r(ps_search_config(fsgs), "-fsgusealtpron")
        && !fsg_model_has_alt(fsg))
        fsg_search_add_altpron(fsgs, fsg);

    return (fsg_model_t *)hash_table_enter(fsgs->fsgs, name, fsg);
}


fsg_model_t *
fsg_set_remove_byname(fsg_search_t *fsgs, char const *key)
{
    fsg_model_t *oldfsg;
    void *val;

    /* Look for the matching FSG. */
    if (hash_table_lookup(fsgs->fsgs, key, &val) < 0) {
        E_ERROR("FSG `%s' to be deleted not found\n", key);
        return NULL;
    }
    oldfsg = val;

    /* Remove it from the FSG table. */
    hash_table_delete(fsgs->fsgs, key);
    /* If this was the currently active FSG, also delete other stuff */
    if (fsgs->fsg == oldfsg) {
        fsg_lextree_free(fsgs->lextree);
        fsgs->lextree = NULL;
        fsg_history_set_fsg(fsgs->history, NULL, NULL);
        fsgs->fsg = NULL;
    }
    return oldfsg;
}


fsg_model_t *
fsg_set_remove(fsg_search_t *fsgs, fsg_model_t *fsg)
{
    char const *key;
    hash_iter_t *itor;

    key = NULL;
    for (itor = hash_table_iter(fsgs->fsgs);
         itor; itor = hash_table_iter_next(itor)) {
        fsg_model_t *oldfsg;

        oldfsg = (fsg_model_t *) hash_entry_val(itor->ent);
        if (oldfsg == fsg) {
            key = hash_entry_key(itor->ent);
            hash_table_iter_free(itor);
            break;
        }
    }
    if (key == NULL) {
        E_WARN("FSG '%s' to be deleted not found\n", fsg_model_name(fsg));
        return NULL;
    }
    else
        return fsg_set_remove_byname(fsgs, key);
}


fsg_model_t *
fsg_set_select(fsg_search_t *fsgs, const char *name)
{
    fsg_model_t *fsg;

    fsg = fsg_set_get_fsg(fsgs, name);
    if (fsg == NULL) {
        E_ERROR("FSG '%s' not known; cannot make it current\n", name);
        return NULL;
    }
    fsgs->fsg = fsg;
    return fsg;
}

fsg_set_iter_t *
fsg_set_iter(fsg_set_t *fsgs)
{
    return hash_table_iter(fsgs->fsgs);
}

fsg_set_iter_t *
fsg_set_iter_next(fsg_set_iter_t *itor)
{
    return hash_table_iter_next(itor);
}

fsg_model_t *
fsg_set_iter_fsg(fsg_set_iter_t *itor)
{
    return ((fsg_model_t *)itor->ent->val);
}

void
fsg_set_iter_free(fsg_set_iter_t *itor)
{
    hash_table_iter_free(itor);
}

static void
fsg_search_sen_active(fsg_search_t *fsgs)
{
    gnode_t *gn;
    fsg_pnode_t *pnode;
    hmm_t *hmm;

    acmod_clear_active(ps_search_acmod(fsgs));

    for (gn = fsgs->pnode_active; gn; gn = gnode_next(gn)) {
        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        hmm = fsg_pnode_hmmptr(pnode);
        assert(hmm_frame(hmm) == fsgs->frame);
        acmod_activate_hmm(ps_search_acmod(fsgs), hmm);
    }
}


/*
 * Evaluate all the active HMMs.
 * (Executed once per frame.)
 */
static void
fsg_search_hmm_eval(fsg_search_t *fsgs)
{
    gnode_t *gn;
    fsg_pnode_t *pnode;
    hmm_t *hmm;
    int32 bestscore;
    int32 n, maxhmmpf;

    bestscore = WORST_SCORE;

    if (!fsgs->pnode_active) {
        E_ERROR("Frame %d: No active HMM!!\n", fsgs->frame);
        return;
    }

    for (n = 0, gn = fsgs->pnode_active; gn; gn = gnode_next(gn), n++) {
        int32 score;

        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        hmm = fsg_pnode_hmmptr(pnode);
        assert(hmm_frame(hmm) == fsgs->frame);

#if __FSG_DBG__
        E_INFO("pnode(%08x) active @frm %5d\n", (int32) pnode,
               fsgs->frame);
        hmm_dump(hmm, stdout);
#endif
        score = hmm_vit_eval(hmm);
#if __FSG_DBG_CHAN__
        E_INFO("pnode(%08x) after eval @frm %5d\n",
               (int32) pnode, fsgs->frame);
        hmm_dump(hmm, stdout);
#endif

        if (score BETTER_THAN bestscore)
            bestscore = score;
    }

#if __FSG_DBG__
    E_INFO("[%5d] %6d HMM; bestscr: %11d\n", fsgs->frame, n, bestscore);
#endif
    fsgs->n_hmm_eval += n;

    /* Adjust beams if #active HMMs larger than absolute threshold */
    maxhmmpf = cmd_ln_int32_r(ps_search_config(fsgs), "-maxhmmpf");
    if (maxhmmpf != -1 && n > maxhmmpf) {
        /*
         * Too many HMMs active; reduce the beam factor applied to the default
         * beams, but not if the factor is already at a floor (0.1).
         */
        if (fsgs->beam_factor > 0.1) {        /* Hack!!  Hardwired constant 0.1 */
            fsgs->beam_factor *= 0.9f;        /* Hack!!  Hardwired constant 0.9 */
            fsgs->beam =
                (int32) (fsgs->beam_orig * fsgs->beam_factor);
            fsgs->pbeam =
                (int32) (fsgs->pbeam_orig * fsgs->beam_factor);
            fsgs->wbeam =
                (int32) (fsgs->wbeam_orig * fsgs->beam_factor);
        }
    }
    else {
        fsgs->beam_factor = 1.0f;
        fsgs->beam = fsgs->beam_orig;
        fsgs->pbeam = fsgs->pbeam_orig;
        fsgs->wbeam = fsgs->wbeam_orig;
    }

    if (n > fsg_lextree_n_pnode(fsgs->lextree))
        E_FATAL("PANIC! Frame %d: #HMM evaluated(%d) > #PNodes(%d)\n",
                fsgs->frame, n, fsg_lextree_n_pnode(fsgs->lextree));

    fsgs->bestscore = bestscore;
}


static void
fsg_search_pnode_trans(fsg_search_t *fsgs, fsg_pnode_t * pnode)
{
    fsg_pnode_t *child;
    hmm_t *hmm;
    int32 newscore, thresh, nf;

    assert(pnode);
    assert(!fsg_pnode_leaf(pnode));

    nf = fsgs->frame + 1;
    thresh = fsgs->bestscore + fsgs->beam;

    hmm = fsg_pnode_hmmptr(pnode);

    for (child = fsg_pnode_succ(pnode);
         child; child = fsg_pnode_sibling(child)) {
        newscore = hmm_out_score(hmm) + child->logs2prob;

        if ((newscore BETTER_THAN thresh)
            && (newscore BETTER_THAN hmm_in_score(&child->hmm))) {
            /* Incoming score > pruning threshold and > target's existing score */
            if (hmm_frame(&child->hmm) < nf) {
                /* Child node not yet activated; do so */
                fsgs->pnode_active_next =
                    glist_add_ptr(fsgs->pnode_active_next,
                                  (void *) child);
            }

            hmm_enter(&child->hmm, newscore, hmm_out_history(hmm), nf);
        }
    }
}


static void
fsg_search_pnode_exit(fsg_search_t *fsgs, fsg_pnode_t * pnode)
{
    hmm_t *hmm;
    fsg_link_t *fl;
    int32 wid;
    fsg_pnode_ctxt_t ctxt;

    assert(pnode);
    assert(fsg_pnode_leaf(pnode));

    hmm = fsg_pnode_hmmptr(pnode);
    fl = fsg_pnode_fsglink(pnode);
    assert(fl);

    wid = fsg_link_wid(fl);
    assert(wid >= 0);

#if __FSG_DBG__
    E_INFO("[%5d] Exit(%08x) %10d(score) %5d(pred)\n",
           fsgs->frame, (int32) pnode,
           hmm_out_score(hmm), hmm_out_history(hmm));
#endif

    /*
     * Check if this is filler or single phone word; these do not model right
     * context (i.e., the exit score applies to all right contexts).
     */
    if (fsg_model_is_filler(fsgs->fsg, wid)
        /* FIXME: This might be slow due to repeated calls to dict_to_id(). */
        || (dict_is_single_phone(ps_search_dict(fsgs),
                                   dict_wordid(ps_search_dict(fsgs),
                                               fsg_model_word_str(fsgs->fsg, wid))))) {
        /* Create a dummy context structure that applies to all right contexts */
        fsg_pnode_add_all_ctxt(&ctxt);

        /* Create history table entry for this word exit */
        fsg_history_entry_add(fsgs->history,
                              fl,
                              fsgs->frame,
                              hmm_out_score(hmm),
                              hmm_out_history(hmm),
                              pnode->ci_ext, ctxt);

    }
    else {
        /* Create history table entry for this word exit */
        fsg_history_entry_add(fsgs->history,
                              fl,
                              fsgs->frame,
                              hmm_out_score(hmm),
                              hmm_out_history(hmm),
                              pnode->ci_ext, pnode->ctxt);
    }
}


/*
 * (Beam) prune the just evaluated HMMs, determine which ones remain
 * active, which ones transition to successors, which ones exit and
 * terminate in their respective destination FSM states.
 * (Executed once per frame.)
 */
static void
fsg_search_hmm_prune_prop(fsg_search_t *fsgs)
{
    gnode_t *gn;
    fsg_pnode_t *pnode;
    hmm_t *hmm;
    int32 thresh, word_thresh, phone_thresh;

    assert(fsgs->pnode_active_next == NULL);

    thresh = fsgs->bestscore + fsgs->beam;
    phone_thresh = fsgs->bestscore + fsgs->pbeam;
    word_thresh = fsgs->bestscore + fsgs->wbeam;

    for (gn = fsgs->pnode_active; gn; gn = gnode_next(gn)) {
        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        hmm = fsg_pnode_hmmptr(pnode);

        if (hmm_bestscore(hmm) >= thresh) {
            /* Keep this HMM active in the next frame */
            if (hmm_frame(hmm) == fsgs->frame) {
                hmm_frame(hmm) = fsgs->frame + 1;
                fsgs->pnode_active_next =
                    glist_add_ptr(fsgs->pnode_active_next,
                                  (void *) pnode);
            }
            else {
                assert(hmm_frame(hmm) == fsgs->frame + 1);
            }

            if (!fsg_pnode_leaf(pnode)) {
                if (hmm_out_score(hmm) >= phone_thresh) {
                    /* Transition out of this phone into its children */
                    fsg_search_pnode_trans(fsgs, pnode);
                }
            }
            else {
                if (hmm_out_score(hmm) >= word_thresh) {
                    /* Transition out of leaf node into destination FSG state */
                    fsg_search_pnode_exit(fsgs, pnode);
                }
            }
        }
    }
}


/*
 * Propagate newly created history entries through null transitions.
 */
static void
fsg_search_null_prop(fsg_search_t *fsgs)
{
    int32 bpidx, n_entries, thresh, newscore;
    fsg_hist_entry_t *hist_entry;
    fsg_link_t *l;
    int32 s;
    fsg_model_t *fsg;

    fsg = fsgs->fsg;
    thresh = fsgs->bestscore + fsgs->wbeam; /* Which beam really?? */

    n_entries = fsg_history_n_entries(fsgs->history);

    for (bpidx = fsgs->bpidx_start; bpidx < n_entries; bpidx++) {
        fsg_arciter_t *itor;
        hist_entry = fsg_history_entry_get(fsgs->history, bpidx);

        l = fsg_hist_entry_fsglink(hist_entry);

        /* Destination FSG state for history entry */
        s = l ? fsg_link_to_state(l) : fsg_model_start_state(fsg);

        /*
         * Check null transitions from d to all other states.  (Only need to
         * propagate one step, since FSG contains transitive closure of null
         * transitions.)
         */
        /* Add all links from from_state to dst */
        for (itor = fsg_model_arcs(fsg, s); itor;
             itor = fsg_arciter_next(itor)) {
            fsg_link_t *l = fsg_arciter_get(itor);

            /* FIXME: Need to deal with tag transitions somehow. */
            if (fsg_link_wid(l) != -1)
                continue;
            newscore =
                fsg_hist_entry_score(hist_entry) +
                (fsg_link_logs2prob(l) >> SENSCR_SHIFT);

            if (newscore >= thresh) {
                fsg_history_entry_add(fsgs->history, l,
                                      fsg_hist_entry_frame(hist_entry),
                                      newscore,
                                      bpidx,
                                      fsg_hist_entry_lc(hist_entry),
                                      fsg_hist_entry_rc(hist_entry));
            }
        }
    }
}


/*
 * Perform cross-word transitions; propagate each history entry created in this
 * frame to lextree roots attached to the target FSG state for that entry.
 */
static void
fsg_search_word_trans(fsg_search_t *fsgs)
{
    int32 bpidx, n_entries;
    fsg_hist_entry_t *hist_entry;
    fsg_link_t *l;
    int32 score, newscore, thresh, nf, d;
    fsg_pnode_t *root;
    int32 lc, rc;

    n_entries = fsg_history_n_entries(fsgs->history);

    thresh = fsgs->bestscore + fsgs->beam;
    nf = fsgs->frame + 1;

    for (bpidx = fsgs->bpidx_start; bpidx < n_entries; bpidx++) {
        hist_entry = fsg_history_entry_get(fsgs->history, bpidx);
        assert(hist_entry);
        score = fsg_hist_entry_score(hist_entry);
        assert(fsgs->frame == fsg_hist_entry_frame(hist_entry));

        l = fsg_hist_entry_fsglink(hist_entry);

        /* Destination state for hist_entry */
        d = l ? fsg_link_to_state(l) : fsg_model_start_state(fsgs->
                                                                fsg);

        lc = fsg_hist_entry_lc(hist_entry);

        /* Transition to all root nodes attached to state d */
        for (root = fsg_lextree_root(fsgs->lextree, d);
             root; root = root->sibling) {
            rc = root->ci_ext;

            if ((root->ctxt.bv[lc >> 5] & (1 << (lc & 0x001f))) &&
                (hist_entry->rc.bv[rc >> 5] & (1 << (rc & 0x001f)))) {
                /*
                 * Last CIphone of history entry is in left-context list supported by
                 * target root node, and
                 * first CIphone of target root node is in right context list supported
                 * by history entry;
                 * So the transition can go ahead (if new score is good enough).
                 */
                newscore = score + root->logs2prob;

                if ((newscore BETTER_THAN thresh)
                    && (newscore BETTER_THAN hmm_in_score(&root->hmm))) {
                    if (hmm_frame(&root->hmm) < nf) {
                        /* Newly activated node; add to active list */
                        fsgs->pnode_active_next =
                            glist_add_ptr(fsgs->pnode_active_next,
                                          (void *) root);
#if __FSG_DBG__
                        E_INFO
                            ("[%5d] WordTrans bpidx[%d] -> pnode[%08x] (activated)\n",
                             fsgs->frame, bpidx, (int32) root);
#endif
                    }
                    else {
#if __FSG_DBG__
                        E_INFO
                            ("[%5d] WordTrans bpidx[%d] -> pnode[%08x]\n",
                             fsgs->frame, bpidx, (int32) root);
#endif
                    }

                    hmm_enter(&root->hmm, newscore, bpidx, nf);
                }
            }
        }
    }
}


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
unsigned int tatal_time00000,tatal_time00001,tatal_time00002,tatal_time00003,tatal_time00004,tatal_time00005;
#endif

int
fsg_search_step(ps_search_t *search, int frame_idx)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    int16 const *senscr;
    acmod_t *acmod = search->acmod;
    gnode_t *gn;
    fsg_pnode_t *pnode;
    hmm_t *hmm;

#if (CMU_SPHINX_TEST_PERFORMANCE == 1)
	unsigned int begin,end;

	begin = Iava_SysGetUS();

	if(frame_idx == 0)
	{
		tatal_time00000 = 0;
		tatal_time00001 = 0;
		tatal_time00002 = 0;
		tatal_time00003 = 0;
		tatal_time00004 = 0;
		tatal_time00005 = 0;		
	}

//	Iava_SysPrintf("ps_search_t    fsg_search_step frame_idx = %d!!!",frame_idx);
#endif


    /* Activate our HMMs for the current frame if need be. */
    if (!acmod->compallsen)
        fsg_search_sen_active(fsgs);

	
    /* Compute GMM scores for the current frame. */
    senscr = acmod_score(acmod, &frame_idx);
    fsgs->n_sen_eval += acmod->n_senone_active;
    hmm_context_set_senscore(fsgs->hmmctx, senscr);


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	end = Iava_SysGetUS();

	tatal_time00000 = tatal_time00000 + (end - begin);
#endif


    /* Mark backpointer table for current frame. */
    fsgs->bpidx_start = fsg_history_n_entries(fsgs->history);



    /* Evaluate all active pnodes (HMMs) */
    fsg_search_hmm_eval(fsgs);


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	begin = Iava_SysGetUS();

	tatal_time00001 = tatal_time00001 + (begin - end);
#endif	


    /*
     * Prune and propagate the HMMs evaluated; create history entries for
     * word exits.  The words exits are tentative, and may be pruned; make
     * the survivors permanent via fsg_history_end_frame().
     */
    fsg_search_hmm_prune_prop(fsgs);
    fsg_history_end_frame(fsgs->history);


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	end = Iava_SysGetUS();

	tatal_time00002 = tatal_time00002 + (end - begin);
#endif



    /*
     * Propagate new history entries through any null transitions, creating
     * new history entries, and then make the survivors permanent.
     */
    fsg_search_null_prop(fsgs);
    fsg_history_end_frame(fsgs->history);



#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	begin = Iava_SysGetUS();

	tatal_time00003 = tatal_time00003 + (begin - end);
#endif


    /*
     * Perform cross-word transitions; propagate each history entry across its
     * terminating state to the root nodes of the lextree attached to the state.
     */
    fsg_search_word_trans(fsgs);


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	end = Iava_SysGetUS();

	tatal_time00004 = tatal_time00004 + (end - begin);
#endif


    /*
     * We've now come full circle, HMM and FSG states have been updated for
     * the next frame.
     * Update the active lists, deactivate any currently active HMMs that
     * did not survive into the next frame
     */
    for (gn = fsgs->pnode_active; gn; gn = gnode_next(gn)) {
        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        hmm = fsg_pnode_hmmptr(pnode);

        if (hmm_frame(hmm) == fsgs->frame) {
            /* This HMM NOT activated for the next frame; reset it */
            fsg_psubtree_pnode_deactivate(pnode);
        }
        else {
            assert(hmm_frame(hmm) == (fsgs->frame + 1));
        }
    }

    /* Free the currently active list */
    glist_free(fsgs->pnode_active);

    /* Make the next-frame active list the current one */
    fsgs->pnode_active = fsgs->pnode_active_next;
    fsgs->pnode_active_next = NULL;

    /* End of this frame; ready for the next */
    ++fsgs->frame;


#if (CMU_SPHINX_TEST_PERFORMANCE == 1)	
	begin = Iava_SysGetUS();

	tatal_time00005 = tatal_time00005 + (begin - end);

	if((frame_idx != 0) && (frame_idx % 30) == 0)
		Iava_SysPrintf("fsg_search_step tatal_time00000 = %d  tatal_time00001 = %d  tatal_time00002 = %d  tatal_time00003 = %d  tatal_time00004 = %d  tatal_time00005 = %d  ",tatal_time00000,tatal_time00001,tatal_time00002,tatal_time00003,tatal_time00004,tatal_time00005);
#endif


    return 1;
}


/*
 * Set all HMMs to inactive, clear active lists, initialize FSM start
 * state to be the only active node.
 * (Executed at the start of each utterance.)
 */
int
fsg_search_start(ps_search_t *search)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    int32 silcipid;
    fsg_pnode_ctxt_t ctxt;

    /* Reset dynamic adjustment factor for beams */
    fsgs->beam_factor = 1.0f;
    fsgs->beam = fsgs->beam_orig;
    fsgs->pbeam = fsgs->pbeam_orig;
    fsgs->wbeam = fsgs->wbeam_orig;

    silcipid = bin_mdef_ciphone_id(ps_search_acmod(fsgs)->mdef, "SIL");

    /* Initialize EVERYTHING to be inactive */
    assert(fsgs->pnode_active == NULL);
    assert(fsgs->pnode_active_next == NULL);

    fsg_history_reset(fsgs->history);
    fsg_history_utt_start(fsgs->history);
    fsgs->final = FALSE;

    /* Dummy context structure that allows all right contexts to use this entry */
    fsg_pnode_add_all_ctxt(&ctxt);

    /* Create dummy history entry leading to start state */
    fsgs->frame = -1;
    fsgs->bestscore = 0;
    fsg_history_entry_add(fsgs->history,
                          NULL, -1, 0, -1, silcipid, ctxt);
    fsgs->bpidx_start = 0;

    /* Propagate dummy history entry through NULL transitions from start state */
    fsg_search_null_prop(fsgs);

    /* Perform word transitions from this dummy history entry */
    fsg_search_word_trans(fsgs);

    /* Make the next-frame active list the current one */
    fsgs->pnode_active = fsgs->pnode_active_next;
    fsgs->pnode_active_next = NULL;

    ++fsgs->frame;

    fsgs->n_hmm_eval = 0;
    fsgs->n_sen_eval = 0;

    return 0;
}

/*
 * Cleanup at the end of each utterance.
 */
int
fsg_search_finish(ps_search_t *search)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    gnode_t *gn;
    fsg_pnode_t *pnode;
    int32 n_hist;

    /* Deactivate all nodes in the current and next-frame active lists */
    for (gn = fsgs->pnode_active; gn; gn = gnode_next(gn)) {
        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        fsg_psubtree_pnode_deactivate(pnode);
    }
    for (gn = fsgs->pnode_active_next; gn; gn = gnode_next(gn)) {
        pnode = (fsg_pnode_t *) gnode_ptr(gn);
        fsg_psubtree_pnode_deactivate(pnode);
    }

    glist_free(fsgs->pnode_active);
    fsgs->pnode_active = NULL;
    glist_free(fsgs->pnode_active_next);
    fsgs->pnode_active_next = NULL;

    fsgs->final = TRUE;

    n_hist = fsg_history_n_entries(fsgs->history);
    E_INFO
        ("%d frames, %d HMMs (%d/fr), %d senones (%d/fr), %d history entries (%d/fr)\n\n",
         fsgs->frame, fsgs->n_hmm_eval,
         (fsgs->frame > 0) ? fsgs->n_hmm_eval / fsgs->frame : 0,
         fsgs->n_sen_eval,
         (fsgs->frame > 0) ? fsgs->n_sen_eval / fsgs->frame : 0,
         n_hist, (fsgs->frame > 0) ? n_hist / fsgs->frame : 0);

    return 0;
}

static int
fsg_search_find_exit(fsg_search_t *fsgs, int frame_idx, int final, int32 *out_score, int32* out_is_final)
{
    fsg_hist_entry_t *hist_entry;
    fsg_model_t *fsg;
    int bpidx, frm, last_frm, besthist;
    int32 bestscore;

    if (frame_idx == -1)
        frame_idx = fsgs->frame - 1;
    last_frm = frm = frame_idx;

    /* Scan backwards to find a word exit in frame_idx. */
    bpidx = fsg_history_n_entries(fsgs->history) - 1;
    while (bpidx > 0) {
        hist_entry = fsg_history_entry_get(fsgs->history, bpidx);
        if (fsg_hist_entry_frame(hist_entry) <= frame_idx) {
            frm = last_frm = fsg_hist_entry_frame(hist_entry);
            break;
        }
    }

    /* No hypothesis (yet). */
    if (bpidx <= 0) 
        return bpidx;

    /* Now find best word exit in this frame. */
    bestscore = INT_MIN;
    besthist = -1;
    fsg = fsgs->fsg;
    while (frm == last_frm) {
        fsg_link_t *fl;
        int32 score;

        fl = fsg_hist_entry_fsglink(hist_entry);
        score = fsg_hist_entry_score(hist_entry);
        
        if (fl == NULL)
	    break;

		/* Prefer final hypothesis */
		if (score == bestscore && fsg_link_to_state(fl) == fsg_model_final_state(fsg)) {
    			besthist = bpidx;
		} else if (score BETTER_THAN bestscore) {
            /* Only enforce the final state constraint if this is a final hypothesis. */
            if ((!final)
                || fsg_link_to_state(fl) == fsg_model_final_state(fsg)) {
                bestscore = score;
                besthist = bpidx;
            }
        }
        
        --bpidx;
        if (bpidx < 0)
            break;
        hist_entry = fsg_history_entry_get(fsgs->history, bpidx);
        frm = fsg_hist_entry_frame(hist_entry);
    }

    /* Final state not reached. */
    if (besthist == -1) {
        E_ERROR("Final result does not match the grammar in frame %d\n", frame_idx);
        return -1;
    }

    /* This here's the one we want. */
    if (out_score)
        *out_score = bestscore;
    if (out_is_final) {
	fsg_link_t *fl;
	hist_entry = fsg_history_entry_get(fsgs->history, besthist);
	fl = fsg_hist_entry_fsglink(hist_entry);
	*out_is_final = (fsg_link_to_state(fl) == fsg_model_final_state(fsg));
    }
    return besthist;
}

/* FIXME: Mostly duplicated with ngram_search_bestpath(). */
static ps_latlink_t *
fsg_search_bestpath(ps_search_t *search, int32 *out_score, int backward)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;

    if (search->last_link == NULL) {
        search->last_link = ps_lattice_bestpath(search->dag, NULL,
                                                1.0, fsgs->ascale);
        if (search->last_link == NULL)
            return NULL;
        /* Also calculate betas so we can fill in the posterior
         * probability field in the segmentation. */
        if (search->post == 0)
            search->post = ps_lattice_posterior(search->dag, NULL, fsgs->ascale);
    }
    if (out_score)
        *out_score = search->last_link->path_scr + search->dag->final_node_ascr;
    return search->last_link;
}

char const *
fsg_search_hyp(ps_search_t *search, int32 *out_score, int32 *out_is_final)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    dict_t *dict = ps_search_dict(search);
	acmod_t *acmod =  search->acmod;
    char *c;
    size_t len;
    int bp, bpidx;

    /* Get last backpointer table index. */
    bpidx = fsg_search_find_exit(fsgs, fsgs->frame, fsgs->final, out_score, out_is_final);
    /* No hypothesis (yet). */
    if (bpidx <= 0)
        return NULL;

    /* If bestpath is enabled and the utterance is complete, then run it. */
    if (fsgs->bestpath && fsgs->final) {
        ps_lattice_t *dag;
        ps_latlink_t *link;

        if ((dag = fsg_search_lattice(search)) == NULL) {
    	    E_WARN("Failed to obtain the lattice while bestpath enabled\n");
            return NULL;
        }
        if ((link = fsg_search_bestpath(search, out_score, FALSE)) == NULL) {
    	    E_WARN("Failed to find the bestpath in a lattice\n");
            return NULL;
        }
        return ps_lattice_hyp(dag, link);
    }

    bp = bpidx;
    len = 0;
    while (bp > 0) {
        fsg_hist_entry_t *hist_entry = fsg_history_entry_get(fsgs->history, bp);
        fsg_link_t *fl = fsg_hist_entry_fsglink(hist_entry);
        char const *baseword;
        int32 wid;

		int lc = fsg_hist_entry_lc(hist_entry);
		int ci;

		if(lc < acmod->mdef->n_ciphone)
		{
			ci = lc;
		}
		else
		{
			ci = (acmod->mdef)->phone[lc].info.cd.ctx[0];
		}

		printf("\n senone  : %s",acmod->mdef->ciname[ci]);

        bp = fsg_hist_entry_pred(hist_entry);
        wid = fsg_link_wid(fl);
        if (wid < 0 || fsg_model_is_filler(fsgs->fsg, wid))
            continue;
        baseword = dict_basestr(dict,
                                dict_wordid(dict,
                                            fsg_model_word_str(fsgs->fsg, wid)));
        len += strlen(baseword) + 1;
    }
    
    ckd_free(search->hyp_str);
    if (len == 0) {
	search->hyp_str = NULL;
	return search->hyp_str;
    }
    search->hyp_str = ckd_calloc(1, len);

    bp = bpidx;
    c = search->hyp_str + len - 1;
    while (bp > 0) {
        fsg_hist_entry_t *hist_entry = fsg_history_entry_get(fsgs->history, bp);
        fsg_link_t *fl = fsg_hist_entry_fsglink(hist_entry);
        char const *baseword;
        int32 wid;

        bp = fsg_hist_entry_pred(hist_entry);
        wid = fsg_link_wid(fl);
        if (wid < 0 || fsg_model_is_filler(fsgs->fsg, wid))
            continue;
        baseword = dict_basestr(dict,
                                dict_wordid(dict,
                                            fsg_model_word_str(fsgs->fsg, wid)));
        len = strlen(baseword);
        c -= len;
        memcpy(c, baseword, len);
        if (c > search->hyp_str) {
            --c;
            *c = ' ';
        }
    }

    return search->hyp_str;
}

static void
fsg_seg_bp2itor(ps_seg_t *seg, fsg_hist_entry_t *hist_entry)
{
    fsg_search_t *fsgs = (fsg_search_t *)seg->search;
    fsg_hist_entry_t *ph = NULL;
    int32 bp;

    if ((bp = fsg_hist_entry_pred(hist_entry)) >= 0)
        ph = fsg_history_entry_get(fsgs->history, bp);
    seg->word = fsg_model_word_str(fsgs->fsg, hist_entry->fsglink->wid);
    seg->ef = fsg_hist_entry_frame(hist_entry);
    seg->sf = ph ? fsg_hist_entry_frame(ph) + 1 : 0;
    /* This is kind of silly but it happens for null transitions. */
    if (seg->sf > seg->ef) seg->sf = seg->ef;
    seg->prob = 0; /* Bogus value... */
    /* "Language model" score = transition probability. */
    seg->lback = 1;
    seg->lscr = hist_entry->fsglink->logs2prob;
    if (ph) {
        /* FIXME: Not sure exactly how cross-word triphones are handled. */
        seg->ascr = hist_entry->score - ph->score - seg->lscr;
    }
    else
        seg->ascr = hist_entry->score - seg->lscr;
}


static void
fsg_seg_free(ps_seg_t *seg)
{
    fsg_seg_t *itor = (fsg_seg_t *)seg;
    ckd_free(itor->hist);
    ckd_free(itor);
}

static ps_seg_t *
fsg_seg_next(ps_seg_t *seg)
{
    fsg_seg_t *itor = (fsg_seg_t *)seg;

    if (++itor->cur == itor->n_hist) {
        fsg_seg_free(seg);
        return NULL;
    }

    fsg_seg_bp2itor(seg, itor->hist[itor->cur]);
    return seg;
}

static ps_segfuncs_t fsg_segfuncs = {
    /* seg_next */ fsg_seg_next,
    /* seg_free */ fsg_seg_free
};

static ps_seg_t *
fsg_search_seg_iter(ps_search_t *search, int32 *out_score)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;
    fsg_seg_t *itor;
    int bp, bpidx, cur;

    bpidx = fsg_search_find_exit(fsgs, fsgs->frame, fsgs->final, out_score, NULL);
    /* No hypothesis (yet). */
    if (bpidx <= 0)
        return NULL;

    /* If bestpath is enabled and the utterance is complete, then run it. */
    if (fsgs->bestpath && fsgs->final) {
        ps_lattice_t *dag;
        ps_latlink_t *link;

        if ((dag = fsg_search_lattice(search)) == NULL)
            return NULL;
        if ((link = fsg_search_bestpath(search, out_score, TRUE)) == NULL)
            return NULL;
        return ps_lattice_seg_iter(dag, link, 1.0);
    }

    /* Calling this an "iterator" is a bit of a misnomer since we have
     * to get the entire backtrace in order to produce it.  On the
     * other hand, all we actually need is the bptbl IDs, and we can
     * allocate a fixed-size array of them. */
    itor = ckd_calloc(1, sizeof(*itor));
    itor->base.vt = &fsg_segfuncs;
    itor->base.search = search;
    itor->base.lwf = 1.0;
    itor->n_hist = 0;
    bp = bpidx;
    while (bp > 0) {
        fsg_hist_entry_t *hist_entry = fsg_history_entry_get(fsgs->history, bp);
        bp = fsg_hist_entry_pred(hist_entry);
        ++itor->n_hist;
    }
    if (itor->n_hist == 0) {
        ckd_free(itor);
        return NULL;
    }
    itor->hist = ckd_calloc(itor->n_hist, sizeof(*itor->hist));
    cur = itor->n_hist - 1;
    bp = bpidx;
    while (bp > 0) {
        fsg_hist_entry_t *hist_entry = fsg_history_entry_get(fsgs->history, bp);
        itor->hist[cur] = hist_entry;
        bp = fsg_hist_entry_pred(hist_entry);
        --cur;
    }

    /* Fill in relevant fields for first element. */
    fsg_seg_bp2itor((ps_seg_t *)itor, itor->hist[0]);
    
    return (ps_seg_t *)itor;
}

static int
fsg_search_prob(ps_search_t *search)
{
    fsg_search_t *fsgs = (fsg_search_t *)search;

    /* If bestpath is enabled and the utterance is complete, then run it. */
    if (fsgs->bestpath && fsgs->final) {
        ps_lattice_t *dag;
        ps_latlink_t *link;

        if ((dag = fsg_search_lattice(search)) == NULL)
            return 0;
        if ((link = fsg_search_bestpath(search, NULL, TRUE)) == NULL)
            return 0;
        return search->post;
    }
    else {
        /* FIXME: Give some kind of good estimate here, eventually. */
        return 0;
    }
}

static ps_latnode_t *
find_node(ps_lattice_t *dag, fsg_model_t *fsg, int sf, int32 wid, int32 node_id)
{
    ps_latnode_t *node;

    for (node = dag->nodes; node; node = node->next)
        if ((node->sf == sf) && (node->wid == wid) && (node->node_id == node_id))
            break;
    return node;
}

static ps_latnode_t *
new_node(ps_lattice_t *dag, fsg_model_t *fsg, int sf, int ef, int32 wid, int32 node_id, int32 ascr)
{
    ps_latnode_t *node;

    node = find_node(dag, fsg, sf, wid, node_id);

    if (node) {
        /* Update end frames. */
        if (node->lef == -1 || node->lef < ef)
            node->lef = ef;
        if (node->fef == -1 || node->fef > ef)
            node->fef = ef;
        /* Update best link score. */
        if (ascr BETTER_THAN node->info.best_exit)
            node->info.best_exit = ascr;
    }
    else {
        /* New node; link to head of list */
        node = listelem_malloc(dag->latnode_alloc);
        node->wid = wid;
        node->sf = sf;
        node->fef = node->lef = ef;
        node->reachable = FALSE;
        node->entries = NULL;
        node->exits = NULL;
        node->info.best_exit = ascr;
        node->node_id = node_id;

        node->next = dag->nodes;
        dag->nodes = node;
        ++dag->n_nodes;
    }

    return node;
}

static ps_latnode_t *
find_start_node(fsg_search_t *fsgs, ps_lattice_t *dag)
{
    ps_latnode_t *node;
    glist_t start = NULL;
    int nstart = 0;

    /* Look for all nodes starting in frame zero with some exits. */
    for (node = dag->nodes; node; node = node->next) {
        if (node->sf == 0 && node->exits) {
            E_INFO("Start node %s.%d:%d:%d\n",
                   fsg_model_word_str(fsgs->fsg, node->wid),
                   node->sf, node->fef, node->lef);
            start = glist_add_ptr(start, node);
            ++nstart;
        }
    }

    /* If there was more than one start node candidate, then we need
     * to create an artificial start node with epsilon transitions to
     * all of them. */
    if (nstart == 1) {
        node = gnode_ptr(start);
    }
    else {
        gnode_t *st;
        int wid;

        wid = fsg_model_word_add(fsgs->fsg, "<s>");
        if (fsgs->fsg->silwords)
            bitvec_set(fsgs->fsg->silwords, wid);
        node = new_node(dag, fsgs->fsg, 0, 0, wid, -1, 0);
        for (st = start; st; st = gnode_next(st))
            ps_lattice_link(dag, node, gnode_ptr(st), 0, 0);
    }
    glist_free(start);
    return node;
}

static ps_latnode_t *
find_end_node(fsg_search_t *fsgs, ps_lattice_t *dag)
{
    ps_latnode_t *node;
    glist_t end = NULL;
    int nend = 0;

    /* Look for all nodes ending in last frame with some entries. */
    for (node = dag->nodes; node; node = node->next) {
        if (node->lef == dag->n_frames - 1 && node->entries) {
            E_INFO("End node %s.%d:%d:%d (%d)\n",
                   fsg_model_word_str(fsgs->fsg, node->wid),
                   node->sf, node->fef, node->lef, node->info.best_exit);
            end = glist_add_ptr(end, node);
            ++nend;
        }
    }

    if (nend == 1) {
        node = gnode_ptr(end);
    }
    else if (nend == 0) {
        ps_latnode_t *last = NULL;
        int ef = 0;

        /* If there were no end node candidates, then just use the
         * node with the last exit frame. */
        for (node = dag->nodes; node; node = node->next) {
            if (node->lef > ef && node->entries) {
                last = node;
                ef = node->lef;
            }
        }
        node = last;
        if (node)
            E_INFO("End node %s.%d:%d:%d (%d)\n",
                   fsg_model_word_str(fsgs->fsg, node->wid),
                   node->sf, node->fef, node->lef, node->info.best_exit);
    }    
    else {
        /* If there was more than one end node candidate, then we need
         * to create an artificial end node with epsilon transitions
         * out of all of them. */
        gnode_t *st;
        int wid;
        wid = fsg_model_word_add(fsgs->fsg, "</s>");
        if (fsgs->fsg->silwords)
            bitvec_set(fsgs->fsg->silwords, wid);
        node = new_node(dag, fsgs->fsg, fsgs->frame, fsgs->frame, wid, -1, 0);
        /* Use the "best" (in reality it will be the only) exit link
         * score from this final node as the link score. */
        for (st = end; st; st = gnode_next(st)) {
            ps_latnode_t *src = gnode_ptr(st);
            ps_lattice_link(dag, src, node, src->info.best_exit, fsgs->frame);
        }
    }
    glist_free(end);
    return node;
}

static void
mark_reachable(ps_lattice_t *dag, ps_latnode_t *end)
{
    glist_t q;

    /* It doesn't matter which order we do this in. */
    end->reachable = TRUE;
    q = glist_add_ptr(NULL, end);
    while (q) {
        ps_latnode_t *node = gnode_ptr(q);
        latlink_list_t *x;

        /* Pop the front of the list. */
        q = gnode_free(q, NULL);
        /* Expand all its predecessors that haven't been seen yet. */
        for (x = node->entries; x; x = x->next) {
            ps_latnode_t *next = x->link->from;
            if (!next->reachable) {
                next->reachable = TRUE;
                q = glist_add_ptr(q, next);
            }
        }
    }
}

/**
 * Generate a lattice from FSG search results.
 *
 * One might think that this is simply a matter of adding acoustic
 * scores to the FSG's edges.  However, one would be wrong.  The
 * crucial difference here is that the word lattice is acyclic, and it
 * also contains timing information.
 */
static ps_lattice_t *
fsg_search_lattice(ps_search_t *search)
{
    fsg_search_t *fsgs;
    fsg_model_t *fsg;
    ps_latnode_t *node;
    ps_lattice_t *dag;
    int32 i, n;

    fsgs = (fsg_search_t *)search;

    /* Check to see if a lattice has previously been created over the
     * same number of frames, and reuse it if so. */
    if (search->dag && search->dag->n_frames == fsgs->frame)
        return search->dag;

    /* Nope, create a new one. */
    ps_lattice_free(search->dag);
    search->dag = NULL;
    dag = ps_lattice_init_search(search, fsgs->frame);
    fsg = fsgs->fsg;

    /*
     * Each history table entry represents a link in the word graph.
     * The set of nodes is determined by the number of unique
     * (word,start-frame) pairs in the history table.  So we will
     * first find all those nodes.
     */
    n = fsg_history_n_entries(fsgs->history);
    for (i = 0; i < n; ++i) {
        fsg_hist_entry_t *fh = fsg_history_entry_get(fsgs->history, i);
        int32 ascr;
        int sf;

        /* Skip null transitions. */
        if (fh->fsglink == NULL || fh->fsglink->wid == -1)
            continue;

        /* Find the start node of this link. */
        if (fh->pred) {
            fsg_hist_entry_t *pfh = fsg_history_entry_get(fsgs->history, fh->pred);
            /* FIXME: We include the transition score in the lattice
             * link score.  This is because of the practical
             * difficulty of obtaining it separately in bestpath or
             * forward-backward search, and because it is essentially
             * a unigram probability, so there is no need to treat it
             * separately from the acoustic score.  However, it's not
             * clear that this will actually yield correct results.*/
            ascr = fh->score - pfh->score;
            sf = pfh->frame + 1;
        }
        else {
            ascr = fh->score;
            sf = 0;
        }

        /*
         * Note that although scores are tied to links rather than
         * nodes, it's possible that there are no links out of the
         * destination node, and thus we need to preserve its score in
         * case it turns out to be utterance-final.
         */
        new_node(dag, fsg, sf, fh->frame, fh->fsglink->wid, fsg_link_to_state(fh->fsglink), ascr);
    }

    /*
     * Now, we will create links only to nodes that actually exist.
     */
    n = fsg_history_n_entries(fsgs->history);
    for (i = 0; i < n; ++i) {
        fsg_hist_entry_t *fh = fsg_history_entry_get(fsgs->history, i);
        fsg_arciter_t *itor;
        ps_latnode_t *src, *dest;
        int32 ascr;
        int sf;

        /* Skip null transitions. */
        if (fh->fsglink == NULL || fh->fsglink->wid == -1)
            continue;

        /* Find the start node of this link and calculate its link score. */
        if (fh->pred) {
            fsg_hist_entry_t *pfh = fsg_history_entry_get(fsgs->history, fh->pred);
            sf = pfh->frame + 1;
            ascr = fh->score - pfh->score;
        }
        else {
            ascr = fh->score;
            sf = 0;
        }
        src = find_node(dag, fsg, sf, fh->fsglink->wid, fsg_link_to_state(fh->fsglink));
        sf = fh->frame + 1;

        for (itor = fsg_model_arcs(fsg, fsg_link_to_state(fh->fsglink));
             itor; itor = fsg_arciter_next(itor)) {
            fsg_link_t *link = fsg_arciter_get(itor);
            
            /* FIXME: Need to figure out what to do about tag transitions. */
            if (link->wid >= 0) {
                /*
                 * For each non-epsilon link following this one, look for a
                 * matching node in the lattice and link to it.
                 */
                if ((dest = find_node(dag, fsg, sf, link->wid, fsg_link_to_state(link))) != NULL)
            	    ps_lattice_link(dag, src, dest, ascr, fh->frame);
            }
            else {
                /*
                 * Transitive closure on nulls has already been done, so we
                 * just need to look one link forward from them.
                 */
                fsg_arciter_t *itor2;
                
                /* Add all non-null links out of j. */
                for (itor2 = fsg_model_arcs(fsg, fsg_link_to_state(link));
                     itor2; itor2 = fsg_arciter_next(itor2)) {
                    fsg_link_t *link = fsg_arciter_get(itor2);

                    if (link->wid == -1)
                        continue;
                    
                    if ((dest = find_node(dag, fsg, sf, link->wid, fsg_link_to_state(link))) != NULL) {
                        ps_lattice_link(dag, src, dest, ascr, fh->frame);
                    }
                }
            }
        }
    }


    /* Figure out which nodes are the start and end nodes. */
    if ((dag->start = find_start_node(fsgs, dag)) == NULL) {
	E_WARN("Failed to find the start node\n");
        goto error_out;
    }
    if ((dag->end = find_end_node(fsgs, dag)) == NULL) {
	E_WARN("Failed to find the end node\n");
        goto error_out;
    }


    E_INFO("lattice start node %s.%d end node %s.%d\n",
           fsg_model_word_str(fsg, dag->start->wid), dag->start->sf,
           fsg_model_word_str(fsg, dag->end->wid), dag->end->sf);
    /* FIXME: Need to calculate final_node_ascr here. */

    /*
     * Convert word IDs from FSG to dictionary.
     */
    for (node = dag->nodes; node; node = node->next) {
        node->wid = dict_wordid(dag->search->dict,
                                fsg_model_word_str(fsg, node->wid));
        node->basewid = dict_basewid(dag->search->dict, node->wid);
    }

    /*
     * Now we are done, because the links in the graph are uniquely
     * defined by the history table.  However we should remove any
     * nodes which are not reachable from the end node of the FSG.
     * Everything is reachable from the start node by definition.
     */
    mark_reachable(dag, dag->end);

    ps_lattice_delete_unreachable(dag);
    {
        int32 silpen, fillpen;

        silpen = (int32)(logmath_log(fsg->lmath,
                                     cmd_ln_float32_r(ps_search_config(fsgs), "-silprob"))
                         * fsg->lw)
            >> SENSCR_SHIFT;
        fillpen = (int32)(logmath_log(fsg->lmath,
                                      cmd_ln_float32_r(ps_search_config(fsgs), "-fillprob"))
                          * fsg->lw)
            >> SENSCR_SHIFT;
        ps_lattice_bypass_fillers(dag, silpen, fillpen);
    }
    search->dag = dag;

    return dag;


error_out:
    ps_lattice_free(dag);
    return NULL;

}
