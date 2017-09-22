package edu.cmu.pocketsphinx.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;





import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.pocketsphinx;

public class testModelJsgf implements Runnable {
	final public int BUFF_SIZE = 4096;//4096 1024 8192

	private Handler mHandler = null;

	//运行标记
	private boolean runFlag = false;
	
	private String mRawFile = null;
	
	private long startTime = 0;
	private long endTime = 0;
	
	private final double sampleRate = 8000.0;
	
	/**
	 * PocketSphinx native decoder object.
	 */
	private Decoder decoder;
	
	private Config c = null;
	
	public testModelJsgf(String hmm,String jsgf,String dic,Handler m)
	{
		//sphinx init
		init(hmm,jsgf,dic);
		
		mHandler = m;

		//
	}
	
	public testModelJsgf(String hmm,String jsgf,String dic,Handler m,String key,String value)
	{
		//sphinx init
		init(hmm,jsgf,dic,key,value);
		
		mHandler = m;

		//
	}
	
	private void init(String hmm,String jsgf,String dic,String key,String value)
	{
		pocketsphinx.setLogfile("/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/pocketsphinx_jsgf.log");
		this.c = new Config();
		
//		c.setFloat("-samprate", sampleRate);
//		c.setString("-CyVoiceE", "CyVoiceE_jsgf");
//		c.setBoolean("-bestpath", false);
		
		
		if((hmm != null) && (jsgf != null) && (dic != null))
		{
			c.setString("-hmm",hmm);
			c.setString("-lm",jsgf);
			//c.setString("-fsg",jsgf);
			c.setString("-dict",dic);
			
			c.setString("-CyVoiceE", "sphinx");
		}
		else
		{
			c.setString("-CyVoiceE", "CyVoiceE_jsgf");
			
//			String str = "(打开车门)\n( 关闭车门){关车门}\n( 打开电灯 ){刘福良}\n关闭电灯\n(请帮我打开车门)\n";
//			String str1 = "";
			if(jsgf != null)
				c.setString("-Custom_jsgf",jsgf);
		}
		
		
		if((key != null) && (value != null) && (key.length() != 0) && (value.length() != 0))
		{			
			
			if(value.equals("true") == true)
				c.setBoolean(key, true);
			else if(value.equals("false") == true)
				c.setBoolean(key, false);			
			else
			{
				int intValue;
				double dValue = 1e-48;
				
//				if((Object)value instanceof Integer)
//				{
//					intValue = Integer.parseInt(value);
//					c.setInt(key, intValue);
//				}
//				else if((Object)value instanceof Double)
//				{
//					dValue = Double.parseDouble(value);
//					c.setFloat(key, dValue);
//				}
//				else
//				{
//					c.setString(key,value);
//				}
				
				try{
					intValue = Integer.parseInt(value);
					c.setInt(key, intValue);
				}
				catch  (Exception e) {   
					  
					// TODO Auto-generated catch block
					// e.printStackTrace();
					
					try{
						dValue = Double.parseDouble(value);
						c.setFloat(key, dValue);
					}
					catch  (Exception e1) {   
						  
						// TODO Auto-generated catch block
						// e.printStackTrace();
						
						c.setString(key,value);
				    } finally  {   
				           
				    }
					

			    } finally  {   
			           
			    }   

				
			}
		}

		
		c.setFloat("-samprate", sampleRate);
		c.setBoolean("-bestpath", false);
		
//		c.setInt("-maxhmmpf", 2000);
		
		
		
		c.setInt("-maxwpf", 1);		//
		c.setInt("-maxhmmpf", 1000);
		//c.setInt("-fwdflatefwid",1);
		//c.setInt("-pl_window", 2);

				
		//c.setString("-rawlogdir", "/sdcard/Android/data/edu.cmu.pocketsphinx");
//		c.setFloat("-samprate", 8000.0);
//		c.setInt("-maxhmmpf", 2000);
//		c.setInt("-maxwpf", 10);
//		c.setInt("-pl_window", 2);
//		c.setBoolean("-backtrace", true);
//		c.setBoolean("-bestpath", false);
		
		
//		c.setString("-toprule", "command.test");
		
		
		//测试 如下的两个值，对速度的影响-----影响不大
//		c.setBoolean("-fwdtree", false);
//		c.setBoolean("-fwdflat", true);
		
		//c.setBoolean("-remove_noise", false);
		
		this.decoder = new Decoder(this.c);
	}
	
