#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "sphinxbase/ckd_alloc.h"
#include "cyVoiceE_model.h"

#include "emu_sys_log.h"

#define NEW_MODEL_FILE_TITLE_LEN (1024)

static unsigned long get_file_size(const char *filename)
{    
	unsigned long size;
	FILE* fp = fopen( filename, "r" );
	if(fp==NULL)
	{
		printf("ERROR: Open file %s failed.\n", filename);
		return 0;
	}
	fseek( fp, 0L, SEEK_END );
	size = ftell(fp);
	fclose(fp);
	return size;
}
static unsigned char getKey(unsigned char *p_in)
{
	unsigned char ret = 0;
	unsigned int sum = 0;

	int i = 0;
	int len = strlen((char *)p_in);
	for(i = 0;i < len;i++)
	{
		sum = sum + p_in[i];
	}

	ret = sum & 0xff;
	return ret;
}

void cyVoiceE_encrypt(unsigned char *pin,unsigned int len,unsigned char flag)
{
	int i;
	for(i = 0;i < len;i++)
	{
		pin[i] = pin[i] ^ flag;
	}
}
static int cyVoiceE_loadFile(char *filename,unsigned char *ptr,unsigned int len,unsigned char *key)
{
	int read_len = 0,fileLen = 0;
	char title[NEW_MODEL_FILE_TITLE_LEN] = {0};
	char *p_temp = NULL,*p_temp1 = NULL;
	FILE *fh;
	fh = fopen(filename, "rb");
	if(fh == NULL)
		return -1;

	read_len = fread(title,1,0x10,fh);
	if(read_len != 0x10)
	{
		return -2;
	}
	
	fseek( fh, 0L, SEEK_SET );
	
	p_temp1 = strstr(title,"cyVoiceE_V");
	if(p_temp1 == NULL)//判断是否 cyVoiceE 模型
	{
		
		read_len = fread(ptr,1,len,fh);
		if(read_len != len)
			return -3;
			
		*key = 0;
	}
	else
	{
		read_len = fread(title,1,NEW_MODEL_FILE_TITLE_LEN,fh);
		if(read_len != NEW_MODEL_FILE_TITLE_LEN)
		{
			return -2;
		}
	
		// cyVoiceE 模型
		fileLen = len - NEW_MODEL_FILE_TITLE_LEN;
		
		read_len = fread(ptr,1,fileLen,fh);
		if(read_len != fileLen)
			return -3;
	
		
	
		p_temp = strstr(title,"\n");
		p_temp[0] = 0;
	
		*key = getKey(title);
	}

	
	if(fh)
		fclose(fh);
	

	return 0;
}

//初始化
cyVoiceE_modle * cyVoiceE_init(char *file)
{
	cyVoiceE_modle *p_handle = NULL;	
	unsigned int fileSize = 0,realfileSize = 0;
	if((file == NULL) || (strlen(file) >= cyVoiceE_modle_file_len))
		return NULL;
		
	realfileSize = get_file_size(file);
	//if(realfileSize > NEW_MODEL_FILE_TITLE_LEN)
	//	fileSize = realfileSize - NEW_MODEL_FILE_TITLE_LEN + 1;
	//else
		fileSize = realfileSize + 10;
		
	p_handle = (cyVoiceE_modle *)ckd_malloc(sizeof(cyVoiceE_modle));
	memset(p_handle,0,sizeof(cyVoiceE_modle));
	
	strcpy(p_handle->fileName,file);
	p_handle->fileSize = realfileSize;
	

	p_handle->ptr = (unsigned char *)ckd_malloc(fileSize);
	if(p_handle->ptr == NULL)
		cyVoiceE_uninit(p_handle);
		
	memset(p_handle->ptr,0xff,fileSize);

	cyVoiceE_loadFile(p_handle->fileName,p_handle->ptr,p_handle->fileSize,&p_handle->reserved);

	return p_handle;
}

//解密数据
unsigned char * cyVoiceE_decrypt(cyVoiceE_modle *handle,unsigned int *p_len)
{
	unsigned int len = handle->fileSize;
	if(handle->reserved != 0)
	{
		len = handle->fileSize - NEW_MODEL_FILE_TITLE_LEN;
		cyVoiceE_encrypt(handle->ptr,len,handle->reserved);
	}

	*p_len = len;
	return handle->ptr;
}

//逆初始化
void cyVoiceE_uninit(cyVoiceE_modle *handle)
{
	if(handle != NULL)
	{
		if(handle->ptr != NULL)
			ckd_free(handle->ptr);
			
		ckd_free(handle);
		
		handle = NULL;
	}
}




//解密数据
unsigned char * cyVoiceE_decrypt_memory(unsigned char *pin,unsigned int *buff_len)
{
	char title[NEW_MODEL_FILE_TITLE_LEN] = {0};
	char *p_temp = NULL;
	char key = 0;
	int len = 0;
	unsigned char *pout = pin + NEW_MODEL_FILE_TITLE_LEN;
	unsigned int real_len = 0;

	

	memcpy(title,pin,NEW_MODEL_FILE_TITLE_LEN);
	p_temp = strstr(title,"\n");
	p_temp[0] = 0;
	
	key = getKey((unsigned char *)title);
		
	len = *buff_len - NEW_MODEL_FILE_TITLE_LEN;
	
	real_len = len + 10;
	pout = (unsigned char *)ckd_malloc(real_len);
	memset(pout,0xff,real_len);
	memcpy(pout,(pin + NEW_MODEL_FILE_TITLE_LEN),len);
	cyVoiceE_encrypt(pout,len,key);

	*buff_len = len;
	
	return pout;
}
void cyVoiceE_decrypt_memory_free(unsigned char *pin)
{
	if(pin != NULL)
		ckd_free(pin);
}

