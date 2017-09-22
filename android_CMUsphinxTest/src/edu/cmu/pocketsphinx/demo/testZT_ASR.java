package edu.cmu.pocketsphinx.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ztspeech.asrtest.ASRInterface;
import com.ztspeech.asrtest.OnlineASR;




public class testZT_ASR implements Runnable,ASRInterface {
	final public int BUFF_SIZE = 300*16*2;//300*16*2

	private Handler mHandler = null;

	//���б��
	private boolean runFlag = false;
	
	private String mRawFile = null;
	
	private long startTime = 0;
	private long endTime = 0;

	private OnlineASR asr = null;
	private int testFileLen = 0;

	
	@Override
	public void OnResult(String asrtext) {
//		int waveTimeMs = waveDataSize / waveFrameSize * waveFrameMs;
//		float rtRatio = 0;
//		recoStopTime = new Date();
//		int recoTimeMs = (int) (recoStopTime.getTime() - recoStartTime
//				.getTime());
//		if (waveTimeMs > 0)
//			rtRatio = (float) recoTimeMs / waveTimeMs;
		
	//	asrtext2 += " [ " + asrtext + " ]";
//		tvText.setText("[ " + asrtext + " ]" + " RT: " + rtRatio);
	//	asralltext += "\n[ " + asrtext + " ]" + " RT: " + rtRatio;
	//	tvTexta.setText(tvTexta.getText() + "\n" + tvText.getText());
	//	tvText.setText(asralltext);
		

		
		//Log.e(CommonFun.TAG, "end......."+hypothesis);
		if((asrtext == null) || (asrtext == ""))
			asrtext = "no match";
		Log.e(CommonFun.TAG, "asrtext.length() = "+ asrtext.length());
		
		
	}
	
	@Override
	public void OnFinalResult(String asrtext) {
	
		
		endTime = System.currentTimeMillis();
		
		//Log.e(CommonFun.TAG, "end......."+hypothesis);
		if((asrtext == null) || (asrtext == ""))
			asrtext = "no match";
		Log.e(CommonFun.TAG, "asrtext.length() = "+ asrtext.length());
		
		int time = (int)(endTime - startTime);
		
		if(testFileLen != 0)
		{
			sendMsg(1,asrtext,time,testFileLen);
			testFileLen = 0;
		}
	}

	public void OnError(String errormsg) {
//		tvText.setText(errormsg);
		
		if(testFileLen != 0)
		{
			endTime = System.currentTimeMillis();
			int time = (int)(endTime - startTime);
			sendMsg(1,"no match",time,testFileLen);
			testFileLen = 0;
		}
			
	}

	public void OnAutoStop(String msg) {
//		btnStopRec.performClick();
		
		if(testFileLen != 0)
		{
			endTime = System.currentTimeMillis();
			int time = (int)(endTime - startTime);
			sendMsg(1,"no match",time,testFileLen);
			testFileLen = 0;
		}
	}
	
	
	