	private void init(String hmm,String jsgf,String dic)
	{
		pocketsphinx.setLogfile("/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/pocketsphinx_jsgf.log");
		this.c = new Config();
		
//		c.setFloat("-samprate", sampleRate);
//		c.setString("-CyVoiceE", "CyVoiceE_jsgf");
//		c.setBoolean("-bestpath", false);
		
		
		if((hmm != null) && (jsgf != null) && (dic != null))
		{
			c.setString("-hmm",hmm);
			c.setString("-lm",jsgf);
			//c.setString("-fsg",jsgf);
			c.setString("-dict",dic);
			
			c.setString("-CyVoiceE", "sphinx");
		}
		else
		{
			c.setString("-CyVoiceE", "CyVoiceE_jsgf");
			
//			String str = "(打开车门)\n( 关闭车门){关车门}\n( 打开电灯 ){刘福良}\n关闭电灯\n(请帮我打开车门)\n";
//			String str1 = "";
			if(jsgf != null)
				c.setString("-Custom_jsgf",jsgf);
		}

		
		c.setFloat("-samprate", sampleRate);
		c.setBoolean("-bestpath", false);
		
		c.setInt("-Custom_flag", 2);
		
//		c.setInt("-maxhmmpf", 2000);
		
		
		
		

				
		//c.setString("-rawlogdir", "/sdcard/Android/data/edu.cmu.pocketsphinx");
//		c.setFloat("-samprate", sampleRate);
//		c.setInt("-maxhmmpf", 2000);
//		c.setInt("-maxwpf", 10);
//		c.setInt("-pl_window", 2);
//		c.setBoolean("-backtrace", true);
//		c.setBoolean("-bestpath", false);
		
		
//		c.setString("-toprule", "command.test");
		
		
		//测试 如下的两个值，对速度的影响-----影响不大
//		c.setBoolean("-fwdtree", false);
//		c.setBoolean("-fwdflat", true);
		
		//c.setBoolean("-remove_noise", false);
		
		this.decoder = new Decoder(this.c);
	}
	
	private short[] byteArray2ShortArray(byte[] data, int items) {
		short[] retVal =new short[items/2];
		for (int i =0; i < retVal.length; i++)
		retVal[i] = (short) ((data[i *2]&0xff) | (data[i *2+1]&0xff) <<8);
		return retVal;
	} 

	private void sendMsg(int msgId,String hypothesis,int time,int len)
	{
		Message msg = new Message();
        Bundle data = new Bundle();

        data.putString("result",hypothesis);	        
        data.putInt("useTimeMs", time);		        
        data.putInt("dataLength", len);
        
        msg.setData(data);
        msg.what = msgId;
        mHandler.sendMessage(msg);
	}
	public void set_runFlag(boolean f)
	{
		this.runFlag = f;
	}
	
	public void startTestRawFile(String rawFile)
	{
		mRawFile = rawFile;
		
		set_runFlag(true);
		
		Log.e(CommonFun.TAG, "[startTestRawFile] runFlag == true file = " + mRawFile);
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int count = 0;
		
		while(true)
		{
			if(this.runFlag == true)
			{
				String hypothesis = null;
				
				//结束当前文件
				set_runFlag(false);
				
				count = 0;
				Log.e(CommonFun.TAG, "runFlag == true file = " + mRawFile);
				
				File f = new File(mRawFile);
		        if (!f.exists()) {
		        	sendMsg(1,"no match",0,0);
		        	continue;
		        }
				
				
				short[] filecontent = null;
				byte[] b = null;
				//filecontent = CommonFun.readFileData(mContext);
				
				RandomAccessFile file = null;
				try {
					file = new RandomAccessFile(mRawFile,"rw");
					
					int len = (int)file.length();
					
					//取得文件长度（字节数） 
					b=new byte[len]; 
					
					file.readFully(b);
					
					file.close();
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
								  
				filecontent = byteArray2ShortArray(b,b.length);
				
				
				startTime = System.currentTimeMillis();
				//重复加载模型文件，
				//this.decoder = new Decoder(this.c);
				
				//Log.e("decoder.startUtt", "begin................."+filecontent.length);
				this.decoder.startUtt(null);
				//Log.e("decoder.startUtt", "end..............................");
				int i = 0;
				int size = BUFF_SIZE;
				while(i != filecontent.length)
				{
					
					if( i + BUFF_SIZE > filecontent.length)
					{
						size = filecontent.length - i;
					}
					else
						size = BUFF_SIZE;
					
					short[] temp = new short[size];
					System.arraycopy(filecontent, i, temp, 0, size);
					
					this.decoder.processRaw(temp, temp.length, false, false);


					
					i += size;
				}
			
				this.decoder.endUtt();
				
				
				if(this.decoder!=null)
				{
					try{
							//Log.e("Hypothesis", "begin..............................");
							final Hypothesis hyp = this.decoder.getHyp();
							hypothesis = hyp.getHypstr();		
							//Log.e("Hypothesis", "end......."+hypothesis);
							
							
							endTime = System.currentTimeMillis();
							
							//Log.e(CommonFun.TAG, "end......."+hypothesis);
							if(hypothesis == null)
								hypothesis = "no match";
							Log.e(CommonFun.TAG, "hypothesis.length() = "+hypothesis.length());
							
							int time = (int)(endTime - startTime);
							
							sendMsg(1,hypothesis,time,filecontent.length);
				            
					}
					catch(NullPointerException ex)
					{
						
						endTime = System.currentTimeMillis();
						int time = (int)(endTime - startTime);
						sendMsg(1,"no match",time,filecontent.length);
						
					}
				}
				
				
				

				
				
			}
			else
			{
				if(count < 10)
					count++;
				else
					break;
				
				Log.e(CommonFun.TAG, "runFlag == flase");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		Log.e(CommonFun.TAG, "run end!!!");
		
		
		
	}//run

}//testModelJsgf
