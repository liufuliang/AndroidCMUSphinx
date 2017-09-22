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

/* System headers. */
#include <string.h>

/* SphinxBase headers. */
#include <sphinxbase/pio.h>
#include <sphinxbase/strfuncs.h>


/* Local headers. */
#include "dict.h"

#include "emu_sys_log.h"
#include "cyVoiceE_model.h"

#define DELIM	" \t\n"         /* Set of field separator characters */
#define DEFAULT_NUM_PHONE	(MAX_S3CIPID+1)

#if WIN32
#define snprintf sprintf_s
#endif 

#define CYVOICEE_BUFF_SIZE	(1024)

extern const char *const cmu6_lts_phone_table[];

static s3cipid_t
dict_ciphone_id(dict_t * d, const char *str)
{
    if (d->nocase)
        return bin_mdef_ciphone_id_nocase(d->mdef, str);
    else
        return bin_mdef_ciphone_id(d->mdef, str);
}


const char *
dict_ciphone_str(dict_t * d, s3wid_t wid, int32 pos)
{
    assert(d != NULL);
    assert((wid >= 0) && (wid < d->n_word));
    assert((pos >= 0) && (pos < d->word[wid].pronlen));

    return bin_mdef_ciphone_str(d->mdef, d->word[wid].ciphone[pos]);
}


s3wid_t
dict_add_word(dict_t * d, char const *word, s3cipid_t const * p, int32 np)
{
    int32 len;
    dictword_t *wordp;
    s3wid_t newwid;
    char *wword;

    if (d->n_word >= d->max_words) {
        E_INFO("Reallocating to %d KiB for word entries\n",
               (d->max_words + S3DICT_INC_SZ) * sizeof(dictword_t) / 1024);
        d->word =
            (dictword_t *) ckd_realloc(d->word,
                                       (d->max_words +
                                        S3DICT_INC_SZ) * sizeof(dictword_t));
        d->max_words = d->max_words + S3DICT_INC_SZ;
    }

    wordp = d->word + d->n_word;
    wordp->word = (char *) ckd_salloc(word);    /* Freed in dict_free */

    /* Associate word string with d->n_word in hash table */
    if (hash_table_enter_int32(d->ht, wordp->word, d->n_word) != d->n_word) {
        ckd_free(wordp->word);
        wordp->word = NULL;
        return BAD_S3WID;
    }

    /* Fill in word entry, and set defaults */
    if (p && (np > 0)) {
        wordp->ciphone = (s3cipid_t *) ckd_malloc(np * sizeof(s3cipid_t));      /* Freed in dict_free */
        memcpy(wordp->ciphone, p, np * sizeof(s3cipid_t));
        wordp->pronlen = np;
    }
    else {
        wordp->ciphone = NULL;
        wordp->pronlen = 0;
    }
    wordp->alt = BAD_S3WID;
    wordp->basewid = d->n_word;

    /* Determine base/alt wids */
    wword = ckd_salloc(word);
    if ((len = dict_word2basestr(wword)) > 0) {
	int32 w;

        /* Truncated to a baseword string; find its ID */
        if (hash_table_lookup_int32(d->ht, wword, &w) < 0) {
            E_ERROR("Missing base word for: %s\n", word);
            ckd_free(wword);
            ckd_free(wordp->word);
            wordp->word = NULL;
            return BAD_S3WID;
        }

        /* Link into alt list */
        wordp->basewid = w;
        wordp->alt = d->word[w].alt;
        d->word[w].alt = d->n_word;
    }
    ckd_free(wword);

    newwid = d->n_word++;

    return newwid;
}


static int32
dict_read(FILE * fp, dict_t * d)
{
    lineiter_t *li;
    char **wptr;
    s3cipid_t *p;
    int32 lineno, nwd;
    s3wid_t w;
    int32 i, maxwd;
    size_t stralloc, phnalloc;

    maxwd = 512;
    p = (s3cipid_t *) ckd_calloc(maxwd + 4, sizeof(*p));
    wptr = (char **) ckd_calloc(maxwd, sizeof(char *)); /* Freed below */

    lineno = 0;
    stralloc = phnalloc = 0;
    for (li = lineiter_start(fp); li; li = lineiter_next(li)) {
        lineno++;
        if (0 == strncmp(li->buf, "##", 2)
            || 0 == strncmp(li->buf, ";;", 2))
            continue;

        if ((nwd = str2words(li->buf, wptr, maxwd)) < 0) {
            /* Increase size of p, wptr. */
            nwd = str2words(li->buf, NULL, 0);
            assert(nwd > maxwd); /* why else would it fail? */
            maxwd = nwd;
            p = (s3cipid_t *) ckd_realloc(p, (maxwd + 4) * sizeof(*p));
            wptr = (char **) ckd_realloc(wptr, maxwd * sizeof(*wptr));
        }

        if (nwd == 0)           /* Empty line */
            continue;
        /* wptr[0] is the word-string and wptr[1..nwd-1] the pronunciation sequence */
        if (nwd == 1) {
            E_ERROR("Line %d: No pronunciation for word '%s'; ignored\n",
                    lineno, wptr[0]);
            continue;
        }


        /* Convert pronunciation string to CI-phone-ids */
        for (i = 1; i < nwd; i++) {
            p[i - 1] = dict_ciphone_id(d, wptr[i]);
            if (NOT_S3CIPID(p[i - 1])) {
                E_ERROR("Line %d: Phone '%s' is mising in the acoustic model; word '%s' ignored\n",
                        lineno, wptr[i], wptr[0]);
                break;
            }
        }

        if (i == nwd) {         /* All CI-phones successfully converted to IDs */
            w = dict_add_word(d, wptr[0], p, nwd - 1);
            if (NOT_S3WID(w))
                E_ERROR
                    ("Line %d: Failed to add the word '%s' (duplicate?); ignored\n",
                     lineno, wptr[0]);
            else {
                stralloc += strlen(d->word[w].word);
                phnalloc += d->word[w].pronlen * sizeof(s3cipid_t);
            }
        }
    }
    E_INFO("Allocated %d KiB for strings, %d KiB for phones\n",
           (int)stralloc / 1024, (int)phnalloc / 1024);
    ckd_free(p);
    ckd_free(wptr);

    return 0;
}


