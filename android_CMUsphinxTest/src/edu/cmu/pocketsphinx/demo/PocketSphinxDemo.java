package edu.cmu.pocketsphinx.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import com.example.cmusphinxtest_1.R;
import com.ztspeech.asrtest.ASRInterface;
import com.ztspeech.asrtest.OnlineASR;

import edu.cmu.pocketsphinx.testSphinxModel;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PocketSphinxDemo extends Activity implements OnTouchListener, RecognitionListener,testModelListener {
	
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;
	
	/*
	 *  ������ԵĲ�����������ֵ
	 */
	EditText edit_text_parameter_key = null;
	EditText edit_text_parameter_value = null;
	
	
	PocketSphinxDemo mPocketSphinxDemo = null;
	
	AssetManager assetMgr = null;
	String modelDir = null;
	
	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.Button01)
		{
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if(this.rec == null)
				{
					CommonFun.logPath = "8k_raw_" + edit_text.getText().toString();
					this.rec = new RecognizerTask();
					this.rec_thread = new Thread(this.rec);
					this.listening = false;
					
					
					this.rec.setRecognitionListener(this);
					this.rec_thread.start();
				}
				
				
				
				
				
				start_date = new Date();
				this.listening = true;
				this.rec.start();
				break;
			case MotionEvent.ACTION_UP:
				Date end_date = new Date();
				long nmsec = end_date.getTime() - start_date.getTime();
				this.speech_dur = (float)nmsec / 1000;
				if (this.listening) {
					Log.d(getClass().getName(), "Showing Dialog");
					this.rec_dialog = ProgressDialog.show(PocketSphinxDemo.this, "", "Recognizing speech...", true);
					this.rec_dialog.setCancelable(false);
					this.listening = false;
				}
				this.rec.stop();
				break;
			default:
				;
			}
		}
//		else if(v.getId() == R.id.Button_jsgf52)
//		{
//			if(event.getAction() == MotionEvent.ACTION_UP){  
//                Log.d("test", "cansal button ---> cancel"); 
//                
////                testSphinxModel.test(
////        				"/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/test_sphinx_config.xml",
////        				"/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest.log", 
////        				"/sdcard/Android/data/edu.cmu.pocketsphinx/hmm/zh_test/digitDemo.cd_cont_200", 
////        				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test/digitDemo.lm.DMP", 
////        				"/sdcard/Android/data/edu.cmu.pocketsphinx/lm/zh_test/digitDemo.dic");
//                
//            }   
//            if(event.getAction() == MotionEvent.ACTION_DOWN){  
//                Log.d("test", "cansal button ---> down");  
//                
//            } 
//		}
		
		/* Let the button handle its own state */
		return false;
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mPocketSphinxDemo = this;
		
		assetMgr = getAssets();
		modelDir = getSDPath();
		
		
		
		
		
//		this.rec = new RecognizerTask();
//		this.rec_thread = new Thread(this.rec);
//		this.listening = false;
		
		Button b3 = (Button) findViewById(R.id.Button_jsgf52);
		b3.setOnTouchListener(this);
		Button b = (Button) findViewById(R.id.Button01);
		b.setOnTouchListener(this);
		this.performance_text = (TextView) findViewById(R.id.PerformanceText);
		this.edit_text = (EditText) findViewById(R.id.EditText01);
		
		
//		this.rec.setRecognitionListener(this);
//		this.rec_thread.start();
		
		this.edit_text_parameter_key = (EditText)findViewById(R.id.edit_parameter_key_id);
		this.edit_text_parameter_value = (EditText)findViewById(R.id.edit_parameter_value_id);
		