	public testZT_ASR(String hotWords,AssetManager assetMgr,String modelDir,Handler m)
	{
		
		if (asr == null) {
			asr = new OnlineASR();
			asr.InitASR(modelDir + "/ztspeech_asr", this, assetMgr);

//			String words = "�Ӽ�����,���������Ϣ,�����,���ɿƽ�,�����ۺ�,��Ѵ��,��������,�������� (����),��������(����),�������������ѣ�,���ҽֵ�,������ͼ����Ϣ,������Ӱ������,���������ŵ㲥,�����������ۺ�,��������Ƶ�㲥,���Žֵ�,��������,�������1,�������10,�������11,�������2,�������3,�������4,�������5,�������6,�������7,�������8,�������9,����Ӣ��,�껯����վ,���޽�ͨ�㲥,�������Ź㲥,�����ۺ�,ͩ��Ӱ��Ƶ��,ͩ������Ƶ��,ͩ������Ƶ��,����(VOD),���Ӻ�Ƶ��,�� ԰(����),��԰,��԰($),��԰(����),��԰(��ת),��԰D,��԰����,��԰Ƶ��,��԰Ƶ��(����),��԰Ƶ��(����Ƶ��),��԰Ƶ��(�Բ�),��԰Ƶ��D,���1,���2,���3,ɭɭ����,������Ѷ,��������,����һ��,��������,���ֶ���,����һ��,��������,����ʱ��㲥,����ͯ��,����Ƶ��,��Ц�糡,��Ц�糡 (����),��Ц�糡 HD,��Ц�糡 ����,��Ц�糡(����),��Ц�糡(�Ĺ�),��Ц�糡(��ת),��Ц�糡-�Ĺ�����,��Ц�糡HD,��Ц�糡��,��Ц�糡����,��Ц�糡?,��Ц�糡�����ѣ�,ŷ������,ŷ������,ŷ������ (����),ŷ������($),ŷ������(����),ŷ������(����Բ�),ŷ�������Բ���,ŷ���糡,ŷ������,ŷ����ƬNVOD,ŷ��ӰԺ,ŷ��ӰԺ(NVOD),ŷ��ӰԺ-����,ŷ��ӰԺHD,ŷ��ӰԺ����,ŷ��ӰԺ����,ŷ����Ӱ,ŷ����Ӱ($),ŷ����Ӱ(����),ŷ����Ӱ(�����),ŷ����Ӱ*,ŷ����Ӱ����,ŷ����Ӱ����,ŷ����ѡ,ŷ������,�軪����,����ר��,��������,�������,��������Ƶ��,��������,�����㲥,�����㲥1,���������ۺ�,��������,��������($),��������(����),��������(����Բ�),�人һ��,�人����,�人����,�人����,�人����,�人����㲥��̨,�人����,�人ζ��,�人����,�人����,�人����ָ��,�人��̨��ͨ�㲥,�人��̨�ٶ��㲥,�人��̨���ֹ㲥,���,�Ͻ���������,����,����֮��,���ֹ㲥,���ֹ㲥1,���ֹ㲥2,���ֹ㲥3,���ֹ㲥4,���ֹ㲥5,����֮��,��������,����,����Ӱ��,����Ƶ��,���ݹ���,����Ӱ��,�����ۺ�,���ݶ���,�����������,�����������,������ѧ����,������¼����,�㴨����,����һ��,��������,���ж���,��������,���й㲥,��������,��ͷ��ͨ����֮���㲥,��ͷ������Ѷ֮���㲥,��ͷ����֮���㲥,����̨,�������ߵ���,����Ӱ������Ƶ��,��������1,��������2,��������,����,���� HD,���� ����,����-HD,����HD,���ս�ͨ�㲥��,������������,����������ͨ,����������ͨ��ѡ����,��������,������������,������������#,���ս���,���ս����㲥,���չ���,��������,��������#,��������(����),��������(����),��������-R,��������-����,��������HD,�������Ӹ���,�������Ӹ���#,�������Ӹ���Ƶ��,��������?����?,�������ӣ����壩,���չ���,���ճ���,�����ٶ�,����Ӱ��,�����п�,���չ���,���չ��¹㲥,���ս���,���ս���#,��������,�������չ㲥,�������յ�̨,��������,�������Ź㲥,���������ۺ�,���������ۺϹ㲥,���յ�̨�㲥,���յ���̨,���վ�����������,���վ����������ֹ㲥,��������,��������#,���յ�Ƶ,���ղƾ�,���ղƾ��㲥,����Զ��һ��,������ױ,��������,�������ֹ㲥,������Ƶ�㲥,���ո���,���ո���(HD),���ո���(����),���ո���(�ɻؿ�),���ո�������,����,������ͨ�㲥,����ũ��㲥,��������,��������#,��������(����),�������ӣ�ѡ����,����ָ��,��������,�������Ź㲥,�������ι㲥,��������,���������㲥,��������̨,�������й㲥,�������ֹ㲥,���Ź���,����̨,���Ž���,�����ۺ�,����-1,����-2,����-3,��������㲥��̨,�������ֵ���,���ݵ㲥,������Ѷ,�� Ħ,��Ħ,��Ħ($),��Ħ(����),��ĦƵ��,��ĦƵ��(����),����-��������,����-��Ħ,������չ(ѡ��),����Ƶ��,����Ӱ��,������������,�����ۺ�,����TV-1,����TV-2,����TV-3,����TV-4,���ϵ���,��ˮTV-1,��ˮTV-2,��ˮTV-����,��ˮͼ��,��������Ƶ��,������ͨ,����ͼ��,��������,�������Ÿ���,ɳ��Ժ��һ��,ɳ��Ժ������,ɳ��Ժ�߶���,ɳ��Ժ������,ɳ��Ժ������,ɳ��Ժ�ߵ���,����-1,����-2,����-3,���ݽ�ͨ���ֹ㲥,���������ۺϹ㲥,���ݳ������չ㲥,�Ӷ���,�Ӷ�ͼ��,�Ӷ�Ƶ��,�ӱ�,�ӱ� ��ͨ,�ӱ���ͨ,�ӱ���̨ͨ,�ӱ���ͨ�㲥,�ӱ���ͨ�㲥��̨,�ӱ�����̨,�ӱ�����㲥��̨,�ӱ�����,�ӱ�ũ��㲥,�ӱ�ũ��㲥��̨,�ӱ�ũ��,�ӱ���,�ӱ�����,�ӱ�����#,�ӱ�����(����),�ӱ�����(����),�ӱ����Ӹ���,�ӱ����е�̨,�ӱ�����,�ӱ��ٶ�,�ӱ��ٶ��ƽ�,�ӱ�Ӱ��,�ӱ�����ָ��,�ӱ�����̨,�ӱ����չ㲥,�ӱ����չ㲥��̨,�ӱ����Ź㲥,�ӱ����ι㲥,�ӱ��Ӽ�,�ӱ�����̨,�ӱ�����㲥,�ӱ������̨,�ӱ���̨,�ӱ���̨��ͨ,�ӱ���̨ũ��,�ӱ���̨�㲥,�ӱ���̨����,�ӱ���̨����,�ӱ���̨����,�ӱ���̨����,�ӱ���̨����,�ӱ���̨����,�ӱ��ƽ�,�ӱ�����,�ӱ�����̨,�ӱ����ù㲥,�ӱ����ù㲥��̨,�ӱ�����,�ӱ�����,�ӱ�����,�ӱ����ֹ㲥,�ӱ�����,����,���Ͻ�ͨ�㲥,���Ϲ���,��������,��������#,��������(����),���Ϲ���,����Ϸ���㲥,������ũ��,��������,�������Ź㲥,��������-1,��������-2,���Ϸ���,���ϵ��Ӿ�,���ϵ���̨,���ϵ���̨-2,���ϵ���̨-3,���ϵ���̨-4,���ϵ���̨-5,���ϵ���̨-6,���ϵ���̨-8,���Ͼ��ù㲥,���϶���,�ӹ㽻ͨ�㲥,������,����Ӱ��,��������,�����ۺ�,��������,մ����Ӱ,մ���ۺ�,մ������,Ȫ��1��,Ȫ��2��,Ȫ��3��,Ȫ��4��,Ȫ������,���ơ��ƽ�,�������,�������(����),�������-�Ĺ��¼,����Ƶ��,����TV5,����ʱ��,����ʱװ,��������5̨,���ɷ���,���ɽ��ã�VOD��,�������,������� (����),�������(����),������ء�,�������?,������أ����ѣ�,̩��ͼ��,̩������,̩������,̩���ۺ�,̩������,̩��ͼ��,̩���̿�,̩������,̩ɽ�ȷ�,̩ɽ����,̩������,̩��Ӱ������,̩�ݷ���Ƶ��,���ݹ���,�������ֵ���,���ݿƽ�,�����ۺ�,������Ѷ,������Ƶ�㲥1,������Ƶ�㲥2,������Ƶ�㲥3,��������̨-1,��������̨-2,��������̨-3,��������̨-4,�����㶫,���н���,��������,����1015,��01,��02,��03,��04,��05,��06,��08,��09,����,����-1,����-2,����-3,����-CCTV-1����,����-CCTV-5 ����,����-��������,����-��������,����-�㶫����,����-���ո���,����-�㽭����,����-���ڸ���,����-���ϸ���,����-����������,����0,����01,����02,����1,����10,����11,����12,����13,����14,����2,����3,����4,����5,����6,����7,����8,����9,����CINEMAX��Ӱ,����CNN,����ESPN,���ԡ�,����һ,����һ����,������,������,���Ծ�,���Զ�,���Զ�����,������,��������,�����ź�,�����ź�-1,�����ź�-1����,�����ź�-2,�����ź�-2����,�����ź�-3,�����ź�-4,�����ź�-5,�����ź�-6,�����ź�1,�����ź�2,�����ź�3,�����ź�4,�����ź�5,�����ź�һ,�����ź���,�����ź���,�����źŶ�,�����ź���,�����ź���,�����ź���,���԰�,������,���Է������,���Է�˵�Ӱ,���Է����Ѷ,����ʮ,����ʮһ,����ʮ��,����ʮ��,����ʮ��,����ʮ��,���Ի�������,������,��������1,����̽��Ƶ��,��������1,��������2,�����ǿ�����,�����ǿյ�Ӱ,���Է���ʱ��,���Ե�Ӱ1,���Ե�Ӱ2,���Խ�Ŀ,���Խ�Ŀ(��),���Խ�Ŀ1,���Խ�Ŀ1(����),���Խ�Ŀ10,���Խ�Ŀ11,���Խ�Ŀ12,���Խ�Ŀ13,���Խ�Ŀ14,���Խ�Ŀ15,���Խ�Ŀ16,���Խ�Ŀ17,���Խ�Ŀ18,���Խ�Ŀ19,���Խ�Ŀ2,���Խ�Ŀ2(����),���Խ�Ŀ20,���Խ�Ŀ21,���Խ�Ŀ2307,���Խ�Ŀ3,���Խ�Ŀ4,���Խ�Ŀ5,���Խ�Ŀ6,���Խ�Ŀ7,���Խ�Ŀ8,���Խ�Ŀ9,���Խ�ĿHS,���Խ�Ŀһ,���Խ�Ŀһ����,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ������,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀ��,���Խ�Ŀʮ,���Խ�Ŀ��,���Լ�¼1,���Լ�¼2,������������,������Ƶ,����Ԥ��,����Ԥ��(����),����Ԥ��(�����),����Ԥ��*,����Ԥ����,����Ԥ������,����Ԥ������,����Ƶ��,����Ƶ��-1,����Ƶ��-10,����Ƶ��-2,����Ƶ��-3,����Ƶ��-4,����Ƶ��-6,����Ƶ��-7,����Ƶ��-8,����Ƶ��-9,����Ƶ��01,����Ƶ��02,����Ƶ��03,����Ƶ��04,����Ƶ��05,����Ƶ��06,����Ƶ��07,����Ƶ��08,����Ƶ��09,����Ƶ��1,����Ƶ��10,����Ƶ��11,����Ƶ��12,����Ƶ��13,����Ƶ��14,����Ƶ��15,����Ƶ��16,����Ƶ��17,����Ƶ��18,����Ƶ��19,����Ƶ��1����,����Ƶ��1���Ĵ���,����Ƶ��2,����Ƶ��20,����Ƶ��21,����Ƶ��2����,����Ƶ��2��������,����Ƶ��3,����Ƶ��3�����գ�,����Ƶ��4,����Ƶ��5,����Ƶ��6,����Ƶ��7,����Ƶ��8,����Ƶ��9,����Ƶ��һ,����Ƶ����,����Ƶ����,����Ƶ����,����Ƶ����,����Ƶ����,���Ը���1,���Ը���2,��������,��������,�����ٶ�,����Ӱ��,����Ӱ�Ӹ���,���Ͻ���,��������,�������Ÿ���,��������,���϶���,��Դ-�ø�,��Դ-�ۺ�,����,����Ӱ��,�����ۺ�,�������㲥��̨,�����ó,���ͼ��,�������,�������,�������,��һ,����,���,�㽭,�㽭 HD,�㽭 ����,�㽭-HD,�㽭-��֮ͨ��,�㽭-������ũ��,�㽭-����Ƶ��,�㽭-����Ƶ��,�㽭-����֮��,�㽭-�ٶ�Ƶ��,�㽭-Ӱ������,�㽭-Ӱ���Ļ�,�㽭-�����Ƽ�,�㽭-����Ƶ��,�㽭-�����ۺ�,�㽭-����֮��,�㽭-��������,�㽭-�㽭����,�㽭-����Ƶ��,�㽭-����,�㽭-Ǯ������,�㽭-���ֵ�Ƶ,�㽭-����,�㽭1̨,�㽭2,�㽭2̨,�㽭3,�㽭3̨,�㽭4,�㽭4̨,�㽭5,�㽭5̨,�㽭6,�㽭6̨,�㽭6Ƶ��,�㽭7,�㽭7̨,�㽭8,�㽭8̨,�㽭HD,�㽭֮��,�㽭��֮ͨ��,�㽭���,�㽭����,�㽭����,�㽭������ũ��,�㽭������Ѷ,�㽭����Ƶ��,�㽭����,�㽭����#,�㽭����(����),�㽭����(ѡ��),�㽭����(����),�㽭����-R,�㽭����-����,�㽭����HD,�㽭���Ӹ���,�㽭���Ӹ���#,�㽭���Ӹ���Ƶ��,�㽭����?����?,�㽭���ӣ�չ����,�㽭���ӣ����壩,�㽭����֮��,�㽭�ٶ�,�㽭�ٶ�Ƶ��,�㽭�㲥-6,�㽭�㲥-7,�㽭�㲥-8,�㽭�㲥һ,�㽭�㲥��,�㽭�㲥��,�㽭�㲥��̨,�㽭�㲥����Ƶ��,�㽭Ӱ��,�㽭Ӱ������,�㽭Ӱ���Ļ�,�㽭����,�㽭�����Ƽ�,�㽭���չ㲥,�㽭����Ƶ��,�㽭����̨,�㽭�����ۺ�,�㽭����Ƶ��,�㽭����֮��,�㽭��������,�㽭�����㲥,�㽭��̨�㲥,�㽭��̨����֮��,�㽭����̨,�㽭��������,�㽭����Ƶ��,�㽭����,�㽭����,�㽭�Ƹ��㲥,�㽭Ǯ������,�㽭����,�㽭���ֵ�Ƶ,�㽭��Ƶ�㲥,�㽭����,�㽭����(HD),�㽭����(����),�㽭����(�ɻؿ�),�㽭��������,����Ϸ��,���Ϲ���,��������,���Ϲ㲥,����Ӱ��,����������Ϣ,��������,�������Ź㲥,��������,������������,�����ۺ�,��������,����1��,����2��,����3��,����һ��,��������,���ڶ���,����糡,����糡(����),����糡���Բ���,����ӰԺ,����Ƭ��,��Ͽ����,��Ͽ����,����,����,����һ��,���ų��й���,���Ž�������,�������ֵ��Ӽ��,���������ۺ�,����ʱ������,���ŵ�ӰƵ��,���ž�������,����Ӱ��,�Ͳ�����,�Ͳ�����,";
			//tvRefText.setText("����ʣ�\n" + words);
			String[] all = hotWords.split(",");
			for (int i = 0; i < all.length; i++) {
				String word = all[i];
				try {
					asr.AddHotWordGBK(word.getBytes("GBK"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			asr.UpdateHotWord();

			asr.online = true;
		}
		
		mHandler = m;

		
	}
	
	
	
	private void sendMsg(int msgId,String hypothesis,int time,int len)
	{
		Message msg = new Message();
        Bundle data = new Bundle();

        data.putString("result",hypothesis);	        
        data.putInt("useTimeMs", time);		        
        data.putInt("dataLength", len);
        
        msg.setData(data);
        msg.what = msgId;
        mHandler.sendMessage(msg);
	}

	private short[] byteArray2ShortArray(byte[] data, int items) {
		short[] retVal =new short[items/2];
		for (int i =0; i < retVal.length; i++)
		retVal[i] = (short) ((data[i *2]&0xff) | (data[i *2+1]&0xff) <<8);
		return retVal;
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
			if(this.runFlag == true)
			{
				String hypothesis = null;
				
				//������ǰ�ļ�
				set_runFlag(false);
				
				count = 0;
				Log.e(CommonFun.TAG, "runFlag == true file = " + mRawFile);
				
				File f = new File(mRawFile);
		        if (!f.exists()) {
		        	sendMsg(1,"no match",0,0);
		        	continue;
		        }
				
				
				short[] filecontent = null;
				byte[] b = null;
				//filecontent = CommonFun.readFileData(mContext);
				
				RandomAccessFile file = null;
				try {
					file = new RandomAccessFile(mRawFile,"rw");
					
					int len = (int)file.length();
					
					//ȡ���ļ����ȣ��ֽ����� 
					b=new byte[len]; 
					
					file.readFully(b);
					
					file.close();
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				//ע�� ��ʽ		  
				filecontent = byteArray2ShortArray(b,b.length);
				testFileLen = filecontent.length;
				
				
				startTime = System.currentTimeMillis();

				
				//Log.e("decoder.startUtt", "begin................."+filecontent.length);
				asr.StartASR();
				//Log.e("decoder.startUtt", "end..............................");
				int i = 0;
				int size = BUFF_SIZE;
				while(i != b.length)
				{
					
					if( i + BUFF_SIZE > b.length)
					{
						size = b.length - i;
					}
					else
						size = BUFF_SIZE;
					
					byte[] temp = new byte[size];
					System.arraycopy(b, i, temp, 0, size);
					
					
					asr.AddBuffer(temp);
					
//					try {
//						Thread.sleep(30);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					
					i += size;
				}
			
				asr.StopASR();
								
				
			}
			else
			{
				if(count < 100)
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