static int32
cyVoiceE_dict_read(cyVoiceE_modle_memory * fp, dict_t * d)
{
    cyVoiceE_lineiter_t *li;
    char **wptr;
    s3cipid_t *p;
    int32 lineno, nwd;
    s3wid_t w;
    int32 i, maxwd;
    size_t stralloc, phnalloc;

    maxwd = 512;
    p = (s3cipid_t *) ckd_calloc(maxwd + 4, sizeof(*p));
    wptr = (char **) ckd_calloc(maxwd, sizeof(char *)); /* Freed below */

    lineno = 0;
    stralloc = phnalloc = 0;
    for (li = cyVoiceE_lineiter_start(fp); li; li = cyVoiceE_lineiter_next(li)) {
        lineno++;
        if (0 == strncmp(li->buf, "##", 2)
            || 0 == strncmp(li->buf, ";;", 2))
            continue;

        if ((nwd = str2words(li->buf, wptr, maxwd)) < 0) {
            /* Increase size of p, wptr. */
            nwd = str2words(li->buf, NULL, 0);
            assert(nwd > maxwd); /* why else would it fail? */
            maxwd = nwd;
            p = (s3cipid_t *) ckd_realloc(p, (maxwd + 4) * sizeof(*p));
            wptr = (char **) ckd_realloc(wptr, maxwd * sizeof(*wptr));
        }

        if (nwd == 0)           /* Empty line */
            continue;
        /* wptr[0] is the word-string and wptr[1..nwd-1] the pronunciation sequence */
        if (nwd == 1) {
            E_ERROR("Line %d: No pronunciation for word '%s'; ignored\n",
                    lineno, wptr[0]);
            continue;
        }


        /* Convert pronunciation string to CI-phone-ids */
        for (i = 1; i < nwd; i++) {
            p[i - 1] = dict_ciphone_id(d, wptr[i]);
            if (NOT_S3CIPID(p[i - 1])) {
                E_ERROR("Line %d: Phone '%s' is mising in the acoustic model; word '%s' ignored\n",
                        lineno, wptr[i], wptr[0]);
                break;
            }
        }

        if (i == nwd) {         /* All CI-phones successfully converted to IDs */
            w = dict_add_word(d, wptr[0], p, nwd - 1);
            if (NOT_S3WID(w))
                E_ERROR
                    ("Line %d: Failed to add the word '%s' (duplicate?); ignored\n",
                     lineno, wptr[0]);
            else {
                stralloc += strlen(d->word[w].word);
                phnalloc += d->word[w].pronlen * sizeof(s3cipid_t);
            }
        }
    }
    E_INFO("Allocated %d KiB for strings, %d KiB for phones\n",
           (int)stralloc / 1024, (int)phnalloc / 1024);
    ckd_free(p);
    ckd_free(wptr);

	cyVoiceE_lineiter_free(li);

    return 0;
}

