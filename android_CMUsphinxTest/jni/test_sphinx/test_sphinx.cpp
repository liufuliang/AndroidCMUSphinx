// test_sphinx_model.cpp : 定义控制台应用程序的入口点。
//

#ifdef WIN32
#include "stdafx.h"
#endif

#include<string.h>
#include<math.h>

#include "pocketsphinx.h"
#include "tinyxml.h"

#pragma comment(lib, "pocketsphinx.lib")
#pragma comment(lib, "sphinxbase.lib")


#define MAX_TEST_FILE_NAME_LEN	(256)
#define MAX_RESULT_LEN	(64)
typedef struct _st_test_data
{
	char file_name[MAX_TEST_FILE_NAME_LEN];
	char result[MAX_RESULT_LEN];
}ST_test_data;



#ifndef WIN32
#include <android/log.h>
#define shninx_SysPrintf(...) __android_log_print(ANDROID_LOG_DEBUG, "shninx_test", __VA_ARGS__)
#endif

ST_test_data *initTestDataFromXML(char *p_xmlFileName,int *p_count)
{
	ST_test_data *p_st_test_data = NULL;
	TiXmlDocument *doc = new TiXmlDocument(p_xmlFileName);  
	bool loadOkay = doc->LoadFile();
	if(loadOkay == false)
	{
		printf( "Could not load test file %s. Error='%s'. \n", p_xmlFileName,doc->ErrorDesc() );  
		return NULL;
	}

	// get dom root of 'phonebookdata.xml', here root should be 'phonebook'.  
	TiXmlElement* root = doc->RootElement();

	TiXmlNode*  node = root->FirstChild( "test_data_count" );
	if(node)
	{
		const char* test_data_count = node->ToElement()->GetText();  
		int data_count = atoi(test_data_count);

		if(data_count <= 0)
		{
			if(doc)
				delete doc;
			return NULL;
		}
		else
		{
			int c = *p_count = data_count;
			//计算空间申请内存
			int size = c * sizeof(ST_test_data);
			p_st_test_data =  (ST_test_data *)malloc(size);
			if(p_st_test_data)
			{
				//获得测试文件目录
				TiXmlNode*  node1 = root->FirstChild( "test_data_folder" );
				if(node1)
				{
					const char* test_data_folder = node1->ToElement()->GetText();
					int index = 0;
					//获得测试数据的目录
					for( TiXmlNode*  data = root->FirstChild( "test_data" );data;data = data->NextSibling( "test_data" ) )
					{
						TiXmlNode* item = data->FirstChild("file_name");
						if(item)
						{
							const char* name = item->ToElement()->GetText();
							strcpy(p_st_test_data[index].file_name,test_data_folder);
							strcat(p_st_test_data[index].file_name,name);

						}

						TiXmlNode* result = data->FirstChild("result");
						if(result)
						{
							const char* char_result = result->ToElement()->GetText();
							strcpy(p_st_test_data[index].result,char_result);
						}

						index++;
					}
				}
			}
		}
	}

	if(doc != NULL)
		free(doc);

	return p_st_test_data;

}

void unInitTestData(ST_test_data *p)
{
	if(p)
		free(p);
}

//去掉空格
void processHyp(char *p_in,char *p_out,char x)
{
	int c = strlen(p_in);
	int id = 0;
	for(int i = 0;i < c;i++)
	{
		if(p_in[i] != x)
		{
			p_out[id] = p_in[i];
			id++;
		}

	}
}

