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

	//运行标记
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

//			String words = "杂技世界,村居廉政信息,村村响,杜蒙科教,杜蒙综合,杨汛桥,极速汽车,极速汽车 (订购),极速汽车(付费),极速汽车（付费）,柯岩街道,柯桥区图文信息,柯桥区影视生活,柯桥区新闻点播,柯桥区新闻综合,柯桥区调频广播,柯桥街道,标清数据,标清测试1,标清测试10,标清测试11,标清测试2,标清测试3,标清测试4,标清测试5,标清测试6,标清测试7,标清测试8,标清测试9,标清英超,株化电视站,株洲交通广播,株洲新闻广播,株洲综合,桐城影视频道,桐城新闻频道,桐城生活频道,档案(VOD),梁子湖频道,梨 园(付费),梨园,梨园($),梨园(付费),梨园(暂转),梨园D,梨园曲艺,梨园频道,梨园频道(付费),梨园频道(付费频道),梨园频道(试播),梨园频道D,检测1,检测2,检测3,森森购物,楚天资讯,楚天音乐,榆林一套,榆林三套,榆林二套,榆阳一套,榆阳二套,欢乐时光广播,欢乐童年,欢乐频道,欢笑剧场,欢笑剧场 (订购),欢笑剧场 HD,欢笑剧场 高清,欢笑剧场(付费),欢笑剧场(文广),欢笑剧场(暂转),欢笑剧场-文广综艺,欢笑剧场HD,欢笑剧场★,欢笑剧场高清,欢笑剧场?,欢笑剧场（付费）,欧亚体育,欧洲足球,欧洲足球 (订购),欧洲足球($),欧洲足球(付费),欧洲足球(免费试播),欧洲足球（试播）,欧美剧场,欧美劲爆,欧美大片NVOD,欧美影院,欧美影院(NVOD),欧美影院-高清,欧美影院HD,欧美影院标清,欧美影院高清,欧美电影,欧美电影($),欧美电影(付费),欧美电影(标清版),欧美电影*,欧美电影标清,欧美电影高清,欧美精选,欧美金曲,歌华导视,歌手专放,正安新闻,武侯政务,武威公共频道,武威导视,武威广播,武威广播1,武威新闻综合,武术世界,武术世界($),武术世界(付费),武术世界(免费试播),武汉一套,武汉七套,武汉三套,武汉二套,武汉五套,武汉人民广播电台,武汉六套,武汉味道,武汉四套,武汉教育,武汉消费指南,武汉电台交通广播,武汉电台少儿广播,武汉电台音乐广播,武进,毕节新闻联播,民乐,民乐之声,民乐广播,民乐广播1,民乐广播2,民乐广播3,民乐广播4,民乐广播5,民族之声,民族音乐,气象,气象影视,气象频道,永州公共,永州影视,永州综合,永州都市,求索动物高清,求索生活高清,求索科学高清,求索纪录高清,汇川新闻,汉中一套,汉中三套,汉中二套,汉中四套,汉中广播,汉语卫视,汕头交通音乐之声广播,汕头新闻资讯之声广播,汕头生活之声广播,江北台,江北有线电视,江夏影视娱乐频道,江夏有线1,江夏有线2,江海明珠,江苏,江苏 HD,江苏 高清,江苏-HD,江苏HD,江苏交通广播网,江苏优漫动画,江苏优漫卡通,江苏优漫卡通（选传）,江苏体育,江苏体育休闲,江苏体育休闲#,江苏健康,江苏健康广播,江苏公共,江苏卫视,江苏卫视#,江苏卫视(基本),江苏卫视(高清),江苏卫视-R,江苏卫视-高清,江苏卫视HD,江苏卫视高清,江苏卫视高清#,江苏卫视高清频道,江苏卫视?高清?,江苏卫视（高清）,江苏国际,江苏城市,江苏少儿,江苏影视,江苏招考,江苏故事,江苏故事广播,江苏教育,江苏教育#,江苏文艺,江苏文艺广播,江苏文艺电台,江苏新闻,江苏新闻广播,江苏新闻综合,江苏新闻综合广播,江苏电台广播,江苏电视台,江苏经典流行音乐,江苏经典流行音乐广播,江苏综艺,江苏综艺#,江苏调频,江苏财经,江苏财经广播,江苏远教一套,江苏靓妆,江苏音乐,江苏音乐广播,江苏音频广播,江苏高清,江苏高清(HD),江苏高清(体验),江苏高清(可回看),江苏高清卫视,江西,江西交通广播,江西农村广播,江西卫视,江西卫视#,江西卫视(基本),江西卫视（选传）,江西指南,江西教育,江西新闻广播,江西旅游广播,江西有线,江西民生广播,江西电视台,江西都市广播,江西音乐广播,江门公共,江门台,江门教育,江门综合,池州-1,池州-2,池州-3,池州人民广播电台,池州数字导视,池州点播,池州资讯,汽 摩,汽摩,汽摩($),汽摩(付费),汽摩频道,汽摩频道(付费),汽车-极速汽车,汽车-汽摩,汽车会展(选传),汽车频道,沁阳影视,沁阳经济生活,沁阳综合,沂南TV-1,沂南TV-2,沂南TV-3,沂南TV-4,沂南导视,沂水TV-1,沂水TV-2,沂水TV-测试,沂水图文,沈铁新闻频道,沈阳交通,沈阳图文,沈阳新闻,沈阳新闻高清,沙发院线一厅,沙发院线三厅,沙发院线二厅,沙发院线五厅,沙发院线四厅,沙发院线导视,沧州-1,沧州-2,沧州-3,沧州交通音乐广播,沧州新闻综合广播,沧州长书文艺广播,河东区,河东图文,河东频道,河北,河北 交通,河北交通,河北交通台,河北交通广播,河北交通广播电台,河北人民台,河北人民广播电台,河北公共,河北农村广播,河北农村广播电台,河北农民,河北区,河北卫视,河北卫视#,河北卫视(基本),河北卫视(订购),河北卫视高清,河北城市电台,河北导视,河北少儿,河北少儿科教,河北影视,河北收视指南,河北文艺台,河北文艺广播,河北文艺广播电台,河北新闻广播,河北旅游广播,河北杂技,河北生活台,河北生活广播,河北生活电台,河北电台,河北电台交通,河北电台农村,河北电台广播,河北电台文艺,河北电台新闻,河北电台旅游,河北电台生活,河北电台经济,河北电台音乐,河北科教,河北经济,河北经济台,河北经济广播,河北经济广播电台,河北购物,河北都市,河北音乐,河北音乐广播,河北高清,河南,河南交通广播,河南公共,河南卫视,河南卫视#,河南卫视(基本),河南国际,河南戏曲广播,河南新农村,河南新闻,河南新闻广播,河南油田-1,河南油田-2,河南法制,河南电视剧,河南电视台,河南电视台-2,河南电视台-3,河南电视台-4,河南电视台-5,河南电视台-6,河南电视台-8,河南经济广播,河南都市,河广交通广播,河西区,油田影视,油田有线,油田综合,油轮旅游,沾化电影,沾化综合,沾化综艺,泉州1套,泉州2套,泉州3套,泉州4套,泉州新闻,法制·科教,法制天地,法制天地(付费),法制天地-文广记录,法制频道,法国TV5,法国时尚,法国时装,法国电视5台,法律服务,法律讲堂（VOD）,法治天地,法治天地 (订购),法治天地(付费),法治天地★,法治天地?,法治天地（付费）,泰兴图文,泰兴新闻,泰兴有线,泰兴综合,泰安公共,泰安图文,泰安教科,泰安新闻,泰山先锋,泰山电视,泰州三套,泰州影视娱乐,泰州法制频道,泸州公共,泸州数字电视,泸州科教,泸州综合,泸州资讯,泸州音频广播1,泸州音频广播2,泸州音频广播3,洛阳电视台-1,洛阳电视台-2,洛阳电视台-3,洛阳电视台-4,活力广东,流行金曲,流行音乐,流连1015,测01,测02,测03,测04,测05,测06,测08,测09,测试,测试-1,测试-2,测试-3,测试-CCTV-1高清,测试-CCTV-5 高清,测试-东方高清,测试-北京高清,测试-广东高清,测试-江苏高清,测试-浙江高清,测试-深圳高清,测试-湖南高清,测试-黑龙江高清,测试0,测试01,测试02,测试1,测试10,测试11,测试12,测试13,测试14,测试2,测试3,测试4,测试5,测试6,测试7,测试8,测试9,测试CINEMAX电影,测试CNN,测试ESPN,测试★,测试一,测试一高清,测试七,测试三,测试九,测试二,测试二高清,测试五,测试体育,测试信号,测试信号-1,测试信号-1高清,测试信号-2,测试信号-2高清,测试信号-3,测试信号-4,测试信号-5,测试信号-6,测试信号1,测试信号2,测试信号3,测试信号4,测试信号5,测试信号一,测试信号七,测试信号三,测试信号二,测试信号五,测试信号六,测试信号四,测试八,测试六,测试凤凰中文,测试凤凰电影,测试凤凰资讯,测试十,测试十一,测试十三,测试十二,测试十五,测试十四,测试华娱卫视,测试四,测试娱乐1,测试探索频道,测试新闻1,测试新闻2,测试星空卫视,测试星空电影,测试法国时尚,测试电影1,测试电影2,测试节目,测试节目(空),测试节目1,测试节目1(高清),测试节目10,测试节目11,测试节目12,测试节目13,测试节目14,测试节目15,测试节目16,测试节目17,测试节目18,测试节目19,测试节目2,测试节目2(高清),测试节目20,测试节目21,测试节目2307,测试节目3,测试节目4,测试节目5,测试节目6,测试节目7,测试节目8,测试节目9,测试节目HS,测试节目一,测试节目一高清,测试节目七,测试节目三,测试节目九,测试节目二,测试节目二高清,测试节目五,测试节目八,测试节目六,测试节目十,测试节目四,测试记录1,测试记录2,测试阳光卫视,测试音频,测试预留,测试预留(付费),测试预留(标清版),测试预留*,测试预留☆,测试预留标清,测试预留高清,测试频道,测试频道-1,测试频道-10,测试频道-2,测试频道-3,测试频道-4,测试频道-6,测试频道-7,测试频道-8,测试频道-9,测试频道01,测试频道02,测试频道03,测试频道04,测试频道05,测试频道06,测试频道07,测试频道08,测试频道09,测试频道1,测试频道10,测试频道11,测试频道12,测试频道13,测试频道14,测试频道15,测试频道16,测试频道17,测试频道18,测试频道19,测试频道1高清,测试频道1（四川）,测试频道2,测试频道20,测试频道21,测试频道2高清,测试频道2（广西）,测试频道3,测试频道3（安徽）,测试频道4,测试频道5,测试频道6,测试频道7,测试频道8,测试频道9,测试频道一,测试频道三,测试频道二,测试频道五,测试频道六,测试频道四,测试高清1,测试高清2,济南商务,济南娱乐,济南少儿,济南影视,济南影视高清,济南教育,济南新闻,济南新闻高清,济南生活,济南都市,济源-济钢,济源-综合,济铁,济阳影视,济阳综合,浏阳人民广播电台,浏阳商贸,浏阳图文,浏阳房产,浏阳新闻,浏阳经济,浙一,浙三,浙二,浙江,浙江 HD,浙江 高清,浙江-HD,浙江-交通之声,浙江-公共新农村,浙江-公共频道,浙江-卫视频道,浙江-城市之声,浙江-少儿频道,浙江-影视娱乐,浙江-影视文化,浙江-教育科技,浙江-文艺频道,浙江-新闻综合,浙江-旅游之声,浙江-民生休闲,浙江-浙江经视,浙江-经济频道,浙江-经视,浙江-钱江都市,浙江-音乐调频,浙江-高清,浙江1台,浙江2,浙江2台,浙江3,浙江3台,浙江4,浙江4台,浙江5,浙江5台,浙江6,浙江6台,浙江6频道,浙江7,浙江7台,浙江8,浙江8台,浙江HD,浙江之声,浙江交通之声,浙江体彩,浙江党建,浙江公共,浙江公共新农村,浙江公共资讯,浙江公共频道,浙江卫视,浙江卫视#,浙江卫视(基本),浙江卫视(选传),浙江卫视(高清),浙江卫视-R,浙江卫视-高清,浙江卫视HD,浙江卫视高清,浙江卫视高清#,浙江卫视高清频道,浙江卫视?高清?,浙江卫视（展播）,浙江卫视（高清）,浙江城市之声,浙江少儿,浙江少儿频道,浙江广播-6,浙江广播-7,浙江广播-8,浙江广播一,浙江广播三,浙江广播二,浙江广播电台,浙江广播音乐频道,浙江影视,浙江影视娱乐,浙江影视文化,浙江教育,浙江教育科技,浙江文艺广播,浙江文艺频道,浙江新闻台,浙江新闻综合,浙江新闻频道,浙江旅游之声,浙江民生休闲,浙江民生广播,浙江电台广播,浙江电台旅游之声,浙江电视台,浙江经济生活,浙江经济频道,浙江经视,浙江网视,浙江财富广播,浙江钱江都市,浙江音乐,浙江音乐调频,浙江音频广播,浙江高清,浙江高清(HD),浙江高清(体验),浙江高清(可回看),浙江高清卫视,海上戏剧,海南公共,海南卫视,海南广播,海南影视,海南文体信息,海南新闻,海南新闻广播,海南旅游,海南旅游卫视,海南综合,海南青少,海口1套,海口2套,海口3套,海口一套,海口三套,海口二套,海外剧场,海外剧场(付费),海外剧场（试播）,海外影院,海外片场,海峡卫视,海峡新闻,海淀,海港,海门一套,海门城市购物,海门教育人文,海门数字电视简介,海门新闻综合,海门时尚娱乐,海门电影频道,海门经济生活,海阳影视,淄博新闻,淄博生活,";
			//tvRefText.setText("命令词：\n" + words);
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
				
				//结束当前文件
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
					
					//取得文件长度（字节数） 
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
				
				
				//注意 格式		  
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
