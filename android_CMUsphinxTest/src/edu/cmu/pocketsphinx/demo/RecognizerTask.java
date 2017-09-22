package edu.cmu.pocketsphinx.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.Nbest;
import edu.cmu.pocketsphinx.pocketsphinx;

/**
 * Speech recognition task, which runs in a worker thread.
 * 
 * This class implements speech recognition for this demo application. It takes
 * the form of a long-running task which accepts requests to start and stop
 * listening, and emits recognition results to a listener.
 * 
 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
 */
public class RecognizerTask implements Runnable {
	
	final public int sampleRateInHz = 16000;//8000
	/**
	 * Audio recording task.
	 * 
	 * This class implements a task which pulls blocks of audio from the system
	 * audio input and places them on a queue.
	 * 
	 * @author David Huggins-Daines <dhuggins@cs.cmu.edu>
	 */
	class AudioTask implements Runnable {
		/**
		 * Queue on which audio blocks are placed.
		 */
		LinkedBlockingQueue<short[]> q;
		AudioRecord rec;
		int block_size;
		boolean done;

		static final int DEFAULT_BLOCK_SIZE = 512;

		AudioTask() {
			this.init(new LinkedBlockingQueue<short[]>(), DEFAULT_BLOCK_SIZE);
		}

		AudioTask(LinkedBlockingQueue<short[]> q) {
			this.init(q, DEFAULT_BLOCK_SIZE);
		}

		AudioTask(LinkedBlockingQueue<short[]> q, int block_size) {
			this.init(q, block_size);
		}

		void init(LinkedBlockingQueue<short[]> q, int block_size) {
			this.done = false;
			this.q = q;
			this.block_size = block_size;
			this.rec = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRateInHz,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 8192);
		}

		public int getBlockSize() {
			return block_size;
		}

		public void setBlockSize(int block_size) {
			this.block_size = block_size;
		}

		public LinkedBlockingQueue<short[]> getQueue() {
			return q;
		}

		public void stop() {
			this.done = true;
		}

		public void run() {
			this.rec.startRecording();
			while (!this.done) {
				int nshorts = this.readBlock();
				if (nshorts <= 0)
					break;
			}
			this.rec.stop();
			this.rec.release();
		}

