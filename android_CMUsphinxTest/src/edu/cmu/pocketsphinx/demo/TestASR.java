package edu.cmu.pocketsphinx.demo;

import java.io.IOException;

import com.example.cmusphinxtest_1.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class TestASR extends Activity implements OnTouchListener, RecognitionListener,testModelListener{
	/**
	 * Editable text view.
	 */
	EditText edit_text;
	
	/**
	 * 
	 */
	private EditText edit_testFilePath = null;
	private EditText edit_testFileName = null;
	
	private EditText edit_gramFilePath = null;
	private EditText edit_gramFileName = null;
	
	private TestASR mTestASR = null;
	
	private void init()
	{
		this.edit_text = (EditText) findViewById(R.id.editTextResult);
		
		this.edit_testFilePath = (EditText) findViewById(R.id.edittestFilePath);
		this.edit_testFileName = (EditText) findViewById(R.id.editTestFileName);
		
		this.edit_gramFilePath = (EditText) findViewById(R.id.editGramFilePath);
		this.edit_gramFileName = (EditText) findViewById(R.id.editGramFileName);
	}
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_asr);
		
		init();
		
		mTestASR = this;
	}
	
	
	
	public void onTestFileButtonClicked(View view)
	{
		String strSDCard = Environment.getExternalStorageDirectory().getPath();
		
		
		//“Ï≤Ω∆Ù∂Ø≤‚ ‘≥Ã–Ú
		Handler h = new Handler();
		Runnable r = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				testModel testModel0 = new testModel("sphinx");
				testModel0.set_testModelListener(mTestASR);

				
				String testFilePath = edit_testFilePath.getText().toString();
				String testFileName = edit_testFileName.getText().toString();
				String xmlFile = testFilePath + testFileName;
				
				String gramFilePath = edit_gramFilePath.getText().toString();
				String gramFileName = edit_gramFileName.getText().toString();
				String gramFile = gramFilePath + gramFileName;
				
				String str1 = null;
				try {
					str1 = CommonFun.loadAFileToStringDE3(gramFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				testModel0.startTest(
						xmlFile,
						"/mnt/sdcard/Android/data/edu.cmu.pocketsphinx/android_test/sphinxTest.log", 
						null, 
						str1, /* command.gram  command.fsg */
						null);
				
			}
			
		};
		h.postDelayed(r, 10);	
		
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

	@Override
	public void onPartialResults(Bundle b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResults(Bundle b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