/*
*成功，则返回第一个参数buf；
*在读字符时遇到end-of-file，则eof指示器被设置，如果还没读入任何字符就遇到这种情况，则buf保持原来的内容，返回NULL；
*如果发生读入错误，error指示器被设置，返回NULL，buf的值可能被改变
*/






cyVoiceE_modle_memory *cyVoiceE_fopen(char *p_data,unsigned int count)
{
	int i = 0;
	cyVoiceE_modle_memory *p_cyVoiceE_modle_memory = NULL;
	p_cyVoiceE_modle_memory = (cyVoiceE_modle_memory *)ckd_malloc(sizeof(cyVoiceE_modle_memory));
	if(p_cyVoiceE_modle_memory == NULL)
		return NULL;

	
	p_cyVoiceE_modle_memory->set = p_data;
	p_cyVoiceE_modle_memory->cur = p_data;	
	p_cyVoiceE_modle_memory->end = p_data + count;
	
	return p_cyVoiceE_modle_memory;
}
long cyVoiceE_ftell(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	long ret;
	if(p_cyVoiceE_modle_memory == NULL)
		return EOF;
	
	ret = p_cyVoiceE_modle_memory->cur - p_cyVoiceE_modle_memory->set;
	
	return ret;
}

int cyVoiceE_fseek(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory, long offset, int fromwhere)
{
	char *p_temp = NULL;
	if(p_cyVoiceE_modle_memory == NULL)
		return EOF;
		
	if(fromwhere == SEEK_SET)
	{
		p_temp = p_cyVoiceE_modle_memory->set;
	}
	else if(fromwhere == SEEK_CUR)
	{
		p_temp = p_cyVoiceE_modle_memory->cur;
	}
	else if(fromwhere == SEEK_END)
	{
		p_temp = p_cyVoiceE_modle_memory->end;
	}
	
	if((p_temp + offset) < p_cyVoiceE_modle_memory->set)
		return EOF;
	
	if((p_temp + offset) > p_cyVoiceE_modle_memory->end)
		return EOF;
		
	p_cyVoiceE_modle_memory->cur = p_temp + offset;
	
	return 0;
}

unsigned int cyVoiceE_fread ( void *buffer, unsigned int size, unsigned int count, cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	register char c;
	unsigned int i = 0,j = 0;
	char *p_buff = (char *)buffer;
	
	for(i = 0;i < count;)
	{
		for(j = 0;j < size;j++)
		{
			if(p_cyVoiceE_modle_memory->cur == p_cyVoiceE_modle_memory->end)
			{
				c = (char)(*(p_cyVoiceE_modle_memory->cur));
				goto end;
			}
			else
				c = (char)(*((p_cyVoiceE_modle_memory->cur)++));

			p_buff[i * size + j] = c;
		}
		
		i++;
	}
end:
	return i;
}

char *cyVoiceE_fgets(char *buf, int bufsize, cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	register int i = 0;
	register char c = '\n';
	register char *p_data = NULL;
	
	if(p_cyVoiceE_modle_memory == NULL)
		return NULL;
		
	p_data = p_cyVoiceE_modle_memory->cur;
	
	memset(buf,0x0,bufsize);
	
	for(i  = 0;i < (bufsize - 1);)
	{
		if(p_cyVoiceE_modle_memory->cur == p_cyVoiceE_modle_memory->end)
		{
			c = (char)(*(p_cyVoiceE_modle_memory->cur));
			break;
		}
		else
			c = (char)(*((p_cyVoiceE_modle_memory->cur)++));
		
		if(c == '\n')
		{
			buf[i++] = c;
			break;
		}

		
		buf[i++] = c;
	}
	
	if(i != 0)
		return buf;
	else
		return NULL;

}

int cyVoiceE_fgetc(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	register signed char c;
	int ret = 0;
	
	if(p_cyVoiceE_modle_memory == NULL)
		return EOF;
		
	if(p_cyVoiceE_modle_memory->cur == p_cyVoiceE_modle_memory->end)
		c = (signed char)(*(p_cyVoiceE_modle_memory->cur));
	else
		c = (signed char)(*((p_cyVoiceE_modle_memory->cur)++));

	ret = c;

	//Iava_SysPrintf("cyVoiceE_fgetc  %d = %d  %d",EOF,ret,c);
	
	return (int)c;
}

int cyVoiceE_ferror(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	if(p_cyVoiceE_modle_memory == NULL)
		return EOF;
		
	return 0;
}

void cyVoiceE_clearerr(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	if(p_cyVoiceE_modle_memory == NULL)
		return;
		
	// todo
}

void cyVoiceE_rewind(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	if(p_cyVoiceE_modle_memory == NULL)
		return;
		
	p_cyVoiceE_modle_memory->cur = p_cyVoiceE_modle_memory->set;
}

int cyVoiceE_fclose(cyVoiceE_modle_memory *p_cyVoiceE_modle_memory)
{
	if(p_cyVoiceE_modle_memory == NULL)
		return EOF;
	else
		ckd_free(p_cyVoiceE_modle_memory);
		
	return 0;
}