//		this.rec = new RecognizerTask();
//		this.rec_thread = new Thread(this.rec);
//		this.listening = false;
//				
//		this.rec.setRecognitionListener(this);
//		this.rec_thread.start();
		
		if(this.rec == null)
		{
			CommonFun.logPath = "8k_raw_" + edit_text.getText().toString();
			this.rec = new RecognizerTask();
			this.rec_thread = new Thread(this.rec);
			this.listening = false;
			
			
			this.rec.setRecognitionListener(this);
			this.rec_thread.start();
		}
	}
	
	/** Called when the activity is first created. */
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
//
//		Button b = (Button) findViewById(R.id.Button01);
//		b.setOnTouchListener(this);
//		this.performance_text = (TextView) findViewById(R.id.PerformanceText);
//		this.edit_text = (EditText) findViewById(R.id.EditText01);
//
//	}

	/*
	 * �����ť����ʼ����
	 * 
	 * */
	public void onTestButtonClicked(View view)
	{
//		//�����ʾ
//		edit_text.setText("");
//		
//		//���Ӧ�õ������ģ���������������Ӧ�ã���ò��õ�ǰACTIVITY
//		Context c = getApplicationContext();
//		//��������
//		new testFileDataThread(c,mhandler);
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("vcmd");
				testModel0.set_testModelListener(mPocketSphinxDemo);
				
//				"monophone.am","keyword.txt","g2p.word"
				String path = getSDPath();
				
				testModel0.startTest(
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_VCMD_config_android.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_vcmd.log", 
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/VCMD/monophone.am", 
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/VCMD/keyword.txt", 
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/VCMD/g2p.word");
				
			}
			
		};
		h.postDelayed(r, 10);
	}
	
	public Handler mhandler = new Handler()
	{
		@Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        
	        Bundle data = msg.getData();
	        String result = data.getString("result");
	        
	        edit_text.setText(result);
		}
	};
	
	public void onTest_ngram52ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String path = getSDPath();
				
				testModel0.startTest(
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_sphinx_config_android.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram52.log", 
						path + "/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k", 
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/ngram_52.lm.DMP", /* command.gram  command.fsg */
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/mandarin_notone_new.dic");
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	
	public void onTest_ngram100ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String path = getSDPath();
				testModel0.startTest(
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_sphinx_config_android.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram100.log", 
						path + "/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k", 
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/ngram_100.lm.DMP", /* command.gram  command.fsg */
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/mandarin_notone_new.dic");
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	public void onTest_ngram200ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String path = getSDPath();
				
				testModel0.startTest(
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_sphinx_config_android.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram200.log", 
						path + "/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k", 
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/ngram_200.lm.DMP", /* command.gram  command.fsg */
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/mandarin_notone_new.dic");
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	public void onTest_ngram500ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String path = getSDPath();
				
				testModel0.startTest(
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_sphinx_config_android.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						path + "/Android/data/edu.cmu.pocketsphinx/hmm/test/tdt_sc_8k", 
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/ngram_500.lm.DMP", /* command.gram  command.fsg */
						path + "/Android/data/edu.cmu.pocketsphinx/lm/test/mandarin_notone_new.dic");
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	public void onTest_jsgf52ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

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
				
				String name = "(Ӣ����ɽ)\n"
						+"(��Ԫ)\n"
						+"(³ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�ų���)\n"
						+"(����)\n"
						+"(�ޱ���)\n"
						+"(��ϲ��)\n"
						+"(������)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(���ų�չ)\n"
						+"(����)\n"
						+"(��Ȼ)\n"
						+"(�Ǻ���)\n"
						+"(�Ϸ�)\n"
						+"(��ϼ)\n"
						+"(��չ)\n"
						+"(�þ�)\n"
						+"(�캣��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(������)\n"
						+"(Ф���)\n"
						+"(Էһ��)\n"
						+"(��������)\n"
						+"(���Ͽ�)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���)\n"
						+"(�����ִ�)\n"
						+"(����)\n"
						+"(���Ҷ)\n"
						+"(ʷ��)\n"
						+"(��ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(���)\n"
						+"(������)\n"
						+"(����)\n"
						+"(��Т��)\n"
						+"(���ƻ�)\n"
						+"(��͢��)\n"
						+"(��������)\n"
						+"(�����)\n"
						+"(�Կ���)\n"
						+"(������)\n"
						+"(ְ��)\n"
						+"(������ͬ)\n"
						+"(����)\n"
						+"(��ҵ��)\n"
						+"(����)\n"
						+"(�Ź���)\n"
						+"(��������)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����Сĥ)\n"
						+"(���)\n"
						+"(��־��)\n"
						+"(��Ц��)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���л�)\n"
						+"(����)\n"
						+"(֣����)\n"
						+"(����С��)\n"
						+"(����ʦ)\n"
						+"(����ï)\n"
						+"(������)\n"
						+"(�ٴ�ү)\n"
						+"(����R)\n"
						+"(�����)\n"
						+"(������)\n"
						+"(��ά��)\n"
						+"(������)\n"
						+"(���ɻ�)\n"
						+"(˧��Ϊ)\n"
						+"(����)\n"
						+"(�ƺ���)\n"
						+"(��Զ��)\n"
						+"(����)\n"
						+"(��־��)\n"
						+"(Τ���)\n"
						+"(������)\n"
						+"(��С��)\n"
						+"(�̰���)\n"
						+"(��ݲܳ�)\n"
						+"(������)\n"
						+"(���Ѱ���)\n"
						+"(�￪����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(Ǯ����)\n"
						+"(ʯ��ƽ)\n";
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
				String command = "(�򿪿յ�)\n"
						+"(����ѭ��)\n"
						+"(����ѭ��)\n"
						+"(�رտյ�)\n"
						+"(���촰)\n"
						+"(�ر��촰)\n"
						+"(����)\n"
						+"(�Ҷ�)\n"
						+"(��绰)\n"
						+"(�򿪵���)\n"
						+"(�رյ���)\n"
						+"(�򿪱�������)\n"
						+"(�ر�����)\n"
						+"(����)\n"
						+"(��ͣ)\n"
						+"(�ղ�)\n"
						+"(�������)\n"
						+"(����ѭ��)\n"
						+"(ȫ��ѭ��)\n"
						+"(�����ղ�����)\n"
						+"(��һ·����)\n"
						+"(����������)\n"
						+"(��һ̨)\n"
						+"(��һ̨)\n"
						+"(������)\n"
						+"(�򿪵�̨)\n"
						+"(�ر�������)\n"
						+"(�Զ���̨)\n"
						+"(��ѭ��)\n"
						+"(��ѭ��)\n"
						+"(ͨѶ¼)\n"
						+"(����)\n"
						+"(��������)\n"
						+"(�رչ㲥)\n"
						+"(��̨)\n"
						+"(�����ղ��б�)\n"
						+"(һ·����)\n"
						+"(�㲥)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(����)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(�򿪹㲥)\n"
						+"(�رյ�̨	)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(�򿪵�ͼ)\n"
						+"(��ͼ)\n"
						+"(ȡ������)\n"
						+"(�رյ�ͼ)\n"
						+"(�˳�����)\n"
						+"(�˳���ͼ)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(M.P.��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(ȡ������)\n"
						+"(��ͣ����)\n"
						+"(ֹͣ����)\n"
						+"(�˳�����)\n"
						+"(����Ƶ)\n"
						+"(����Ƶ)\n"
						+"(������Ƶ)\n"
						+"(����Ƶ)\n"
						+"(����Ӱ)\n"
						+"(��Ƭ)\n"
						+"(�ر���Ƶ)\n"
						+"(ȡ����Ƶ)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(A.��D.P.)\n"
						+"(��������)\n"
						+"(����M.P.��)\n"
						+"(�ر���������)\n"
						+"(ȡ����������)\n"
						+"(��ͣ��������)\n"
						+"(ֹͣ��������)\n"
						+"(�˳���������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�޸�����)\n"
						+"(��������)\n"
						+"(�趨)\n"
						+"(�޸��趨)\n"
						+"(����)\n"
						+"(�ر�����)\n"
						+"(�������)\n"
						+"(�˳�����)\n";
				
				String key = edit_text_parameter_key.getText().toString();
				String value = edit_text_parameter_value.getText().toString();
				String str1 = null;
				if(key.contains("name") == true)
				{
					str1 = str + name;
				}
				else if(key.contains("navigation") == true)
				{
					str1 = str + navigation;
				}
				else if(key.contains("command") == true)
				{
					str1 = str + command;
				}
				
				String path = getSDPath();
				
				String xmlFile = edit_text_parameter_key.getHint().toString() + key;
				testModel0.startTest(
						xmlFile,
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf52.log", 
						null, 
						str1, /* command.gram  command.fsg */
						null);
				
//				String key = edit_text_parameter_key.getText().toString();
//				String value = edit_text_parameter_value.getText().toString();
//				
//				testModel0.startTest(
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf52.log", 
//						null, 
//						str, /* command.gram  command.fsg */
//						null,
//						key,value);
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	public void onTest_jsgf100ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

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
						+ "(������)\n"
						+ "(�򿪵���){�򿪵���}\n"
						+ "(��������){�򿪵���}\n"
						+ "(�򿪵�ͼ){�򿪵���}\n"
						+ "(��ͼ){�򿪵���}\n"
						+ "(����){�򿪵���}\n"
						+ "(�رյ���){�رյ���}\n"
						+ "(ȡ������){�رյ���}\n"
						+ "(�رյ�ͼ){�رյ���}\n"
						+ "(�˳�����){�رյ���}\n"
						+ "(�˳���ͼ){�رյ���}\n"
						+ "(������){������}\n"
						+ "(��������){������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(M.P.��){������}\n"
						+ "(����){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n"
						+ "(ȡ������){�ر�����}\n"
						+ "(��ͣ����){�ر�����}\n"
						+ "(ֹͣ����){�ر�����}\n"
						+ "(�˳�����){�ر�����}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(������Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ӱ){����Ƶ}\n"
						+ "(��Ƭ){����Ƶ}\n"
						+ "(�ر���Ƶ){�ر���Ƶ}\n"
						+ "(ȡ����Ƶ){�ر���Ƶ}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(A.��D.P.){����������}\n"
						+ "(��������){����������}\n"
						+ "(����M.P.��){����������}\n"
						+ "(�ر���������){�ر���������}\n"
						+ "(ȡ����������){�ر���������}\n"
						+ "(��ͣ��������){�ر���������}\n"
						+ "(ֹͣ��������){�ر���������}\n"
						+ "(�˳���������){�ر���������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(�޸�����){������}\n"
						+ "(��������){������}\n"
						+ "(�趨){������}\n"
						+ "(�޸��趨){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n";
				
				
				String name = "(Ӣ����ɽ)\n"
						+"(��Ԫ)\n"
						+"(³ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�ų���)\n"
						+"(����)\n"
						+"(�ޱ���)\n"
						+"(��ϲ��)\n"
						+"(������)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(���ų�չ)\n"
						+"(����)\n"
						+"(��Ȼ)\n"
						+"(�Ǻ���)\n"
						+"(�Ϸ�)\n"
						+"(��ϼ)\n"
						+"(��չ)\n"
						+"(�þ�)\n"
						+"(�캣��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(������)\n"
						+"(Ф���)\n"
						+"(Էһ��)\n"
						+"(��������)\n"
						+"(���Ͽ�)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���)\n"
						+"(�����ִ�)\n"
						+"(����)\n"
						+"(���Ҷ)\n"
						+"(ʷ��)\n"
						+"(��ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(���)\n"
						+"(������)\n"
						+"(����)\n"
						+"(��Т��)\n"
						+"(���ƻ�)\n"
						+"(��͢��)\n"
						+"(��������)\n"
						+"(�����)\n"
						+"(�Կ���)\n"
						+"(������)\n"
						+"(ְ��)\n"
						+"(������ͬ)\n"
						+"(����)\n"
						+"(��ҵ��)\n"
						+"(����)\n"
						+"(�Ź���)\n"
						+"(��������)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����Сĥ)\n"
						+"(���)\n"
						+"(��־��)\n"
						+"(��Ц��)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���л�)\n"
						+"(����)\n"
						+"(֣����)\n"
						+"(����С��)\n"
						+"(����ʦ)\n"
						+"(����ï)\n"
						+"(������)\n"
						+"(�ٴ�ү)\n"
						+"(����R)\n"
						+"(�����)\n"
						+"(������)\n"
						+"(��ά��)\n"
						+"(������)\n"
						+"(���ɻ�)\n"
						+"(˧��Ϊ)\n"
						+"(����)\n"
						+"(�ƺ���)\n"
						+"(��Զ��)\n"
						+"(����)\n"
						+"(��־��)\n"
						+"(Τ���)\n"
						+"(������)\n"
						+"(��С��)\n"
						+"(�̰���)\n"
						+"(��ݲܳ�)\n"
						+"(������)\n"
						+"(���Ѱ���)\n"
						+"(�￪����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(Ǯ����)\n"
						+"(ʯ��ƽ)\n";
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
				String command = "(�򿪿յ�)\n"
						+"(����ѭ��)\n"
						+"(����ѭ��)\n"
						+"(�رտյ�)\n"
						+"(���촰)\n"
						+"(�ر��촰)\n"
						+"(����)\n"
						+"(�Ҷ�)\n"
						+"(��绰)\n"
						+"(�򿪵���)\n"
						+"(�رյ���)\n"
						+"(�򿪱�������)\n"
						+"(�ر�����)\n"
						+"(����)\n"
						+"(��ͣ)\n"
						+"(�ղ�)\n"
						+"(�������)\n"
						+"(����ѭ��)\n"
						+"(ȫ��ѭ��)\n"
						+"(�����ղ�����)\n"
						+"(��һ·����)\n"
						+"(����������)\n"
						+"(��һ̨)\n"
						+"(��һ̨)\n"
						+"(������)\n"
						+"(�򿪵�̨)\n"
						+"(�ر�������)\n"
						+"(�Զ���̨)\n"
						+"(��ѭ��)\n"
						+"(��ѭ��)\n"
						+"(ͨѶ¼)\n"
						+"(����)\n"
						+"(��������)\n"
						+"(�رչ㲥)\n"
						+"(��̨)\n"
						+"(�����ղ��б�)\n"
						+"(һ·����)\n"
						+"(�㲥)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(����)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(�򿪹㲥)\n"
						+"(�رյ�̨	)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(�򿪵�ͼ)\n"
						+"(��ͼ)\n"
						+"(ȡ������)\n"
						+"(�رյ�ͼ)\n"
						+"(�˳�����)\n"
						+"(�˳���ͼ)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(M.P.��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(ȡ������)\n"
						+"(��ͣ����)\n"
						+"(ֹͣ����)\n"
						+"(�˳�����)\n"
						+"(����Ƶ)\n"
						+"(����Ƶ)\n"
						+"(������Ƶ)\n"
						+"(����Ƶ)\n"
						+"(����Ӱ)\n"
						+"(��Ƭ)\n"
						+"(�ر���Ƶ)\n"
						+"(ȡ����Ƶ)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(A.��D.P.)\n"
						+"(��������)\n"
						+"(����M.P.��)\n"
						+"(�ر���������)\n"
						+"(ȡ����������)\n"
						+"(��ͣ��������)\n"
						+"(ֹͣ��������)\n"
						+"(�˳���������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�޸�����)\n"
						+"(��������)\n"
						+"(�趨)\n"
						+"(�޸��趨)\n"
						+"(����)\n"
						+"(�ر�����)\n"
						+"(�������)\n"
						+"(�˳�����)\n";
				
				String key = edit_text_parameter_key.getText().toString();
				String value = edit_text_parameter_value.getText().toString();
				String str1 = null;
				if(key.contains("name") == true)
				{
					str1 = str + name;
				}
				else if(key.contains("navigation") == true)
				{
					str1 = str + navigation;
				}
				else if(key.contains("command") == true)
				{
					str1 = str + command;
				}
				
				String path = getSDPath();
				
				String xmlFile = edit_text_parameter_key.getHint().toString() + key;
				testModel0.startTest(
						xmlFile,
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf100.log", 
						null, 
						str1, /* command.gram  command.fsg */
						null);
				
//				String key = edit_text_parameter_key.getText().toString();
//				String value = edit_text_parameter_value.getText().toString();
//				testModel0.startTest(
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf100.log", 
//						null, 
//						str, /* command.gram  command.fsg */
//						null,
//						key,value);
				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}

	public void onTest_jsgf200ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				
				String str =  "(�򿪿յ�)\n"
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
						+ "(������)\n"
						+ "(�򿪵���){�򿪵���}\n"
						+ "(��������){�򿪵���}\n"
						+ "(�򿪵�ͼ){�򿪵���}\n"
						+ "(��ͼ){�򿪵���}\n"
						+ "(����){�򿪵���}\n"
						+ "(�رյ���){�رյ���}\n"
						+ "(ȡ������){�رյ���}\n"
						+ "(�رյ�ͼ){�رյ���}\n"
						+ "(�˳�����){�رյ���}\n"
						+ "(�˳���ͼ){�رյ���}\n"
						+ "(������){������}\n"
						+ "(��������){������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(M.P.��){������}\n"
						+ "(����){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n"
						+ "(ȡ������){�ر�����}\n"
						+ "(��ͣ����){�ر�����}\n"
						+ "(ֹͣ����){�ر�����}\n"
						+ "(�˳�����){�ر�����}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(������Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ӱ){����Ƶ}\n"
						+ "(��Ƭ){����Ƶ}\n"
						+ "(�ر���Ƶ){�ر���Ƶ}\n"
						+ "(ȡ����Ƶ){�ر���Ƶ}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(A.��D.P.){����������}\n"
						+ "(��������){����������}\n"
						+ "(����M.P.��){����������}\n"
						+ "(�ر���������){�ر���������}\n"
						+ "(ȡ����������){�ر���������}\n"
						+ "(��ͣ��������){�ر���������}\n"
						+ "(ֹͣ��������){�ر���������}\n"
						+ "(�˳���������){�ر���������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(�޸�����){������}\n"
						+ "(��������){������}\n"
						+ "(�趨){������}\n"
						+ "(�޸��趨){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n"
						+ "(�������){�ر�����}\n"
						+ "(�˳�����){�ر�����}\n"
						+ "(��������){��������}\n"
						+ "(������){��������}\n"
						+ "(��������){��������}\n"
						+ "(������){��������}\n"
						+ "(��Ƶ){��������}\n"
						+ "(A.M.){��������}\n"
						+ "(F.M.){��������}\n"
						+ "(������){��������}\n"
						+ "(���㲥){��������}\n"
						+ "(�����㲥){��������}\n"
						+ "(�㲥){��������}\n"
						+ "(������){��������}\n"
						+ "(�ر�������){�ر�������}\n"
						+ "(��������){�ر�������}\n"
						+ "(�˳�������){�ر�������}\n"
						+ "(�رչ㲥){�ر�������}\n"
						+ "(���г���¼��){���г���¼��}\n"
						+ "(�����г���¼��){���г���¼��}\n"
						+ "(����¼��){���г���¼��}\n"
						+ "(��D.V.R.){���г���¼��}\n"
						+ "(����¼��){���г���¼��}\n"
						+ "(D.V.R.){���г���¼��}\n"
						+ "(�г�¼��){���г���¼��}\n"
						+ "(¼��){���г���¼��}\n"
						+ "(�����г�¼��){���г���¼��}\n"
						+ "(�����г���¼){���г���¼��}\n"
						+ "(�ر��г���¼��){�ر��г���¼��}\n"
						+ "(�˳��г���¼��){�ر��г���¼��}\n"
						+ "(�ر�D.V.R.){�ر��г���¼��}\n"
						+ "(�ؼ�¼��){�ر��г���¼��}\n"
						+ "(�˳��г�¼��){�ر��г���¼��}\n"
						+ "(������){������}\n"
						+ "(��������){������}\n"
						+ "(��������){������}\n"
						+ "(����){������}\n"
						+ "(�绰){������}\n"
						+ "(����){������}\n"
						+ "(��绰){������}\n"
						+ "(�����ֻ�){������}\n"
						+ "(�����ֻ�){������}\n"
						+ "(�˳�����){�˳�����}\n"
						+ "(�ر�����){�˳�����}\n"
						+ "(�˳������ֻ�){�˳�����}\n"
						+ "(ȡ���ֻ�����){�˳�����}\n"
						+ "(ȡ����������){�˳�����}\n"
						+ "(��D.T.V.){��DTV}\n"
						+ "(�򿪵���){��DTV}\n"
						+ "(���ֵ���){��DTV}\n"
						+ "(���ص���){��DTV}\n"
						+ "(��C.M.M.B.){��DTV}\n"
						+ "(����){��DTV}\n"
						+ "(�ر�D.T.V.){�ر�DTV}\n"
						+ "(�رյ���){�ر�DTV}\n"
						+ "(�رճ��ص���){�ر�DTV}\n"
						+ "(�ر�C.M.M.B.){�ر�DTV}\n"
						+ "(�˳�D.T.V.){�ر�DTV}\n"
						+ "(���ֻ�����){���ֻ�����}\n"
						+ "(�ֻ�����){���ֻ�����}\n"
						+ "(��M.H.L.){���ֻ�����}\n"
						+ "(�򿪻���){���ֻ�����}\n"
						+ "(�ر��ֻ�����){�ر��ֻ�����}\n"
						+ "(�ر�M.H.L.){�ر��ֻ�����}\n"
						+ "(�رջ���){�ر��ֻ�����}\n"
						+ "(�Ͽ�����){�ر��ֻ�����}\n"
						+ "(�˳��ֻ�����){�ر��ֻ�����}\n"
						+ "(��A.U.X.){��AUX}\n"
						+ "(�򿪰¿�˹){��AUX}\n"
						+ "(����Ƶ����){��AUX}\n"
						+ "(�¿�˹){��AUX}\n"
						+ "(�ر�A.U.X.){�ر�AUX}\n"
						+ "(�رհ¿�˹){�ر�AUX}\n"
						+ "(�ر���Ƶ����){�ر�AUX}\n"
						+ "(�˳��¿�˹){�ر�AUX}\n"
						+ "(�Ͽ��¿�˹){�ر�AUX}\n"
						+ "(ȷ��){ȷ��}\n"
						+ "(ȡ��){ȡ��}\n"
						+ "(����){��������_�в���_�Ĵ���}\n"
						+ "(���){��������_�в���_���ղ�}\n"
						+ "(���){��������_�������_�����}\n"
						+ "(���){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�������_̩��_Խ�ϲ�Ʒ����}\n"
						+ "(������){��������_�в���_���ϲ�}\n"
						+ "(�����){��������_�������_�¹���}\n"
						+ "(�����){��������_�в���_���ղ�}\n"
						+ "(�嵭��){��������_�в���_��ʽ�ز˹�}\n"
						+ "(���ʵ�){��������_�в���_���ղ�}\n"
						+ "(�����){��������_�в���_�Ĵ���}\n"
						+ "(�����){��������_�в���_�Ĵ���}\n"
						+ "(�����){��������_�в���_�㽭��}\n"
						+ "(΢����){��������_�в���_�Ĵ���}\n"
						+ "(���̵�){��������_�в���_������}\n"
						+ "(�����){��������_�в���_ɽ����}\n"
						+ "(��ˬ��){��������_�в���_��ʽ�ز˹�}\n"
						+ "(��̬����){��������_�в���_�Ĵ���}\n";
				
				String name = "(Ӣ����ɽ)\n"
						+"(��Ԫ)\n"
						+"(³ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�ų���)\n"
						+"(����)\n"
						+"(�ޱ���)\n"
						+"(��ϲ��)\n"
						+"(������)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(���ų�չ)\n"
						+"(����)\n"
						+"(��Ȼ)\n"
						+"(�Ǻ���)\n"
						+"(�Ϸ�)\n"
						+"(��ϼ)\n"
						+"(��չ)\n"
						+"(�þ�)\n"
						+"(�캣��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(������)\n"
						+"(Ф���)\n"
						+"(Էһ��)\n"
						+"(��������)\n"
						+"(���Ͽ�)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���)\n"
						+"(�����ִ�)\n"
						+"(����)\n"
						+"(���Ҷ)\n"
						+"(ʷ��)\n"
						+"(��ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(���)\n"
						+"(������)\n"
						+"(����)\n"
						+"(��Т��)\n"
						+"(���ƻ�)\n"
						+"(��͢��)\n"
						+"(��������)\n"
						+"(�����)\n"
						+"(�Կ���)\n"
						+"(������)\n"
						+"(ְ��)\n"
						+"(������ͬ)\n"
						+"(����)\n"
						+"(��ҵ��)\n"
						+"(����)\n"
						+"(�Ź���)\n"
						+"(��������)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����Сĥ)\n"
						+"(���)\n"
						+"(��־��)\n"
						+"(��Ц��)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���л�)\n"
						+"(����)\n"
						+"(֣����)\n"
						+"(����С��)\n"
						+"(����ʦ)\n"
						+"(����ï)\n"
						+"(������)\n"
						+"(�ٴ�ү)\n"
						+"(����R)\n"
						+"(�����)\n"
						+"(������)\n"
						+"(��ά��)\n"
						+"(������)\n"
						+"(���ɻ�)\n"
						+"(˧��Ϊ)\n"
						+"(����)\n"
						+"(�ƺ���)\n"
						+"(��Զ��)\n"
						+"(����)\n"
						+"(��־��)\n"
						+"(Τ���)\n"
						+"(������)\n"
						+"(��С��)\n"
						+"(�̰���)\n"
						+"(��ݲܳ�)\n"
						+"(������)\n"
						+"(���Ѱ���)\n"
						+"(�￪����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(Ǯ����)\n"
						+"(ʯ��ƽ)\n";
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
				String command = "(�򿪿յ�)\n"
						+"(����ѭ��)\n"
						+"(����ѭ��)\n"
						+"(�رտյ�)\n"
						+"(���촰)\n"
						+"(�ر��촰)\n"
						+"(����)\n"
						+"(�Ҷ�)\n"
						+"(��绰)\n"
						+"(�򿪵���)\n"
						+"(�رյ���)\n"
						+"(�򿪱�������)\n"
						+"(�ر�����)\n"
						+"(����)\n"
						+"(��ͣ)\n"
						+"(�ղ�)\n"
						+"(�������)\n"
						+"(����ѭ��)\n"
						+"(ȫ��ѭ��)\n"
						+"(�����ղ�����)\n"
						+"(��һ·����)\n"
						+"(����������)\n"
						+"(��һ̨)\n"
						+"(��һ̨)\n"
						+"(������)\n"
						+"(�򿪵�̨)\n"
						+"(�ر�������)\n"
						+"(�Զ���̨)\n"
						+"(��ѭ��)\n"
						+"(��ѭ��)\n"
						+"(ͨѶ¼)\n"
						+"(����)\n"
						+"(��������)\n"
						+"(�رչ㲥)\n"
						+"(��̨)\n"
						+"(�����ղ��б�)\n"
						+"(һ·����)\n"
						+"(�㲥)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(����)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(�򿪹㲥)\n"
						+"(�رյ�̨	)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(�򿪵�ͼ)\n"
						+"(��ͼ)\n"
						+"(ȡ������)\n"
						+"(�رյ�ͼ)\n"
						+"(�˳�����)\n"
						+"(�˳���ͼ)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(M.P.��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(ȡ������)\n"
						+"(��ͣ����)\n"
						+"(ֹͣ����)\n"
						+"(�˳�����)\n"
						+"(����Ƶ)\n"
						+"(����Ƶ)\n"
						+"(������Ƶ)\n"
						+"(����Ƶ)\n"
						+"(����Ӱ)\n"
						+"(��Ƭ)\n"
						+"(�ر���Ƶ)\n"
						+"(ȡ����Ƶ)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(A.��D.P.)\n"
						+"(��������)\n"
						+"(����M.P.��)\n"
						+"(�ر���������)\n"
						+"(ȡ����������)\n"
						+"(��ͣ��������)\n"
						+"(ֹͣ��������)\n"
						+"(�˳���������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�޸�����)\n"
						+"(��������)\n"
						+"(�趨)\n"
						+"(�޸��趨)\n"
						+"(����)\n"
						+"(�ر�����)\n"
						+"(�������)\n"
						+"(�˳�����)\n";
				
				String key = edit_text_parameter_key.getText().toString();
				String value = edit_text_parameter_value.getText().toString();
				String str1 = null;
				if(key.contains("name") == true)
				{
					str1 = str + name;
				}
				else if(key.contains("navigation") == true)
				{
					str1 = str + navigation;
				}
				else if(key.contains("command") == true)
				{
					str1 = str + command;
				}
				
				String path = getSDPath();
				
				String xmlFile = edit_text_parameter_key.getHint().toString() + key;
				testModel0.startTest(
						xmlFile,
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf200.log", 
						null, 
						str1, /* command.gram  command.fsg */
						null);
				

//				String key = edit_text_parameter_key.getText().toString();
//				String value = edit_text_parameter_value.getText().toString();
//				testModel0.startTest(
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf200.log", 
//						null, 
//						str, /* command.gram  command.fsg */
//						null,
//						key,value);
				
			}
			
		};
		h.postDelayed(r, 10);	
		
		
		
	}
	
	
	public void onTest_jsgf500ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				
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
						+ "(������)\n"
						+ "(�򿪵���){�򿪵���}\n"
						+ "(��������){�򿪵���}\n"
						+ "(�򿪵�ͼ){�򿪵���}\n"
						+ "(��ͼ){�򿪵���}\n"
						+ "(����){�򿪵���}\n"
						+ "(�رյ���){�رյ���}\n"
						+ "(ȡ������){�رյ���}\n"
						+ "(�رյ�ͼ){�رյ���}\n"
						+ "(�˳�����){�رյ���}\n"
						+ "(�˳���ͼ){�رյ���}\n"
						+ "(������){������}\n"
						+ "(��������){������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(M.P.��){������}\n"
						+ "(����){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n"
						+ "(ȡ������){�ر�����}\n"
						+ "(��ͣ����){�ر�����}\n"
						+ "(ֹͣ����){�ر�����}\n"
						+ "(�˳�����){�ر�����}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(������Ƶ){����Ƶ}\n"
						+ "(����Ƶ){����Ƶ}\n"
						+ "(����Ӱ){����Ƶ}\n"
						+ "(��Ƭ){����Ƶ}\n"
						+ "(�ر���Ƶ){�ر���Ƶ}\n"
						+ "(ȡ����Ƶ){�ر���Ƶ}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(����������){����������}\n"
						+ "(������������){����������}\n"
						+ "(A.��D.P.){����������}\n"
						+ "(��������){����������}\n"
						+ "(����M.P.��){����������}\n"
						+ "(�ر���������){�ر���������}\n"
						+ "(ȡ����������){�ر���������}\n"
						+ "(��ͣ��������){�ر���������}\n"
						+ "(ֹͣ��������){�ر���������}\n"
						+ "(�˳���������){�ر���������}\n"
						+ "(������){������}\n"
						+ "(����){������}\n"
						+ "(�޸�����){������}\n"
						+ "(��������){������}\n"
						+ "(�趨){������}\n"
						+ "(�޸��趨){������}\n"
						+ "(����){������}\n"
						+ "(�ر�����){�ر�����}\n"
						+ "(�������){�ر�����}\n"
						+ "(�˳�����){�ر�����}\n"
						+ "(��������){��������}\n"
						+ "(������){��������}\n"
						+ "(��������){��������}\n"
						+ "(������){��������}\n"
						+ "(��Ƶ){��������}\n"
						+ "(A.M.){��������}\n"
						+ "(F.M.){��������}\n"
						+ "(������){��������}\n"
						+ "(���㲥){��������}\n"
						+ "(�����㲥){��������}\n"
						+ "(�㲥){��������}\n"
						+ "(������){��������}\n"
						+ "(�ر�������){�ر�������}\n"
						+ "(��������){�ر�������}\n"
						+ "(�˳�������){�ر�������}\n"
						+ "(�رչ㲥){�ر�������}\n"
						+ "(���г���¼��){���г���¼��}\n"
						+ "(�����г���¼��){���г���¼��}\n"
						+ "(����¼��){���г���¼��}\n"
						+ "(��D.V.R.){���г���¼��}\n"
						+ "(����¼��){���г���¼��}\n"
						+ "(D.V.R.){���г���¼��}\n"
						+ "(�г�¼��){���г���¼��}\n"
						+ "(¼��){���г���¼��}\n"
						+ "(�����г�¼��){���г���¼��}\n"
						+ "(�����г���¼){���г���¼��}\n"
						+ "(�ر��г���¼��){�ر��г���¼��}\n"
						+ "(�˳��г���¼��){�ر��г���¼��}\n"
						+ "(�ر�D.V.R.){�ر��г���¼��}\n"
						+ "(�ؼ�¼��){�ر��г���¼��}\n"
						+ "(�˳��г�¼��){�ر��г���¼��}\n"
						+ "(������){������}\n"
						+ "(��������){������}\n"
						+ "(��������){������}\n"
						+ "(����){������}\n"
						+ "(�绰){������}\n"
						+ "(����){������}\n"
						+ "(��绰){������}\n"
						+ "(�����ֻ�){������}\n"
						+ "(�����ֻ�){������}\n"
						+ "(�˳�����){�˳�����}\n"
						+ "(�ر�����){�˳�����}\n"
						+ "(�˳������ֻ�){�˳�����}\n"
						+ "(ȡ���ֻ�����){�˳�����}\n"
						+ "(ȡ����������){�˳�����}\n"
						+ "(��D.T.V.){��DTV}\n"
						+ "(�򿪵���){��DTV}\n"
						+ "(���ֵ���){��DTV}\n"
						+ "(���ص���){��DTV}\n"
						+ "(��C.M.M.B.){��DTV}\n"
						+ "(����){��DTV}\n"
						+ "(�ر�D.T.V.){�ر�DTV}\n"
						+ "(�رյ���){�ر�DTV}\n"
						+ "(�رճ��ص���){�ر�DTV}\n"
						+ "(�ر�C.M.M.B.){�ر�DTV}\n"
						+ "(�˳�D.T.V.){�ر�DTV}\n"
						+ "(���ֻ�����){���ֻ�����}\n"
						+ "(�ֻ�����){���ֻ�����}\n"
						+ "(��M.H.L.){���ֻ�����}\n"
						+ "(�򿪻���){���ֻ�����}\n"
						+ "(�ر��ֻ�����){�ر��ֻ�����}\n"
						+ "(�ر�M.H.L.){�ر��ֻ�����}\n"
						+ "(�رջ���){�ر��ֻ�����}\n"
						+ "(�Ͽ�����){�ر��ֻ�����}\n"
						+ "(�˳��ֻ�����){�ر��ֻ�����}\n"
						+ "(��A.U.X.){��AUX}\n"
						+ "(�򿪰¿�˹){��AUX}\n"
						+ "(����Ƶ����){��AUX}\n"
						+ "(�¿�˹){��AUX}\n"
						+ "(�ر�A.U.X.){�ر�AUX}\n"
						+ "(�رհ¿�˹){�ر�AUX}\n"
						+ "(�ر���Ƶ����){�ر�AUX}\n"
						+ "(�˳��¿�˹){�ر�AUX}\n"
						+ "(�Ͽ��¿�˹){�ر�AUX}\n"
						+ "(ȷ��){ȷ��}\n"
						+ "(ȡ��){ȡ��}\n"
						+ "(����){��������_�в���_�Ĵ���}\n"
						+ "(���){��������_�в���_���ղ�}\n"
						+ "(���){��������_�������_�����}\n"
						+ "(���){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�в���_�Ĵ���}\n"
						+ "(������){��������_�������_̩��_Խ�ϲ�Ʒ����}\n"
						+ "(������){��������_�в���_���ϲ�}\n"
						+ "(�����){��������_�������_�¹���}\n"
						+ "(�����){��������_�в���_���ղ�}\n"
						+ "(�嵭��){��������_�в���_��ʽ�ز˹�}\n"
						+ "(���ʵ�){��������_�в���_���ղ�}\n"
						+ "(�����){��������_�в���_�Ĵ���}\n"
						+ "(�����){��������_�в���_�Ĵ���}\n"
						+ "(�����){��������_�в���_�㽭��}\n"
						+ "(΢����){��������_�в���_�Ĵ���}\n"
						+ "(���̵�){��������_�в���_������}\n"
						+ "(�����){��������_�в���_ɽ����}\n"
						+ "(��ˬ��){��������_�в���_��ʽ�ز˹�}\n"
						+ "(��̬����){��������_�в���_�Ĵ���}\n"
						+ "(�����){��������_�в���_������}\n"
						+ "(ƫ�̵�){��������_�������_�Ϲ���}\n"
						+ "(ƫ���){��������_�������_�Ϲ���}\n"
						+ "(�崼��){��������_�в���_������}\n"
						+ "(���д����){��������_�������_��ʽ��ζ}\n"
						+ "(��ζ�ص�){��������_�в���_�Ĵ���}\n"
						+ "(����){��������_�в���_���ղ�}\n"
						+ "(����){��������_�������_������}\n"
						+ "(�Ƿ�){��������_�������_ţ�ǵ�}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(�������ֺ�){��������_�в���_���ֺ�}\n"
						+ "(��ʤ��){��������_�����_��ʤ��}\n"
						+ "(��){��������_�в���_���ݲ�}\n"
						+ "(����){��������_�в���_���ݲ�}\n"
						+ "(�Է�){����}\n"
						+ "(��){��������_�в���_�Ĵ���}\n"
						+ "(����){��������_�����_����}\n"
						+ "(����){��������_�����_�����}\n"
						+ "(�¹�){��������_�������_�¹���}\n"
						+ "(���˵�){��������_������س���_�������}\n"
						+ "(���к�){��������_�������_���к�����Ʒ}\n"
						+ "(���к����){��������_�������_���к�����Ʒ}\n"
						+ "(��){��������_�в���_�ƹ��}\n"
						+ "(����){��������_��Ʒ��_��Ʒ��}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(���){��������_�������_�����}\n"
						+ "(����˹){��������_�������_�����}\n"
						+ "(��){��������_�в���_������}\n"
						+ "(��ʽ){��������_�������_��ʽ��Ʒ����}\n"
						+ "(����){����}\n"
						+ "(�˹�){����}\n"
						+ "(�͹�){����}\n"
						+ "(����){����}\n"
						+ "(����){����}\n"
						+ "(����){����}\n"
						+ "(�ۺϲ˹�){����}\n"
						+ "(�ۺϲ͹�){����}\n"
						+ "(�ۺϲ���){����}\n"
						+ "(�ۺϷ���){����}\n"
						+ "(��¥){��������_�в���_�ۺϾ�¥}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(���){��������_�����_�����}\n"
						+ "(���){��������_�����_�����}\n"
						+ "(�㶫){��������_�в���_�㶫��}\n"
						+ "(�㸮){��������_�в���_�㶫��}\n"
						+ "(����){��������_�в���_���ʾ�¥}\n"
						+ "(����){��������_�������_��������}\n"
						+ "(����){��������_�������_��������}\n"
						+ "(��ʽ){��������_�������_��������}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(����){��������_�в���_���ϲ�}\n"
						+ "(����){��������_�в���_�Ϻ���}\n"
						+ "(��){��������_�в���_���ղ�}\n"
						+ "(����){��������_�в���_���ղ�}\n"
						+ "(���){��������_�в���_�����}\n"
						+ "(��Ұ��){��������_�����_��Ұ��}\n"
						+ "(����){��������_�в���_���ղ�}\n"
						+ "(�����){��������_�в���_������}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(���˹�){��������_�в���_������}\n"
						+ "(�ϵ»�){��������_�����_�ϵ»�}\n"
						+ "(���){����}\n"
						+ "(�ϱ���){��������_�в���_���ֺ�}\n"
						+ "(���ֺ�){��������_�в���_���ֺ�}\n"
						+ "(³){��������_�в���_ɽ����}\n"
						+ "(����){��������_�����_����}\n"
						+ "(������){��������_�������_��ʽ��ζ}\n"
						+ "(��ʽ){��������_�������_��ʽ��ζ}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(ī����){��������_�������_ī�����}\n"
						+ "(ţ��){��������_�������_ţ�ǵ�}\n"
						+ "(ţ��){��������_�������_ţ�ǵ�}\n"
						+ "(�Ϲ�){��������_�������_�Ϲ���}\n"
						+ "(����){��������_�в���_����˹�}\n"
						+ "(����){��������_�в���_����˹�}\n"
						+ "(������ʳ){��������_�в���_����˹�}\n"
						+ "(�ձ�){��������_�������_�ձ�����}\n"
						+ "(�ձ���){��������_�������_�ձ�����}\n"
						+ "(��ʽ){��������_�������_�ձ�����}\n"
						+ "(ɽ��){��������_�в���_ɽ����}\n"
						+ "(�Ϻ�){��������_�в���_�Ϻ���}\n"
						+ "(�Ϻ���){��������_�в���_�Ϻ���}\n"
						+ "(����){��������_�в���_�����}\n"
						+ "(�Ĵ�){��������_�в���_�Ĵ���}\n"
						+ "(��){��������_�в���_���ղ�}\n"
						+ "(�ղ�){��������_�в���_���ղ�}\n"
						+ "(�ز�){��������_�в���_��ʽ�ز˹�}\n"
						+ "(��ʳ){��������_�в���_��ʽ�ز˹�}\n"
						+ "(�ز˹�){��������_�в���_��ʽ�ز˹�}\n"
						+ "(�й��ز˹�){��������_�в���_��ʽ�ز˹�}\n"
						+ "(̨��){��������_�в���_̨���}\n"
						+ "(̩��){��������_�������_̩��_Խ�ϲ�Ʒ����}\n"
						+ "(���){��������_��Ʒ��_��Ʒ��}\n"
						+ "(��Ʒ��){��������_��Ʒ��_��Ʒ��}\n"
						+ "(���){��������_�������_�������}\n"
						+ "(����){��������_�в���_������}\n"
						+ "(����){��������_�������_������}\n"
						+ "(������){��������_�������_������}\n"
						+ "(��){��������_�в���_���ϲ�}\n"
						+ "(����){����}\n"
						+ "(����){��������_�������_�������޲�}\n"
						+ "(�����){��������_�������_��ʽ��Ʒ����}\n"
						+ "(��ʽ){��������_�������_��ʽ��Ʒ����}\n"
						+ "(ӡ��){��������_�������_ӡ�ȷ�ζ}\n"
						+ "(Ӣ��){��������_�������_Ӣ��ʽ��Ʒ����}\n"
						+ "(Ӣʽ){��������_�������_Ӣ��ʽ��Ʒ����}\n"
						+ "(���Ͷ���){��������_�����_���Ͷ���}\n"
						+ "(Խ��){��������_�������_̩��_Խ�ϲ�Ʒ����}\n"
						+ "(��){��������_�в���_�㶫��}\n"
						+ "(�ƹ�){��������_�в���_�ƹ��}\n"
						+ "(����){��������_�в���_�ƹ��}\n"
						+ "(��){��������_�в���_�㽭��}\n"
						+ "(�㽭){��������_�в���_�㽭��}\n"
						+ "(�в�){��������_�в���_�в���}\n"
						+ "(��ʽ�ز�){��������_�в���_��ʽ�ز˹�}\n"
						+ "(�ۺϷ�ζ����){��������_�������_������}\n"
						+ "(�ۺϷ�ζ������){��������_�������_������}\n"
						+ "(��ɫ��ζ������){��������_�������_������}\n"
						+ "(������){��������_�������_������}\n"
						+ "(�ۺϷ�ζ��¥){��������_�в���_�ۺϾ�¥}\n"
						+ "(��ζ){��������_�в���_��ɫ_�ط���ζ����}\n"
						+ "(H.I.��){�������з���_���ֳ���_KTV}\n"
						+ "(K.T.V.){�������з���_���ֳ���_KTV}\n"
						+ "(K.��){�������з���_���ֳ���_KTV}\n"
						+ "(�ĵ�){�������з���_���ֳ���_����}\n"
						+ "(����){�������з���_���ֳ���_KTV}\n"
						+ "(����Ϸ){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(�ϰ�){�������з���_���ֳ���_����}\n"
						+ "(��˹��){�������з���_���ֳ���_����}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(�����){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(������Ϸ����){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(������Ϸ����){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(������Ϸ��){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(����){�������з���_���ֳ���_KTV}\n"
						+ "(������){�������з���_���ֳ���_����}\n"
						+ "(������������){�������з���_���ֳ���_����}\n"
						+ "(�Ⱦ�){�������з���_���ֳ���_�ư�}\n"
						+ "(�ư�){�������з���_���ֳ���_�ư�}\n"
						+ "(�ƹ�){�������з���_���ֳ���_�ư�}\n"
						+ "(�Ƽ�){�������з���_���ֳ���_�ư�}\n"
						+ "(����){�������з���_���ֳ���_�ư�}\n"
						+ "(��ʳ��){�������з���_���ֳ���_�ư�}\n"
						+ "(����){�������з���_���ֳ���_�ư�}\n"
						+ "(����O.K.){�������з���_���ֳ���_KTV}\n"
						+ "(����O.K.����){�������з���_���ֳ���_KTV}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(��������){�������з���_���ֳ���_����}\n"
						+ "(����������){�������з���_���ֳ���_����}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(�����){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(����Ϸ){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(ҹ��){�������з���_���ֳ���_����}\n"
						+ "(��Ϸ��){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(��Ϸ����){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(��Ϸ��){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(��Ϸ��){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(������){�������з���_���ֳ���_��Ϸ��}\n"
						+ "(����){�������з���_���ֳ���_����}\n"
						+ "(�����){��������_����վ_�����}\n"
						+ "(�����ʯ��){��������_����վ_�����}\n"
						+ "(�������Ȼ��){��������_����վ_�����}\n"
						+ "(�����ʼҿ���){��������_����վ_����}\n"
						+ "(����){��������_����վ_����վ}\n"
						+ "(���͵ĵط�){��������_����վ_����վ}\n"
						+ "(����վ){��������_����վ_����վ}\n"
						+ "(����){��������_����վ_����}\n"
						+ "(����ʯ��){��������_����վ_����}\n"
						+ "(ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(�й�ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(�й�ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(�й�ʯ�ͻ���){��������_����վ_�й�ʯ��}\n"
						+ "(�й�ʯ����Ȼ��){��������_����վ_�й�ʯ��}\n"
						+ "(��ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(��ʯ��){��������_����վ_�й�ʯ��}\n"
						+ "(�Ƶ�){ס�޷���_���ݾƵ�_���ݾƵ�}\n"
						+ "(����){ס�޷���_���ݾƵ�_���ݾƵ�}\n"
						+ "(�д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(ס��){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(����){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�����д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�͵�){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�͵��д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(��ջ){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�õ�){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�õ��д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�ù�){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�ù��д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(����){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�����д���){ס�޷���_�ù��д���_�ù��д���}\n"
						+ "(�������){ס�޷���_�ù��д���_��������}\n"
						+ "(����Ƶ�){ס�޷���_�ù��д���_��������}\n"
						+ "(�����ջ){ס�޷���_�ù��д���_��������}\n"
						+ "(�����õ�){ס�޷���_�ù��д���_��������}\n"
						+ "(�����ù�){ס�޷���_�ù��д���_��������}\n"
						+ "(��������){ס�޷���_�ù��д���_��������}\n"
						+ "(���Ǳ���){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ�����){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ��Ƶ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���ǾƵ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǳ���){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ�����){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ��Ƶ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���ǾƵ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǳ���){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ�����){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���Ǽ��Ƶ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(���ǾƵ�){ס�޷���_���ݾƵ�_���Ǽ�����}\n"
						+ "(A.T.M.){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(A.T.M.��){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(A.T.M.�Զ�ȡ���){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(ȡ��){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(ȡ���){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(ȡǮ){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(�Զ���Ա��){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(�Զ�ȡ���){���ڱ��շ���_�Զ�����_�Զ�����}\n"
						+ "(����){���ڱ��շ���_����_����}\n"
						+ "(����){���ڱ��շ���_����_����}\n"
						+ "(ת��){���ڱ��շ���_����_����}\n"
						+ "(���){���ڱ��շ���_����_����}\n"
						+ "(���Ҷһ�){���ڱ��շ���_����_����}\n"
						+ "(���){���ڱ��շ���_����_����}\n"
						+ "(�����п�){���ڱ��շ���_����_����}\n"
						+ "(Ǯ��){���ڱ��շ���_����_����}\n"
						+ "(���){���ڱ��շ���_����_����}\n"
						+ "(��Ǯ){���ڱ��շ���_����_����}\n"
						+ "(����){���ڱ��շ���_����_����}\n"
						+ "(�һ�����){���ڱ��շ���_����_����}\n"
						+ "(����){������ʩ_��������_��������}\n"
						+ "(��������){������ʩ_��������_��������}\n"
						+ "(����������){������ʩ_��������_��������}\n"
						+ "(����ϴ�ּ�){������ʩ_��������_��������}\n"
						+ "(����){������ʩ_��������_��������}\n"
						+ "(�ϲ���){������ʩ_��������_��������}\n"
						+ "(��������){������ʩ_��������_��������}\n"
						+ "(ͣ��){��ͨ��ʩ����_ͣ����}\n"
						+ "(ͣ����){��ͨ��ʩ����_ͣ����}\n"
						+ "(ͣ����){��ͨ��ʩ����_ͣ����}\n"
						+ "(ͣ��λ){��ͨ��ʩ����_ͣ����}\n"
						+ "(������){������ʩ_��������_��������}\n"
						+ "(ϴ�ּ�){������ʩ_��������_��������}\n"
						+ "(ҩ�ĵ���){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩ����){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩƷ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩƷ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҩ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(ҽҩ��){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(��ҩ){ҽ�Ʊ�������_ҽҩ�������۵�_ҩ��}\n"
						+ "(�ٻ��̳�){�������_�̳�_�̳�}\n"
						+ "(�ٻ��̳�){�������_�̳�_�̳�}\n"
						+ "(�ٻ��̵�){�������_�̳�_�̳�}\n"
						+ "(����){�������_�̳�_�̳�}\n"
						+ "(����){�������_�̳�_�̳�}\n"
						+ "(�̳�){�������_�̳�_�̳�}\n"
						+ "(�̳�){�������_�̳�_�̳�}\n"
						+ "(����){�������_�̳�_�̳�}\n"
						+ "(��ҵ����){�������_�̳�_�̳�}\n"
						+ "(�����г�){�������_�����г�_����}\n"
						+ "(����){�������_�����г�_����}\n"
						+ "(�����ۺϳ���){�������_�����г�_����}\n"
						+ "(��ѡ�̳�){�������_�����г�_����}\n"
						+ "(�ۺ��������̳�){�������_�����г�_����}\n";
				
				String name = "(Ӣ����ɽ)\n"
						+"(��Ԫ)\n"
						+"(³ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�ų���)\n"
						+"(����)\n"
						+"(�ޱ���)\n"
						+"(��ϲ��)\n"
						+"(������)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(���ų�չ)\n"
						+"(����)\n"
						+"(��Ȼ)\n"
						+"(�Ǻ���)\n"
						+"(�Ϸ�)\n"
						+"(��ϼ)\n"
						+"(��չ)\n"
						+"(�þ�)\n"
						+"(�캣��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(������)\n"
						+"(Ф���)\n"
						+"(Էһ��)\n"
						+"(��������)\n"
						+"(���Ͽ�)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���)\n"
						+"(�����ִ�)\n"
						+"(����)\n"
						+"(���Ҷ)\n"
						+"(ʷ��)\n"
						+"(��ΰ��)\n"
						+"(������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(���)\n"
						+"(������)\n"
						+"(����)\n"
						+"(��Т��)\n"
						+"(���ƻ�)\n"
						+"(��͢��)\n"
						+"(��������)\n"
						+"(�����)\n"
						+"(�Կ���)\n"
						+"(������)\n"
						+"(ְ��)\n"
						+"(������ͬ)\n"
						+"(����)\n"
						+"(��ҵ��)\n"
						+"(����)\n"
						+"(�Ź���)\n"
						+"(��������)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����Сĥ)\n"
						+"(���)\n"
						+"(��־��)\n"
						+"(��Ц��)\n"
						+"(����)\n"
						+"(С��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(���л�)\n"
						+"(����)\n"
						+"(֣����)\n"
						+"(����С��)\n"
						+"(����ʦ)\n"
						+"(����ï)\n"
						+"(������)\n"
						+"(�ٴ�ү)\n"
						+"(����R)\n"
						+"(�����)\n"
						+"(������)\n"
						+"(��ά��)\n"
						+"(������)\n"
						+"(���ɻ�)\n"
						+"(˧��Ϊ)\n"
						+"(����)\n"
						+"(�ƺ���)\n"
						+"(��Զ��)\n"
						+"(����)\n"
						+"(��־��)\n"
						+"(Τ���)\n"
						+"(������)\n"
						+"(��С��)\n"
						+"(�̰���)\n"
						+"(��ݲܳ�)\n"
						+"(������)\n"
						+"(���Ѱ���)\n"
						+"(�￪����)\n"
						+"(����)\n"
						+"(����)\n"
						+"(Ǯ����)\n"
						+"(ʯ��ƽ)\n";
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
				String command = "(�򿪿յ�)\n"
						+"(����ѭ��)\n"
						+"(����ѭ��)\n"
						+"(�رտյ�)\n"
						+"(���촰)\n"
						+"(�ر��촰)\n"
						+"(����)\n"
						+"(�Ҷ�)\n"
						+"(��绰)\n"
						+"(�򿪵���)\n"
						+"(�رյ���)\n"
						+"(�򿪱�������)\n"
						+"(�ر�����)\n"
						+"(����)\n"
						+"(��ͣ)\n"
						+"(�ղ�)\n"
						+"(�������)\n"
						+"(����ѭ��)\n"
						+"(ȫ��ѭ��)\n"
						+"(�����ղ�����)\n"
						+"(��һ·����)\n"
						+"(����������)\n"
						+"(��һ̨)\n"
						+"(��һ̨)\n"
						+"(������)\n"
						+"(�򿪵�̨)\n"
						+"(�ر�������)\n"
						+"(�Զ���̨)\n"
						+"(��ѭ��)\n"
						+"(��ѭ��)\n"
						+"(ͨѶ¼)\n"
						+"(����)\n"
						+"(��������)\n"
						+"(�رչ㲥)\n"
						+"(��̨)\n"
						+"(�����ղ��б�)\n"
						+"(һ·����)\n"
						+"(�㲥)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(����)\n"
						+"(��һ��)\n"
						+"(��һ��)\n"
						+"(��������)\n"
						+"(�򿪹㲥)\n"
						+"(�رյ�̨	)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(�򿪵�ͼ)\n"
						+"(��ͼ)\n"
						+"(ȡ������)\n"
						+"(�رյ�ͼ)\n"
						+"(�˳�����)\n"
						+"(�˳���ͼ)\n"
						+"(������)\n"
						+"(��������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(M.P.��)\n"
						+"(����)\n"
						+"(����)\n"
						+"(ȡ������)\n"
						+"(��ͣ����)\n"
						+"(ֹͣ����)\n"
						+"(�˳�����)\n"
						+"(����Ƶ)\n"
						+"(����Ƶ)\n"
						+"(������Ƶ)\n"
						+"(����Ƶ)\n"
						+"(����Ӱ)\n"
						+"(��Ƭ)\n"
						+"(�ر���Ƶ)\n"
						+"(ȡ����Ƶ)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(����������)\n"
						+"(������������)\n"
						+"(A.��D.P.)\n"
						+"(��������)\n"
						+"(����M.P.��)\n"
						+"(�ر���������)\n"
						+"(ȡ����������)\n"
						+"(��ͣ��������)\n"
						+"(ֹͣ��������)\n"
						+"(�˳���������)\n"
						+"(������)\n"
						+"(����)\n"
						+"(�޸�����)\n"
						+"(��������)\n"
						+"(�趨)\n"
						+"(�޸��趨)\n"
						+"(����)\n"
						+"(�ر�����)\n"
						+"(�������)\n"
						+"(�˳�����)\n";
				
				String key = edit_text_parameter_key.getText().toString();
				String value = edit_text_parameter_value.getText().toString();
				String str1 = null;
				if(key.contains("name") == true)
				{
					str1 = str + name;
				}
				else if(key.contains("navigation") == true)
				{
					str1 = str + navigation;
				}
				else if(key.contains("command") == true)
				{
					str1 = str + command;
				}
				String xmlFile = edit_text_parameter_key.getHint().toString() + key;
				testModel0.startTest(
						xmlFile,
						"/storage/sdcard0/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf500.log", 
						null, 
						str1, /* command.gram  command.fsg */
						null);
				
