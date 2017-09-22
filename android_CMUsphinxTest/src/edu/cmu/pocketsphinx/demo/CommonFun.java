package edu.cmu.pocketsphinx.demo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.util.EncodingUtils;

public class CommonFun {
	public static String logPath = "8k_raw_";
	public static String TAG = "edu.cmu.pocketsphinx.demo";
	public static String filename = "test.raw";
	public static long lastClickTime;
	public static long beforeTimewait;
	public static long micTimewait;
    public static boolean isPauseTime() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD > 900) {   
            return true;   
        }   
        return false;   
    }
    public static boolean isbeforeTimeouts(){
        long time = System.currentTimeMillis();
        long timeD = time - beforeTimewait;
        if ( 0 < timeD && timeD > 3000) {   
            return true;   
        }   
        return false; 
    }
    
    public static boolean micTimeout(){
        long time = System.currentTimeMillis();
        long timeD = time - micTimewait;
        if ( 0 < timeD && timeD > 3000) {   
            return true;   
        }   
        return false; 
    }

	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTimeString(){
  		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");//设置日期格式
  		String returnStr = df.format(new Date());
  		return returnStr;
  	}
	
    public static byte[] shortlistToBytelist(short[] s, int lengths) {
    	
        byte[] targets = new byte[lengths*2];  
    	for(int i =0 ; i <lengths;i++ )
    	{
    		 targets[2*i+1] = (byte) ((s[i] >> 8) & 0xFF);
    		 targets[2*i] =  (byte) (s[i] & 0xFF);
    	}
        return targets;  
    }
	public static short[] byteArray2ShortArray(byte[] data, int items) {
		short[] retVal =new short[items/2];
		for (int i =0; i < retVal.length; i++)
		retVal[i] = (short) ((data[i *2]&0xff) | (data[i *2+1]&0xff) <<8);
		return retVal;
	} 
	
	
	
	 @SuppressWarnings("unused")
	public static void writeShorts(short[] shorts, String filename) throws IOException {
	        File file = createFile(filename);
	        FileOutputStream fos = new FileOutputStream(file);
	        for (short aShort : shorts) {
	            fos.write(toBytes(aShort));
	        }
	        fos.close();
	    
	 }
	 
	public static void writeBytes(byte[] bytes, String filename) throws IOException {
	        File file = createFile(filename);
	        FileOutputStream fos = new FileOutputStream(file);
	        fos.write(bytes);
	        fos.close();
	    
	}
	 public static File createFile(String filename) throws IOException {
	        File file = new File(filename);
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	        return file;
	    
	 }
	 public static byte[] toBytes(short aShort) {
	        return new byte[]{
	                (byte) ((aShort >> 8) & 0xFF), 
	                (byte) (aShort & 0xFF)
	        };
	    
	 }

	@SuppressWarnings("null")
	public static String getSDPath(String strfname) {  
	     File sdDir = null;  
	     boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在   
	     if (sdCardExist) {  
	         sdDir = Environment.getExternalStorageDirectory();// 获取跟目录   
	         return sdDir.toString()+"/"+strfname;  
	     }  
	     return sdDir.toString();
	}  
	
	   public static short[] readFileData(Context context){
		   byte [] buffer = null;
		   try{  
			   InputStream in = context.getAssets().open(filename);
//			   InputStream in = context.getResources().openRawResource(R.raw.test);   
			   int length = in.available();       
			   buffer = new byte[length];        
			   in.read(buffer);         
			   in.close();            
		   }catch(Exception e){
			   e.printStackTrace();         
		    
		   } 
		   return byteArray2ShortArray(buffer,buffer.length);
	    } 
	   
	   public static byte[] readFileDataByte(Context context){
		   byte [] buffer = null;
		   try{  
			   InputStream in = context.getAssets().open(filename);
//			   InputStream in = context.getResources().openRawResource(R.raw.test);   
			   int length = in.available();       
			   buffer = new byte[length];        
			   in.read(buffer);         
			   in.close();            
		   }catch(Exception e){
			   e.printStackTrace();         
		    
		   } 
		   return buffer;
	    } 

	   public static String loadAFileToStringDE3(String fileName) throws IOException {
		   
		   File f = new File(fileName);
	        //long beginTime = System.currentTimeMillis();
	        BufferedReader br = null;
	        String ret = null;
	        try {
	            br =  new BufferedReader(new FileReader(f));
	            String line = null;
	            StringBuffer sb = new StringBuffer((int)f.length());
	            while( (line = br.readLine() ) != null ) {
	                sb.append("(").append(line).append(")\n");
	            }
	            ret = sb.toString();
	        } finally {
	            if(br!=null) {try{br.close();} catch(Exception e){} }
	        }
	        //long endTime = System.currentTimeMillis();
	        //System.out.println("方法3用时"+ (endTime-beginTime) + "ms");
	        return ret;        
	    }
	   
	   
	   
	   public static  void showMessage(final Activity c,String message) {
			final String str = message;
			c.runOnUiThread(new Runnable()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub  LENGTH_LONG
					Toast.makeText(c, str, Toast.LENGTH_SHORT).show();
					
				}}
			);
					
		}

}
