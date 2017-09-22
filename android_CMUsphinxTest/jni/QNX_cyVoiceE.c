#ifndef __QNX_CYVOICEe_C_
#define __QNX_CYVOICEe_C_

#include <stdio.h>
#include <stdarg.h>
#include <time.h>

#include "pocketsphinx.h"
#include "QNX_cyVoiceE.h"

extern cmd_ln_t *parse_options(cmd_ln_t *cmdln, const arg_t *defn, int32 argc, char* argv[], int32 strict);


//static const char *CYVOICE_VERSION = "QNX cyVoiceE_V1.2.1 -RELEASE";
static const char *CYVOICE_VERSION = "QNX cyVoiceE_V1.2.1 -DEBUG";
/*
���� :		�������ʶ������İ汾��

�������


����ֵ		�汾��
*/
Public char *cyVoiceE_get_version(void)
{

	return CYVOICE_VERSION;
}



static int cyVoiceE_check_license(void)
{
	struct tm *a;
	 time_t  tloc;
	 time(&tloc);

	 a = localtime(&tloc);
//	 printf("\n tm.tm_year = %d",a->tm_year);
//	 printf("\n tm.tm_mon = %d",a->tm_mon);
//	 printf("\n tm.tm_mday = %d",a->tm_mday);

	 if((a->tm_year + 1900) > 2015)
	 {
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 printf("\n Error!!!	This is a debug Version!!! ");
		 return -1;
	 }
	 else
	 {
		 printf("\n version: %s\n",cyVoiceE_get_version());
		 return 0;
	 }
}


/*
���� :		����ʶ������б���Դ�ͷ�

�������	 cyVoiceE_config c[in] ���ñ�

����ֵ		�ɹ�		=0
			����		>0
*/
Public int cyVoiceE_config_free(cyVoiceE_config c)
{
	cmd_ln_t *config = (cmd_ln_t *)c;

	return cmd_ln_free_r(config);
}


/*
���� :		����ʶ�������ʼ��

�������	 cyVoiceE_config c[in] ���ñ�

����ֵ		�ɹ�		>0
			����		=0
*/
Public cyVoiceE_decoder cyVoiceE_decoder_init(cyVoiceE_config c)
{
	cmd_ln_t *config = (cmd_ln_t *)c;
	ps_decoder_t *d = 0;

	if(cyVoiceE_check_license() < 0)
	{
		return 0;
	}
	else
	{

		d = ps_init(config);

		return (cyVoiceE_decoder)d;
	}
}



/*
���� :		��������ʶ�����棬����ʼһ������ʶ��

�������	 cyVoiceE_decoder d[in] ʶ������ʵ��

����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_start(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_start_utt(ps,NULL);

	return ret;
}

/*
���� :		����ʶ�����洦��ʶ������

�������	 cyVoiceE_decoder d[in] ʶ������ʵ��
		 short *data [in]		��ʶ����Ƶ����
		 unsigned int count[in] ���ݵĸ���

����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_process_raw(cyVoiceE_decoder d,short *data,unsigned int count)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_process_raw(ps,data,count,FALSE, FALSE);

	return ret;
}

/*
���� :		ֹͣ����ʶ�����棬��һ������ʶ�����

�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_end(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_end_utt(ps);

	return ret;
}


/*
���� :		���һ������ʶ��Ľ��

�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		>=0
			����		<0
*/
Public char * cyVoiceE_decoder_get_result(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;
	int32 score;
	char const *hyp, *uttid;

	hyp = ps_get_hyp(ps,&score, &uttid);

	return hyp;
}


/*
���� :		ֹͣ����ʶ�����棬��һ������ʶ�����

�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		=0
			����		>0
*/
Public int cyVoiceE_decoder_free(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_free(ps);

	return ret;
}




#endif