		int readBlock() {
			short[] buf = new short[this.block_size];
			int nshorts = this.rec.read(buf, 0, buf.length);
			if (nshorts > 0) {
				Log.d(getClass().getName(), "Posting " + nshorts + " samples to queue");
				this.q.add(buf);
			}
			return nshorts;
		}
	}

	Config c;
	/**
	 * PocketSphinx native decoder object.
	 */
	Decoder ps;
	/**
	 * Audio recording task.
	 */
	AudioTask audio;
	/**
	 * Thread associated with recording task.
	 */
	Thread audio_thread;
	/**
	 * Queue of audio buffers.
	 */
	LinkedBlockingQueue<short[]> audioq;
	/**
	 * Listener for recognition results.
	 */
	RecognitionListener rl;
	/**
	 * Whether to report partial results.
	 */
	boolean use_partials;

	/**
	 * State of the main loop.
	 */
	enum State {
		IDLE, LISTENING
	};
	/**
	 * Events for main loop.
	 */
	enum Event {
		NONE, START, STOP, SHUTDOWN
	};

	/**
	 * Current event.
	 */
	Event mailbox;

	public RecognitionListener getRecognitionListener() {
		return rl;
	}

	public void setRecognitionListener(RecognitionListener rl) {
		this.rl = rl;
	}

	public void setUsePartials(boolean use_partials) {
		this.use_partials = use_partials;
	}

	public boolean getUsePartials() {
		return this.use_partials;
	}

	
	public RecognizerTask(String CyVoiceE,String custom_param,String custom_data) {
		//voiceE.setLogfile("/sdcard/Android/data/CyVoiceE.log");
		this.c = new Config();
		
		c.setFloat("-samprate", 16000.0);		
		c.setString("-CyVoiceE", CyVoiceE);
		c.setBoolean("-bestpath", false);
		if((custom_param != null) && (custom_data != null))
			c.setString(custom_param, custom_data);
		c.setInt("-Custom_flag", 0);//识别英文用
		
		this.ps = new Decoder(c);
		this.audio = null;
		this.audioq = new LinkedBlockingQueue<short[]>();
		this.use_partials = false;
		this.mailbox = Event.NONE;
	}
	
	
	public RecognizerTask() {
//		pocketsphinx
//				.setLogfile("/sdcard/Android/data/edu.cmu.pocketsphinx/pocketsphinx.log");
		this.c = new Config();
		/*
		 * In 2.2 and above we can use getExternalFilesDir() or whatever it's
		 * called
		 */
		
		
		
				
//		c.setString("-hmm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_test/test_sc_16k_200");
//		c.setString("-dict",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test/digitDemo.dic");
//		c.setString("-lm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test/digitDemo.lm.DMP");
		
		/*
				
		c.setString("-hmm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_bergtrain/bergtrain");
		c.setString("-dict",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_bergtrain/bergtrain.dic");
		c.setString("-lm",
				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_bergtrain/bergtrain.lm.DMP");
				
				
		
		*/
		
		
		
//		c.setString("-hmm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/en/tidigits");
//		c.setString("-dict",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en/tidigits.dic");
//		c.setString("-lm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en/tidigits.DMP");
		
		
//		c.setString("-hmm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_digital/digitDemo.cd_cont_200");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_digital/digitDemo.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_digital/digitDemo.lm.dmp");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_digital/8111.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_digital/digitDemo_text.txt.arpa");
				
			
		
		
		
		
		
//		c.setString("-hmm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
//		c.setString("-dict",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_CN/mandarin_notone.dic");
//		c.setString("-lm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_CN/gigatdt.5000.DMP");
				
//		c.setString("-hmm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_broadcastnews/test.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_broadcastnews/test.lm.DMP");
				
		
//		c.setString("-hmm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_command/command.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_command/command.lm");
		
		
//		c.setString("-hmm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/hmm/tdt_sc_8k");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/lm/command.dic");
//		c.setString("-jsgf",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/lm/command.gram");
		
				
//		c.setString("-hmm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_broadcastnews/zh_broadcastnews_ptm256_8000");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_broadcastnews/zh_broadcastnews_utf8.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_broadcastnews/zh_broadcastnews_64000_utf8.DMP");
		
		
//		c.setString("-hmm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_hmm/tdt_sc_8k");
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test_new/test.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test_new/test.lm");
		
//		c.setString("-dict",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_lm/my_db_1.dic");
//		c.setString("-lm",
//		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_lm/my_db_1.lm.DMP");
		
		
		

//		c.setString("-hmm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/en_US/hub4wsj_sc_8k");
//		c.setString("-dict",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en_US/hub4.5000.dic");
//		c.setString("-lm",
//				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/en_US/hub4.5000.DMP");
		
		
		c.setString("-hmm",
//		"/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k");
				"/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k");
		c.setString("-dict",
				"/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/lm/test/digital.dic");
		c.setString("-jsgf",
				"/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/lm/test/digital.gram");
		
		String rawlogdir = "/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/data/"+CommonFun.logPath;
		File file = new File(rawlogdir);
        if (!file.exists()) {
            file.mkdir();
        }
				
		c.setString("-rawlogdir", rawlogdir);
		
		
		c.setFloat("-samprate", sampleRateInHz);
//		c.setInt("-maxhmmpf", 2000);
//		c.setInt("-maxwpf", 10);
//		c.setInt("-pl_window", 2);
//		c.setBoolean("-backtrace", true);
		c.setBoolean("-bestpath", false);
		
		c.setString("-CyVoiceE", "CyVoiceE_jsgf"); //
//		c.setString("-CyVoiceE", "CyVoiceE_ngram");
		
		String str = "(打开空调)\n"
				+ "(车内循环)\n"
				+ "(车外循环)\n"
				+ "(关闭空调)\n"
				+ "(打开天窗)\n"
				+ "(关闭天窗)\n"
				+ "(接听)\n"
				+ "(挂断)\n"
				+ "(打电话)\n"
				+ "(打开导航)\n"
				+ "(关闭导航)\n"
				+ "(打开本地音乐)\n"
				+ "(关闭音乐)\n"
				+ "(播放)\n"
				+ "(暂停)\n"
				+ "(收藏)\n"
				+ "(随机播放)\n"
				+ "(单曲循环)\n"
				+ "(全部循环)\n"
				+ "(播放收藏音乐)\n"
				+ "(打开一路享听)\n"
				+ "(打开有声读物)\n"
				+ "(上一台)\n"
				+ "(下一台)\n"
				+ "(打开新闻)\n"
				+ "(打开电台)\n"
				+ "(关闭收音机)\n"
				+ "(自动搜台)\n"
				+ "(内循环)\n"
				+ "(外循环)\n"
				+ "(通讯录)\n"
				+ "(导航)\n"
				+ "(本地音乐)\n"
				+ "(关闭广播)\n"
				+ "(电台)\n"
				+ "(播放收藏列表)\n"
				+ "(一路享听)\n"
				+ "(广播)\n"
				+ "(上一曲)\n"
				+ "(下一曲)\n"
				+ "(有声读物)\n"
				+ "(上一个)\n"
				+ "(下一个)\n"
				+ "(上一条)\n"
				+ "(下一条)\n"
				+ "(新闻)\n"
				+ "(上一首)\n"
				+ "(下一首)\n"
				+ "(打开收音机)\n"
				+ "(打开广播)\n"
				+ "(关闭电台)\n"
				+ "(收音机)\n";
		String navigation = "(最近的中式素菜在哪里)\n"
				+"(最近的自动柜员机在哪里)\n"
				+"(最近的自动取款机怎么走)\n"
				+"(最近的自选商场怎么走)\n"
				+"(最近的综合菜馆怎么走)\n"
				+"(最近的综合餐馆在哪里)\n"
				+"(最近的综合餐厅怎么走)\n"
				+"(最近的综合饭馆在哪里)\n"
				+"(最近的综合饭馆怎么走)\n"
				+"(周边有中国素菜馆吗)\n"
				+"(周边有中石化吗)\n"
				+"(周边有中石油吗)\n"
				+"(周边有中式素菜饭馆吗)\n"
				+"(周边有中式素菜酒楼吗)\n"
				+"(周边有中式素菜吗)\n"
				+"(周边有自动柜员机吗)\n"
				+"(周边有自选商场吗)\n"
				+"(周边有综合餐厅吗)\n"
				+"(周边有综合饭馆吗)\n"
				+"(周围的四星酒店)\n"
				+"(周围的中国石油化工)\n"
				+"(周围哪儿有药房)\n"
				+"(周围能找到中石化吗)\n"
				+"(周边有什么百货商场)\n"
				+"(周边有什么百货商城)\n"
				+"(周边有什么百货商店)\n"
				+"(周边有什么北京菜馆)\n"
				+"(周边有什么北京餐厅)\n"
				+"(周边有什么北京饭店)\n"
				+"(周边有什么北京老字号餐厅)\n"
				+"(周边有什么北京老字号小吃)\n"
				+"(周边有什么北京小吃)\n"
				+"(周边有什么宾馆)\n"
				+"(周边有什么宾馆招待所)\n"
				+"(周边有什么菜馆)\n"
				+"(周边有什么餐馆)\n"
				+"(周边有什么餐厅)\n"
				+"(周边有什么厕所)\n"
				+"(周边有什么超级市场)\n"
				+"(周边有什么超市)\n"
				+"(周边有什么潮州菜馆)\n"
				+"(周边有什么潮州餐厅)\n"
				+"(周边有什么潮州风味餐厅)\n"
				+"(周边有什么吃饭的地方)\n"
				+"(周边有什么川菜馆)\n"
				+"(周边有什么蛋糕店)\n"
				+"(周边有什么蛋糕房)\n"
				+"(周边有拉屎的地方吗)\n"
				+"(周边有老字号餐厅吗)\n"
				+"(周边有老字号风味餐厅吗)\n"
				+"(周边有老字号小吃吗)\n"
				+"(周边有鲁菜馆吗)\n"
				+"(周边有旅店吗)\n"
				+"(周边有旅店招待所吗)\n"
				+"(周边有旅馆吗)\n"
				+"(周边有旅馆招待所吗)\n"
				+"(周边有旅社吗)\n"
				+"(周边有旅社招待所吗)\n"
				+"(周边有买药的地儿吗)\n"
				+"(周边有买药的地方吗)\n"
				+"(我要去附近的宾馆该怎么走)\n"
				+"(我要去附近的宾馆给我导一下吧)\n"
				+"(我要去附近的宾馆招待所)\n"
				+"(我要去附近的菜馆)\n"
				+"(我要去附近的餐馆)\n"
				+"(我要去附近的餐馆该怎么走)\n"
				+"(我要去附近的餐厅)\n"
				+"(我要去附近的餐厅该怎么走)\n"
				+"(我要去附近的餐厅给我导一下吧)\n"
				+"(我要去附近的厕所)\n"
				+"(我要去附近的超级市场)\n"
				+"(我要去附近的超市)\n"
				+"(我要去附近的潮州菜馆)\n"
				+"(我要给车加点油)\n"
				+"(我要喝酒)\n"
				+"(我要吼一嗓子)\n"
				+"(我要汇款)\n"
				+"(我要加点柴油)\n"
				+"(我要加汽油)\n"
				+"(我要加油)\n"
				+"(我要加油怎么走)\n"
				+"(我要看电影)\n"
				+"(我要拉屎)\n"
				+"(我要练歌)\n"
				+"(我要买包包)\n"
				+"(我要买菜)\n"
				+"(我要买点儿菜)\n"
				+"(我要买裤子)\n"
				+"(我要买零食)\n"
				+"(我要买蔬菜)\n"
				+"(我要买水果)\n"
				+"(我要买些水果)\n"
				+"(我要买鞋)\n"
				+"(我要买药)\n"
				+"(我要买衣服)\n"
				+"(我要尿尿)\n"
				+"(我要取钞)\n"
				+"(我要取钞票)\n"
				+"(我要取钱)\n"
				+"(我要取现)\n";
		c.setString("-Custom_jsgf",str);
		c.setInt("-Custom_flag", 2);
		//c.setBoolean("-remove_noise", false);
		
		
		/*
		c.setString("-hmm",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh/tdt_sc_8k");
		c.setString("-dict",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_TW/mandarin_notone.dic");
		c.setString("-lm",
		"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_TW/gigatdt.5000.DMP");
		*/
//		c.setString("-rawlogdir", "/sdcard/Android/data/edu.cmu.pocketsphinx");
//		c.setFloat("-samprate", 8000.0);
//		c.setInt("-maxhmmpf", 2000);
//		c.setInt("-maxwpf", 10);
//		c.setInt("-pl_window", 2);
//		c.setBoolean("-backtrace", true);
//		c.setBoolean("-bestpath", false);


//		c.setInt("-nbest",6);
//		int n = c.getInt("-maxwpf");
//		n = c.getInt("-maxhmmpf");
		
		this.ps = new Decoder(c);
		this.audio = null;
		this.audioq = new LinkedBlockingQueue<short[]>();
		this.use_partials = false;
		this.mailbox = Event.NONE;
	}

	public void run() {
		/* Main loop for this thread. */
		boolean done = false;
		/* State of the main loop. */
		State state = State.IDLE;
		/* Previous partial hypothesis. */
		String partial_hyp = null;
		
		while (!done) {
			/* Read the mail. */
			Event todo = Event.NONE;
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			synchronized (this.mailbox) {
				todo = this.mailbox;
				/* If we're idle then wait for something to happen. */
				if (state == State.IDLE && todo == Event.NONE) {
					try {
						Log.e(getClass().getName(), "waiting");
						this.mailbox.wait();
						todo = this.mailbox;
						Log.e(getClass().getName(), "got" + todo);
					} catch (InterruptedException e) {
						/* Quit main loop. */
						Log.e(getClass().getName(), "Interrupted waiting for mailbox, shutting down");
						todo = Event.SHUTDOWN;
					}
				}
				/* Reset the mailbox before releasing, to avoid race condition. */
				this.mailbox = Event.NONE;
			}
			/* Do whatever the mail says to do. */
			switch (todo) {
			case NONE:
				if (state == State.IDLE)
					Log.e(getClass().getName(), "Received NONE in mailbox when IDLE, threading error?");
				break;
			case START:
				if (state == State.IDLE) { 
					
					//this.ps = new Decoder(this.c);
					
					Log.e(getClass().getName(), "START");
					this.audio = new AudioTask(this.audioq, 1024);
					this.audio_thread = new Thread(this.audio);
					this.ps.startUtt();
					this.audio_thread.start();
					state = State.LISTENING;
				}
				else
					Log.e(getClass().getName(), "Received START in mailbox when LISTENING");
				break;
			case STOP:
				if (state == State.IDLE)
					Log.e(getClass().getName(), "Received STOP in mailbox when IDLE");
				else {
					Log.d(getClass().getName(), "STOP");
					assert this.audio != null;
					this.audio.stop();
					try {
						this.audio_thread.join();
					}
					catch (InterruptedException e) {
						Log.e(getClass().getName(), "Interrupted waiting for audio thread, shutting down");
						done = true;
					}
					/* Drain the audio queue. */
					short[] buf;
					while ((buf = this.audioq.poll()) != null) {
						Log.d(getClass().getName(), "Reading " + buf.length + " samples from queue");
						this.ps.processRaw(buf, buf.length, false, false);
					}
					this.ps.endUtt();
					this.audio = null;
					this.audio_thread = null;
					
					ArrayList<String> nBestResult = new ArrayList<String>();
					String result = null;
					Bundle b = new Bundle();
					Nbest nBestHyp = new Nbest(this.ps);
					int n = 5;//c.getInt("-nbest");
					Hypothesis hyp = this.ps.getHyp();
					
					if (hyp == null) {
						Log.e(getClass().getName(), "Recognition failure");
						
						result = "Recognition failure";
					}
					else {
						
						result = hyp.getHypstr();
						Log.e(getClass().getName(), "Final hypothesis : " + result);
						
					}
					b.putString("hyp", result);
					nBestResult.add(result);
					
					for (int i = 1; i < n && (nBestHyp.next() == false); i++) {
						hyp = nBestHyp.hyp();
						
						if (hyp == null) {
							Log.e(getClass().getName(), "Recognition failure");
							
							result = "Recognition failure";
						}
						else {
							
							result = hyp.getHypstr();
							Log.e(getClass().getName(), "Final hypothesis [ " + i + " ]: " + result);
							
						}
						nBestResult.add(result);
				              
				    }

					b.putStringArrayList("nbest_hyp", nBestResult);
					if (this.rl != null)
						this.rl.onResults(b);
					
					
//					Hypothesis hyp = this.ps.getHyp();
//					if (this.rl != null) {
//						if (hyp == null) {
//							Log.e(getClass().getName(), "Recognition failure");
//							this.rl.onError(-1);
//						}
//						else {
//							Bundle b = new Bundle();
//							Log.e(getClass().getName(), "Final hypothesis: " + hyp.getHypstr());
//							b.putString("hyp", hyp.getHypstr());
//							this.rl.onResults(b);
//						}
//					}
					state = State.IDLE;
				}
				break;
			case SHUTDOWN:
				Log.d(getClass().getName(), "SHUTDOWN");
				if (this.audio != null) {
					this.audio.stop();
					assert this.audio_thread != null;
					try {
						this.audio_thread.join();
					}
					catch (InterruptedException e) {
						/* We don't care! */
					}
				}
				this.ps.endUtt();
				this.audio = null;
				this.audio_thread = null;
				state = State.IDLE;
				done = true;
				break;
			}
			/* Do whatever's appropriate for the current state.  Actually this just means processing audio if possible. */
			if (state == State.LISTENING) {
				assert this.audio != null;
				try {
					short[] buf = this.audioq.take();
					Log.e(getClass().getName(), "State.LISTENING Reading " + buf.length + " samples from queue");
					this.ps.processRaw(buf, buf.length, false, false);
					Hypothesis hyp = this.ps.getHyp();
					if (hyp != null) {
						String hypstr = hyp.getHypstr();
						if (hypstr != partial_hyp) {
							Log.e(getClass().getName(), "Hypothesis: " + hyp.getHypstr());
							if (this.rl != null && hyp != null) {
								Bundle b = new Bundle();
								b.putString("hyp", hyp.getHypstr());
								this.rl.onPartialResults(b);
							}
						}
						partial_hyp = hypstr;
					}
				} catch (InterruptedException e) {
					Log.e(getClass().getName(), "Interrupted in audioq.take");
				}
			}
		}
	}

	public void start() {
		Log.e(getClass().getName(), "signalling START");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Log.e(getClass().getName(), "signalled START");
			this.mailbox = Event.START;
		}
	}

	public void stop() {
		Log.e(getClass().getName(), "signalling STOP");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Log.e(getClass().getName(), "signalled STOP");
			this.mailbox = Event.STOP;
		}
	}

	public void shutdown() {
		Log.d(getClass().getName(), "signalling SHUTDOWN");
		synchronized (this.mailbox) {
			this.mailbox.notifyAll();
			Log.d(getClass().getName(), "signalled SHUTDOWN");
			this.mailbox = Event.SHUTDOWN;
		}
	}
}