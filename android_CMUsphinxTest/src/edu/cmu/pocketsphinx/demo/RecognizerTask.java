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
		c.setInt("-Custom_flag", 0);//ʶ��Ӣ����
		
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
		
		String str = "(�򿪿յ�)\n"
				+ "(����ѭ��)\n"
				+ "(����ѭ��)\n"
				+ "(�رտյ�)\n"
				+ "(���촰)\n"
				+ "(�ر��촰)\n"
				+ "(����)\n"
				+ "(�Ҷ�)\n"
				+ "(��绰)\n"
				+ "(�򿪵���)\n"
				+ "(�رյ���)\n"
				+ "(�򿪱�������)\n"
				+ "(�ر�����)\n"
				+ "(����)\n"
				+ "(��ͣ)\n"
				+ "(�ղ�)\n"
				+ "(�������)\n"
				+ "(����ѭ��)\n"
				+ "(ȫ��ѭ��)\n"
				+ "(�����ղ�����)\n"
				+ "(��һ·����)\n"
				+ "(����������)\n"
				+ "(��һ̨)\n"
				+ "(��һ̨)\n"
				+ "(������)\n"
				+ "(�򿪵�̨)\n"
				+ "(�ر�������)\n"
				+ "(�Զ���̨)\n"
				+ "(��ѭ��)\n"
				+ "(��ѭ��)\n"
				+ "(ͨѶ¼)\n"
				+ "(����)\n"
				+ "(��������)\n"
				+ "(�رչ㲥)\n"
				+ "(��̨)\n"
				+ "(�����ղ��б�)\n"
				+ "(һ·����)\n"
				+ "(�㲥)\n"
				+ "(��һ��)\n"
				+ "(��һ��)\n"
				+ "(��������)\n"
				+ "(��һ��)\n"
				+ "(��һ��)\n"
				+ "(��һ��)\n"
				+ "(��һ��)\n"
				+ "(����)\n"
				+ "(��һ��)\n"
				+ "(��һ��)\n"
				+ "(��������)\n"
				+ "(�򿪹㲥)\n"
				+ "(�رյ�̨)\n"
				+ "(������)\n";
		String navigation = "(�������ʽ�ز�������)\n"
				+"(������Զ���Ա��������)\n"
				+"(������Զ�ȡ�����ô��)\n"
				+"(�������ѡ�̳���ô��)\n"
				+"(������ۺϲ˹���ô��)\n"
				+"(������ۺϲ͹�������)\n"
				+"(������ۺϲ�����ô��)\n"
				+"(������ۺϷ���������)\n"
				+"(������ۺϷ�����ô��)\n"
				+"(�ܱ����й��ز˹���)\n"
				+"(�ܱ�����ʯ����)\n"
				+"(�ܱ�����ʯ����)\n"
				+"(�ܱ�����ʽ�ز˷�����)\n"
				+"(�ܱ�����ʽ�ز˾�¥��)\n"
				+"(�ܱ�����ʽ�ز���)\n"
				+"(�ܱ����Զ���Ա����)\n"
				+"(�ܱ�����ѡ�̳���)\n"
				+"(�ܱ����ۺϲ�����)\n"
				+"(�ܱ����ۺϷ�����)\n"
				+"(��Χ�����ǾƵ�)\n"
				+"(��Χ���й�ʯ�ͻ���)\n"
				+"(��Χ�Ķ���ҩ��)\n"
				+"(��Χ���ҵ���ʯ����)\n"
				+"(�ܱ���ʲô�ٻ��̳�)\n"
				+"(�ܱ���ʲô�ٻ��̳�)\n"
				+"(�ܱ���ʲô�ٻ��̵�)\n"
				+"(�ܱ���ʲô�����˹�)\n"
				+"(�ܱ���ʲô��������)\n"
				+"(�ܱ���ʲô��������)\n"
				+"(�ܱ���ʲô�������ֺŲ���)\n"
				+"(�ܱ���ʲô�������ֺ�С��)\n"
				+"(�ܱ���ʲô����С��)\n"
				+"(�ܱ���ʲô����)\n"
				+"(�ܱ���ʲô�����д���)\n"
				+"(�ܱ���ʲô�˹�)\n"
				+"(�ܱ���ʲô�͹�)\n"
				+"(�ܱ���ʲô����)\n"
				+"(�ܱ���ʲô����)\n"
				+"(�ܱ���ʲô�����г�)\n"
				+"(�ܱ���ʲô����)\n"
				+"(�ܱ���ʲô���ݲ˹�)\n"
				+"(�ܱ���ʲô���ݲ���)\n"
				+"(�ܱ���ʲô���ݷ�ζ����)\n"
				+"(�ܱ���ʲô�Է��ĵط�)\n"
				+"(�ܱ���ʲô���˹�)\n"
				+"(�ܱ���ʲô�����)\n"
				+"(�ܱ���ʲô���ⷿ)\n"
				+"(�ܱ�����ʺ�ĵط���)\n"
				+"(�ܱ������ֺŲ�����)\n"
				+"(�ܱ������ֺŷ�ζ������)\n"
				+"(�ܱ������ֺ�С����)\n"
				+"(�ܱ���³�˹���)\n"
				+"(�ܱ����õ���)\n"
				+"(�ܱ����õ��д�����)\n"
				+"(�ܱ����ù���)\n"
				+"(�ܱ����ù��д�����)\n"
				+"(�ܱ���������)\n"
				+"(�ܱ��������д�����)\n"
				+"(�ܱ�����ҩ�ĵض���)\n"
				+"(�ܱ�����ҩ�ĵط���)\n"
				+"(��Ҫȥ�����ı��ݸ���ô��)\n"
				+"(��Ҫȥ�����ı��ݸ��ҵ�һ�°�)\n"
				+"(��Ҫȥ�����ı����д���)\n"
				+"(��Ҫȥ�����Ĳ˹�)\n"
				+"(��Ҫȥ�����Ĳ͹�)\n"
				+"(��Ҫȥ�����Ĳ͹ݸ���ô��)\n"
				+"(��Ҫȥ�����Ĳ���)\n"
				+"(��Ҫȥ�����Ĳ�������ô��)\n"
				+"(��Ҫȥ�����Ĳ������ҵ�һ�°�)\n"
				+"(��Ҫȥ�����Ĳ���)\n"
				+"(��Ҫȥ�����ĳ����г�)\n"
				+"(��Ҫȥ�����ĳ���)\n"
				+"(��Ҫȥ�����ĳ��ݲ˹�)\n"
				+"(��Ҫ�����ӵ���)\n"
				+"(��Ҫ�Ⱦ�)\n"
				+"(��Ҫ��һɤ��)\n"
				+"(��Ҫ���)\n"
				+"(��Ҫ�ӵ����)\n"
				+"(��Ҫ������)\n"
				+"(��Ҫ����)\n"
				+"(��Ҫ������ô��)\n"
				+"(��Ҫ����Ӱ)\n"
				+"(��Ҫ��ʺ)\n"
				+"(��Ҫ����)\n"
				+"(��Ҫ�����)\n"
				+"(��Ҫ���)\n"
				+"(��Ҫ������)\n"
				+"(��Ҫ�����)\n"
				+"(��Ҫ����ʳ)\n"
				+"(��Ҫ���߲�)\n"
				+"(��Ҫ��ˮ��)\n"
				+"(��Ҫ��Щˮ��)\n"
				+"(��Ҫ��Ь)\n"
				+"(��Ҫ��ҩ)\n"
				+"(��Ҫ���·�)\n"
				+"(��Ҫ����)\n"
				+"(��Ҫȡ��)\n"
				+"(��Ҫȡ��Ʊ)\n"
				+"(��ҪȡǮ)\n"
				+"(��Ҫȡ��)\n";
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