//				String key = edit_text_parameter_key.getText().toString();
//				String value = edit_text_parameter_value.getText().toString();
//				testModel0.startTest(
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
//						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_jsgf500.log", 
//						null, 
//						str, /* command.gram  command.fsg */
//						null,
//						key,value);			
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	
	
	public void onTest_zts52ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "�򿪿յ�,"
						+ "����ѭ��,"
						+ "����ѭ��,"
						+ "�رտյ�,"
						+ "���촰,"
						+ "�ر��촰,"
						+ "����,"
						+ "�Ҷ�,"
						+ "��绰,"
						+ "�򿪵���,"
						+ "�رյ���,"
						+ "�򿪱�������,"
						+ "�ر�����,"
						+ "����,"
						+ "��ͣ,"
						+ "�ղ�,"
						+ "�������,"
						+ "����ѭ��,"
						+ "ȫ��ѭ��,"
						+ "�����ղ�����,"
						+ "��һ·����,"
						+ "����������,"
						+ "��һ̨,"
						+ "��һ̨,"
						+ "������,"
						+ "�򿪵�̨,"
						+ "�ر�������,"
						+ "�Զ���̨,"
						+ "��ѭ��,"
						+ "��ѭ��,"
						+ "ͨѶ¼,"
						+ "����,"
						+ "��������,"
						+ "�رչ㲥,"
						+ "��̨,"
						+ "�����ղ��б�,"
						+ "һ·����,"
						+ "�㲥,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "����,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "�򿪹㲥,"
						+ "�رյ�̨,"
						+ "������,";
				
				
				String path = getSDPath();
				
				testModel0.startTest(path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						hotWords, assetMgr, modelDir);				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	
	public void onTest_zts100ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "�򿪿յ�,"
						+ "����ѭ��,"
						+ "����ѭ��,"
						+ "�رտյ�,"
						+ "���촰,"
						+ "�ر��촰,"
						+ "����,"
						+ "�Ҷ�,"
						+ "��绰,"
						+ "�򿪵���,"
						+ "�رյ���,"
						+ "�򿪱�������,"
						+ "�ر�����,"
						+ "����,"
						+ "��ͣ,"
						+ "�ղ�,"
						+ "�������,"
						+ "����ѭ��,"
						+ "ȫ��ѭ��,"
						+ "�����ղ�����,"
						+ "��һ·����,"
						+ "����������,"
						+ "��һ̨,"
						+ "��һ̨,"
						+ "������,"
						+ "�򿪵�̨,"
						+ "�ر�������,"
						+ "�Զ���̨,"
						+ "��ѭ��,"
						+ "��ѭ��,"
						+ "ͨѶ¼,"
						+ "����,"
						+ "��������,"
						+ "�رչ㲥,"
						+ "��̨,"
						+ "�����ղ��б�,"
						+ "һ·����,"
						+ "�㲥,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "����,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "�򿪹㲥,"
						+ "�رյ�̨,"
						+ "������,"
						+ "���ճ���,�����ٶ�,����Ӱ��,�����п�,���չ���,���չ��¹㲥,���ս���,���ս���#,��������,�������չ㲥,�������յ�̨,��������,�������Ź㲥,���������ۺ�,���������ۺϹ㲥,���յ�̨�㲥,���յ���̨,���վ�����������,���վ����������ֹ㲥,��������,��������#,���յ�Ƶ,���ղƾ�,���ղƾ��㲥,����Զ��һ��,������ױ,��������,�������ֹ㲥,������Ƶ�㲥,���ո���,���ո���(HD),���ո���(����),���ո���(�ɻؿ�),���ո�������,����,������ͨ�㲥,����ũ��㲥,��������,��������#,��������(����),�������ӣ�ѡ����,����ָ��,��������,�������Ź㲥,�������ι㲥,��������,���������㲥,��������̨,�������й㲥,�������ֹ㲥,";
				
				
				String path = getSDPath();
				
				testModel0.startTest(path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						hotWords, assetMgr, modelDir);				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	
	
	public void onTest_zts200ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "�򿪿յ�,"
						+ "����ѭ��,"
						+ "����ѭ��,"
						+ "�رտյ�,"
						+ "���촰,"
						+ "�ر��촰,"
						+ "����,"
						+ "�Ҷ�,"
						+ "��绰,"
						+ "�򿪵���,"
						+ "�رյ���,"
						+ "�򿪱�������,"
						+ "�ر�����,"
						+ "����,"
						+ "��ͣ,"
						+ "�ղ�,"
						+ "�������,"
						+ "����ѭ��,"
						+ "ȫ��ѭ��,"
						+ "�����ղ�����,"
						+ "��һ·����,"
						+ "����������,"
						+ "��һ̨,"
						+ "��һ̨,"
						+ "������,"
						+ "�򿪵�̨,"
						+ "�ر�������,"
						+ "�Զ���̨,"
						+ "��ѭ��,"
						+ "��ѭ��,"
						+ "ͨѶ¼,"
						+ "����,"
						+ "��������,"
						+ "�رչ㲥,"
						+ "��̨,"
						+ "�����ղ��б�,"
						+ "һ·����,"
						+ "�㲥,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "����,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "�򿪹㲥,"
						+ "�رյ�̨,"
						+ "������,"
						+ "���ճ���,�����ٶ�,����Ӱ��,�����п�,���չ���,���չ��¹㲥,���ս���,���ս���#,��������,�������չ㲥,�������յ�̨,��������,�������Ź㲥,���������ۺ�,���������ۺϹ㲥,���յ�̨�㲥,���յ���̨,���վ�����������,���վ����������ֹ㲥,��������,��������#,���յ�Ƶ,���ղƾ�,���ղƾ��㲥,����Զ��һ��,������ױ,��������,�������ֹ㲥,������Ƶ�㲥,���ո���,���ո���(HD),���ո���(����),���ո���(�ɻؿ�),���ո�������,����,������ͨ�㲥,����ũ��㲥,��������,��������#,��������(����),�������ӣ�ѡ����,����ָ��,��������,�������Ź㲥,�������ι㲥,��������,���������㲥,��������̨,�������й㲥,�������ֹ㲥,"
						+ "����һ��,��������,���ֶ���,����һ��,��������,����ʱ��㲥,����ͯ��,����Ƶ��,��Ц�糡,��Ц�糡 (����),��Ц�糡 HD,��Ц�糡 ����,��Ц�糡(����),��Ц�糡(�Ĺ�),��Ц�糡(��ת),��Ц�糡-�Ĺ�����,��Ц�糡HD,��Ц�糡��,��Ц�糡����,��Ц�糡?,��Ц�糡�����ѣ�,ŷ������,ŷ������,ŷ������ (����),ŷ������($),ŷ������(����),ŷ������(����Բ�),ŷ�������Բ���,ŷ���糡,ŷ������,ŷ����ƬNVOD,ŷ��ӰԺ,ŷ��ӰԺ(NVOD),ŷ��ӰԺ-����,ŷ��ӰԺHD,ŷ��ӰԺ����,ŷ��ӰԺ����,ŷ����Ӱ,ŷ����Ӱ($),ŷ����Ӱ(����),ŷ����Ӱ(�����),ŷ����Ӱ*,ŷ����Ӱ����,ŷ����Ӱ����,ŷ����ѡ,ŷ������,�軪����,����ר��,��������,�������,��������Ƶ��,��������,�����㲥,�����㲥1,���������ۺ�,��������,��������($),��������(����),��������(����Բ�),�人һ��,�人����,�人����,�人����,�人����,�人����㲥��̨,�人����,�人ζ��,�人����,�人����,�人����ָ��,�人��̨��ͨ�㲥,�人��̨�ٶ��㲥,�人��̨���ֹ㲥,���,�Ͻ���������,����,����֮��,���ֹ㲥,���ֹ㲥1,���ֹ㲥2,���ֹ㲥3,���ֹ㲥4,���ֹ㲥5,����֮��,��������,����,����Ӱ��,����Ƶ��,���ݹ���,����Ӱ��,�����ۺ�,���ݶ���,�����������,�����������,������ѧ����,������¼����,�㴨����,����һ��,��������,���ж���,";
				
				
				String path = getSDPath();
				
				testModel0.startTest(path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						hotWords, assetMgr, modelDir);				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	
	public void onTest_zts500ButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//�첽�������Գ���
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "�򿪿յ�,"
						+ "����ѭ��,"
						+ "����ѭ��,"
						+ "�رտյ�,"
						+ "���촰,"
						+ "�ر��촰,"
						+ "����,"
						+ "�Ҷ�,"
						+ "��绰,"
						+ "�򿪵���,"
						+ "�رյ���,"
						+ "�򿪱�������,"
						+ "�ر�����,"
						+ "����,"
						+ "��ͣ,"
						+ "�ղ�,"
						+ "�������,"
						+ "����ѭ��,"
						+ "ȫ��ѭ��,"
						+ "�����ղ�����,"
						+ "��һ·����,"
						+ "����������,"
						+ "��һ̨,"
						+ "��һ̨,"
						+ "������,"
						+ "�򿪵�̨,"
						+ "�ر�������,"
						+ "�Զ���̨,"
						+ "��ѭ��,"
						+ "��ѭ��,"
						+ "ͨѶ¼,"
						+ "����,"
						+ "��������,"
						+ "�رչ㲥,"
						+ "��̨,"
						+ "�����ղ��б�,"
						+ "һ·����,"
						+ "�㲥,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��һ��,"
						+ "����,"
						+ "��һ��,"
						+ "��һ��,"
						+ "��������,"
						+ "�򿪹㲥,"
						+ "�رյ�̨,"
						+ "������,"
						+ "���ճ���,�����ٶ�,����Ӱ��,�����п�,���չ���,���չ��¹㲥,���ս���,���ս���#,��������,�������չ㲥,�������յ�̨,��������,�������Ź㲥,���������ۺ�,���������ۺϹ㲥,���յ�̨�㲥,���յ���̨,���վ�����������,���վ����������ֹ㲥,��������,��������#,���յ�Ƶ,���ղƾ�,���ղƾ��㲥,����Զ��һ��,������ױ,��������,�������ֹ㲥,������Ƶ�㲥,���ո���,���ո���(HD),���ո���(����),���ո���(�ɻؿ�),���ո�������,����,������ͨ�㲥,����ũ��㲥,��������,��������#,��������(����),�������ӣ�ѡ����,����ָ��,��������,�������Ź㲥,�������ι㲥,��������,���������㲥,��������̨,�������й㲥,�������ֹ㲥,"
						+ "���Ͼ��ù㲥,���϶���,�ӹ㽻ͨ�㲥,������,����Ӱ��,��������,�����ۺ�,��������,մ����Ӱ,մ���ۺ�,մ������,Ȫ��1��,Ȫ��2��,Ȫ��3��,Ȫ��4��,Ȫ������,���ơ��ƽ�,�������,�������(����),�������-�Ĺ��¼,����Ƶ��,����TV5,����ʱ��,����ʱװ,��������5̨,���ɷ���,���ɽ��ã�VOD��,�������,������� (����),�������(����),������ء�,�������?,������أ����ѣ�,̩��ͼ��,̩������,̩������,̩���ۺ�,̩������,̩��ͼ��,̩���̿�,̩������,̩ɽ�ȷ�,̩ɽ����,̩������,̩��Ӱ������,̩�ݷ���Ƶ��,���ݹ���,�������ֵ���,���ݿƽ�,�����ۺ�,������Ѷ,������Ƶ�㲥1,������Ƶ�㲥2,������Ƶ�㲥3,��������̨-1,��������̨-2,��������̨-3,��������̨-4,�����㶫,���н���,��������,����1015,��01,��02,��03,��04,��05,��06,��08,��09,����,����-1,����-2,����-3,����-CCTV-1����,����-CCTV-5 ����,����-��������,����-��������,����-�㶫����,����-���ո���,����-�㽭����,����-���ڸ���,����-���ϸ���,����-����������,����0,����01,����02,����1,����10,����11,����12,����13,����14,����2,����3,����4,����5,����6,����7,����8,����9,����CINEMAX��Ӱ,����CNN,����ESPN,���ԡ�,����һ,����һ����,������,������,���Ծ�,���Զ�,���Զ�����,������,��������,�����ź�,�����ź�-1,�����ź�-1����,�����ź�-2,�����ź�-2����,�����ź�-3,�����ź�-4,�����ź�-5,�����ź�-6,�����ź�1,�����ź�2,�����ź�3,�����ź�4,�����ź�5,�����ź�һ,�����ź���,�����ź���,�����źŶ�,�����ź���,�����ź���,�����ź���,���԰�,������,���Է������,���Է�˵�Ӱ,���Է����Ѷ,����ʮ,����ʮһ,����ʮ��,����ʮ��,����ʮ��,����ʮ��,���Ի�������,������,��������1,����̽��Ƶ��,��������1,��������2,�����ǿ�����,�����ǿյ�Ӱ,���Է���ʱ��,���Ե�Ӱ1,���Ե�Ӱ2,���Խ�Ŀ,���Խ�Ŀ(��),���Խ�Ŀ1,���Խ�Ŀ1(����),���Խ�Ŀ10,���Խ�Ŀ11,���Խ�Ŀ12,���Խ�Ŀ13,���Խ�Ŀ14,���Խ�Ŀ15,���Խ�Ŀ16,���Խ�Ŀ17,���Խ�Ŀ18,���Խ�Ŀ19,���Խ�Ŀ2,���Խ�Ŀ2(����),���Խ�Ŀ20,���Խ�Ŀ21,���Խ�Ŀ2307,���Խ�Ŀ3,���Խ�Ŀ4,���Խ�Ŀ5,���Խ�Ŀ6,���Խ�Ŀ7,���Խ�Ŀ8,���Խ�Ŀ9,���Խ�ĿHS,���Խ�Ŀһ,���Խ�Ŀһ����,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ������,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀʮ,���Խ�Ŀ��,���Լ�¼1,���Լ�¼2,������������,������Ƶ,����Ԥ��,����Ԥ��(����),����Ԥ��(�����),����Ԥ��*,����Ԥ����,����Ԥ������,����Ԥ������,����Ƶ��,����Ƶ��-1,����Ƶ��-10,����Ƶ��-2,����Ƶ��-3,����Ƶ��-4,����Ƶ��-6,����Ƶ��-7,����Ƶ��-8,����Ƶ��-9,����Ƶ��01,����Ƶ��02,����Ƶ��03,����Ƶ��04,����Ƶ��05,����Ƶ��06,����Ƶ��07,����Ƶ��08,����Ƶ��09,����Ƶ��1,����Ƶ��10,����Ƶ��11,����Ƶ��12,����Ƶ��13,����Ƶ��14,����Ƶ��15,����Ƶ��16,����Ƶ��17,����Ƶ��18,����Ƶ��19,����Ƶ��1����,����Ƶ��1���Ĵ���,����Ƶ��2,����Ƶ��20,����Ƶ��21,����Ƶ��2����,����Ƶ��2��������,����Ƶ��3,����Ƶ��3�����գ�,����Ƶ��4,����Ƶ��5,����Ƶ��6,����Ƶ��7,����Ƶ��8,����Ƶ��9,����Ƶ��һ,����Ƶ����,����Ƶ����,����Ƶ����,����Ƶ����,����Ƶ����,���Ը���1,���Ը���2,��������,��������,�����ٶ�,����Ӱ��,����Ӱ�Ӹ���,���Ͻ���,��������,�������Ÿ���,��������,���϶���,��Դ-�ø�,��Դ-�ۺ�,����,����Ӱ��,�����ۺ�,�������㲥��̨,�����ó,���ͼ��,�������,�������,�������,��һ,����,���,�㽭,�㽭 HD,�㽭 ����,�㽭-HD,�㽭-��֮ͨ��,�㽭-������ũ��,�㽭-����Ƶ��,�㽭-����Ƶ��,�㽭-����֮��,�㽭-�ٶ�Ƶ��,�㽭-Ӱ������,�㽭-Ӱ���Ļ�,�㽭-�����Ƽ�,�㽭-����Ƶ��,�㽭-�����ۺ�,�㽭-����֮��,�㽭-��������,�㽭-�㽭����,�㽭-����Ƶ��,�㽭-����,�㽭-Ǯ������,�㽭-���ֵ�Ƶ,�㽭-����,�㽭1̨,�㽭2,�㽭2̨,�㽭3,�㽭3̨,�㽭4,�㽭4̨,�㽭5,�㽭5̨,�㽭6,�㽭6̨,�㽭6Ƶ��,�㽭7,�㽭7̨,�㽭8,�㽭8̨,�㽭HD,�㽭֮��,�㽭��֮ͨ��,�㽭���,�㽭����,�㽭����,�㽭������ũ��,�㽭������Ѷ,�㽭����Ƶ��,�㽭����,�㽭����#,�㽭����(����),�㽭����(ѡ��),�㽭����(����),�㽭����-R,�㽭����-����,�㽭����HD,�㽭���Ӹ���,�㽭���Ӹ���#,�㽭���Ӹ���Ƶ��,�㽭����?����?,�㽭���ӣ�չ����,�㽭���ӣ����壩,�㽭����֮��,�㽭�ٶ�,�㽭�ٶ�Ƶ��,�㽭�㲥-6,�㽭�㲥-7,�㽭�㲥-8,�㽭�㲥һ,�㽭�㲥��,�㽭�㲥��,�㽭�㲥��̨,�㽭�㲥����Ƶ��,�㽭Ӱ��,�㽭Ӱ������,�㽭Ӱ���Ļ�,�㽭����,�㽭�����Ƽ�,�㽭���չ㲥,�㽭����Ƶ��,�㽭����̨,�㽭�����ۺ�,�㽭����Ƶ��,�㽭����֮��,�㽭��������,�㽭�����㲥,�㽭��̨�㲥,�㽭��̨����֮��,�㽭����̨,�㽭��������,�㽭����Ƶ��,�㽭����,�㽭����,�㽭�Ƹ��㲥,�㽭Ǯ������,�㽭����,�㽭���ֵ�Ƶ,�㽭��Ƶ�㲥,�㽭����,�㽭����(HD),�㽭����(����),�㽭����(�ɻؿ�),�㽭��������,����Ϸ��,���Ϲ���,��������,���Ϲ㲥,����Ӱ��,����������Ϣ,��������,�������Ź㲥,��������,������������,�����ۺ�,��������,����1��,";
				
				
				String path = getSDPath();
				
				testModel0.startTest(path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						hotWords, assetMgr, modelDir);				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	//д���ݵ�SD�е��ļ�
	void writeFileSdcardFile(String fileName,String write_str)
	{
        
		try {
			RandomAccessFile afile = new RandomAccessFile(fileName, "rw");
			
			long len = afile.length();
			
			afile.seek(len);
			
			//afile.writeUTF(write_str);
			
			byte [] bytes = write_str.getBytes();
			afile.write(bytes); 
			//afile.writeByte(0x0d);
			//afile.writeByte(0x0a);
			
			afile.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	
	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final PocketSphinxDemo that = this;
		final String hyp = b.getString("hyp");
		that.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
			}
		});
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		final PocketSphinxDemo that = this;
		
		ArrayList<String> nBestResult = b.getStringArrayList("nbest_hyp");
		int n = nBestResult.size();
		String str = null;
		for(int i = 0;i < n;i++)
		{
			str = str + "result[" + i + "] = " + nBestResult.get(i) + "\n";
			
		}
		
		final String str1 = str;
		this.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(str1);
				Date end_date = new Date();
				long nmsec = end_date.getTime() - that.start_date.getTime();
				float rec_dur = (float)nmsec / 1000;
				that.performance_text.setText(String.format("%.2f seconds %.2f xRT",
															that.speech_dur,
															rec_dur / that.speech_dur));
				Log.d(getClass().getName(), "Hiding Dialog");
				that.rec_dialog.dismiss();
			}
		});
	}

	public void onError(int err) {
		final PocketSphinxDemo that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});
	}

	@Override
	public void processCurrentData(String fileName, String result, int time) {
		// TODO Auto-generated method stub
		
		String str = "processCurrentData :" + fileName + "\nresult :" + result + "\n use time: " + time;
		Log.e(CommonFun.TAG, str);
		
		this.edit_text.setText(str);
		
	}

	@Override
	public void processTotalInfo(Bundle data1) {
		// TODO Auto-generated method stub
		
		int test_count = data1.getInt("test_count");
        double success = data1.getDouble("succes_count");
        double successRate =  data1.getDouble("successRate");
        int toutalTimeMs = data1.getInt("toutalTimeMs");
        int toutalDataLen = data1.getInt("toutalDataLen");
        
        double average = (double)toutalTimeMs / test_count;
        
        double dataTimeMS = (double)toutalDataLen / 16.0;//(double)toutalDataLen / 8.0;
        double real_time_rate = (double)toutalTimeMs / dataTimeMS;
        
        String str = " test_count :" + test_count + "\n success :" + success + "\n successRate: " 
        			+ successRate + "\n toutalTimeMs: " + toutalTimeMs + "\n average time: " + average
        			 + "\n toutalDataLen: " + toutalDataLen + "\n real time rate: " + real_time_rate;
		Log.e(CommonFun.TAG, str);
		
		this.edit_text.setText(str);
		
		
	}
	
	
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		
		return sdDir.toString();

	}

	
	
	
	
}