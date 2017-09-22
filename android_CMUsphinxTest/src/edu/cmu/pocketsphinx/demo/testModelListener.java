package edu.cmu.pocketsphinx.demo;

import android.os.Bundle;

public interface testModelListener {
	
	//处理一个文件的结果
	void processCurrentData(String fileName,String result,int time);
	//获得本次测试的总得信息
	void processTotalInfo(Bundle b);

}
