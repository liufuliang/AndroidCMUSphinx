package edu.cmu.pocketsphinx.demo;

import android.os.Bundle;

public interface testModelListener {
	
	//����һ���ļ��Ľ��
	void processCurrentData(String fileName,String result,int time);
	//��ñ��β��Ե��ܵ���Ϣ
	void processTotalInfo(Bundle b);

}
