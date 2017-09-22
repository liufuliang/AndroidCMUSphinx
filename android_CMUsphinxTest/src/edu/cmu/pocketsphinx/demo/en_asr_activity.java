package edu.cmu.pocketsphinx.demo;

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cmusphinxtest_1.R;


public class en_asr_activity  extends Activity implements OnTouchListener, RecognitionListener{

	EditText mEditText = null;
	en_asr_activity mEn_asr_activity = null;
	
	private EditText edit_text = null;

	
	private RecognizerTask rec = null;
	private Thread rec_thread = null;
	
	private long start_time = 0;
	private long end_time = 0;

		
	private String name = "(hello)\n"
			+"(how are you)\n"
			+"(hi)\n"
			+"(good night)\n"
			+"(good morning)\n"
			+"(good afternoon)\n"
			+"(i am fine)\n"
			+"(thanks)\n"
			+"(nice to meet you)\n"
			+"(how do you do)\n";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.en_asr);
		
		Button b = (Button) findViewById(R.id.Button03);
		b.setOnTouchListener(this);

		this.edit_text = (EditText) findViewById(R.id.EditText03);
		
		mEn_asr_activity = this;
		
		//然后决定是否使用云地语音识别还是本地语音识别
		RecognizerAsyncTask task = new RecognizerAsyncTask(this);
		task.execute();
	}



	
	/*
	 * 注意继承时需要设定三个泛型Params，Progress和Result的类型，如AsyncTask<Void,Inetger,Void>：
	 * Params是指调用execute()方法时传入的参数类型和doInBackgound()的参数类型
	 * Progress是指更新进度时传递的参数类型，即publishProgress()和onProgressUpdate()的参数类型
	 * Result是指doInBackground()的返回值类
	 */
	class RecognizerAsyncTask extends AsyncTask<Void,Integer,Integer>{  
        private en_asr_activity context;  
        RecognizerAsyncTask(en_asr_activity context) {  
            this.context = context;  
        }  
  
        /** 
         * 运行在UI线程中，在调用doInBackground()之前执行 
         */  
        @Override  
        protected void onPreExecute() {  
            Toast.makeText(context,"云地语音识别引擎初始化开始。。。",Toast.LENGTH_SHORT).show();  
        }  
        /** 
         * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法 
         */  
        @Override  
        protected Integer doInBackground(Void... params) {  
        	
//        	Common.phone = 
        	
        	
//			this.rec = new RecognizerTask("CyVoiceE_jsgf");
        	this.context.rec = new RecognizerTask("CyVoiceE_jsgf","-Custom_jsgf",this.context.name);

        	this.context.rec_thread = new Thread(this.context.rec);

			
			
        	this.context.rec.setRecognitionListener(this.context);
        	this.context.rec_thread.start(); 
            return 0;  
        }  
  
        /** 
         * 运行在ui线程中，在doInBackground()执行完毕后执行 
         */  
        @Override  
        protected void onPostExecute(Integer integer) {  
            Toast.makeText(context,"云地语音识别引擎初始化完成!",Toast.LENGTH_SHORT).show();  
        }  
  
        /** 
         * 在publishProgress()被调用以后执行，publishProgress()用于更新进度 
         */  
        @Override  
        protected void onProgressUpdate(Integer... values) {  
        	super.onProgressUpdate(values); 
        }  
    }
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId() == R.id.Button03)
		{
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				if(this.rec == null)
				{
					
					String str = new String("语音识别引擎未初始化完成，请稍后。。。。。。");
					Log.e(CommonFun.TAG,str);
							
					
					CommonFun.showMessage(this,str);
				}
				else
				{
					this.rec.start();
					
					this.start_time =  System.currentTimeMillis();
				}
				
				
				break;
			case MotionEvent.ACTION_UP:

				if(this.rec != null)
				{
					this.rec.stop();
					this.end_time = System.currentTimeMillis();
				}

				break;
			default:
				;
			}
		}
		
		
		/* Let the button handle its own state */
		return false;
	}
	
	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final en_asr_activity that = this;
		final String hyp = b.getString("hyp");
		that.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
			}
		});
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		
		int time = (int) ((this.end_time - this.start_time) / 25);
		final String hyp = b.getString("hyp") + "	你的语音评分是 : " + time + " 分";
		final en_asr_activity that = this;
		this.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
				
			}
		});
	}

	public void onError(int err) {
		final en_asr_activity that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				String StrErr = "error = ";
				that.edit_text.setText(StrErr);
			}
		});
	}


}
