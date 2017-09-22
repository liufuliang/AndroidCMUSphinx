package edu.cmu.pocketsphinx;

public class testSphinxModel {

	public static void test(String xml,String log,String hmm,String lm,String dic) {
	    pocketsphinxJNI.testSphinxModel(xml,log,hmm,lm,dic);
	  }
}
