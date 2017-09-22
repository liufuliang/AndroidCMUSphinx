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
		
		//Ȼ������Ƿ�ʹ���Ƶ�����ʶ���Ǳ�������ʶ��
		RecognizerAsyncTask task = new RecognizerAsyncTask(this);
		task.execute();
	}



	
	/*
	 * ע��̳�ʱ��Ҫ�趨��������Params��Progress��Result�����ͣ���AsyncTask<Void,Inetger,Void>��
	 * Params��ָ����execute()����ʱ����Ĳ������ͺ�doInBackgound()�Ĳ�������
	 * Progress��ָ���½���ʱ���ݵĲ������ͣ���publishProgress()��onProgressUpdate()�Ĳ�������
	 * Result��ָdoInBackground()�ķ���ֵ��
	 */
	class RecognizerAsyncTask extends AsyncTask<Void,Integer,Integer>{  
        private en_asr_activity context;  
        RecognizerAsyncTask(en_asr_activity context) {  
            this.context = context;  
        }  
  
        /** 
         * ������UI�߳��У��ڵ���doInBackground()֮ǰִ�� 
         */  
        @Override  
        protected void onPreExecute() {  
            Toast.makeText(context,"�Ƶ�����ʶ�������ʼ����ʼ������",Toast.LENGTH_SHORT).show();  
        }  
        /** 
         * ��̨���еķ������������з�UI�̣߳�����ִ�к�ʱ�ķ��� 
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
         * ������ui�߳��У���doInBackground()ִ����Ϻ�ִ�� 
         */  
        @Override  
        protected void onPostExecute(Integer integer) {  
            Toast.makeText(context,"�Ƶ�����ʶ�������ʼ�����!",Toast.LENGTH_SHORT).show();  
        }  
  
        /** 
         * ��publishProgress()�������Ժ�ִ�У�publishProgress()���ڸ��½��� 
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
					
					String str = new String("����ʶ������δ��ʼ����ɣ����Ժ󡣡���������");
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
		final String hyp = b.getString("hyp") + "	������������� : " + time + " ��";
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