static int cyVoiceE_load_dic(acmod_t *acmod)
{
	cyVoiceE_modle_memory *fp;
	cyVoiceE_lineiter_t *li;
    int32 lineno = 0;
	char *p_str = NULL,*p_str_key,*p_str_value;
	int i = 0,n = 0;
	hash_table_t *phash = NULL;

	if ((fp = cyVoiceE_fopen(acmod->p_data_dict,acmod->dict_count)) == NULL) {
			E_ERROR_SYSTEM("Failed to cyVoiceE_load_dic '0x%x' for reading", acmod->p_data_dict);
    		return -1;
		}
	
	for (li = cyVoiceE_lineiter_start(fp); li; li = cyVoiceE_lineiter_next(li)) {
	    	if (0 != strncmp(li->buf, "##", 2) && 0 != strncmp(li->buf, ";;", 2))
				n++;
	}
	cyVoiceE_rewind(fp);

	//注意释放内存
	acmod->hash_buff_size = acmod->dict_count;
	p_str_key = acmod->p_hash_buff = (char *)ckd_malloc(acmod->hash_buff_size);//有n个词条就需要n个
	memset(p_str_key,0,acmod->hash_buff_size);


	//hash_table_new
	//hash_table_lookup
	//hash_table_enter
	//hash_table_free
	phash = acmod->cyVoiceE_hash_dic = hash_table_new(n,0);
	

	for(li = cyVoiceE_lineiter_start(fp); li; li = cyVoiceE_lineiter_next(li)) 
	{
		i = 0;
        if (0 == strncmp(li->buf, "##", 2) || 0 == strncmp(li->buf, ";;", 2))
            continue;


		p_str = li->buf;
		while ((*p_str) && isspace((unsigned char)(*p_str)))
            ++p_str;

		while (isspace((unsigned char)(p_str[i])) == NULL)
		{
			p_str_key[i] = p_str[i];
			i++;
		}

		p_str_value = p_str_key + i + 1;

		p_str = p_str + i;
		while ((*p_str) && isspace((unsigned char)(*p_str)))
            ++p_str;

		i = 0;
		while (((unsigned char)(p_str[i]) != 0x0a) && ((unsigned char)(p_str[i]) != 0x0))
		{
			p_str_value[i] = p_str[i];
			i++;
		}

        hash_table_enter(phash,p_str_key,p_str_value);

		p_str_key = p_str_value + i + 1;
    }

	cyVoiceE_lineiter_free(li);

	cyVoiceE_fclose(fp);

	return 0;
}
static int cyVoiceE_findWrodAndSavePhone(word_phone_t *pWord_phones,hash_table_t *phash,char *p_key,int index)
{
	char *value = NULL;
	char line[CYVOICEE_BUFF_SIZE] = {0};
	char ext[16] = {0};
	int ret = 0;
	char *p_key1 = p_key;

	memset(line,0,CYVOICEE_BUFF_SIZE);
	strcpy(line,p_key1);
	if(index != 0)
	{
		sprintf(ext,"(%d)",index);
		strcat(line,ext);

		p_key1 = line;
	}


	ret = hash_table_lookup(phash,p_key1,(void **)&value);
	if(ret == 0)
	{
		int phone_len = strlen(value) + 1;

		//如果音素链表没有创建，则创建并初始化
		if(pWord_phones == NULL)
		{
			pWord_phones = (word_phone_t *)ckd_malloc(sizeof(word_phone_t));
					
			pWord_phones->p_phone_1 = (char *)ckd_malloc(phone_len);
			memset(pWord_phones->p_phone_1,0,phone_len);
			strcpy(pWord_phones->p_phone_1,value);

			pWord_phones->p_phone_2 = NULL;
			pWord_phones->p_phone_3 = NULL;
			pWord_phones->p_phone_4 = NULL;
			pWord_phones->p_phone_5 = NULL;
			pWord_phones->next = NULL;

		}
		else
		{
			char *p = NULL;
			if(pWord_phones->p_phone_1 == NULL)
			{
				p = pWord_phones->p_phone_1 = (char *)ckd_malloc(phone_len);
			}
			else if(pWord_phones->p_phone_2 == NULL)
			{
				p = pWord_phones->p_phone_2 = (char *)ckd_malloc(phone_len);
			}
			else if(pWord_phones->p_phone_3 == NULL)
			{
				p = pWord_phones->p_phone_3 = (char *)ckd_malloc(phone_len);
			}
			else if(pWord_phones->p_phone_4 == NULL)
			{
				p = pWord_phones->p_phone_4 = (char *)ckd_malloc(phone_len);
			}
			else if(pWord_phones->p_phone_5 == NULL)
			{
				p = pWord_phones->p_phone_5 = (char *)ckd_malloc(phone_len);
			}


			memset(p,0,phone_len);
			strcpy(p,value);
		}


		//继续查找下一层
		index++;
		cyVoiceE_findWrodAndSavePhone(pWord_phones,phash,p_key,index);
	}

	return ret;

}
static int cyVoiceE_createOneLineDicToBuff(word_phone_t *pWord_phones,char *src,char *dst,int *dst_index)
{
	word_phone_t *pWord_phones_temp;
	int len = 0,x = 0,y = 0,max = 0,i,j,z,h,x0,max_len = 1,start,index;
	char *p_phones[1024] = {NULL};
	int phonesNumber[256] = {0};
	char ***p_group;
	char *p_temp;

	for(pWord_phones_temp = pWord_phones;pWord_phones_temp != NULL;pWord_phones_temp = pWord_phones_temp->next)
	{
		x = 0;
		if(pWord_phones_temp->p_phone_1 != NULL)
		{
			x++;
			p_phones[max] = pWord_phones_temp->p_phone_1;
			max++;
		}
		if(pWord_phones_temp->p_phone_2 != NULL)
		{
			x++;
			p_phones[max] = pWord_phones_temp->p_phone_2;
			max++;
		}
		if(pWord_phones_temp->p_phone_3 != NULL)
		{
			x++;
			p_phones[max] = pWord_phones_temp->p_phone_3;
			max++;
		}
		if(pWord_phones_temp->p_phone_4 != NULL)
		{
			x++;
			p_phones[max] = pWord_phones_temp->p_phone_4;
			max++;
		}
		if(pWord_phones_temp->p_phone_5 != NULL)
		{
			x++;
			p_phones[max] = pWord_phones_temp->p_phone_5;
			max++;
		}

		if(x == 0)
			return -1;


		max_len = max_len * x;

		phonesNumber[len] = x;

		len++;
	}

	if(len == 0)
		return -1;
	else if(len == 1)
	{
		//如果只有一个词，
		for(i = 0;i < phonesNumber[0];i++)
		{
			x = *dst_index;
			p_temp = &(dst[x]);

			strcpy(p_temp,src);
			x = strlen(p_temp);
			p_temp[x] = 0x09;

			strcat(p_temp,p_phones[i]);
			x = strlen(p_temp);
			p_temp[x] = 0x0a;

			*dst_index = *dst_index + x + 1;
		}
	}
	else
	{
		p_group = (char ***)ckd_calloc_2d(max_len,len,sizeof(char **));

		j = max_len;
		start = 0;
		h = 1;
		//如果有多个词组成
		//遍历每种可能
		for(i = 0;i < len;i++)//多个音素词组
		{
			index = 0;
			j = max_len / phonesNumber[i] / h;//

			//当前节点音素集合的循环次数
			for(x = 0;x < j;x++)
			{
				//同一个节点音素个数
				for(z = 0;z < phonesNumber[i];z++)
				{
					//相同音素循环的次数
					for(x0 = 0;x0 < h;x0++)
					{
						p_group[index][i] = p_phones[start + z];
						index++;
					}
				}

				
			}
			
			start = start + phonesNumber[i];
			h = h * phonesNumber[i];
		}


		//处理好组合，生成文本
		for(i = 0;i < max_len;i++)
		{
			x = *dst_index;
			p_temp = &(dst[x]);

			strcpy(p_temp,src);
			x = strlen(p_temp);
			p_temp[x] = 0x09;

			for(j = 0;j < len;j++)
			{
				strcat(p_temp,p_group[i][j]);
				x = strlen(p_temp);
				p_temp[x] = 0x20;
			}
			x = strlen(p_temp);
			p_temp[x - 1] = 0x0a;

			*dst_index = *dst_index + x;
		}

		if(p_group != NULL)
			ckd_free_2d_ptr(p_group);
	}
	

	return 0;
}
static void cyVoiceE_freeWord_phones(word_phone_t *pWord_phones)
{
	word_phone_t *pWord_phones_temp,*pWord_phones_temp1;
	for(pWord_phones_temp = pWord_phones_temp1 = pWord_phones;pWord_phones_temp != NULL;)
	{
		if(pWord_phones_temp->p_phone_1 != NULL)
		{
			ckd_free(pWord_phones_temp->p_phone_1);
		}
		else if(pWord_phones_temp->p_phone_2 != NULL)
		{
			ckd_free(pWord_phones_temp->p_phone_2);
		}
		else if(pWord_phones_temp->p_phone_3 != NULL)
		{
			ckd_free(pWord_phones_temp->p_phone_3);
		}
		else if(pWord_phones_temp->p_phone_4 != NULL)
		{
			ckd_free(pWord_phones_temp->p_phone_4);
		}
		else if(pWord_phones_temp->p_phone_5 != NULL)
		{
			ckd_free(pWord_phones_temp->p_phone_5);
		}

		pWord_phones_temp = pWord_phones_temp->next;

		if(pWord_phones_temp1 != NULL)
			ckd_free(pWord_phones_temp1);

		pWord_phones_temp1 = pWord_phones_temp;
	}
}

