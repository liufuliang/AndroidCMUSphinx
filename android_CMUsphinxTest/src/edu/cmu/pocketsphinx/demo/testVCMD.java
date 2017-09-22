package edu.cmu.pocketsphinx.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.VCMD.JNI.VCMDEngine;


public class testVCMD implements Runnable {
	final public int BUFF_SIZE = 4096;//4096 1024

	private Handler mHandler = null;

	//运行标记
	private boolean runFlag = false;
	
	private String mRawFile = null;
	
	private long startTime = 0;
	private long endTime = 0;

	private List<String> lyricList = new ArrayList<String>();
	

	
	public testVCMD(String am,String keyword,String word,Handler m)
	{
		
		//sphinx init
		init(am,keyword,word);
		
		mHandler = m;

		
	}
	
	private void loadResult(String resultFile)
	{

		if(lyricList.size()>0){
			lyricList.clear();
		}
		
		File f = new File(resultFile);
		try {
			BufferedReader bw = new BufferedReader(new InputStreamReader(
					new FileInputStream(f),"GB2312"));
			String line = null;
			String nextline = null;
			// 因为不知道有几行数据，所以先存入list集合中
			while ((line = bw.readLine()) != null) {	
				if((nextline = bw.readLine()) != null){
					lyricList.add(line+"   "+nextline);
				}
				else{
					lyricList.add(line);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	
//	"monophone.am","keyword.txt","g2p.word"
	private void init(String am,String keyword,String word)
	{
		int initFlag = VCMDEngine.Initial(am,keyword,word);
		
		//加载结果
		loadResult(keyword);
		
	}
	
	

	public void set_runFlag(boolean f)
	{
		this.runFlag = f;
	}
	
	public void startTestRawFile(String rawFile)
	{
//		String spStr[] = rawFile.split(".raw");
//		
//		mRawFile = spStr[0] +".pcm";
		
		mRawFile = rawFile;
		
		set_runFlag(true);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int count = 0;
		
		while(true)
		{
			if(runFlag == true)
			{
				count = 0;
				Log.e(CommonFun.TAG, "runFlag == true file = " + mRawFile);
				
				RandomAccessFile file = null;
				int fileLen = 0;
				try {
					file = new RandomAccessFile(mRawFile,"rw");
					
					fileLen = (int)file.length();
					
					//取得文件长度（字节数） 
					
					file.close();
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				startTime = System.currentTimeMillis();
				
				
				int index = VCMDEngine.RecVoice(mRawFile);
			
				int a = index/2;
				int b = index%2;
				String strResult = "NULL";
				if(index >= 0)
				{
					if(lyricList.get(a).contains("   ")){
						strResult = lyricList.get(a).split("   ")[b];
					}
					else{
						strResult = lyricList.get(a);
					}
				}
				
				
				try{							
					Message msg = new Message();
			        Bundle data = new Bundle();
	
			        data.putString("result",strResult);
			        
			        endTime = System.currentTimeMillis();
			        int time = (int)(endTime - startTime);
			        data.putInt("useTimeMs", time);
			        
			        data.putInt("dataLength", fileLen);
			        
			        msg.setData(data);
			        msg.what = 1 ;
			        mHandler.sendMessage(msg);
	
		            
				}
				catch(NullPointerException ex)
				{
					
				}
				
				
				//结束当前文件的测试
				set_runFlag(false);
				
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
}
