package com.VCMD.JNI;

public class VCMDEngine {
	
	static{
		System.loadLibrary("VCmdRec");
	}
	
   public native static int Initial(String hmm, String keyword, String g2p);
   public native static int RecVoice(String wav);
   public native static byte [] RecVoice2(String wav);
}