static int cyVoiceE_createOneLineDic(acmod_t *acmod,unsigned char *src,unsigned char *dst,int *dst_index)
{
	int ret = -9,r;
	int src_len = strlen((const char *)src);
	int i = 0,len = 0,last = 0,phone_len = 0;
	//
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line1[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char *p_temp = NULL;
	char *value = NULL;
	char *p_key = NULL;
	hash_table_t *phash = acmod->cyVoiceE_hash_dic;
	word_phone_t *pWord_phones;
	word_phone_t *pWord_phones_temp;

	if(acmod->p_cyVoiceE_word_phone == NULL)
	{
		acmod->p_cyVoiceE_word_phone = (word_phone_t *)ckd_malloc(sizeof(word_phone_t));

		pWord_phones_temp = pWord_phones = acmod->p_cyVoiceE_word_phone;

		pWord_phones_temp->p_phone_1 = NULL;
		pWord_phones_temp->p_phone_2 = NULL;
		pWord_phones_temp->p_phone_3 = NULL;
		pWord_phones_temp->p_phone_4 = NULL;
		pWord_phones_temp->p_phone_5 = NULL;
		pWord_phones_temp->next = NULL;

	}
	

	while(i < src_len)
	{
		len = src_len - i;
		memcpy(line,src,src_len);
		p_temp = line + i;

		//如果 当前节点已经使用过，则申请下一个节点
		if((pWord_phones_temp->p_phone_1 != NULL) && (pWord_phones_temp->next == NULL))
		{
			pWord_phones_temp->next = (word_phone_t *)ckd_malloc(sizeof(word_phone_t));

			pWord_phones_temp = pWord_phones_temp->next;

			pWord_phones_temp->p_phone_1 = NULL;
			pWord_phones_temp->p_phone_2 = NULL;
			pWord_phones_temp->p_phone_3 = NULL;
			pWord_phones_temp->p_phone_4 = NULL;
			pWord_phones_temp->p_phone_5 = NULL;
			pWord_phones_temp->next = NULL;

		}

		//查找对应词的音素
		for(last = len;last > 0 ;last--)
		{
			p_temp[last] = 0;
			p_key = (char *)&(p_temp[0]);

			r = cyVoiceE_findWrodAndSavePhone(pWord_phones_temp,phash,p_key,0);
			if(r >= 0)
				break;
			
		}

		//检查当前的词是否找到，如果等于零，则视为为找到，返回异常
		if(last == 0)
			return -1;

		i = i + last;
	}

	//存储一行数据到缓冲区
	ret = cyVoiceE_createOneLineDicToBuff(pWord_phones,(char *)src,(char *)dst,dst_index);


	if(acmod->p_cyVoiceE_word_phone != NULL)
	{
		cyVoiceE_freeWord_phones(acmod->p_cyVoiceE_word_phone);
		acmod->p_cyVoiceE_word_phone = NULL;
	}


	return ret;
}


static int cyVoiceE_createDic(acmod_t *acmod,unsigned char *src,unsigned char *dst,int dst_len)
{
	int count = 0,i = 0,j = 0,x = 0,len = 0,ret = 0;
	unsigned char line[CYVOICEE_BUFF_SIZE] = {0};
	unsigned char line1[CYVOICEE_BUFF_SIZE] = {0};
	int in_count = strlen((const char *)src);
	char *p_start = NULL,*p_end = NULL;

	//循环取出源数据的每一行，然后对每一行添加对应的音素，注意可能有多音字的情况，会出现一行源数据，对应两行以上的标注成的新词
	while(1)
	{
		//读取一行的内容，对其内容做分词
		memset(line,0,sizeof(line));
		memset(line1,0,sizeof(line1));
		x= 0;
		while ((unsigned char)(src[i]) != 0x0a)
		{
			line[x++] = src[i++];

			if(i >= in_count)
				return -1;
		}

		

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

		//创建一行新词
		ret = cyVoiceE_createOneLineDic(acmod,line1,dst,&count);
		if(ret < 0)
			return -2;


		while ((unsigned char)(src[i]) == 0x0a)
		{
			i++;
		}

		if(i >= in_count)
			break;

	}

	if(count < dst_len)
		return count;
	else
		return -9;

}

#ifdef WIN32

void cyVoiceE_saveCreateNewDic(char *fileName,char *p_in,unsigned intlen)
{

	FILE *fh_log = fopen(fileName, "wb");
	if(fh_log == NULL)
		return;
	
	fwrite(p_in, intlen, 1, fh_log);


	if(fh_log)
		fclose(fh_log);
	
}

#endif

//释放新创建的词典相关资源
int cyVoiceE_releaseDicRes(acmod_t *acmod)
{
	cmd_ln_t *config = acmod->config;
	//acmod->p_data_dict = acmod->p_data_new_dict
	//使用完之后已经释放了空间
//	if(acmod->p_data_new_dict != NULL)
//		ckd_free(acmod->p_data_new_dict);




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

	return 0;
}


static int cyVoiceE_releaseDicMemory(cmd_ln_t *config, acmod_t *acmod)
{

	if(acmod->p_hash_buff != NULL)
	{
		ckd_free(acmod->p_hash_buff);
		acmod->p_hash_buff = NULL;
	}


	hash_table_free(acmod->cyVoiceE_hash_dic);

	return 0;
}

static int cyVoiceE_createNewDic(cmd_ln_t *config, acmod_t *acmod)
{
	char const *custom_jsgf = cmd_ln_str_r(config, "-Custom_jsgf");
	char const *custom_ngram = cmd_ln_str_r(config, "-Custom_ngram");
	char const *custom_fsg = cmd_ln_str_r(config, "-Custom_fsg");
	int custom_flag = (int)cmd_ln_int_r(config, "-Custom_flag");
	int ret = -9;

	//加载词典到哈希表,为后续分词或者创建新词典使用
	cyVoiceE_load_dic(acmod);

	//如果选择使用分词，则不用闯进新的词典--缓冲区
	if(custom_flag != 2)
	{
		return -1;
	}

	//读取用户添加的语法--如果遇到不能识别的词，在词典尾部加入新词，并标注
	//占时不考虑加新词，如果自动分词不合适，导致影响识别率，再做这一步
	//创建新词典缓冲区，填写新词典内容
	if((custom_jsgf != NULL) || (custom_ngram != NULL) || (custom_fsg != NULL))
	{
		//
		if(custom_jsgf != NULL)
		{
			int len = strlen(custom_jsgf);

			//计算并申请新词典的存储缓冲区
			len = len * 5;

			acmod->p_data_new_dict = (char *)ckd_malloc(len);
			if(acmod->p_data_new_dict == NULL)
				return -2;
			memset(acmod->p_data_new_dict,0,len);

			//分行创建新的词到新词典缓冲区，返回新词典缓冲区的长度
			ret = cyVoiceE_createDic(acmod,(unsigned char *)custom_jsgf,(unsigned char *)(acmod->p_data_new_dict),len);
			if(ret < 0)
			{
#ifdef WIN32
		cyVoiceE_saveCreateNewDic(".//lm//new_dic.txt",(char *)(acmod->p_data_new_dict),len);
#endif
				ckd_free(acmod->p_data_new_dict);
				return -3;
			}

			

			acmod->p_data_dict = acmod->p_data_new_dict;
			acmod->dict_count = ret;

#ifdef WIN32
		cyVoiceE_saveCreateNewDic(".//lm//new_dic.txt",(char *)(acmod->p_data_dict),acmod->dict_count);
#endif

			//释放大辞典资源
			cyVoiceE_releaseDicMemory(config,acmod);


			//重新加载新的词典
			cyVoiceE_load_dic(acmod);
		}
	}


	

	

	return ret;
}




int
dict_write(dict_t *dict, char const *filename, char const *format)
{
    FILE *fh;
    int i;

    if ((fh = fopen(filename, "w")) == NULL) {
        E_ERROR_SYSTEM("Failed to open '%s'", filename);
        return -1;
    }
    for (i = 0; i < dict->n_word; ++i) {
        char *phones;
        int j, phlen;
        if (!dict_real_word(dict, i))
            continue;
        for (phlen = j = 0; j < dict_pronlen(dict, i); ++j)
            phlen += strlen(dict_ciphone_str(dict, i, j)) + 1;
        phones = ckd_calloc(1, phlen);
        for (j = 0; j < dict_pronlen(dict, i); ++j) {
            strcat(phones, dict_ciphone_str(dict, i, j));
            if (j != dict_pronlen(dict, i) - 1)
                strcat(phones, " ");
        }
        fprintf(fh, "%-30s %s\n", dict_wordstr(dict, i), phones);
        ckd_free(phones);
    }
    fclose(fh);
    return 0;
}

int cyVoiceE_dic_pre(cmd_ln_t *config, acmod_t *acmod)
{
	int ret = 0;
	char const *isCyVoiceE = cmd_ln_str_r(config, "-CyVoiceE");
	 
	cyVoiceE_modle *p_cyVoiceE_modle_dict = NULL,*p_cyVoiceE_modle_fdict = NULL;
	char *p_data_dict = NULL,*p_data_fdict = NULL,*p_data = NULL;
	

    

	if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
	{
		char const *dictfile = NULL, *fillerfile = NULL;
		unsigned int count = 0;

		if (config) {
			dictfile = cmd_ln_str_r(config, "-dict");
			fillerfile = cmd_ln_str_r(config, "-fdict");
		}

		if (dictfile) {
			p_cyVoiceE_modle_dict = cyVoiceE_init((char *)dictfile);
			p_data_dict = (char *)cyVoiceE_decrypt(p_cyVoiceE_modle_dict,&count);
		}
		else
		{
			E_ERROR_SYSTEM("Failed to open dictionary file '%s' for reading", dictfile);
			return -1;
		}

		acmod->p_cyVoiceE_modle_dict = p_cyVoiceE_modle_dict;
		acmod->p_data_dict = p_data_dict;
		acmod->dict_count = count;



		if (fillerfile) {
			p_cyVoiceE_modle_fdict = cyVoiceE_init((char *)fillerfile);
			p_data_fdict = (char *)cyVoiceE_decrypt(p_cyVoiceE_modle_fdict,&count);
		}
		else
		{
			E_ERROR_SYSTEM("Failed to open fillerfile '%s' for reading", fillerfile);
			return -1;
		}

		acmod->p_cyVoiceE_modle_fdict = p_cyVoiceE_modle_fdict;
		acmod->p_data_fdict = p_data_fdict;
		acmod->fdict_count = count;


	}
	else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) )
	{
		unsigned int count = 0;
		if (acmod->cyVoiceE_dic != NULL) {
			
			p_data_dict = (char *)acmod->cyVoiceE_dic;
			count = acmod->cyVoiceE_dic_len;
			p_data_dict = (char *)cyVoiceE_decrypt_memory((unsigned char *)p_data_dict,&count);
		}
		else
		{
			E_ERROR_SYSTEM("Failed to open dic cyVoiceE_dic '0x%x", acmod->cyVoiceE_dic);
			return -1;
		}

		acmod->p_cyVoiceE_modle_dict = p_cyVoiceE_modle_dict;
		acmod->p_data_dict = p_data_dict;
		acmod->dict_count = count;

		if (acmod->cyVoiceE_noisedict != NULL) {
			p_data_fdict = (char *)acmod->cyVoiceE_noisedict;
			count = acmod->cyVoiceE_noisedict_len;
			p_data_fdict = (char *)cyVoiceE_decrypt_memory((unsigned char *)p_data_fdict,&count);
		}
		else
		{
			E_ERROR_SYSTEM("Failed to open cyVoiceE_noisedict '0x%x", acmod->cyVoiceE_noisedict);
			return -1;
		}

		acmod->p_cyVoiceE_modle_fdict = p_cyVoiceE_modle_fdict;
		acmod->p_data_fdict = p_data_fdict;
		acmod->fdict_count = count;


		/* 创建新的词典 */
		cyVoiceE_createNewDic(config,acmod);

	}



	return ret;

}
int cyVoiceE_dic_pre_end(cmd_ln_t *config, acmod_t *acmod)
{
	int ret = 0;
	char const *isCyVoiceE = cmd_ln_str_r(config, "-CyVoiceE");
	if((strcmp(isCyVoiceE,"sphinx") == 0) || (strcmp(isCyVoiceE,"CyVoiceE") == 0))
	{
		cyVoiceE_uninit(acmod->p_cyVoiceE_modle_dict);
		cyVoiceE_uninit(acmod->p_cyVoiceE_modle_fdict);
	}
	else if((strcmp(isCyVoiceE,"CyVoiceE_jsgf") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_fsg") == 0) || (strcmp(isCyVoiceE,"CyVoiceE_ngram") == 0) )
	{
		cyVoiceE_decrypt_memory_free((unsigned char *)acmod->p_data_dict);
		cyVoiceE_decrypt_memory_free((unsigned char *)acmod->p_data_fdict);
	}

	return ret;

}

dict_t *
dict_init(cmd_ln_t *config, acmod_t *acmod)
{
	bin_mdef_t * mdef = acmod->mdef;
    cyVoiceE_modle_memory *fp, *fp2;
    int32 n;
    cyVoiceE_lineiter_t *li;
    dict_t *d;
    s3cipid_t sil;

	

	if(cyVoiceE_dic_pre(config,acmod) < 0)
		return NULL;
    
	/*
	* First obtain #words in dictionary (for hash table allocation).
	* Reason: The PC NT system doesn't like to grow memory gradually.  Better to allocate
	* all the required memory in one go.
	*/
	fp = NULL;
	n = 0;
	if (acmod->p_data_dict != NULL) {

		if ((fp = cyVoiceE_fopen(acmod->p_data_dict,acmod->dict_count)) == NULL) {
			cyVoiceE_dic_pre_end(config,acmod);
			E_ERROR_SYSTEM("Failed to open dictionary file '0x%x' for reading", acmod->p_data_dict);
    		return NULL;
		}
		for (li = cyVoiceE_lineiter_start(fp); li; li = cyVoiceE_lineiter_next(li)) {
	    	if (0 != strncmp(li->buf, "##", 2) && 0 != strncmp(li->buf, ";;", 2))
				n++;
		}


		cyVoiceE_lineiter_free(li);
		cyVoiceE_rewind(fp);
	}

	fp2 = NULL;
	if (acmod->p_data_fdict != NULL) {

		if ((fp2 = cyVoiceE_fopen(acmod->p_data_fdict,acmod->fdict_count)) == NULL) {
			cyVoiceE_dic_pre_end(config,acmod);
			E_ERROR_SYSTEM("Failed to open filler dictionary file '0x%x' for reading", acmod->p_data_fdict);
			cyVoiceE_fclose(fp);
			return NULL;
		}
		for (li = cyVoiceE_lineiter_start(fp2); li; li = cyVoiceE_lineiter_next(li)) {
	    	if (0 != strncmp(li->buf, "##", 2) && 0 != strncmp(li->buf, ";;", 2))
				n++;
		}
		cyVoiceE_lineiter_free(li);
		cyVoiceE_rewind(fp2);
	}

	/*
		* Allocate dict entries.  HACK!!  Allow some extra entries for words not in file.
		* Also check for type size restrictions.
		*/
	d = (dict_t *) ckd_calloc(1, sizeof(dict_t));       /* freed in dict_free() */
	d->refcnt = 1;
	d->max_words = (n + S3DICT_INC_SZ < MAX_S3WID) ? n + S3DICT_INC_SZ : MAX_S3WID;
	if (n >= MAX_S3WID) {
		E_ERROR("Number of words in dictionaries (%d) exceeds limit (%d)\n", n,
				MAX_S3WID);
		cyVoiceE_fclose(fp);
		cyVoiceE_fclose(fp2);
		cyVoiceE_dic_pre_end(config,acmod);
		ckd_free(d);
		return NULL;
	}

	E_INFO("Allocating %d * %d bytes (%d KiB) for word entries\n",
			d->max_words, sizeof(dictword_t),
			d->max_words * sizeof(dictword_t) / 1024);
	d->word = (dictword_t *) ckd_calloc(d->max_words, sizeof(dictword_t));      /* freed in dict_free() */
	d->n_word = 0;
	if (mdef)
		d->mdef = bin_mdef_retain(mdef);

	/* Create new hash table for word strings; case-insensitive word strings */
	if (config && cmd_ln_exists_r(config, "-dictcase"))
		d->nocase = cmd_ln_boolean_r(config, "-dictcase");
	d->ht = hash_table_new(d->max_words, d->nocase);

	
	Iava_SysPrintf("dict_init +++++ d->max_words = %d",d->max_words);

	/* Digest main dictionary file */
	if (fp) {
		E_INFO("Reading main dictionary: \n");
		cyVoiceE_dict_read(fp, d);
		cyVoiceE_fclose(fp);

		E_INFO("%d words read\n", d->n_word);
	}

	/* Now the filler dictionary file, if it exists */
	d->filler_start = d->n_word;
	if (fp2) {
		E_INFO("Reading filler dictionary: \n");
		cyVoiceE_dict_read(fp2, d);
		cyVoiceE_fclose(fp2);

		E_INFO("%d words read\n", d->n_word - d->filler_start);
	}





	cyVoiceE_dic_pre_end(config,acmod);





    if (mdef)
        sil = bin_mdef_silphone(mdef);
    else
        sil = 0;
    if (dict_wordid(d, S3_START_WORD) == BAD_S3WID) {
        dict_add_word(d, S3_START_WORD, &sil, 1);
    }
    if (dict_wordid(d, S3_FINISH_WORD) == BAD_S3WID) {
        dict_add_word(d, S3_FINISH_WORD, &sil, 1);
    }
    if (dict_wordid(d, S3_SILENCE_WORD) == BAD_S3WID) {
        dict_add_word(d, S3_SILENCE_WORD, &sil, 1);
    }

    d->filler_end = d->n_word - 1;

    /* Initialize distinguished word-ids */
    d->startwid = dict_wordid(d, S3_START_WORD);
    d->finishwid = dict_wordid(d, S3_FINISH_WORD);
    d->silwid = dict_wordid(d, S3_SILENCE_WORD);

    if ((d->filler_start > d->filler_end)
        || (!dict_filler_word(d, d->silwid))) {
        E_ERROR("Word '%s' must occur (only) in filler dictionary\n",
                S3_SILENCE_WORD);
        dict_free(d);
        return NULL;
    }

    /* No check that alternative pronunciations for filler words are in filler range!! */

    return d;
}


s3wid_t
dict_wordid(dict_t *d, const char *word)
{
    int32 w;

    assert(d);
    assert(word);

    if (hash_table_lookup_int32(d->ht, word, &w) < 0)
        return (BAD_S3WID);
    return w;
}


int
dict_filler_word(dict_t *d, s3wid_t w)
{
    assert(d);
    assert((w >= 0) && (w < d->n_word));

    w = dict_basewid(d, w);
    if ((w == d->startwid) || (w == d->finishwid))
        return 0;
    if ((w >= d->filler_start) && (w <= d->filler_end))
        return 1;
    return 0;
}

int
dict_real_word(dict_t *d, s3wid_t w)
{
    assert(d);
    assert((w >= 0) && (w < d->n_word));

    w = dict_basewid(d, w);
    if ((w == d->startwid) || (w == d->finishwid))
        return 0;
    if ((w >= d->filler_start) && (w <= d->filler_end))
        return 0;
    return 1;
}


int32
dict_word2basestr(char *word)
{
    int32 i, len;

    len = strlen(word);
    if (word[len - 1] == ')') {
        for (i = len - 2; (i > 0) && (word[i] != '('); --i);

        if (i > 0) {
            /* The word is of the form <baseword>(...); strip from left-paren */
            word[i] = '\0';
            return i;
        }
    }

    return -1;
}

dict_t *
dict_retain(dict_t *d)
{
    ++d->refcnt;
    return d;
}

int
dict_free(dict_t * d)
{
    int i;
    dictword_t *word;

    if (d == NULL)
        return 0;
    if (--d->refcnt > 0)
        return d->refcnt;

    /* First Step, free all memory allocated for each word */
    for (i = 0; i < d->n_word; i++) {
        word = (dictword_t *) & (d->word[i]);
        if (word->word)
            ckd_free((void *) word->word);
        if (word->ciphone)
            ckd_free((void *) word->ciphone);
    }

    if (d->word)
        ckd_free((void *) d->word);
    if (d->ht)
        hash_table_free(d->ht);
    if (d->mdef)
        bin_mdef_free(d->mdef);
    ckd_free((void *) d);

    return 0;
}

void
dict_report(dict_t * d)
{
    E_INFO_NOFN("Initialization of dict_t, report:\n");
    E_INFO_NOFN("Max word: %d\n", d->max_words);
    E_INFO_NOFN("No of word: %d\n", d->n_word);
    E_INFO_NOFN("\n");
}