extern "C" int test_sphinx_model(char *p_xmlFileName,char *p_logFileName,char *p_hmm,char *p_lm,char *p_dic)
{
#ifndef WIN32
	shninx_SysPrintf("test_sphinx_model start:");
#endif
	char hyp_1[256] = {0};
	FILE *fh_log = fopen(p_logFileName, "w");
	if(fh_log == NULL)
		return 0;
		
#ifndef WIN32
	shninx_SysPrintf("test_sphinx_model start: 00000");
#endif

	int count = 0,succes = 0,succes1 = 0;
	ST_test_data *p_stTestData = initTestDataFromXML(p_xmlFileName,&count);
	if((p_stTestData != NULL) && (count > 0))
	{
		ps_decoder_t *ps;
		cmd_ln_t *config;
		FILE *fh;
		char const *hyp, *uttid;
		int16 buf[512];
		int rv;
		int32 score;

#ifndef WIN32
		shninx_SysPrintf("test_sphinx_model start: count = %d",count);
		shninx_SysPrintf("test_sphinx_model start: hmm = %s",p_hmm);
		shninx_SysPrintf("test_sphinx_model start: lm = %s",p_lm);
		shninx_SysPrintf("test_sphinx_model start: dict = %s",p_dic);
#endif

		//err_set_logfp(NULL);	//避免编译不过
		config = cmd_ln_init(NULL, ps_args(), TRUE,
						 "-hmm", p_hmm,
						 "-lm", p_lm,
						 "-dict",p_dic,
						 NULL);

		if (config == NULL)
		{
			#ifndef WIN32
				shninx_SysPrintf("test_sphinx_model: config == NULL");
			#endif
			
			if(fh_log)
				fclose(fh_log);
		
			return 0;
		}
		
				
		
		for(int i = 0;i < count;i++)
		{
			ps = ps_init(config);
			if (ps == NULL)
			{
				#ifndef WIN32
					shninx_SysPrintf("test_sphinx_model: ps == NULL");
				#endif
				
				
				if(fh_log)
					fclose(fh_log);
				return 0;
			}
		
			#ifndef WIN32
				shninx_SysPrintf("test_sphinx_model: i = %d",i);
				shninx_SysPrintf("test_sphinx_model: file_name = %s",p_stTestData[i].file_name);
				shninx_SysPrintf("test_sphinx_model: result = %s",p_stTestData[i].result);
			#endif

			//记录文件名
			fwrite(p_stTestData[i].file_name, strlen(p_stTestData[i].file_name), 1, fh_log);
			

			//打开测试数据
			fh = fopen(p_stTestData[i].file_name, "rb");
			if (fh == NULL) {
				#ifndef WIN32
					shninx_SysPrintf("Failed to open file :%s",p_stTestData[i].file_name);
				#else
					printf("Failed to open file :%s",p_stTestData[i].file_name);
				#endif

				fwrite(",", strlen(","), 1, fh_log);
				fwrite("Failed to open file", strlen("Failed to open file\n"), 1, fh_log);
				continue;
			}
			
#if 0
			rv = ps_decode_raw(ps, fh, "goforward", -1);
			if (rv < 0)
				continue;
			hyp = ps_get_hyp(ps, &score, &uttid);
			if (hyp == NULL)
				continue;

			printf("ps_decode_raw Recognized: %s\n", hyp);
			memset(hyp_1,0,sizeof(hyp_1));
			processHyp((char *)hyp,hyp_1,0x20);
			if(strcmp(hyp_1,p_stTestData[i].result) == 0)
			{
				succes++;								
			}
			fwrite(",", strlen(","), 1, fh_log);
			fwrite(hyp, strlen(hyp), 1, fh_log);
#endif

			#ifndef WIN32
				shninx_SysPrintf("test_sphinx_model: ps_start_utt");
			#endif
			
			fseek(fh, 0, SEEK_SET);
			rv = ps_start_utt(ps, "goforward");
			if (rv < 0)
			{
				fwrite("ps_start_utt\n", strlen("ps_start_utt\n"), 1, fh_log);
				continue;
			}
			while (!feof(fh)) {
				size_t nsamp;
				nsamp = fread(buf, 2, 512, fh);
				rv = ps_process_raw(ps, buf, nsamp, FALSE, FALSE);
			}
			rv = ps_end_utt(ps);
			if (rv < 0)
			{
				fwrite("ps_end_utt \n", strlen("ps_end_utt\n"), 1, fh_log);
				continue;
			}
				
			#ifndef WIN32
				shninx_SysPrintf("test_sphinx_model: ps_end_utt");
			#endif
			
			hyp = ps_get_hyp(ps, &score, &uttid);
			if (hyp == NULL)
			{
				fwrite("ps_get_hyp\n", strlen("ps_get_hyp\n"), 1, fh_log);
				continue;
			}
				
			#ifdef WIN32
				printf("ps_process_raw Recognized: %s\n", hyp);
			#else
				shninx_SysPrintf("ps_process_raw Recognized: %s\n", hyp);
			#endif
			
			memset(hyp_1,0,sizeof(hyp_1));
			processHyp((char *)hyp,hyp_1,0x20);
			if(strcmp(hyp_1,p_stTestData[i].result) == 0)
			{
				succes1++;								
			}
			fwrite(",", strlen(","), 1, fh_log);
			fwrite(hyp, strlen(hyp), 1, fh_log);
			fwrite("\n", strlen("\n"), 1, fh_log);

			fclose(fh);	
			
			#ifndef WIN32
				shninx_SysPrintf("test_sphinx_model : succes1 = %d",succes1);
			#endif
			
			ps_free(ps);
		}

		double rate = succes / count;
		double rate1 = succes1 / count;
		
		#ifndef WIN32
			shninx_SysPrintf("test_sphinx_model : rate = %f  rate1 = %f",rate,rate1);
		#endif	
			
		memset(hyp_1,0,sizeof(hyp_1));
		sprintf(hyp_1,"file ps_decode_raw succes rate,%f\n",rate);
		fwrite(hyp_1, strlen(hyp_1), 1, fh_log);
		memset(hyp_1,0,sizeof(hyp_1));
		sprintf(hyp_1,"stream ps_process_raw succes rate,%f\n",rate1);
		fwrite(hyp_1, strlen(hyp_1), 1, fh_log);
		
		
		
		cmd_ln_free_r(config);
		
		#ifndef WIN32
			shninx_SysPrintf("test_sphinx_model : end");
		#endif
	}

	unInitTestData(p_stTestData);

	if(fh_log)
		fclose(fh_log);
	return count;
}
