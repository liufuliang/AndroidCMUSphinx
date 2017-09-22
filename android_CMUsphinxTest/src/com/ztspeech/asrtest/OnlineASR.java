package com.ztspeech.asrtest;

import java.io.UnsupportedEncodingException;

import android.os.Handler;
import android.os.Message;

public class OnlineASR {
	public boolean InitASR(String dir, ASRInterface asrInterface, Object asset)
	{
		asrinterface = asrInterface;
		if (inited){
			SendMsg(MSG_ERROR,"warn: can not init ASR twice");
			return true;
		}
		boolean ret = inited = SysInit(dir, asset) && LineInit();
		if (!ret){
			SendMsg(MSG_ERROR,"error: InitASR");
		}else{
			SendMsg(MSG_ERROR,"succe: InitASR");			
		}
		return ret;
	}
	public boolean ReleaseASR()
	{
		boolean ret = LineRelease() && SysRelease();
		if (!ret){
			SendMsg(MSG_ERROR,"error: ReleaseASR");
		}else{
			SendMsg(MSG_ERROR,"succe: ReleaseASR");			
		}
		inited = false;
		return ret;
	}
	public boolean StartASR(){
		if(!inited){
			if (asrinterface!=null){
				SendMsg(MSG_ERROR,"error: must init ASR first");
			}
			return false;
		}
		if (recording){
			StopASR();
		}
		pretext = "";
		boolean ret = RecStart();
		recording = true;
		if (!ret){
			SendMsg(MSG_ERROR,"error: RecStart");
		}
		wavlength = 0;
		prelength = 0;
//		SendMsg(MSG_RESULT,"");	//lfl
		return ret;
	}
	public boolean StopASR()
	{
		if(!inited){
			if (asrinterface!=null){
				SendMsg(MSG_ERROR,"error: must init ASR first");
			}
			return false;
		}
		if(!recording){
			return true;
		}
		boolean ret = RecStop();
		byte [] text = GetFinalText();
		
		try {
			String asrtext = new String(text,"GBK");
//			if (!pretext.equals(asrtext)){
//				pretext = asrtext;
//				SendMsg(MSG_FINAL_RESULT,asrtext);
//			}
			

			SendMsg(MSG_FINAL_RESULT,asrtext);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SendMsg(MSG_ERROR,e.getMessage());
			e.printStackTrace();
		}
		recording = false;
		return ret;
	}
	public boolean AddBuffer(byte[] data)
	{
		if (!inited) return false;
		if (!recording) return false;

		wavlength += data.length;

		byte[] text = FillWaveBuffer(data);

		try {
			String asrtext = new String(text,"GBK");

			if (asrtext.length()>1 && !pretext.equals(asrtext))
			{
				SendMsg(MSG_RESULT,asrtext);
				pretext = asrtext;
				prelength = wavlength;
			}

//			if ((prelength>32*800) && (prelength+MAX_SILENCE_BYTES_ED<wavlength))
//			{
//				StopASR(); SendMsg(MSG_AUTO_STOP,"stop: too long no voice");
//			}
//			else if (MAX_SILENCE_BYTES_ST<wavlength)
//			{
//				StopASR(); SendMsg(MSG_AUTO_STOP,"stop: too long no voice");
//			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SendMsg(MSG_ERROR,e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what)
			{
			case MSG_RESULT:
				asrinterface.OnResult((String) msg.obj);
				break;
			case MSG_ERROR:
				asrinterface.OnError((String) msg.obj);
				break;
			case MSG_AUTO_STOP:
				asrinterface.OnAutoStop((String) msg.obj);
				break;
			case MSG_FINAL_RESULT:
				asrinterface.OnFinalResult((String) msg.obj);
				break;
			}
		};
	};
	private void SendMsg(int msgtype, String msgtext){
		Message msg = handler.obtainMessage();
		msg.obj = msgtext;
		msg.what = msgtype;
		handler.sendMessage(msg);
	}

	private native boolean SysInit(String modelDir, Object asset);
	private native boolean SysRelease();
	private native boolean LineInit();
	private native boolean LineRelease();
	private native boolean RecStart();
	private native boolean RecStop();
	private native byte[] FillWaveBuffer(byte[] wavdata);
	private native byte[] GetFinalText();

	public native boolean AddHotWordGBK(byte[] word);
	public native boolean UpdateHotWord();
	public native boolean ClearHotWords();
	
	static {System.loadLibrary("OnlineASR");}
	private ASRInterface asrinterface = null;
	public boolean online;
	public String pretext;
	public int wavlength=0;
	public int prelength=0;
	public boolean inited=false;
	public boolean recording=false;
	private static final int MSG_RESULT = 0;
	private static final int MSG_ERROR = 1;
	private static final int MSG_AUTO_STOP = 2;
	private static final int MSG_FINAL_RESULT = 3;
	private static final int MAX_SILENCE_BYTES_ST = 5*(16000*2);	
	private static final int MAX_SILENCE_BYTES_ED = 1*(16000*2);	
}
