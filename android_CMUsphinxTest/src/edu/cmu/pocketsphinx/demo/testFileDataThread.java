package edu.cmu.pocketsphinx.demo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.pocketsphinx;

public class testFileDataThread implements Runnable {
	final public int BUFF_SIZE = 4096;//4096 1024
	
	private Thread mThread = null;
	private Handler mHandler = null;
	private Context mContext = null;
	/**
	 * PocketSphinx native decoder object.
	 */
	private Decoder decoder;
	
	private void init()
	{
		pocketsphinx.setLogfile("/sdcard/Android/data/edu.cmu.pocketsphinx/pocketsphinx_test.log");
		Config c = new Config();
		
		/*
		 * In 2.2 and above we can use getExternalFilesDir() or whatever it's
		 * called
		 */

		/*
		c.setString("-hmm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/en/tidigits");
		c.setString("-dict",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en/tidigits.dic");
		c.setString("-lm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en/tidigits.DMP");
		
		c.setString("-hmm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
				c.setString("-dict",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_CN/mandarin_notone.dic");
				c.setString("-lm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_CN/gigatdt.5000.DMP");
		*/
		
		
				
		
		
		
		
		c.setString("-hmm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_hmm/tdt_sc_8k");
		c.setString("-dict",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_lm/test.dic");
		c.setString("-lm",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_lm/test.lm");
		
		
		
		//c.setString("-hmm",
		//		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/en_US/hub4wsj_sc_8k");
		//c.setString("-dict",
		//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en_US/hub4.5000.dic");
		//c.setString("-lm",
		//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en_US/hub4.5000.DMP");
				
		//c.setString("-rawlogdir", "/sdcard/Android/data/edu.cmu.pocketsphinx");
		c.setFloat("-samprate", 8000.0);
		c.setInt("-maxhmmpf", 2000);
		//c.setInt("-maxwpf", 10);
		//c.setInt("-pl_window", 2);
		//c.setBoolean("-backtrace", true);
		//c.setBoolean("-bestpath", false);
		c.setBoolean("-remove_noise", false);
		
		
		/*
		c.setString("-hmm",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
		c.setString("-dict",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_TW/mandarin_notone.dic");
		c.setString("-lm",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_TW/gigatdt.5000.DMP");
		*/
		//c.setString("-rawlogdir", "/sdcard/Android/data/edu.cmu.pocketsphinx");
//		c.setFloat("-samprate", 8000.0);
//		c.setInt("-maxhmmpf", 2000);
//		c.setInt("-maxwpf", 10);
//		c.setInt("-pl_window", 2);
//		c.setBoolean("-backtrace", true);
//		c.setBoolean("-bestpath", false);



		decoder = new Decoder(c);
	}
	
	public testFileDataThread(Context c,Handler h)
	{
		//sphinx init
		init();
		
		mHandler = h;
		mContext = c;
		
		mThread = new Thread(this);
		mThread.start();
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		short[] filecontent = null;
		filecontent = CommonFun.readFileData(mContext);
		
		
		
		Log.e("decoder.startUtt", "begin................."+filecontent.length);
		decoder.startUtt(null);
		Log.e("decoder.startUtt", "end..............................");
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
			
			decoder.processRaw(temp, temp.length, false, false);


			
			i += size;
		}
	
		decoder.endUtt();

		if(decoder!=null){
		try{
				Log.e("Hypothesis", "begin..............................");
				final Hypothesis hyp = decoder.getHyp();
				String hypothesis = hyp.getHypstr();		
				Log.e("Hypothesis", "end......."+hypothesis);
				
				
				Message msg = new Message();
		        Bundle data = new Bundle();

		        data.putString("result",hypothesis);
		        msg.setData(data);
		        msg.what = 1 ;
		        mHandler.sendMessage(msg);

	            
		}
		catch(NullPointerException ex){
			
			}
		}
		
	}


}
