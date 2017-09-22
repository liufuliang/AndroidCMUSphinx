#ifndef __CYVOICEE_MODEL_H_
#define __CYVOICEE_MODEL_H_




#ifdef __cplusplus
extern "C" {
#endif

#define cyVoiceE_modle_file_len	(256)
typedef struct stcyVoiceE_modle_st
{
	char fileName[cyVoiceE_modle_file_len];
	unsigned int fileSize;
	unsigned char *ptr;
	unsigned char reserved;	
}cyVoiceE_modle;

typedef struct stcyVoiceE_modle_memory
{
	char *set;
	char *cur;
	char *end;
}cyVoiceE_modle_memory;

//初始化
cyVoiceE_modle * cyVoiceE_init(char *file);

//解密数据
unsigned char * cyVoiceE_decrypt(cyVoiceE_modle *handle,unsigned int *p_len);

//逆初始化
void cyVoiceE_uninit(cyVoiceE_modle *handle);




/*
*
*	模拟的文件操作函数
*
*/
cyVoiceE_modle_memory *cyVoiceE_fopen(char *p_data,unsigned int count);
long cyVoiceE_ftell(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
int cyVoiceE_fseek(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory, long offset, int fromwhere);
unsigned int cyVoiceE_fread( void *buffer, unsigned int size, unsigned int count, cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
char *cyVoiceE_fgets(char *buf, int bufsize, cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
int cyVoiceE_fgetc(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
int cyVoiceE_ferror(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
void cyVoiceE_clearerr(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
void cyVoiceE_rewind(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);
int cyVoiceE_fclose(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory);

/*
*
*	解密数据，处理数据
*
*/
unsigned char * cyVoiceE_decrypt_memory(unsigned char *pin,unsigned int *buff_len);
void cyVoiceE_decrypt_memory_free(unsigned char *pin);



#ifdef __cplusplus
}
#endif
#endif//__CYVOICEE_MODEL_H_