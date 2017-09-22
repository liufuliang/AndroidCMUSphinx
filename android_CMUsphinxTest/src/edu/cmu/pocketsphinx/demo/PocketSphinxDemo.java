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
	 *  输入测试的参数名，参数值
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
	 * 点击按钮，开始测试
	 * 
	 * */
	public void onTestButtonClicked(View view)
	{
//		//清空显示
//		edit_text.setText("");
//		
//		//获得应用的上下文，生命周期是整个应用，最好不用当前ACTIVITY
//		Context c = getApplicationContext();
//		//启动测试
//		new testFileDataThread(c,mhandler);
		
		
		//异步启动测试程序
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
		
		
		//异步启动测试程序
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
		
		
		//异步启动测试程序
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
		
		
		//异步启动测试程序
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
		
		
		//异步启动测试程序
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

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
				
				String name = "(英大林山)\n"
						+"(戴元)\n"
						+"(鲁伟亮)\n"
						+"(刘海波)\n"
						+"(杨梦琴)\n"
						+"(马传洁)\n"
						+"(张成震)\n"
						+"(李猛)\n"
						+"(罗宝琪)\n"
						+"(张喜琴)\n"
						+"(黄新征)\n"
						+"(阿初)\n"
						+"(小涛)\n"
						+"(海雅车展)\n"
						+"(申阳)\n"
						+"(欣然)\n"
						+"(吳浩明)\n"
						+"(邢飞)\n"
						+"(秋霞)\n"
						+"(晓展)\n"
						+"(裘军)\n"
						+"(朱海兵)\n"
						+"(吕育红)\n"
						+"(王连福)\n"
						+"(姬江涛)\n"
						+"(肖贵鸿)\n"
						+"(苑一峰)\n"
						+"(王秋亮家)\n"
						+"(济南库)\n"
						+"(陈征)\n"
						+"(牙科)\n"
						+"(李常艳)\n"
						+"(王哥现代)\n"
						+"(陈易)\n"
						+"(徐红叶)\n"
						+"(史楠楠)\n"
						+"(刘伟华)\n"
						+"(霍铭锋)\n"
						+"(郭晨新)\n"
						+"(陈忠)\n"
						+"(裴浩)\n"
						+"(晶晶爸)\n"
						+"(田忠)\n"
						+"(陈孝金)\n"
						+"(陈善辉)\n"
						+"(胡廷海)\n"
						+"(杭州张韵)\n"
						+"(马军哥)\n"
						+"(翟克友)\n"
						+"(曾凤珍)\n"
						+"(职办)\n"
						+"(李丽芬同)\n"
						+"(阿贵)\n"
						+"(宝业钟)\n"
						+"(阮廓)\n"
						+"(张国林)\n"
						+"(婚庆威少)\n"
						+"(小辆)\n"
						+"(黄钻)\n"
						+"(健李小磨)\n"
						+"(韩璐)\n"
						+"(吴志茵)\n"
						+"(张笑怡)\n"
						+"(满根)\n"
						+"(小梦)\n"
						+"(由欣)\n"
						+"(蛋蛋)\n"
						+"(江南)\n"
						+"(凌中华)\n"
						+"(阿佳)\n"
						+"(郑光鹏)\n"
						+"(交行小邱)\n"
						+"(杜老师)\n"
						+"(陈文茂)\n"
						+"(蔡正林)\n"
						+"(寿大爷)\n"
						+"(李磅礡)\n"
						+"(二表哥)\n"
						+"(赵三京)\n"
						+"(范维新)\n"
						+"(赵立民)\n"
						+"(王飞机)\n"
						+"(帅歌为)\n"
						+"(李智)\n"
						+"(唐海燕)\n"
						+"(杨远龙)\n"
						+"(君君)\n"
						+"(程志彬)\n"
						+"(韦广尖)\n"
						+"(陈美欢)\n"
						+"(陈小云)\n"
						+"(程阿丽)\n"
						+"(语驾曹成)\n"
						+"(王俊宇)\n"
						+"(永佳阿荣)\n"
						+"(田开吊车)\n"
						+"(秦岭)\n"
						+"(安艳)\n"
						+"(钱鼓手)\n"
						+"(石国平)\n";
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
				String command = "(打开空调)\n"
						+"(车内循环)\n"
						+"(车外循环)\n"
						+"(关闭空调)\n"
						+"(打开天窗)\n"
						+"(关闭天窗)\n"
						+"(接听)\n"
						+"(挂断)\n"
						+"(打电话)\n"
						+"(打开导航)\n"
						+"(关闭导航)\n"
						+"(打开本地音乐)\n"
						+"(关闭音乐)\n"
						+"(播放)\n"
						+"(暂停)\n"
						+"(收藏)\n"
						+"(随机播放)\n"
						+"(单曲循环)\n"
						+"(全部循环)\n"
						+"(播放收藏音乐)\n"
						+"(打开一路享听)\n"
						+"(打开有声读物)\n"
						+"(上一台)\n"
						+"(下一台)\n"
						+"(打开新闻)\n"
						+"(打开电台)\n"
						+"(关闭收音机)\n"
						+"(自动搜台)\n"
						+"(内循环)\n"
						+"(外循环)\n"
						+"(通讯录)\n"
						+"(导航)\n"
						+"(本地音乐)\n"
						+"(关闭广播)\n"
						+"(电台)\n"
						+"(播放收藏列表)\n"
						+"(一路享听)\n"
						+"(广播)\n"
						+"(上一曲)\n"
						+"(下一曲)\n"
						+"(有声读物)\n"
						+"(上一个)\n"
						+"(下一个)\n"
						+"(上一条)\n"
						+"(下一条)\n"
						+"(新闻)\n"
						+"(上一首)\n"
						+"(下一首)\n"
						+"(打开收音机)\n"
						+"(打开广播)\n"
						+"(关闭电台	)\n"
						+"(收音机)\n"
						+"(启动导航)\n"
						+"(打开地图)\n"
						+"(地图)\n"
						+"(取消导航)\n"
						+"(关闭地图)\n"
						+"(退出导航)\n"
						+"(退出地图)\n"
						+"(打开音乐)\n"
						+"(启动音乐)\n"
						+"(听音乐)\n"
						+"(音乐)\n"
						+"(M.P.三)\n"
						+"(歌曲)\n"
						+"(听歌)\n"
						+"(取消音乐)\n"
						+"(暂停音乐)\n"
						+"(停止音乐)\n"
						+"(退出音乐)\n"
						+"(打开视频)\n"
						+"(看视频)\n"
						+"(播放视频)\n"
						+"(放视频)\n"
						+"(看电影)\n"
						+"(看片)\n"
						+"(关闭视频)\n"
						+"(取消视频)\n"
						+"(打开蓝牙音乐)\n"
						+"(启动蓝牙音乐)\n"
						+"(听蓝牙音乐)\n"
						+"(播放蓝牙音乐)\n"
						+"(A.二D.P.)\n"
						+"(蓝牙歌曲)\n"
						+"(蓝牙M.P.三)\n"
						+"(关闭蓝牙音乐)\n"
						+"(取消蓝牙音乐)\n"
						+"(暂停蓝牙音乐)\n"
						+"(停止蓝牙音乐)\n"
						+"(退出蓝牙音乐)\n"
						+"(打开设置)\n"
						+"(设置)\n"
						+"(修改设置)\n"
						+"(更改设置)\n"
						+"(设定)\n"
						+"(修改设定)\n"
						+"(调节)\n"
						+"(关闭设置)\n"
						+"(设置完成)\n"
						+"(退出设置)\n";
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

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
						+ "(收音机)\n"
						+ "(打开导航){打开导航}\n"
						+ "(启动导航){打开导航}\n"
						+ "(打开地图){打开导航}\n"
						+ "(地图){打开导航}\n"
						+ "(导航){打开导航}\n"
						+ "(关闭导航){关闭导航}\n"
						+ "(取消导航){关闭导航}\n"
						+ "(关闭地图){关闭导航}\n"
						+ "(退出导航){关闭导航}\n"
						+ "(退出地图){关闭导航}\n"
						+ "(打开音乐){打开音乐}\n"
						+ "(启动音乐){打开音乐}\n"
						+ "(听音乐){打开音乐}\n"
						+ "(音乐){打开音乐}\n"
						+ "(M.P.三){打开音乐}\n"
						+ "(歌曲){打开音乐}\n"
						+ "(听歌){打开音乐}\n"
						+ "(关闭音乐){关闭音乐}\n"
						+ "(取消音乐){关闭音乐}\n"
						+ "(暂停音乐){关闭音乐}\n"
						+ "(停止音乐){关闭音乐}\n"
						+ "(退出音乐){关闭音乐}\n"
						+ "(打开视频){打开视频}\n"
						+ "(看视频){打开视频}\n"
						+ "(播放视频){打开视频}\n"
						+ "(放视频){打开视频}\n"
						+ "(看电影){打开视频}\n"
						+ "(看片){打开视频}\n"
						+ "(关闭视频){关闭视频}\n"
						+ "(取消视频){关闭视频}\n"
						+ "(打开蓝牙音乐){打开蓝牙音乐}\n"
						+ "(启动蓝牙音乐){打开蓝牙音乐}\n"
						+ "(听蓝牙音乐){打开蓝牙音乐}\n"
						+ "(播放蓝牙音乐){打开蓝牙音乐}\n"
						+ "(A.二D.P.){打开蓝牙音乐}\n"
						+ "(蓝牙歌曲){打开蓝牙音乐}\n"
						+ "(蓝牙M.P.三){打开蓝牙音乐}\n"
						+ "(关闭蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(取消蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(暂停蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(停止蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(退出蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(打开设置){打开设置}\n"
						+ "(设置){打开设置}\n"
						+ "(修改设置){打开设置}\n"
						+ "(更改设置){打开设置}\n"
						+ "(设定){打开设置}\n"
						+ "(修改设定){打开设置}\n"
						+ "(调节){打开设置}\n"
						+ "(关闭设置){关闭设置}\n";
				
				
				String name = "(英大林山)\n"
						+"(戴元)\n"
						+"(鲁伟亮)\n"
						+"(刘海波)\n"
						+"(杨梦琴)\n"
						+"(马传洁)\n"
						+"(张成震)\n"
						+"(李猛)\n"
						+"(罗宝琪)\n"
						+"(张喜琴)\n"
						+"(黄新征)\n"
						+"(阿初)\n"
						+"(小涛)\n"
						+"(海雅车展)\n"
						+"(申阳)\n"
						+"(欣然)\n"
						+"(吳浩明)\n"
						+"(邢飞)\n"
						+"(秋霞)\n"
						+"(晓展)\n"
						+"(裘军)\n"
						+"(朱海兵)\n"
						+"(吕育红)\n"
						+"(王连福)\n"
						+"(姬江涛)\n"
						+"(肖贵鸿)\n"
						+"(苑一峰)\n"
						+"(王秋亮家)\n"
						+"(济南库)\n"
						+"(陈征)\n"
						+"(牙科)\n"
						+"(李常艳)\n"
						+"(王哥现代)\n"
						+"(陈易)\n"
						+"(徐红叶)\n"
						+"(史楠楠)\n"
						+"(刘伟华)\n"
						+"(霍铭锋)\n"
						+"(郭晨新)\n"
						+"(陈忠)\n"
						+"(裴浩)\n"
						+"(晶晶爸)\n"
						+"(田忠)\n"
						+"(陈孝金)\n"
						+"(陈善辉)\n"
						+"(胡廷海)\n"
						+"(杭州张韵)\n"
						+"(马军哥)\n"
						+"(翟克友)\n"
						+"(曾凤珍)\n"
						+"(职办)\n"
						+"(李丽芬同)\n"
						+"(阿贵)\n"
						+"(宝业钟)\n"
						+"(阮廓)\n"
						+"(张国林)\n"
						+"(婚庆威少)\n"
						+"(小辆)\n"
						+"(黄钻)\n"
						+"(健李小磨)\n"
						+"(韩璐)\n"
						+"(吴志茵)\n"
						+"(张笑怡)\n"
						+"(满根)\n"
						+"(小梦)\n"
						+"(由欣)\n"
						+"(蛋蛋)\n"
						+"(江南)\n"
						+"(凌中华)\n"
						+"(阿佳)\n"
						+"(郑光鹏)\n"
						+"(交行小邱)\n"
						+"(杜老师)\n"
						+"(陈文茂)\n"
						+"(蔡正林)\n"
						+"(寿大爷)\n"
						+"(李磅礡)\n"
						+"(二表哥)\n"
						+"(赵三京)\n"
						+"(范维新)\n"
						+"(赵立民)\n"
						+"(王飞机)\n"
						+"(帅歌为)\n"
						+"(李智)\n"
						+"(唐海燕)\n"
						+"(杨远龙)\n"
						+"(君君)\n"
						+"(程志彬)\n"
						+"(韦广尖)\n"
						+"(陈美欢)\n"
						+"(陈小云)\n"
						+"(程阿丽)\n"
						+"(语驾曹成)\n"
						+"(王俊宇)\n"
						+"(永佳阿荣)\n"
						+"(田开吊车)\n"
						+"(秦岭)\n"
						+"(安艳)\n"
						+"(钱鼓手)\n"
						+"(石国平)\n";
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
				String command = "(打开空调)\n"
						+"(车内循环)\n"
						+"(车外循环)\n"
						+"(关闭空调)\n"
						+"(打开天窗)\n"
						+"(关闭天窗)\n"
						+"(接听)\n"
						+"(挂断)\n"
						+"(打电话)\n"
						+"(打开导航)\n"
						+"(关闭导航)\n"
						+"(打开本地音乐)\n"
						+"(关闭音乐)\n"
						+"(播放)\n"
						+"(暂停)\n"
						+"(收藏)\n"
						+"(随机播放)\n"
						+"(单曲循环)\n"
						+"(全部循环)\n"
						+"(播放收藏音乐)\n"
						+"(打开一路享听)\n"
						+"(打开有声读物)\n"
						+"(上一台)\n"
						+"(下一台)\n"
						+"(打开新闻)\n"
						+"(打开电台)\n"
						+"(关闭收音机)\n"
						+"(自动搜台)\n"
						+"(内循环)\n"
						+"(外循环)\n"
						+"(通讯录)\n"
						+"(导航)\n"
						+"(本地音乐)\n"
						+"(关闭广播)\n"
						+"(电台)\n"
						+"(播放收藏列表)\n"
						+"(一路享听)\n"
						+"(广播)\n"
						+"(上一曲)\n"
						+"(下一曲)\n"
						+"(有声读物)\n"
						+"(上一个)\n"
						+"(下一个)\n"
						+"(上一条)\n"
						+"(下一条)\n"
						+"(新闻)\n"
						+"(上一首)\n"
						+"(下一首)\n"
						+"(打开收音机)\n"
						+"(打开广播)\n"
						+"(关闭电台	)\n"
						+"(收音机)\n"
						+"(启动导航)\n"
						+"(打开地图)\n"
						+"(地图)\n"
						+"(取消导航)\n"
						+"(关闭地图)\n"
						+"(退出导航)\n"
						+"(退出地图)\n"
						+"(打开音乐)\n"
						+"(启动音乐)\n"
						+"(听音乐)\n"
						+"(音乐)\n"
						+"(M.P.三)\n"
						+"(歌曲)\n"
						+"(听歌)\n"
						+"(取消音乐)\n"
						+"(暂停音乐)\n"
						+"(停止音乐)\n"
						+"(退出音乐)\n"
						+"(打开视频)\n"
						+"(看视频)\n"
						+"(播放视频)\n"
						+"(放视频)\n"
						+"(看电影)\n"
						+"(看片)\n"
						+"(关闭视频)\n"
						+"(取消视频)\n"
						+"(打开蓝牙音乐)\n"
						+"(启动蓝牙音乐)\n"
						+"(听蓝牙音乐)\n"
						+"(播放蓝牙音乐)\n"
						+"(A.二D.P.)\n"
						+"(蓝牙歌曲)\n"
						+"(蓝牙M.P.三)\n"
						+"(关闭蓝牙音乐)\n"
						+"(取消蓝牙音乐)\n"
						+"(暂停蓝牙音乐)\n"
						+"(停止蓝牙音乐)\n"
						+"(退出蓝牙音乐)\n"
						+"(打开设置)\n"
						+"(设置)\n"
						+"(修改设置)\n"
						+"(更改设置)\n"
						+"(设定)\n"
						+"(修改设定)\n"
						+"(调节)\n"
						+"(关闭设置)\n"
						+"(设置完成)\n"
						+"(退出设置)\n";
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				
				String str =  "(打开空调)\n"
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
						+ "(收音机)\n"
						+ "(打开导航){打开导航}\n"
						+ "(启动导航){打开导航}\n"
						+ "(打开地图){打开导航}\n"
						+ "(地图){打开导航}\n"
						+ "(导航){打开导航}\n"
						+ "(关闭导航){关闭导航}\n"
						+ "(取消导航){关闭导航}\n"
						+ "(关闭地图){关闭导航}\n"
						+ "(退出导航){关闭导航}\n"
						+ "(退出地图){关闭导航}\n"
						+ "(打开音乐){打开音乐}\n"
						+ "(启动音乐){打开音乐}\n"
						+ "(听音乐){打开音乐}\n"
						+ "(音乐){打开音乐}\n"
						+ "(M.P.三){打开音乐}\n"
						+ "(歌曲){打开音乐}\n"
						+ "(听歌){打开音乐}\n"
						+ "(关闭音乐){关闭音乐}\n"
						+ "(取消音乐){关闭音乐}\n"
						+ "(暂停音乐){关闭音乐}\n"
						+ "(停止音乐){关闭音乐}\n"
						+ "(退出音乐){关闭音乐}\n"
						+ "(打开视频){打开视频}\n"
						+ "(看视频){打开视频}\n"
						+ "(播放视频){打开视频}\n"
						+ "(放视频){打开视频}\n"
						+ "(看电影){打开视频}\n"
						+ "(看片){打开视频}\n"
						+ "(关闭视频){关闭视频}\n"
						+ "(取消视频){关闭视频}\n"
						+ "(打开蓝牙音乐){打开蓝牙音乐}\n"
						+ "(启动蓝牙音乐){打开蓝牙音乐}\n"
						+ "(听蓝牙音乐){打开蓝牙音乐}\n"
						+ "(播放蓝牙音乐){打开蓝牙音乐}\n"
						+ "(A.二D.P.){打开蓝牙音乐}\n"
						+ "(蓝牙歌曲){打开蓝牙音乐}\n"
						+ "(蓝牙M.P.三){打开蓝牙音乐}\n"
						+ "(关闭蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(取消蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(暂停蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(停止蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(退出蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(打开设置){打开设置}\n"
						+ "(设置){打开设置}\n"
						+ "(修改设置){打开设置}\n"
						+ "(更改设置){打开设置}\n"
						+ "(设定){打开设置}\n"
						+ "(修改设定){打开设置}\n"
						+ "(调节){打开设置}\n"
						+ "(关闭设置){关闭设置}\n"
						+ "(设置完成){关闭设置}\n"
						+ "(退出设置){关闭设置}\n"
						+ "(打开收音机){打开收音机}\n"
						+ "(打开收音){打开收音机}\n"
						+ "(听收音机){打开收音机}\n"
						+ "(收音机){打开收音机}\n"
						+ "(调频){打开收音机}\n"
						+ "(A.M.){打开收音机}\n"
						+ "(F.M.){打开收音机}\n"
						+ "(听新闻){打开收音机}\n"
						+ "(听广播){打开收音机}\n"
						+ "(收听广播){打开收音机}\n"
						+ "(广播){打开收音机}\n"
						+ "(听报道){打开收音机}\n"
						+ "(关闭收音机){关闭收音机}\n"
						+ "(关收音机){关闭收音机}\n"
						+ "(退出收音机){关闭收音机}\n"
						+ "(关闭广播){关闭收音机}\n"
						+ "(打开行车记录仪){打开行车记录仪}\n"
						+ "(进入行车记录仪){打开行车记录仪}\n"
						+ "(看记录仪){打开行车记录仪}\n"
						+ "(打开D.V.R.){打开行车记录仪}\n"
						+ "(开记录仪){打开行车记录仪}\n"
						+ "(D.V.R.){打开行车记录仪}\n"
						+ "(行车录像){打开行车记录仪}\n"
						+ "(录像){打开行车记录仪}\n"
						+ "(开启行车录像){打开行车记录仪}\n"
						+ "(开启行车记录){打开行车记录仪}\n"
						+ "(关闭行车记录仪){关闭行车记录仪}\n"
						+ "(退出行车记录仪){关闭行车记录仪}\n"
						+ "(关闭D.V.R.){关闭行车记录仪}\n"
						+ "(关记录仪){关闭行车记录仪}\n"
						+ "(退出行车录像){关闭行车记录仪}\n"
						+ "(打开蓝牙){打开蓝牙}\n"
						+ "(进入蓝牙){打开蓝牙}\n"
						+ "(启动蓝牙){打开蓝牙}\n"
						+ "(蓝牙){打开蓝牙}\n"
						+ "(电话){打开蓝牙}\n"
						+ "(拨号){打开蓝牙}\n"
						+ "(打电话){打开蓝牙}\n"
						+ "(连接手机){打开蓝牙}\n"
						+ "(连上手机){打开蓝牙}\n"
						+ "(退出蓝牙){退出蓝牙}\n"
						+ "(关闭蓝牙){退出蓝牙}\n"
						+ "(退出连接手机){退出蓝牙}\n"
						+ "(取消手机连接){退出蓝牙}\n"
						+ "(取消蓝牙连接){退出蓝牙}\n"
						+ "(打开D.T.V.){打开DTV}\n"
						+ "(打开电视){打开DTV}\n"
						+ "(数字电视){打开DTV}\n"
						+ "(车载电视){打开DTV}\n"
						+ "(打开C.M.M.B.){打开DTV}\n"
						+ "(电视){打开DTV}\n"
						+ "(关闭D.T.V.){关闭DTV}\n"
						+ "(关闭电视){关闭DTV}\n"
						+ "(关闭车载电视){关闭DTV}\n"
						+ "(关闭C.M.M.B.){关闭DTV}\n"
						+ "(退出D.T.V.){关闭DTV}\n"
						+ "(打开手机互联){打开手机互联}\n"
						+ "(手机互联){打开手机互联}\n"
						+ "(打开M.H.L.){打开手机互联}\n"
						+ "(打开互联){打开手机互联}\n"
						+ "(关闭手机互联){关闭手机互联}\n"
						+ "(关闭M.H.L.){关闭手机互联}\n"
						+ "(关闭互联){关闭手机互联}\n"
						+ "(断开互联){关闭手机互联}\n"
						+ "(退出手机互联){关闭手机互联}\n"
						+ "(打开A.U.X.){打开AUX}\n"
						+ "(打开奥克斯){打开AUX}\n"
						+ "(打开音频输入){打开AUX}\n"
						+ "(奥克斯){打开AUX}\n"
						+ "(关闭A.U.X.){关闭AUX}\n"
						+ "(关闭奥克斯){关闭AUX}\n"
						+ "(关闭音频输入){关闭AUX}\n"
						+ "(退出奥克斯){关闭AUX}\n"
						+ "(断开奥克斯){关闭AUX}\n"
						+ "(确定){确定}\n"
						+ "(取消){取消}\n"
						+ "(辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜的){餐饮服务_中餐厅_江苏菜}\n"
						+ "(酸的){餐饮服务_外国餐厅_俄国菜}\n"
						+ "(麻的){餐饮服务_中餐厅_四川菜}\n"
						+ "(麻辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(香辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜辣的){餐饮服务_外国餐厅_泰国_越南菜品餐厅}\n"
						+ "(酸辣的){餐饮服务_中餐厅_湖南菜}\n"
						+ "(酸甜的){餐饮服务_外国餐厅_德国菜}\n"
						+ "(香甜的){餐饮服务_中餐厅_江苏菜}\n"
						+ "(清淡的){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(咸鲜的){餐饮服务_中餐厅_安徽菜}\n"
						+ "(鱼香的){餐饮服务_中餐厅_四川菜}\n"
						+ "(椒麻的){餐饮服务_中餐厅_四川菜}\n"
						+ "(酱香的){餐饮服务_中餐厅_浙江菜}\n"
						+ "(微辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜咸的){餐饮服务_中餐厅_福建菜}\n"
						+ "(清香的){餐饮服务_中餐厅_山东菜}\n"
						+ "(清爽的){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(变态辣的){餐饮服务_中餐厅_四川菜}\n";
				
				String name = "(英大林山)\n"
						+"(戴元)\n"
						+"(鲁伟亮)\n"
						+"(刘海波)\n"
						+"(杨梦琴)\n"
						+"(马传洁)\n"
						+"(张成震)\n"
						+"(李猛)\n"
						+"(罗宝琪)\n"
						+"(张喜琴)\n"
						+"(黄新征)\n"
						+"(阿初)\n"
						+"(小涛)\n"
						+"(海雅车展)\n"
						+"(申阳)\n"
						+"(欣然)\n"
						+"(吳浩明)\n"
						+"(邢飞)\n"
						+"(秋霞)\n"
						+"(晓展)\n"
						+"(裘军)\n"
						+"(朱海兵)\n"
						+"(吕育红)\n"
						+"(王连福)\n"
						+"(姬江涛)\n"
						+"(肖贵鸿)\n"
						+"(苑一峰)\n"
						+"(王秋亮家)\n"
						+"(济南库)\n"
						+"(陈征)\n"
						+"(牙科)\n"
						+"(李常艳)\n"
						+"(王哥现代)\n"
						+"(陈易)\n"
						+"(徐红叶)\n"
						+"(史楠楠)\n"
						+"(刘伟华)\n"
						+"(霍铭锋)\n"
						+"(郭晨新)\n"
						+"(陈忠)\n"
						+"(裴浩)\n"
						+"(晶晶爸)\n"
						+"(田忠)\n"
						+"(陈孝金)\n"
						+"(陈善辉)\n"
						+"(胡廷海)\n"
						+"(杭州张韵)\n"
						+"(马军哥)\n"
						+"(翟克友)\n"
						+"(曾凤珍)\n"
						+"(职办)\n"
						+"(李丽芬同)\n"
						+"(阿贵)\n"
						+"(宝业钟)\n"
						+"(阮廓)\n"
						+"(张国林)\n"
						+"(婚庆威少)\n"
						+"(小辆)\n"
						+"(黄钻)\n"
						+"(健李小磨)\n"
						+"(韩璐)\n"
						+"(吴志茵)\n"
						+"(张笑怡)\n"
						+"(满根)\n"
						+"(小梦)\n"
						+"(由欣)\n"
						+"(蛋蛋)\n"
						+"(江南)\n"
						+"(凌中华)\n"
						+"(阿佳)\n"
						+"(郑光鹏)\n"
						+"(交行小邱)\n"
						+"(杜老师)\n"
						+"(陈文茂)\n"
						+"(蔡正林)\n"
						+"(寿大爷)\n"
						+"(李磅礡)\n"
						+"(二表哥)\n"
						+"(赵三京)\n"
						+"(范维新)\n"
						+"(赵立民)\n"
						+"(王飞机)\n"
						+"(帅歌为)\n"
						+"(李智)\n"
						+"(唐海燕)\n"
						+"(杨远龙)\n"
						+"(君君)\n"
						+"(程志彬)\n"
						+"(韦广尖)\n"
						+"(陈美欢)\n"
						+"(陈小云)\n"
						+"(程阿丽)\n"
						+"(语驾曹成)\n"
						+"(王俊宇)\n"
						+"(永佳阿荣)\n"
						+"(田开吊车)\n"
						+"(秦岭)\n"
						+"(安艳)\n"
						+"(钱鼓手)\n"
						+"(石国平)\n";
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
				String command = "(打开空调)\n"
						+"(车内循环)\n"
						+"(车外循环)\n"
						+"(关闭空调)\n"
						+"(打开天窗)\n"
						+"(关闭天窗)\n"
						+"(接听)\n"
						+"(挂断)\n"
						+"(打电话)\n"
						+"(打开导航)\n"
						+"(关闭导航)\n"
						+"(打开本地音乐)\n"
						+"(关闭音乐)\n"
						+"(播放)\n"
						+"(暂停)\n"
						+"(收藏)\n"
						+"(随机播放)\n"
						+"(单曲循环)\n"
						+"(全部循环)\n"
						+"(播放收藏音乐)\n"
						+"(打开一路享听)\n"
						+"(打开有声读物)\n"
						+"(上一台)\n"
						+"(下一台)\n"
						+"(打开新闻)\n"
						+"(打开电台)\n"
						+"(关闭收音机)\n"
						+"(自动搜台)\n"
						+"(内循环)\n"
						+"(外循环)\n"
						+"(通讯录)\n"
						+"(导航)\n"
						+"(本地音乐)\n"
						+"(关闭广播)\n"
						+"(电台)\n"
						+"(播放收藏列表)\n"
						+"(一路享听)\n"
						+"(广播)\n"
						+"(上一曲)\n"
						+"(下一曲)\n"
						+"(有声读物)\n"
						+"(上一个)\n"
						+"(下一个)\n"
						+"(上一条)\n"
						+"(下一条)\n"
						+"(新闻)\n"
						+"(上一首)\n"
						+"(下一首)\n"
						+"(打开收音机)\n"
						+"(打开广播)\n"
						+"(关闭电台	)\n"
						+"(收音机)\n"
						+"(启动导航)\n"
						+"(打开地图)\n"
						+"(地图)\n"
						+"(取消导航)\n"
						+"(关闭地图)\n"
						+"(退出导航)\n"
						+"(退出地图)\n"
						+"(打开音乐)\n"
						+"(启动音乐)\n"
						+"(听音乐)\n"
						+"(音乐)\n"
						+"(M.P.三)\n"
						+"(歌曲)\n"
						+"(听歌)\n"
						+"(取消音乐)\n"
						+"(暂停音乐)\n"
						+"(停止音乐)\n"
						+"(退出音乐)\n"
						+"(打开视频)\n"
						+"(看视频)\n"
						+"(播放视频)\n"
						+"(放视频)\n"
						+"(看电影)\n"
						+"(看片)\n"
						+"(关闭视频)\n"
						+"(取消视频)\n"
						+"(打开蓝牙音乐)\n"
						+"(启动蓝牙音乐)\n"
						+"(听蓝牙音乐)\n"
						+"(播放蓝牙音乐)\n"
						+"(A.二D.P.)\n"
						+"(蓝牙歌曲)\n"
						+"(蓝牙M.P.三)\n"
						+"(关闭蓝牙音乐)\n"
						+"(取消蓝牙音乐)\n"
						+"(暂停蓝牙音乐)\n"
						+"(停止蓝牙音乐)\n"
						+"(退出蓝牙音乐)\n"
						+"(打开设置)\n"
						+"(设置)\n"
						+"(修改设置)\n"
						+"(更改设置)\n"
						+"(设定)\n"
						+"(修改设定)\n"
						+"(调节)\n"
						+"(关闭设置)\n"
						+"(设置完成)\n"
						+"(退出设置)\n";
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				
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
						+ "(收音机)\n"
						+ "(打开导航){打开导航}\n"
						+ "(启动导航){打开导航}\n"
						+ "(打开地图){打开导航}\n"
						+ "(地图){打开导航}\n"
						+ "(导航){打开导航}\n"
						+ "(关闭导航){关闭导航}\n"
						+ "(取消导航){关闭导航}\n"
						+ "(关闭地图){关闭导航}\n"
						+ "(退出导航){关闭导航}\n"
						+ "(退出地图){关闭导航}\n"
						+ "(打开音乐){打开音乐}\n"
						+ "(启动音乐){打开音乐}\n"
						+ "(听音乐){打开音乐}\n"
						+ "(音乐){打开音乐}\n"
						+ "(M.P.三){打开音乐}\n"
						+ "(歌曲){打开音乐}\n"
						+ "(听歌){打开音乐}\n"
						+ "(关闭音乐){关闭音乐}\n"
						+ "(取消音乐){关闭音乐}\n"
						+ "(暂停音乐){关闭音乐}\n"
						+ "(停止音乐){关闭音乐}\n"
						+ "(退出音乐){关闭音乐}\n"
						+ "(打开视频){打开视频}\n"
						+ "(看视频){打开视频}\n"
						+ "(播放视频){打开视频}\n"
						+ "(放视频){打开视频}\n"
						+ "(看电影){打开视频}\n"
						+ "(看片){打开视频}\n"
						+ "(关闭视频){关闭视频}\n"
						+ "(取消视频){关闭视频}\n"
						+ "(打开蓝牙音乐){打开蓝牙音乐}\n"
						+ "(启动蓝牙音乐){打开蓝牙音乐}\n"
						+ "(听蓝牙音乐){打开蓝牙音乐}\n"
						+ "(播放蓝牙音乐){打开蓝牙音乐}\n"
						+ "(A.二D.P.){打开蓝牙音乐}\n"
						+ "(蓝牙歌曲){打开蓝牙音乐}\n"
						+ "(蓝牙M.P.三){打开蓝牙音乐}\n"
						+ "(关闭蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(取消蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(暂停蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(停止蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(退出蓝牙音乐){关闭蓝牙音乐}\n"
						+ "(打开设置){打开设置}\n"
						+ "(设置){打开设置}\n"
						+ "(修改设置){打开设置}\n"
						+ "(更改设置){打开设置}\n"
						+ "(设定){打开设置}\n"
						+ "(修改设定){打开设置}\n"
						+ "(调节){打开设置}\n"
						+ "(关闭设置){关闭设置}\n"
						+ "(设置完成){关闭设置}\n"
						+ "(退出设置){关闭设置}\n"
						+ "(打开收音机){打开收音机}\n"
						+ "(打开收音){打开收音机}\n"
						+ "(听收音机){打开收音机}\n"
						+ "(收音机){打开收音机}\n"
						+ "(调频){打开收音机}\n"
						+ "(A.M.){打开收音机}\n"
						+ "(F.M.){打开收音机}\n"
						+ "(听新闻){打开收音机}\n"
						+ "(听广播){打开收音机}\n"
						+ "(收听广播){打开收音机}\n"
						+ "(广播){打开收音机}\n"
						+ "(听报道){打开收音机}\n"
						+ "(关闭收音机){关闭收音机}\n"
						+ "(关收音机){关闭收音机}\n"
						+ "(退出收音机){关闭收音机}\n"
						+ "(关闭广播){关闭收音机}\n"
						+ "(打开行车记录仪){打开行车记录仪}\n"
						+ "(进入行车记录仪){打开行车记录仪}\n"
						+ "(看记录仪){打开行车记录仪}\n"
						+ "(打开D.V.R.){打开行车记录仪}\n"
						+ "(开记录仪){打开行车记录仪}\n"
						+ "(D.V.R.){打开行车记录仪}\n"
						+ "(行车录像){打开行车记录仪}\n"
						+ "(录像){打开行车记录仪}\n"
						+ "(开启行车录像){打开行车记录仪}\n"
						+ "(开启行车记录){打开行车记录仪}\n"
						+ "(关闭行车记录仪){关闭行车记录仪}\n"
						+ "(退出行车记录仪){关闭行车记录仪}\n"
						+ "(关闭D.V.R.){关闭行车记录仪}\n"
						+ "(关记录仪){关闭行车记录仪}\n"
						+ "(退出行车录像){关闭行车记录仪}\n"
						+ "(打开蓝牙){打开蓝牙}\n"
						+ "(进入蓝牙){打开蓝牙}\n"
						+ "(启动蓝牙){打开蓝牙}\n"
						+ "(蓝牙){打开蓝牙}\n"
						+ "(电话){打开蓝牙}\n"
						+ "(拨号){打开蓝牙}\n"
						+ "(打电话){打开蓝牙}\n"
						+ "(连接手机){打开蓝牙}\n"
						+ "(连上手机){打开蓝牙}\n"
						+ "(退出蓝牙){退出蓝牙}\n"
						+ "(关闭蓝牙){退出蓝牙}\n"
						+ "(退出连接手机){退出蓝牙}\n"
						+ "(取消手机连接){退出蓝牙}\n"
						+ "(取消蓝牙连接){退出蓝牙}\n"
						+ "(打开D.T.V.){打开DTV}\n"
						+ "(打开电视){打开DTV}\n"
						+ "(数字电视){打开DTV}\n"
						+ "(车载电视){打开DTV}\n"
						+ "(打开C.M.M.B.){打开DTV}\n"
						+ "(电视){打开DTV}\n"
						+ "(关闭D.T.V.){关闭DTV}\n"
						+ "(关闭电视){关闭DTV}\n"
						+ "(关闭车载电视){关闭DTV}\n"
						+ "(关闭C.M.M.B.){关闭DTV}\n"
						+ "(退出D.T.V.){关闭DTV}\n"
						+ "(打开手机互联){打开手机互联}\n"
						+ "(手机互联){打开手机互联}\n"
						+ "(打开M.H.L.){打开手机互联}\n"
						+ "(打开互联){打开手机互联}\n"
						+ "(关闭手机互联){关闭手机互联}\n"
						+ "(关闭M.H.L.){关闭手机互联}\n"
						+ "(关闭互联){关闭手机互联}\n"
						+ "(断开互联){关闭手机互联}\n"
						+ "(退出手机互联){关闭手机互联}\n"
						+ "(打开A.U.X.){打开AUX}\n"
						+ "(打开奥克斯){打开AUX}\n"
						+ "(打开音频输入){打开AUX}\n"
						+ "(奥克斯){打开AUX}\n"
						+ "(关闭A.U.X.){关闭AUX}\n"
						+ "(关闭奥克斯){关闭AUX}\n"
						+ "(关闭音频输入){关闭AUX}\n"
						+ "(退出奥克斯){关闭AUX}\n"
						+ "(断开奥克斯){关闭AUX}\n"
						+ "(确定){确定}\n"
						+ "(取消){取消}\n"
						+ "(辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜的){餐饮服务_中餐厅_江苏菜}\n"
						+ "(酸的){餐饮服务_外国餐厅_俄国菜}\n"
						+ "(麻的){餐饮服务_中餐厅_四川菜}\n"
						+ "(麻辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(香辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜辣的){餐饮服务_外国餐厅_泰国_越南菜品餐厅}\n"
						+ "(酸辣的){餐饮服务_中餐厅_湖南菜}\n"
						+ "(酸甜的){餐饮服务_外国餐厅_德国菜}\n"
						+ "(香甜的){餐饮服务_中餐厅_江苏菜}\n"
						+ "(清淡的){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(咸鲜的){餐饮服务_中餐厅_安徽菜}\n"
						+ "(鱼香的){餐饮服务_中餐厅_四川菜}\n"
						+ "(椒麻的){餐饮服务_中餐厅_四川菜}\n"
						+ "(酱香的){餐饮服务_中餐厅_浙江菜}\n"
						+ "(微辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜咸的){餐饮服务_中餐厅_福建菜}\n"
						+ "(清香的){餐饮服务_中餐厅_山东菜}\n"
						+ "(清爽的){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(变态辣的){餐饮服务_中餐厅_四川菜}\n"
						+ "(甜酸的){餐饮服务_中餐厅_福建菜}\n"
						+ "(偏咸的){餐饮服务_外国餐厅_葡国菜}\n"
						+ "(偏甜的){餐饮服务_外国餐厅_葡国菜}\n"
						+ "(清醇的){餐饮服务_中餐厅_北京菜}\n"
						+ "(咸中带甜的){餐饮服务_外国餐厅_美式风味}\n"
						+ "(口味重的){餐饮服务_中餐厅_四川菜}\n"
						+ "(安徽){餐饮服务_中餐厅_安徽菜}\n"
						+ "(巴西){餐饮服务_外国餐厅_巴西菜}\n"
						+ "(扒房){餐饮服务_外国餐厅_牛扒店}\n"
						+ "(北京){餐饮服务_中餐厅_北京菜}\n"
						+ "(北京老字号){餐饮服务_中餐厅_老字号}\n"
						+ "(必胜客){餐饮服务_快餐厅_必胜客}\n"
						+ "(潮){餐饮服务_中餐厅_潮州菜}\n"
						+ "(潮州){餐饮服务_中餐厅_潮州菜}\n"
						+ "(吃饭){餐厅}\n"
						+ "(川){餐饮服务_中餐厅_四川菜}\n"
						+ "(大快活){餐饮服务_快餐厅_大快活}\n"
						+ "(蛋糕){餐饮服务_糕饼店_糕饼店}\n"
						+ "(德国){餐饮服务_外国餐厅_德国菜}\n"
						+ "(凉菜店){餐饮服务_餐饮相关场所_餐饮相关}\n"
						+ "(地中海){餐饮服务_外国餐厅_地中海风格菜品}\n"
						+ "(地中海风格){餐饮服务_外国餐厅_地中海风格菜品}\n"
						+ "(滇){餐饮服务_中餐厅_云贵菜}\n"
						+ "(点心){餐饮服务_甜品店_甜品店}\n"
						+ "(东北){餐饮服务_中餐厅_东北菜}\n"
						+ "(俄国){餐饮服务_外国餐厅_俄国菜}\n"
						+ "(俄罗斯){餐饮服务_外国餐厅_俄国菜}\n"
						+ "(鄂){餐饮服务_中餐厅_湖北菜}\n"
						+ "(法式){餐饮服务_外国餐厅_法式菜品餐厅}\n"
						+ "(方便){餐厅}\n"
						+ "(菜馆){餐厅}\n"
						+ "(餐馆){餐厅}\n"
						+ "(餐厅){餐厅}\n"
						+ "(饭店){餐厅}\n"
						+ "(饭馆){餐厅}\n"
						+ "(综合菜馆){餐厅}\n"
						+ "(综合餐馆){餐厅}\n"
						+ "(综合餐厅){餐厅}\n"
						+ "(综合饭馆){餐厅}\n"
						+ "(酒楼){餐饮服务_中餐厅_综合酒楼}\n"
						+ "(福建){餐饮服务_中餐厅_福建菜}\n"
						+ "(福州){餐饮服务_中餐厅_福建菜}\n"
						+ "(糕点){餐饮服务_糕饼店_糕饼店}\n"
						+ "(糕饼){餐饮服务_糕饼店_糕饼店}\n"
						+ "(广东){餐饮服务_中餐厅_广东菜}\n"
						+ "(广府){餐饮服务_中餐厅_广东菜}\n"
						+ "(海鲜){餐饮服务_中餐厅_海鲜酒楼}\n"
						+ "(韩餐){餐饮服务_外国餐厅_韩国料理}\n"
						+ "(韩国){餐饮服务_外国餐厅_韩国料理}\n"
						+ "(韩式){餐饮服务_外国餐厅_韩国料理}\n"
						+ "(湖北){餐饮服务_中餐厅_湖北菜}\n"
						+ "(湖南){餐饮服务_中餐厅_湖南菜}\n"
						+ "(沪菜){餐饮服务_中餐厅_上海菜}\n"
						+ "(徽){餐饮服务_中餐厅_安徽菜}\n"
						+ "(徽州){餐饮服务_中餐厅_安徽菜}\n"
						+ "(火锅){餐饮服务_中餐厅_火锅店}\n"
						+ "(吉野家){餐饮服务_快餐厅_吉野家}\n"
						+ "(江苏){餐饮服务_中餐厅_江苏菜}\n"
						+ "(京帮菜){餐饮服务_中餐厅_北京菜}\n"
						+ "(京菜){餐饮服务_中餐厅_北京菜}\n"
						+ "(京菜馆){餐饮服务_中餐厅_北京菜}\n"
						+ "(肯德基){餐饮服务_快餐厅_肯德基}\n"
						+ "(快餐){餐厅}\n"
						+ "(老北京){餐饮服务_中餐厅_老字号}\n"
						+ "(老字号){餐饮服务_中餐厅_老字号}\n"
						+ "(鲁){餐饮服务_中餐厅_山东菜}\n"
						+ "(麦当劳){餐饮服务_快餐厅_麦当劳}\n"
						+ "(美国餐){餐饮服务_外国餐厅_美式风味}\n"
						+ "(美式){餐饮服务_外国餐厅_美式风味}\n"
						+ "(闽菜){餐饮服务_中餐厅_福建菜}\n"
						+ "(墨西哥){餐饮服务_外国餐厅_墨西哥菜}\n"
						+ "(牛扒){餐饮服务_外国餐厅_牛扒店}\n"
						+ "(牛排){餐饮服务_外国餐厅_牛扒店}\n"
						+ "(葡国){餐饮服务_外国餐厅_葡国菜}\n"
						+ "(清真){餐饮服务_中餐厅_清真菜馆}\n"
						+ "(回民){餐饮服务_中餐厅_清真菜馆}\n"
						+ "(清真素食){餐饮服务_中餐厅_清真菜馆}\n"
						+ "(日本){餐饮服务_外国餐厅_日本料理}\n"
						+ "(日本餐){餐饮服务_外国餐厅_日本料理}\n"
						+ "(日式){餐饮服务_外国餐厅_日本料理}\n"
						+ "(山东){餐饮服务_中餐厅_山东菜}\n"
						+ "(上海){餐饮服务_中餐厅_上海菜}\n"
						+ "(上海菜){餐饮服务_中餐厅_上海菜}\n"
						+ "(涮肉){餐饮服务_中餐厅_火锅店}\n"
						+ "(四川){餐饮服务_中餐厅_四川菜}\n"
						+ "(苏){餐饮服务_中餐厅_江苏菜}\n"
						+ "(苏菜){餐饮服务_中餐厅_江苏菜}\n"
						+ "(素菜){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(素食){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(素菜馆){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(中国素菜馆){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(台湾){餐饮服务_中餐厅_台湾菜}\n"
						+ "(泰国){餐饮服务_外国餐厅_泰国_越南菜品餐厅}\n"
						+ "(甜点){餐饮服务_甜品店_甜品店}\n"
						+ "(甜品店){餐饮服务_甜品店_甜品店}\n"
						+ "(外国){餐饮服务_外国餐厅_外国餐厅}\n"
						+ "(西北){餐饮服务_中餐厅_西北菜}\n"
						+ "(西餐){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(西餐厅){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(湘){餐饮服务_中餐厅_湖南菜}\n"
						+ "(休闲){餐厅}\n"
						+ "(亚洲){餐饮服务_外国餐厅_其它亚洲菜}\n"
						+ "(意大利){餐饮服务_外国餐厅_意式菜品餐厅}\n"
						+ "(意式){餐饮服务_外国餐厅_意式菜品餐厅}\n"
						+ "(印度){餐饮服务_外国餐厅_印度风味}\n"
						+ "(英国){餐饮服务_外国餐厅_英国式菜品餐厅}\n"
						+ "(英式){餐饮服务_外国餐厅_英国式菜品餐厅}\n"
						+ "(永和豆浆){餐饮服务_快餐厅_永和豆浆}\n"
						+ "(越南){餐饮服务_外国餐厅_泰国_越南菜品餐厅}\n"
						+ "(粤){餐饮服务_中餐厅_广东菜}\n"
						+ "(云贵){餐饮服务_中餐厅_云贵菜}\n"
						+ "(云南){餐饮服务_中餐厅_云贵菜}\n"
						+ "(浙){餐饮服务_中餐厅_浙江菜}\n"
						+ "(浙江){餐饮服务_中餐厅_浙江菜}\n"
						+ "(中餐){餐饮服务_中餐厅_中餐厅}\n"
						+ "(中式素菜){餐饮服务_中餐厅_中式素菜馆}\n"
						+ "(综合风味西餐){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(综合风味西方菜){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(特色风味西餐厅){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(西餐厅){餐饮服务_外国餐厅_西餐厅}\n"
						+ "(综合风味酒楼){餐饮服务_中餐厅_综合酒楼}\n"
						+ "(风味){餐饮服务_中餐厅_特色_地方风味餐厅}\n"
						+ "(H.I.房){体育休闲服务_娱乐场所_KTV}\n"
						+ "(K.T.V.){体育休闲服务_娱乐场所_KTV}\n"
						+ "(K.歌){体育休闲服务_娱乐场所_KTV}\n"
						+ "(蹦迪){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(唱歌){体育休闲服务_娱乐场所_KTV}\n"
						+ "(打游戏){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(迪吧){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(迪斯科){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(迪厅){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(电玩城){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(电子游戏场所){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(电子游戏机室){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(电子游戏厅){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(斗舞){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(歌厅){体育休闲服务_娱乐场所_KTV}\n"
						+ "(歌舞厅){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(公共上网场所){体育休闲服务_娱乐场所_网吧}\n"
						+ "(喝酒){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒吧){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒馆){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒家){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒廊){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒食店){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(酒肆){体育休闲服务_娱乐场所_酒吧}\n"
						+ "(卡拉O.K.){体育休闲服务_娱乐场所_KTV}\n"
						+ "(卡拉O.K.场所){体育休闲服务_娱乐场所_KTV}\n"
						+ "(上网){体育休闲服务_娱乐场所_网吧}\n"
						+ "(上网场所){体育休闲服务_娱乐场所_网吧}\n"
						+ "(上网服务场所){体育休闲服务_娱乐场所_网吧}\n"
						+ "(跳舞){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(玩电玩){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(玩游戏){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(网吧){体育休闲服务_娱乐场所_网吧}\n"
						+ "(舞厅){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(夜店){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(游戏城){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(游戏机室){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(游戏室){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(游戏厅){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(游艺厅){体育休闲服务_娱乐场所_游戏厅}\n"
						+ "(轧舞){体育休闲服务_娱乐场所_迪厅}\n"
						+ "(道达尔){汽车服务_加油站_道达尔}\n"
						+ "(道达尔石油){汽车服务_加油站_道达尔}\n"
						+ "(道达尔天然气){汽车服务_加油站_道达尔}\n"
						+ "(荷兰皇家壳牌){汽车服务_加油站_壳牌}\n"
						+ "(加油){汽车服务_加油站_加油站}\n"
						+ "(加油的地方){汽车服务_加油站_加油站}\n"
						+ "(加油站){汽车服务_加油站_加油站}\n"
						+ "(壳牌){汽车服务_加油站_壳牌}\n"
						+ "(壳牌石油){汽车服务_加油站_壳牌}\n"
						+ "(石化){汽车服务_加油站_中国石化}\n"
						+ "(石油){汽车服务_加油站_中国石油}\n"
						+ "(中国石化){汽车服务_加油站_中国石化}\n"
						+ "(中国石油){汽车服务_加油站_中国石油}\n"
						+ "(中国石油化工){汽车服务_加油站_中国石化}\n"
						+ "(中国石油天然气){汽车服务_加油站_中国石油}\n"
						+ "(中石化){汽车服务_加油站_中国石化}\n"
						+ "(中石油){汽车服务_加油站_中国石油}\n"
						+ "(酒店){住宿服务_宾馆酒店_宾馆酒店}\n"
						+ "(商旅){住宿服务_宾馆酒店_宾馆酒店}\n"
						+ "(招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(住宿){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(宾馆){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(宾馆招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(客店){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(客店招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(客栈){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅店){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅店招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅馆){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅馆招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅社){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(旅社招待所){住宿服务_旅馆招待所_旅馆招待所}\n"
						+ "(青年宾馆){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(青年酒店){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(青年客栈){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(青年旅店){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(青年旅馆){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(青年旅舍){住宿服务_旅馆招待所_青年旅舍}\n"
						+ "(三星宾馆){住宿服务_宾馆酒店_三星级宾馆}\n"
						+ "(三星级宾馆){住宿服务_宾馆酒店_三星级宾馆}\n"
						+ "(三星级酒店){住宿服务_宾馆酒店_三星级宾馆}\n"
						+ "(三星酒店){住宿服务_宾馆酒店_三星级宾馆}\n"
						+ "(四星宾馆){住宿服务_宾馆酒店_四星级宾馆}\n"
						+ "(四星级宾馆){住宿服务_宾馆酒店_四星级宾馆}\n"
						+ "(四星级酒店){住宿服务_宾馆酒店_四星级宾馆}\n"
						+ "(四星酒店){住宿服务_宾馆酒店_四星级宾馆}\n"
						+ "(五星宾馆){住宿服务_宾馆酒店_五星级宾馆}\n"
						+ "(五星级宾馆){住宿服务_宾馆酒店_五星级宾馆}\n"
						+ "(五星级酒店){住宿服务_宾馆酒店_五星级宾馆}\n"
						+ "(五星酒店){住宿服务_宾馆酒店_五星级宾馆}\n"
						+ "(A.T.M.){金融保险服务_自动提款机_自动提款机}\n"
						+ "(A.T.M.机){金融保险服务_自动提款机_自动提款机}\n"
						+ "(A.T.M.自动取款机){金融保险服务_自动提款机_自动提款机}\n"
						+ "(取款){金融保险服务_自动提款机_自动提款机}\n"
						+ "(取款机){金融保险服务_自动提款机_自动提款机}\n"
						+ "(取钱){金融保险服务_自动提款机_自动提款机}\n"
						+ "(自动柜员机){金融保险服务_自动提款机_自动提款机}\n"
						+ "(自动取款机){金融保险服务_自动提款机_自动提款机}\n"
						+ "(银行){金融保险服务_银行_银行}\n"
						+ "(银库){金融保险服务_银行_银行}\n"
						+ "(转账){金融保险服务_银行_银行}\n"
						+ "(汇款){金融保险服务_银行_银行}\n"
						+ "(货币兑换){金融保险服务_银行_银行}\n"
						+ "(理财){金融保险服务_银行_银行}\n"
						+ "(办银行卡){金融保险服务_银行_银行}\n"
						+ "(钱库){金融保险服务_银行_银行}\n"
						+ "(存款){金融保险服务_银行_银行}\n"
						+ "(存钱){金融保险服务_银行_银行}\n"
						+ "(贷款){金融保险服务_银行_银行}\n"
						+ "(兑换货币){金融保险服务_银行_银行}\n"
						+ "(公厕){公共设施_公共厕所_公共厕所}\n"
						+ "(公共厕所){公共设施_公共厕所_公共厕所}\n"
						+ "(公共卫生间){公共设施_公共厕所_公共厕所}\n"
						+ "(公共洗手间){公共设施_公共厕所_公共厕所}\n"
						+ "(厕所){公共设施_公共厕所_公共厕所}\n"
						+ "(上厕所){公共设施_公共厕所_公共厕所}\n"
						+ "(上卫生间){公共设施_公共厕所_公共厕所}\n"
						+ "(停车){交通设施服务_停车场}\n"
						+ "(停车场){交通设施服务_停车场}\n"
						+ "(停车库){交通设施服务_停车场}\n"
						+ "(停车位){交通设施服务_停车场}\n"
						+ "(卫生间){公共设施_公共厕所_公共厕所}\n"
						+ "(洗手间){公共设施_公共厕所_公共厕所}\n"
						+ "(药材店铺){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药材铺){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药店){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药房){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药品店){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药品房){医疗保健服务_医药保健销售店_药房}\n"
						+ "(药铺){医疗保健服务_医药保健销售店_药房}\n"
						+ "(医药店){医疗保健服务_医药保健销售店_药房}\n"
						+ "(买药){医疗保健服务_医药保健销售店_药房}\n"
						+ "(百货商场){购物服务_商场_商场}\n"
						+ "(百货商城){购物服务_商场_商场}\n"
						+ "(百货商店){购物服务_商场_商场}\n"
						+ "(购物){购物服务_商场_商场}\n"
						+ "(买东西){购物服务_商场_商场}\n"
						+ "(商场){购物服务_商场_商场}\n"
						+ "(商城){购物服务_商场_商场}\n"
						+ "(商厦){购物服务_商场_商场}\n"
						+ "(商业场所){购物服务_商场_商场}\n"
						+ "(超级市场){购物服务_超级市场_超市}\n"
						+ "(超市){购物服务_超级市场_超市}\n"
						+ "(大型综合超市){购物服务_超级市场_超市}\n"
						+ "(自选商场){购物服务_超级市场_超市}\n"
						+ "(综合性零售商场){购物服务_超级市场_超市}\n";
				
				String name = "(英大林山)\n"
						+"(戴元)\n"
						+"(鲁伟亮)\n"
						+"(刘海波)\n"
						+"(杨梦琴)\n"
						+"(马传洁)\n"
						+"(张成震)\n"
						+"(李猛)\n"
						+"(罗宝琪)\n"
						+"(张喜琴)\n"
						+"(黄新征)\n"
						+"(阿初)\n"
						+"(小涛)\n"
						+"(海雅车展)\n"
						+"(申阳)\n"
						+"(欣然)\n"
						+"(吳浩明)\n"
						+"(邢飞)\n"
						+"(秋霞)\n"
						+"(晓展)\n"
						+"(裘军)\n"
						+"(朱海兵)\n"
						+"(吕育红)\n"
						+"(王连福)\n"
						+"(姬江涛)\n"
						+"(肖贵鸿)\n"
						+"(苑一峰)\n"
						+"(王秋亮家)\n"
						+"(济南库)\n"
						+"(陈征)\n"
						+"(牙科)\n"
						+"(李常艳)\n"
						+"(王哥现代)\n"
						+"(陈易)\n"
						+"(徐红叶)\n"
						+"(史楠楠)\n"
						+"(刘伟华)\n"
						+"(霍铭锋)\n"
						+"(郭晨新)\n"
						+"(陈忠)\n"
						+"(裴浩)\n"
						+"(晶晶爸)\n"
						+"(田忠)\n"
						+"(陈孝金)\n"
						+"(陈善辉)\n"
						+"(胡廷海)\n"
						+"(杭州张韵)\n"
						+"(马军哥)\n"
						+"(翟克友)\n"
						+"(曾凤珍)\n"
						+"(职办)\n"
						+"(李丽芬同)\n"
						+"(阿贵)\n"
						+"(宝业钟)\n"
						+"(阮廓)\n"
						+"(张国林)\n"
						+"(婚庆威少)\n"
						+"(小辆)\n"
						+"(黄钻)\n"
						+"(健李小磨)\n"
						+"(韩璐)\n"
						+"(吴志茵)\n"
						+"(张笑怡)\n"
						+"(满根)\n"
						+"(小梦)\n"
						+"(由欣)\n"
						+"(蛋蛋)\n"
						+"(江南)\n"
						+"(凌中华)\n"
						+"(阿佳)\n"
						+"(郑光鹏)\n"
						+"(交行小邱)\n"
						+"(杜老师)\n"
						+"(陈文茂)\n"
						+"(蔡正林)\n"
						+"(寿大爷)\n"
						+"(李磅礡)\n"
						+"(二表哥)\n"
						+"(赵三京)\n"
						+"(范维新)\n"
						+"(赵立民)\n"
						+"(王飞机)\n"
						+"(帅歌为)\n"
						+"(李智)\n"
						+"(唐海燕)\n"
						+"(杨远龙)\n"
						+"(君君)\n"
						+"(程志彬)\n"
						+"(韦广尖)\n"
						+"(陈美欢)\n"
						+"(陈小云)\n"
						+"(程阿丽)\n"
						+"(语驾曹成)\n"
						+"(王俊宇)\n"
						+"(永佳阿荣)\n"
						+"(田开吊车)\n"
						+"(秦岭)\n"
						+"(安艳)\n"
						+"(钱鼓手)\n"
						+"(石国平)\n";
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
				String command = "(打开空调)\n"
						+"(车内循环)\n"
						+"(车外循环)\n"
						+"(关闭空调)\n"
						+"(打开天窗)\n"
						+"(关闭天窗)\n"
						+"(接听)\n"
						+"(挂断)\n"
						+"(打电话)\n"
						+"(打开导航)\n"
						+"(关闭导航)\n"
						+"(打开本地音乐)\n"
						+"(关闭音乐)\n"
						+"(播放)\n"
						+"(暂停)\n"
						+"(收藏)\n"
						+"(随机播放)\n"
						+"(单曲循环)\n"
						+"(全部循环)\n"
						+"(播放收藏音乐)\n"
						+"(打开一路享听)\n"
						+"(打开有声读物)\n"
						+"(上一台)\n"
						+"(下一台)\n"
						+"(打开新闻)\n"
						+"(打开电台)\n"
						+"(关闭收音机)\n"
						+"(自动搜台)\n"
						+"(内循环)\n"
						+"(外循环)\n"
						+"(通讯录)\n"
						+"(导航)\n"
						+"(本地音乐)\n"
						+"(关闭广播)\n"
						+"(电台)\n"
						+"(播放收藏列表)\n"
						+"(一路享听)\n"
						+"(广播)\n"
						+"(上一曲)\n"
						+"(下一曲)\n"
						+"(有声读物)\n"
						+"(上一个)\n"
						+"(下一个)\n"
						+"(上一条)\n"
						+"(下一条)\n"
						+"(新闻)\n"
						+"(上一首)\n"
						+"(下一首)\n"
						+"(打开收音机)\n"
						+"(打开广播)\n"
						+"(关闭电台	)\n"
						+"(收音机)\n"
						+"(启动导航)\n"
						+"(打开地图)\n"
						+"(地图)\n"
						+"(取消导航)\n"
						+"(关闭地图)\n"
						+"(退出导航)\n"
						+"(退出地图)\n"
						+"(打开音乐)\n"
						+"(启动音乐)\n"
						+"(听音乐)\n"
						+"(音乐)\n"
						+"(M.P.三)\n"
						+"(歌曲)\n"
						+"(听歌)\n"
						+"(取消音乐)\n"
						+"(暂停音乐)\n"
						+"(停止音乐)\n"
						+"(退出音乐)\n"
						+"(打开视频)\n"
						+"(看视频)\n"
						+"(播放视频)\n"
						+"(放视频)\n"
						+"(看电影)\n"
						+"(看片)\n"
						+"(关闭视频)\n"
						+"(取消视频)\n"
						+"(打开蓝牙音乐)\n"
						+"(启动蓝牙音乐)\n"
						+"(听蓝牙音乐)\n"
						+"(播放蓝牙音乐)\n"
						+"(A.二D.P.)\n"
						+"(蓝牙歌曲)\n"
						+"(蓝牙M.P.三)\n"
						+"(关闭蓝牙音乐)\n"
						+"(取消蓝牙音乐)\n"
						+"(暂停蓝牙音乐)\n"
						+"(停止蓝牙音乐)\n"
						+"(退出蓝牙音乐)\n"
						+"(打开设置)\n"
						+"(设置)\n"
						+"(修改设置)\n"
						+"(更改设置)\n"
						+"(设定)\n"
						+"(修改设定)\n"
						+"(调节)\n"
						+"(关闭设置)\n"
						+"(设置完成)\n"
						+"(退出设置)\n";
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "打开空调,"
						+ "车内循环,"
						+ "车外循环,"
						+ "关闭空调,"
						+ "打开天窗,"
						+ "关闭天窗,"
						+ "接听,"
						+ "挂断,"
						+ "打电话,"
						+ "打开导航,"
						+ "关闭导航,"
						+ "打开本地音乐,"
						+ "关闭音乐,"
						+ "播放,"
						+ "暂停,"
						+ "收藏,"
						+ "随机播放,"
						+ "单曲循环,"
						+ "全部循环,"
						+ "播放收藏音乐,"
						+ "打开一路享听,"
						+ "打开有声读物,"
						+ "上一台,"
						+ "下一台,"
						+ "打开新闻,"
						+ "打开电台,"
						+ "关闭收音机,"
						+ "自动搜台,"
						+ "内循环,"
						+ "外循环,"
						+ "通讯录,"
						+ "导航,"
						+ "本地音乐,"
						+ "关闭广播,"
						+ "电台,"
						+ "播放收藏列表,"
						+ "一路享听,"
						+ "广播,"
						+ "上一曲,"
						+ "下一曲,"
						+ "有声读物,"
						+ "上一个,"
						+ "下一个,"
						+ "上一条,"
						+ "下一条,"
						+ "新闻,"
						+ "上一首,"
						+ "下一首,"
						+ "打开收音机,"
						+ "打开广播,"
						+ "关闭电台,"
						+ "收音机,";
				
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "打开空调,"
						+ "车内循环,"
						+ "车外循环,"
						+ "关闭空调,"
						+ "打开天窗,"
						+ "关闭天窗,"
						+ "接听,"
						+ "挂断,"
						+ "打电话,"
						+ "打开导航,"
						+ "关闭导航,"
						+ "打开本地音乐,"
						+ "关闭音乐,"
						+ "播放,"
						+ "暂停,"
						+ "收藏,"
						+ "随机播放,"
						+ "单曲循环,"
						+ "全部循环,"
						+ "播放收藏音乐,"
						+ "打开一路享听,"
						+ "打开有声读物,"
						+ "上一台,"
						+ "下一台,"
						+ "打开新闻,"
						+ "打开电台,"
						+ "关闭收音机,"
						+ "自动搜台,"
						+ "内循环,"
						+ "外循环,"
						+ "通讯录,"
						+ "导航,"
						+ "本地音乐,"
						+ "关闭广播,"
						+ "电台,"
						+ "播放收藏列表,"
						+ "一路享听,"
						+ "广播,"
						+ "上一曲,"
						+ "下一曲,"
						+ "有声读物,"
						+ "上一个,"
						+ "下一个,"
						+ "上一条,"
						+ "下一条,"
						+ "新闻,"
						+ "上一首,"
						+ "下一首,"
						+ "打开收音机,"
						+ "打开广播,"
						+ "关闭电台,"
						+ "收音机,"
						+ "江苏城市,江苏少儿,江苏影视,江苏招考,江苏故事,江苏故事广播,江苏教育,江苏教育#,江苏文艺,江苏文艺广播,江苏文艺电台,江苏新闻,江苏新闻广播,江苏新闻综合,江苏新闻综合广播,江苏电台广播,江苏电视台,江苏经典流行音乐,江苏经典流行音乐广播,江苏综艺,江苏综艺#,江苏调频,江苏财经,江苏财经广播,江苏远教一套,江苏靓妆,江苏音乐,江苏音乐广播,江苏音频广播,江苏高清,江苏高清(HD),江苏高清(体验),江苏高清(可回看),江苏高清卫视,江西,江西交通广播,江西农村广播,江西卫视,江西卫视#,江西卫视(基本),江西卫视（选传）,江西指南,江西教育,江西新闻广播,江西旅游广播,江西有线,江西民生广播,江西电视台,江西都市广播,江西音乐广播,";
				
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "打开空调,"
						+ "车内循环,"
						+ "车外循环,"
						+ "关闭空调,"
						+ "打开天窗,"
						+ "关闭天窗,"
						+ "接听,"
						+ "挂断,"
						+ "打电话,"
						+ "打开导航,"
						+ "关闭导航,"
						+ "打开本地音乐,"
						+ "关闭音乐,"
						+ "播放,"
						+ "暂停,"
						+ "收藏,"
						+ "随机播放,"
						+ "单曲循环,"
						+ "全部循环,"
						+ "播放收藏音乐,"
						+ "打开一路享听,"
						+ "打开有声读物,"
						+ "上一台,"
						+ "下一台,"
						+ "打开新闻,"
						+ "打开电台,"
						+ "关闭收音机,"
						+ "自动搜台,"
						+ "内循环,"
						+ "外循环,"
						+ "通讯录,"
						+ "导航,"
						+ "本地音乐,"
						+ "关闭广播,"
						+ "电台,"
						+ "播放收藏列表,"
						+ "一路享听,"
						+ "广播,"
						+ "上一曲,"
						+ "下一曲,"
						+ "有声读物,"
						+ "上一个,"
						+ "下一个,"
						+ "上一条,"
						+ "下一条,"
						+ "新闻,"
						+ "上一首,"
						+ "下一首,"
						+ "打开收音机,"
						+ "打开广播,"
						+ "关闭电台,"
						+ "收音机,"
						+ "江苏城市,江苏少儿,江苏影视,江苏招考,江苏故事,江苏故事广播,江苏教育,江苏教育#,江苏文艺,江苏文艺广播,江苏文艺电台,江苏新闻,江苏新闻广播,江苏新闻综合,江苏新闻综合广播,江苏电台广播,江苏电视台,江苏经典流行音乐,江苏经典流行音乐广播,江苏综艺,江苏综艺#,江苏调频,江苏财经,江苏财经广播,江苏远教一套,江苏靓妆,江苏音乐,江苏音乐广播,江苏音频广播,江苏高清,江苏高清(HD),江苏高清(体验),江苏高清(可回看),江苏高清卫视,江西,江西交通广播,江西农村广播,江西卫视,江西卫视#,江西卫视(基本),江西卫视（选传）,江西指南,江西教育,江西新闻广播,江西旅游广播,江西有线,江西民生广播,江西电视台,江西都市广播,江西音乐广播,"
						+ "榆林一套,榆林三套,榆林二套,榆阳一套,榆阳二套,欢乐时光广播,欢乐童年,欢乐频道,欢笑剧场,欢笑剧场 (订购),欢笑剧场 HD,欢笑剧场 高清,欢笑剧场(付费),欢笑剧场(文广),欢笑剧场(暂转),欢笑剧场-文广综艺,欢笑剧场HD,欢笑剧场★,欢笑剧场高清,欢笑剧场?,欢笑剧场（付费）,欧亚体育,欧洲足球,欧洲足球 (订购),欧洲足球($),欧洲足球(付费),欧洲足球(免费试播),欧洲足球（试播）,欧美剧场,欧美劲爆,欧美大片NVOD,欧美影院,欧美影院(NVOD),欧美影院-高清,欧美影院HD,欧美影院标清,欧美影院高清,欧美电影,欧美电影($),欧美电影(付费),欧美电影(标清版),欧美电影*,欧美电影标清,欧美电影高清,欧美精选,欧美金曲,歌华导视,歌手专放,正安新闻,武侯政务,武威公共频道,武威导视,武威广播,武威广播1,武威新闻综合,武术世界,武术世界($),武术世界(付费),武术世界(免费试播),武汉一套,武汉七套,武汉三套,武汉二套,武汉五套,武汉人民广播电台,武汉六套,武汉味道,武汉四套,武汉教育,武汉消费指南,武汉电台交通广播,武汉电台少儿广播,武汉电台音乐广播,武进,毕节新闻联播,民乐,民乐之声,民乐广播,民乐广播1,民乐广播2,民乐广播3,民乐广播4,民乐广播5,民族之声,民族音乐,气象,气象影视,气象频道,永州公共,永州影视,永州综合,永州都市,求索动物高清,求索生活高清,求索科学高清,求索纪录高清,汇川新闻,汉中一套,汉中三套,汉中二套,";
				
				
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
		
		
		//异步启动测试程序
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("zts");
				testModel0.set_testModelListener(mPocketSphinxDemo);

				String hotWords = "打开空调,"
						+ "车内循环,"
						+ "车外循环,"
						+ "关闭空调,"
						+ "打开天窗,"
						+ "关闭天窗,"
						+ "接听,"
						+ "挂断,"
						+ "打电话,"
						+ "打开导航,"
						+ "关闭导航,"
						+ "打开本地音乐,"
						+ "关闭音乐,"
						+ "播放,"
						+ "暂停,"
						+ "收藏,"
						+ "随机播放,"
						+ "单曲循环,"
						+ "全部循环,"
						+ "播放收藏音乐,"
						+ "打开一路享听,"
						+ "打开有声读物,"
						+ "上一台,"
						+ "下一台,"
						+ "打开新闻,"
						+ "打开电台,"
						+ "关闭收音机,"
						+ "自动搜台,"
						+ "内循环,"
						+ "外循环,"
						+ "通讯录,"
						+ "导航,"
						+ "本地音乐,"
						+ "关闭广播,"
						+ "电台,"
						+ "播放收藏列表,"
						+ "一路享听,"
						+ "广播,"
						+ "上一曲,"
						+ "下一曲,"
						+ "有声读物,"
						+ "上一个,"
						+ "下一个,"
						+ "上一条,"
						+ "下一条,"
						+ "新闻,"
						+ "上一首,"
						+ "下一首,"
						+ "打开收音机,"
						+ "打开广播,"
						+ "关闭电台,"
						+ "收音机,"
						+ "江苏城市,江苏少儿,江苏影视,江苏招考,江苏故事,江苏故事广播,江苏教育,江苏教育#,江苏文艺,江苏文艺广播,江苏文艺电台,江苏新闻,江苏新闻广播,江苏新闻综合,江苏新闻综合广播,江苏电台广播,江苏电视台,江苏经典流行音乐,江苏经典流行音乐广播,江苏综艺,江苏综艺#,江苏调频,江苏财经,江苏财经广播,江苏远教一套,江苏靓妆,江苏音乐,江苏音乐广播,江苏音频广播,江苏高清,江苏高清(HD),江苏高清(体验),江苏高清(可回看),江苏高清卫视,江西,江西交通广播,江西农村广播,江西卫视,江西卫视#,江西卫视(基本),江西卫视（选传）,江西指南,江西教育,江西新闻广播,江西旅游广播,江西有线,江西民生广播,江西电视台,江西都市广播,江西音乐广播,"
						+ "河南经济广播,河南都市,河广交通广播,河西区,油田影视,油田有线,油田综合,油轮旅游,沾化电影,沾化综合,沾化综艺,泉州1套,泉州2套,泉州3套,泉州4套,泉州新闻,法制·科教,法制天地,法制天地(付费),法制天地-文广记录,法制频道,法国TV5,法国时尚,法国时装,法国电视5台,法律服务,法律讲堂（VOD）,法治天地,法治天地 (订购),法治天地(付费),法治天地★,法治天地?,法治天地（付费）,泰兴图文,泰兴新闻,泰兴有线,泰兴综合,泰安公共,泰安图文,泰安教科,泰安新闻,泰山先锋,泰山电视,泰州三套,泰州影视娱乐,泰州法制频道,泸州公共,泸州数字电视,泸州科教,泸州综合,泸州资讯,泸州音频广播1,泸州音频广播2,泸州音频广播3,洛阳电视台-1,洛阳电视台-2,洛阳电视台-3,洛阳电视台-4,活力广东,流行金曲,流行音乐,流连1015,测01,测02,测03,测04,测05,测06,测08,测09,测试,测试-1,测试-2,测试-3,测试-CCTV-1高清,测试-CCTV-5 高清,测试-东方高清,测试-北京高清,测试-广东高清,测试-江苏高清,测试-浙江高清,测试-深圳高清,测试-湖南高清,测试-黑龙江高清,测试0,测试01,测试02,测试1,测试10,测试11,测试12,测试13,测试14,测试2,测试3,测试4,测试5,测试6,测试7,测试8,测试9,测试CINEMAX电影,测试CNN,测试ESPN,测试★,测试一,测试一高清,测试七,测试三,测试九,测试二,测试二高清,测试五,测试体育,测试信号,测试信号-1,测试信号-1高清,测试信号-2,测试信号-2高清,测试信号-3,测试信号-4,测试信号-5,测试信号-6,测试信号1,测试信号2,测试信号3,测试信号4,测试信号5,测试信号一,测试信号七,测试信号三,测试信号二,测试信号五,测试信号六,测试信号四,测试八,测试六,测试凤凰中文,测试凤凰电影,测试凤凰资讯,测试十,测试十一,测试十三,测试十二,测试十五,测试十四,测试华娱卫视,测试四,测试娱乐1,测试探索频道,测试新闻1,测试新闻2,测试星空卫视,测试星空电影,测试法国时尚,测试电影1,测试电影2,测试节目,测试节目(空),测试节目1,测试节目1(高清),测试节目10,测试节目11,测试节目12,测试节目13,测试节目14,测试节目15,测试节目16,测试节目17,测试节目18,测试节目19,测试节目2,测试节目2(高清),测试节目20,测试节目21,测试节目2307,测试节目3,测试节目4,测试节目5,测试节目6,测试节目7,测试节目8,测试节目9,测试节目HS,测试节目一,测试节目一高清,测试节目七,测试节目三,测试节目九,测试节目二,测试节目二高清,测试节目五,测试节目八,测试节目六,测试节目十,测试节目四,测试记录1,测试记录2,测试阳光卫视,测试音频,测试预留,测试预留(付费),测试预留(标清版),测试预留*,测试预留☆,测试预留标清,测试预留高清,测试频道,测试频道-1,测试频道-10,测试频道-2,测试频道-3,测试频道-4,测试频道-6,测试频道-7,测试频道-8,测试频道-9,测试频道01,测试频道02,测试频道03,测试频道04,测试频道05,测试频道06,测试频道07,测试频道08,测试频道09,测试频道1,测试频道10,测试频道11,测试频道12,测试频道13,测试频道14,测试频道15,测试频道16,测试频道17,测试频道18,测试频道19,测试频道1高清,测试频道1（四川）,测试频道2,测试频道20,测试频道21,测试频道2高清,测试频道2（广西）,测试频道3,测试频道3（安徽）,测试频道4,测试频道5,测试频道6,测试频道7,测试频道8,测试频道9,测试频道一,测试频道三,测试频道二,测试频道五,测试频道六,测试频道四,测试高清1,测试高清2,济南商务,济南娱乐,济南少儿,济南影视,济南影视高清,济南教育,济南新闻,济南新闻高清,济南生活,济南都市,济源-济钢,济源-综合,济铁,济阳影视,济阳综合,浏阳人民广播电台,浏阳商贸,浏阳图文,浏阳房产,浏阳新闻,浏阳经济,浙一,浙三,浙二,浙江,浙江 HD,浙江 高清,浙江-HD,浙江-交通之声,浙江-公共新农村,浙江-公共频道,浙江-卫视频道,浙江-城市之声,浙江-少儿频道,浙江-影视娱乐,浙江-影视文化,浙江-教育科技,浙江-文艺频道,浙江-新闻综合,浙江-旅游之声,浙江-民生休闲,浙江-浙江经视,浙江-经济频道,浙江-经视,浙江-钱江都市,浙江-音乐调频,浙江-高清,浙江1台,浙江2,浙江2台,浙江3,浙江3台,浙江4,浙江4台,浙江5,浙江5台,浙江6,浙江6台,浙江6频道,浙江7,浙江7台,浙江8,浙江8台,浙江HD,浙江之声,浙江交通之声,浙江体彩,浙江党建,浙江公共,浙江公共新农村,浙江公共资讯,浙江公共频道,浙江卫视,浙江卫视#,浙江卫视(基本),浙江卫视(选传),浙江卫视(高清),浙江卫视-R,浙江卫视-高清,浙江卫视HD,浙江卫视高清,浙江卫视高清#,浙江卫视高清频道,浙江卫视?高清?,浙江卫视（展播）,浙江卫视（高清）,浙江城市之声,浙江少儿,浙江少儿频道,浙江广播-6,浙江广播-7,浙江广播-8,浙江广播一,浙江广播三,浙江广播二,浙江广播电台,浙江广播音乐频道,浙江影视,浙江影视娱乐,浙江影视文化,浙江教育,浙江教育科技,浙江文艺广播,浙江文艺频道,浙江新闻台,浙江新闻综合,浙江新闻频道,浙江旅游之声,浙江民生休闲,浙江民生广播,浙江电台广播,浙江电台旅游之声,浙江电视台,浙江经济生活,浙江经济频道,浙江经视,浙江网视,浙江财富广播,浙江钱江都市,浙江音乐,浙江音乐调频,浙江音频广播,浙江高清,浙江高清(HD),浙江高清(体验),浙江高清(可回看),浙江高清卫视,海上戏剧,海南公共,海南卫视,海南广播,海南影视,海南文体信息,海南新闻,海南新闻广播,海南旅游,海南旅游卫视,海南综合,海南青少,海口1套,";
				
				
				String path = getSDPath();
				
				testModel0.startTest(path + "/Android/data/edu.cmu.pocketsphinx/android_test/test_zts16k_config_android_416.xml",
						path + "/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest_ngram500.log", 
						hotWords, assetMgr, modelDir);				
			}
			
		};
		h.postDelayed(r, 10);	
		
	}
	
	//写数据到SD中的文件
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