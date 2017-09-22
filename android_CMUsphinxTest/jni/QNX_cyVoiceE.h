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
���� :		�������ʶ������İ汾��
���ߣ�		liufuliang
�������


����ֵ		�汾��
*/
Public char *cyVoiceE_get_version(void);


/*
���� :		����ʶ������б��ʼ��
���ߣ�		liufuliang
�������	 [in] �����б� ����
		"-bestpath", "no",
		"-CyVoiceE","CyVoiceE_jsgf",

����ֵ		�ɹ�		>0
			����		=0
*/
Public cyVoiceE_config cyVoiceE_config_init(cyVoiceE_config c, ...);


/*
���� :		����ʶ������б���Դ�ͷ�
���ߣ�		liufuliang
�������	 cyVoiceE_config c[in] ���ñ�

����ֵ		�ɹ�		=0
			����		>0
*/
Public int cyVoiceE_config_free(cyVoiceE_config c);


/*
���� :		����ʶ�������ʼ��
���ߣ�		liufuliang
�������	 cyVoiceE_config c[in] ���ñ�

����ֵ		�ɹ�		>0
			����		=0
*/
Public cyVoiceE_decoder cyVoiceE_decoder_init(cyVoiceE_config c);



/*
���� :		��������ʶ�����棬����ʼһ������ʶ��
���ߣ�		liufuliang
�������	 cyVoiceE_decoder d[in] ʶ������ʵ��

����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_start(cyVoiceE_decoder d);

/*
���� :		����ʶ�����洦��ʶ������
���ߣ�		liufuliang
�������	 cyVoiceE_decoder d[in] ʶ������ʵ��
		 short *data [in]		��ʶ����Ƶ����
		 unsigned int count[in] ���ݵĸ���

����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_process_raw(cyVoiceE_decoder d,short *data,unsigned int count);

/*
���� :		ֹͣ����ʶ�����棬��һ������ʶ�����
���ߣ�		liufuliang
�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		>=0
			����		<0
*/
Public int cyVoiceE_decoder_end(cyVoiceE_decoder d);


/*
���� :		���һ������ʶ��Ľ��
���ߣ�		liufuliang
�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		>=0
			����		<0
*/
Public char * cyVoiceE_decoder_get_result(cyVoiceE_decoder d);


/*
���� :		�ͷ�����ʶ������
���ߣ�		liufuliang
�������	 cyVoiceE_decoder d[in] ʶ������ʵ��


����ֵ		�ɹ�		=0
			����		>0
*/
Public int cyVoiceE_decoder_free(cyVoiceE_decoder d);


#ifdef __cplusplus
} // End of extern "C"
#endif



#endif
