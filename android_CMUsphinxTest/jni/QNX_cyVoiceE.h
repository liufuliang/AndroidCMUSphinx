#ifndef __QNX_CYVOICEe_H_
#define __QNX_CYVOICEe_H_

#include <stdio.h>
#include <stdarg.h>

#ifdef _WINDOWS

#ifdef TTS_ENGINE_EXPORTS
	#define Public __declspec(dllexport)
#else
	#define Public __declspec(dllimport)
#endif

#else

#define Public

#endif


typedef unsigned int	cyVoiceE_config;
typedef unsigned int	cyVoiceE_decoder;

#ifdef __cplusplus
extern "C" {
#endif


/*
功能 :		获得语音识别引擎的版本号
作者：		liufuliang
输入参数


返回值		版本号
*/
Public char *cyVoiceE_get_version(void);


/*
功能 :		语音识别参数列表初始化
作者：		liufuliang
输入参数	 [in] 参数列表 如下
		"-bestpath", "no",
		"-CyVoiceE","CyVoiceE_jsgf",

返回值		成功		>0
			错误		=0
*/
Public cyVoiceE_config cyVoiceE_config_init(cyVoiceE_config c, ...);


/*
功能 :		语音识别参数列表资源释放
作者：		liufuliang
输入参数	 cyVoiceE_config c[in] 配置表

返回值		成功		=0
			错误		>0
*/
Public int cyVoiceE_config_free(cyVoiceE_config c);


/*
功能 :		语音识别引擎初始化
作者：		liufuliang
输入参数	 cyVoiceE_config c[in] 配置表

返回值		成功		>0
			错误		=0
*/
Public cyVoiceE_decoder cyVoiceE_decoder_init(cyVoiceE_config c);



/*
功能 :		启动语音识别引擎，即开始一次语音识别
作者：		liufuliang
输入参数	 cyVoiceE_decoder d[in] 识别引擎实例

返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_start(cyVoiceE_decoder d);

/*
功能 :		语音识别引擎处理识别数据
作者：		liufuliang
输入参数	 cyVoiceE_decoder d[in] 识别引擎实例
		 short *data [in]		待识别音频数据
		 unsigned int count[in] 数据的个数

返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_process_raw(cyVoiceE_decoder d,short *data,unsigned int count);

/*
功能 :		停止语音识别引擎，即一次语音识别结束
作者：		liufuliang
输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		>=0
			错误		<0
*/
Public int cyVoiceE_decoder_end(cyVoiceE_decoder d);


/*
功能 :		获得一次语音识别的结果
作者：		liufuliang
输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		>=0
			错误		<0
*/
Public char * cyVoiceE_decoder_get_result(cyVoiceE_decoder d);


/*
功能 :		释放语音识别引擎
作者：		liufuliang
输入参数	 cyVoiceE_decoder d[in] 识别引擎实例


返回值		成功		=0
			错误		>0
*/
Public int cyVoiceE_decoder_free(cyVoiceE_decoder d);


#ifdef __cplusplus
} // End of extern "C"
#endif



#endif
