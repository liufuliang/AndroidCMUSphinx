package com.ztspeech.asrtest;

public interface ASRInterface {
	public void OnResult(String asrtext);
	public void OnFinalResult(String asrtext);
	public void OnError(String errormsg);
	public void OnAutoStop(String msg);
}
