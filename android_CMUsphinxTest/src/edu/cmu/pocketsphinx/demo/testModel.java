package edu.cmu.pocketsphinx.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.util.EncodingUtils;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class testModel
{

	private sphinx_model_config sphinx_model_config0 = null;
	private int index = 0;
	private testModelJsgf testModelJsgf0 = null;
	private testVCMD testVCMD0 = null;
	private testZT_ASR testZT_ASR0 = null;
	private Thread mThread = null;
	private String mlogFile = null;
	private double success = 0.0;
	private int toutalTimeMs = 0;
	private int toutalDataLen = 0;
	private int toutalCount = 0;
	private int timeMS = 0;
	private testModelListener testModelListener0 = null;
	private int flag = 1;	// sphinx=1 vcmd=2 zts=3
	
	public testModel(String strflag)
	{
		if(strflag.equals("sphinx"))
		{
			flag = 1;
		}
		else if(strflag.equals("vcmd"))
		{
			flag = 2;
		}
		else if(strflag.equals("zts"))
		{
			//中科院自动化所，本地语音识别
			flag = 3;
		}
	}
	
	private sphinx_model_config XML_Parser_readXML(String xmlFile)
	{
		File file = new File(xmlFile);
        if (!file.exists()) {

        	return null;
        }
        
        
		try {
			FileInputStream fin = new FileInputStream(file);
			
			sphinx_model_config0 = XML_Parser.readXML(fin);
			
			fin.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
		
				
		return sphinx_model_config0;
		
	}
	
	public void init()
	{
		//sphinx_model_config0 = null;
		this.index = 0;
		//testModelJsgf0 = null;
		//mThread = null;
		mlogFile = null;
		success = 0.0;
		toutalTimeMs = 0;
		toutalDataLen = 0;
		toutalCount = 0;
		timeMS = 0;
	}
	public void startTest(String xmlFile,String logFile,String hmm,String jsgf,String dic,String key,String value)
	{
		//初始化相关参数
		init();
		
		
		mlogFile = logFile;
		
		File file = new File(mlogFile);
        if (file.exists()) {

        	file.delete();
        }
        
		
		//读取测试文件，
		if(sphinx_model_config0 == null)
		{
			sphinx_model_config0 = XML_Parser_readXML(xmlFile);
			if(sphinx_model_config0 == null)
				return;
		}
		
		if(flag == 1)
		{
			//开始测试数据
			if(testModelJsgf0 == null)
			{
				testModelJsgf0 = new testModelJsgf(hmm,jsgf,dic,mhandler,key,value);
				if(testModelJsgf0 == null)
					return;
				testModelJsgf0.set_runFlag(false);
			}
			
			if(mThread == null)
			{
				mThread = new Thread(testModelJsgf0);
				mThread.start();
			}
		}
		else if(flag == 2)
		{
			//开始测试数据
			if(testVCMD0 == null)
			{
				testVCMD0 = new testVCMD(hmm,jsgf,dic,mhandler);
				if(testVCMD0 == null)
					return;
				testVCMD0.set_runFlag(false);
			}
			
			if(mThread == null)
			{
				mThread = new Thread(testVCMD0);
				mThread.start();
			}
		}
	
		
		if(index < sphinx_model_config0.get_test_data_count())
		{
        	String filePathName = sphinx_model_config0.get_test_data_folder() + 
        			sphinx_model_config0.get_xml_test_datas().get(index).get_file_name();
        	
        	if(flag == 1)
        		testModelJsgf0.startTestRawFile(filePathName);
        	else if(flag == 2)
        		testVCMD0.startTestRawFile(filePathName);
        }
		else
			Log.e(CommonFun.TAG, "startTestjsgf error!   index = " + index);
		
		//
	}
	
	public void startTest(String xmlFile,String logFile,String hmm,String jsgf,String dic)
	{
		//初始化相关参数
		init();
		
		
		mlogFile = logFile;
		
		File file = new File(mlogFile);
        if (file.exists()) {

        	file.delete();
        }
        
		
		//读取测试文件，
		if(sphinx_model_config0 == null)
		{
			file = new File(xmlFile);
	        if (file.exists()) {

	        	sphinx_model_config0 = XML_Parser_readXML(xmlFile);
				if(sphinx_model_config0 == null)
					return;
	        }
	        else
	        	return;
	        
			
		}
		
		if(flag == 1)
		{
			//开始测试数据
			if(testModelJsgf0 == null)
			{
				testModelJsgf0 = new testModelJsgf(hmm,jsgf,dic,mhandler);
				if(testModelJsgf0 == null)
					return;
				testModelJsgf0.set_runFlag(false);
			}
			
			if(mThread == null)
			{
				mThread = new Thread(testModelJsgf0);
				mThread.start();
			}
		}
		else if(flag == 2)
		{
			//开始测试数据
			if(testVCMD0 == null)
			{
				testVCMD0 = new testVCMD(hmm,jsgf,dic,mhandler);
				if(testVCMD0 == null)
					return;
				testVCMD0.set_runFlag(false);
			}
			
			if(mThread == null)
			{
				mThread = new Thread(testVCMD0);
				mThread.start();
			}
		}
	
		
		if(index < sphinx_model_config0.get_test_data_count())
		{
        	String filePathName = sphinx_model_config0.get_test_data_folder() + 
        			sphinx_model_config0.get_xml_test_datas().get(index).get_file_name();
        	
        	if(flag == 1)
        		testModelJsgf0.startTestRawFile(filePathName);
        	else if(flag == 2)
        		testVCMD0.startTestRawFile(filePathName);
        }
		else
			Log.e(CommonFun.TAG, "startTestjsgf error!   index = " + index);
		
		//
	}
	
	public void startTest(String xmlFile,String logFile,String hotWords,AssetManager assetMgr,String modelDir)
	{
		//初始化相关参数
		init();
		
		
		mlogFile = logFile;
		
		File file = new File(mlogFile);
        if (file.exists()) {

        	file.delete();
        }
        
		
		//读取测试文件，
		if(sphinx_model_config0 == null)
		{
			sphinx_model_config0 = XML_Parser_readXML(xmlFile);
			if(sphinx_model_config0 == null)
				return;
		}
		
		if(flag == 3)
		{
			//开始测试数据
			if(testZT_ASR0 == null)
			{
				testZT_ASR0 = new testZT_ASR(hotWords,assetMgr,modelDir,mhandler);
				if(testZT_ASR0 == null)
					return;
				testZT_ASR0.set_runFlag(false);
			}
			
			if(mThread == null)
			{
				mThread = new Thread(testZT_ASR0);
				mThread.start();
			}
		}
		else
		{
			Log.e(CommonFun.TAG, "startTest error!   flag = " + flag);
			return;
		}
		
	
		
		if(index < sphinx_model_config0.get_test_data_count())
		{
        	String filePathName = sphinx_model_config0.get_test_data_folder() + 
        			sphinx_model_config0.get_xml_test_datas().get(index).get_file_name();
        	
        	if(flag == 3)
        		testZT_ASR0.startTestRawFile(filePathName);
        	
        }
		else
			Log.e(CommonFun.TAG, "testZT_ASR0 error!   index = " + index);
		
		//
		
	}
	
	
	public void set_testModelListener(testModelListener t)
	{
		this.testModelListener0 = t;
	}
	
	public testModelListener get_testModelListener()
	{
		return this.testModelListener0;
	}
	
	
	//写数据到SD中的文件
	private void writeFileSdcardFile(String fileName,String write_str)
	{
		
        
        
		try {
			RandomAccessFile afile = new RandomAccessFile(fileName, "rw");
			
			long len = afile.length();
			
			afile.seek(len);
			
			//afile.writeUTF(write_str);
			byte [] bytes = write_str.getBytes();
			afile.write(bytes); 
			
			
			afile.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	private void processStr(byte[] strChar,byte[] strChar_new,byte flag)
	{
		int count = strChar.length;
		int index = 0;
		for(int i = 0;i < count;i++)
		{
			if(strChar[i] != flag)
			{
				strChar_new[index] = strChar[i];
				index++;
			}
			
		}
		
		if(index < count)
			strChar_new[index] = 0;
	}
	private String processStr_1(String str,String flag)
	{
		String ret = "";
		
		String spStr[] = str.split(flag);
		
		for(int i = 0; i < spStr.length;i++)
			ret = ret + spStr[i];
		
		return ret;
	}
	
	private byte getHexFrom(byte in)
	{
		byte ret = 0;
		
		switch(in)
		{
		case 0x30:
			ret = 0x0;
			break;
		case 0x31:
			ret = 0x01;
			break;
		case 0x32:
			ret = 0x02;
			break;
		case 0x33:
			ret = 0x03;
			break;
		case 0x34:
			ret = 0x04;
			break;
		case 0x35:
			ret = 0x05;
			break;
		case 0x36:
			ret = 0x06;
			break;
		case 0x37:
			ret = 0x07;
			break;
		case 0x38:
			ret = 0x08;
			break;
		case 0x39:
			ret = 0x09;
			break;
		case 0x41:
			ret = 0x0A;
			break;
		case 0x42:
			ret = 0x0B;
			break;
		case 0x43:
			ret = 0x0C;
			break;
		case 0x44:
			ret = 0x0D;
			break;
		case 0x45:
			ret = 0x0E;
			break;
		case 0x46:
			ret = 0x0F;
			break;
			
		}
		
		return ret;
	}
	
	private String processStr_2(String str,String flag)
	{
		String ret = "";
		
		String spStr[] = str.split(flag);
		int len = str.length() / 3;
		byte[] b = new byte[len];
		
		for(int i = 0; i < spStr.length;i++)
		{
			String st1 = spStr[i];
			byte[] strChar = st1.getBytes();
			if(strChar.length == 2)
			{
				//strChar
				b[i] = (byte) ((getHexFrom(strChar[1]) & 0XF) | ((getHexFrom(strChar[0]) & 0XF) << 4));
			}
			
		}
			//ret = ret + spStr[i];
		
		return ret;
	}
	
	private byte[] getUTF8BytesFromGBKString(String gbkStr) {   
        int n = gbkStr.length();   
        byte[] utfBytes = new byte[3 * n];   
        int k = 0;   
        for (int i = 0; i < n; i++) {   
            int m = gbkStr.charAt(i);   
            if (m < 128 && m >= 0) {   
                utfBytes[k++] = (byte) m;   
                continue;   
            }   
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));   
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));   
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));   
        }   
        if (k < utfBytes.length) {   
            byte[] tmp = new byte[k];   
            System.arraycopy(utfBytes, 0, tmp, 0, k);   
            return tmp;   
        }   
        return utfBytes;   
    }  
	
	private Handler mhandler = new Handler()
	{
		@Override
	    public void handleMessage(Message msg) 
		{
	        super.handleMessage(msg);
	        
	        if(msg.what == 1 )
	        {
	        	int t = 0;
		        Bundle data = msg.getData();
		        String result = data.getString("result");
		        //
//		        byte[] strChar = result.getBytes();
//		        byte[] strChar_new = new byte[strChar.length];
//		        byte flag = 0x20;
//		        processStr(strChar,strChar_new,flag);
		        
		        String sqlt_flag = " ";
		        String result1 = processStr_1(result,sqlt_flag);
		        //byte[] arrayByte0 = result1.getBytes();
		        
		        
		        
		        
		        String result2 = sphinx_model_config0.get_xml_test_datas().get(index).get_result();//utf8
		        //byte[] arrayByte = result2.getBytes();
		        
		        
//		        result2 = EncodingUtils.getString(arrayByte, "UTF-8");
		        
//				try {
//					if(flag == 2)
//					{
//						//String strUT = URLEncoder.encode(result2, "gb2312");
//						//String striso = processStr_2(strUT,"%");
//						
//						
////						String iso = new String(result1.getBytes("UTF-8"),"ISO-8859-1");    
////						String utf8=new String(result1.getBytes("GB2312"),"UTF-8");  
//
//
//						
//
//						String x = new String(getUTF8BytesFromGBKString(result1), "UTF-8");
//
//						
//						result1 = x;
//
//					}
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

		        
		        //写文件，开始记录测试结果
		        String str = " " + sphinx_model_config0.get_xml_test_datas().get(index).get_file_name() + " ," + result2 + " ," + result1;
		        writeFileSdcardFile(mlogFile,str);
		        
		        if(result1.equals(result2))
		        {
		        	success = success + 1.0;      	
		        }
		        
		        t = data.getInt("useTimeMs");
	        	toutalTimeMs = toutalTimeMs + t;
	        	
	        	int dataLen = data.getInt("dataLength");
	        	toutalDataLen += dataLen;
	        	
	        	if(t > 0)
	        		toutalCount++;
	        	
	        	String strTime = ", 	use time = " + t + " ms"; 
	        	writeFileSdcardFile(mlogFile,strTime);
	        	
		        
		        
		        writeFileSdcardFile(mlogFile,"\n");
		        
		        if(get_testModelListener() != null)
					get_testModelListener().processCurrentData(sphinx_model_config0.get_xml_test_datas().get(index).get_file_name(), result1, t);
		        
		        //开始下一个数据的测试
		        index++;
		        if(index < sphinx_model_config0.get_test_data_count())
		        {
		        	String fn = sphinx_model_config0.get_xml_test_datas().get(index).get_file_name();
		        	String filePathName = sphinx_model_config0.get_test_data_folder() + fn;
		        			
		        	Log.e(CommonFun.TAG, "start Test file = " + fn);
		        	Log.e(CommonFun.TAG, "start Test filename = " + filePathName);
		        	
					if(flag == 1)
		        		testModelJsgf0.startTestRawFile(filePathName);
		        	else if(flag == 2)
		        		testVCMD0.startTestRawFile(filePathName);
		        	else if(flag == 3)
		        		testZT_ASR0.startTestRawFile(filePathName);
					
		        }
				else
				{
					Log.e(CommonFun.TAG, "startTestjsgf finish!   index = " + index);
					
					double successRate = success / toutalCount;
					
					String str1 = "successRate :" + successRate + "\n";
			        writeFileSdcardFile(mlogFile,str1);
			        str1 = "success toutalTimeMs :" + toutalTimeMs + "  ms \n";
			        writeFileSdcardFile(mlogFile,str1);
			        int time = (int) (toutalTimeMs / success);
			        str1 = "success timeMS :" + time + "  ms \n";
			        writeFileSdcardFile(mlogFile,str1);
					
					
			        
			        
			        if(get_testModelListener() != null)
			        {
			        	Bundle data1 = new Bundle();		
				        data1.putInt("test_count",toutalCount);
				        data1.putDouble("succes_count",success);
				        data1.putDouble("successRate",successRate);
				        data1.putInt("toutalTimeMs",toutalTimeMs);
				        data1.putInt("toutalDataLen",toutalDataLen);
				        int rate = 0;
				        if(flag == 1)
				        	rate = 16000;
				        else if(flag == 2)
				        	rate = 16000;
				        else if(flag == 3 )
				        	rate = 16000;
				        
				        data1.putInt("sampleRate", rate);
				        
			        	get_testModelListener().processTotalInfo(data1);
			        }
			        
//					try {
//						mThread.join();
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
		        
	        }
		}
	};


	

}
