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
功能 :		获得语音识别引擎的版本号

输入参数


返回值		版本号
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
功能 :		语音识别参数列表资源释放

输入参数	 cyVoiceE_config c[in] 配置表

返回值		成功		=0
			错误		>0
*/
Public int cyVoiceE_config_free(cyVoiceE_config c)
{
	cmd_ln_t *config = (cmd_ln_t *)c;

	return cmd_ln_free_r(config);
}


/*
功能 :		语音识别引擎初始化

输入参数	 cyVoiceE_config c[in] 配置表

返回值		成功		>0
			错误		=0
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
功能 :		启动语音识别引擎，即开始一次语音识别

输入参数	 cyVoiceE_decoder d[in] 识别引擎实例

返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_start(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_start_utt(ps,NULL);

	return ret;
}

/*
功能 :		语音识别引擎处理识别数据

输入参数	 cyVoiceE_decoder d[in] 识别引擎实例
		 short *data [in]		待识别音频数据
		 unsigned int count[in] 数据的个数

返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_process_raw(cyVoiceE_decoder d,short *data,unsigned int count)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_process_raw(ps,data,count,FALSE, FALSE);

	return ret;
}

/*
功能 :		停止语音识别引擎，即一次语音识别结束

输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_end(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_end_utt(ps);

	return ret;
}


/*
功能 :		获得一次语音识别的结果

输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		>=0
			错误		<0
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
功能 :		停止语音识别引擎，即一次语音识别结束

输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		=0
			错误		>0
*/
Public int cyVoiceE_decoder_free(cyVoiceE_decoder d)
{
	ps_decoder_t *ps = (ps_decoder_t *)d;

	int ret = ps_free(ps);

	return ret;
}




#endif
