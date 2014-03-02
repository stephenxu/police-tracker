package com.stephen.police;


import java.util.HashMap;
import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

public class MainActivity extends Activity {
	
	String TAG = "police log";
	
	private Button btnVolumeUp;
	private Button btnVolumeDown;
	private Button btnScan;
	private Button btnBrightness;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap = new HashMap<Integer, Integer>();
	private ProgressBar rectangleProgressBar;	
	public handler_thread handlerThread;
	public FT311UARTInterface uartInterface;
	EditText readText;
	EditText readText1;
	
	byte[] readBuffer;
	char[] readBufferToChar;
	int[] actualNumBytes;
	StringBuffer readSB = new StringBuffer();
	StringBuffer tempnew = new StringBuffer();
	int tobeFilled = 0;
	int correctLength = 19;
	boolean firstTime = true;
	Long startTime;
	Long startTime1;

	
	Float lastShownDegree = (float) 0;//used to record the last degree value
	Float maxDegree = (float) 20;//the max degree we're allowed to rotate at one time.
	String LF = "\n";
	String CR = "\r";
	//String sampling = "S JHNY1 07 22 108\r\n";
	
	boolean isSwitched = false;
	String lastShownReplycode1 = null;
	String lastShownReplycode2 = null;
	String previousReplycode = "";
	

	int numBytes;
	byte count;
	byte status;
	byte readIndex = 0;

	int baudRate; /* baud rate */
	byte stopBit; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit; /* 8:8bit, 7: 7bit */
	byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
	public Context global_context;
	public boolean bConfiged = false;
	public SharedPreferences sharePrefSettings;
	public String act_string; 
	public class Datalist
	{
		private String replycode;
		private int signal;
		private float degree;
		
		public void setreplycode(String replycode){
			this.replycode = replycode;
		}
		public String getreplycode(){
			return this.replycode;
		}
		
		public void setsignal(int signal){
			this.signal = signal;
		}
		public int getsignal(){
			return signal;
		}
		
		public void setdegree(float degree){
			this.degree = degree;
		}
		public float getdegree(){
			return degree;
		}
	}
	
	Datalist datalist1 = new Datalist();
	Datalist datalist2 = new Datalist();
	Datalist datalisttemp = new Datalist();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		closeBar();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		setContentView(R.layout.main);

		sharePrefSettings = getSharedPreferences("UARTLBPref", 0);
		//cleanPreference();
		readText = (EditText) findViewById(R.id.ReadValues);
		readText1 = (EditText) findViewById(R.id.ReadValues1);
	
		global_context = this;
		
		readBuffer = new byte[4096];
		readBufferToChar = new char[4096]; 
		actualNumBytes = new int[1];
		
		baudRate = 2400;
		stopBit = 1;
		dataBit = 8;
		parity = 0;
		flowControl = 0;
		readText1.setText("");
		
	
		Log.d(TAG, "1");
		
		act_string = getIntent().getAction();
		if( -1 != act_string.indexOf("android.intent.action.MAIN")){
			restorePreference();
		}			
		else if( -1 != act_string.indexOf("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")){
			cleanPreference();
		}	

		uartInterface = new FT311UARTInterface(this, sharePrefSettings);

		
		
		setBrightness(255);
		maxVolume();

		soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 100);  
		
		soundPoolMap.put(1, soundPool.load(this, R.raw.aa, 1));
		soundPoolMap.put(2, soundPool.load(this, R.raw.a0, 1));
		soundPoolMap.put(3, soundPool.load(this, R.raw.a1, 1));
		soundPoolMap.put(4, soundPool.load(this, R.raw.a2, 1));
		soundPoolMap.put(5, soundPool.load(this, R.raw.a3, 1));
		soundPoolMap.put(6, soundPool.load(this, R.raw.a4, 1));
		soundPoolMap.put(7, soundPool.load(this, R.raw.a5, 1));
		soundPoolMap.put(8, soundPool.load(this, R.raw.a6, 1));
		soundPoolMap.put(9, soundPool.load(this, R.raw.a7, 1));
		soundPoolMap.put(10, soundPool.load(this, R.raw.a8, 1));
		soundPoolMap.put(11, soundPool.load(this, R.raw.a9, 1));
		soundPoolMap.put(12, soundPool.load(this, R.raw.a10, 1));
		soundPoolMap.put(13, soundPool.load(this, R.raw.a11, 1));
		soundPoolMap.put(14, soundPool.load(this, R.raw.a12, 1));
		soundPoolMap.put(15, soundPool.load(this, R.raw.a13, 1));
		soundPoolMap.put(16, soundPool.load(this, R.raw.a14, 1));
		soundPoolMap.put(17, soundPool.load(this, R.raw.a15, 1));
		soundPoolMap.put(18, soundPool.load(this, R.raw.a16, 1));
		soundPoolMap.put(19, soundPool.load(this, R.raw.a17, 1));
		soundPoolMap.put(20, soundPool.load(this, R.raw.a18, 1));
		soundPoolMap.put(21, soundPool.load(this, R.raw.a19, 1));
		soundPoolMap.put(22, soundPool.load(this, R.raw.a20, 1));
		soundPoolMap.put(23, soundPool.load(this, R.raw.a21, 1));
		soundPoolMap.put(24, soundPool.load(this, R.raw.a22, 1));
		soundPoolMap.put(25, soundPool.load(this, R.raw.a23, 1));
		soundPoolMap.put(26, soundPool.load(this, R.raw.a24, 1));
		soundPoolMap.put(27, soundPool.load(this, R.raw.a25, 1));
		soundPoolMap.put(28, soundPool.load(this, R.raw.z, 1));
	
		
		rectangleProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		rectangleProgressBar.setMax(25);
		
	

		
		btnVolumeUp = (Button) findViewById(R.id.button_VolumeUp);        
        btnVolumeUp.setOnClickListener(new Button.OnClickListener() {		
			public void onClick(View v) {
				raiseVolume();
				playSound(28);
			}
		});
        btnVolumeUp.setOnLongClickListener(new Button.OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				raiseVolume();
				return false;
			}
		});
      
        btnVolumeDown = (Button) findViewById(R.id.button_VolumeDown);        
        btnVolumeDown.setOnClickListener(new Button.OnClickListener() {		
			public void onClick(View v) {
				lowerVolume();
				playSound(28);		
			}
		});
        btnVolumeDown.setOnLongClickListener(new Button.OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				lowerVolume();
				return false;
			}
			
		});
        
        btnScan = (Button) findViewById(R.id.button_Scan);        
        btnScan.setOnClickListener(new Button.OnClickListener() {		
			public void onClick(View v) {
				switchCode(lastShownReplycode1,lastShownReplycode2);
				playSound(28);
			}
		});
        
        btnBrightness = (Button) findViewById(R.id.button_Brightness);        
        btnBrightness.setOnClickListener(new Button.OnClickListener() {		
			public void onClick(View v) {	
				
				if (getBrightness() == 1f) 
					setBrightness(102);
				
				else if(getBrightness() == 180f * (1f / 255f))
					setBrightness(255);
				
				else 
					setBrightness(180);
				
				playSound(28);
			}
		});	
        
		handlerThread = new handler_thread(handler);
		handlerThread.start();
	}
	
    private void playSound(int sound) {
    	AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);   
        float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);       
        float volume = streamVolumeCurrent/streamVolumeMax;   

        soundPool.play(soundPoolMap.get(sound), volume, volume, 1, 0, 1f);

    }
    
    private void playSound_MaxVolume(int sound) {
    	AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);      
        float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);       
        soundPool.play(soundPoolMap.get(sound), streamVolumeMax, streamVolumeMax, 10, 0, 1f);   
    }
    
    private void maxVolume() {
    	AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), -2);
    }
    
	private void raiseVolume() {
		AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, -2);
		getVolume();
	}
	
	private void lowerVolume() {
		AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if (getVolume()==1){
			
		}else{
		am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, -2);
		getVolume();
		}
	}
	
	private float getVolume() {
		AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
		float volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		Log.d("volume",String.valueOf(volume));
		return volume;
	}
	
	private void setBrightness(int brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        getWindow().setAttributes(lp);
    }
	
	private float getBrightness() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
       	float Brightness = lp.screenBrightness;
   
        return Brightness;
    }
	
	private void closeBar(){
        try{
                //need root privilege 
            String ProcID = "42"; 

            //need root privilege
            Process proc = Runtime.getRuntime().exec(new String[]{"su","-c","service call activity "+ ProcID +" s16 com.android.systemui"}); //WAS 79
            proc.waitFor();

        	}catch(Exception ex){
        		Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        	}
		}
	
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		
			for(int i=0; i<actualNumBytes[0]; i++)
			{
				readBufferToChar[i] = (char)readBuffer[i];
			}
			
			appendData(readBufferToChar, actualNumBytes[0]);
		}
	};

	private class handler_thread extends Thread {
		Handler mHandler;

		handler_thread(Handler h) {
			mHandler = h;
		}

		public void run() {
			Message msg;
			
			while (true) {				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			

				status = uartInterface.ReadData(4096, readBuffer,actualNumBytes);

				if (status == 0x00 && actualNumBytes[0] > 0) {
					msg = mHandler.obtainMessage();
					mHandler.sendMessage(msg);
				}
			}
		}
	}
	
	final Handler edittexthandler = new Handler();
    Runnable runnable = new Runnable(){
        @Override
        public void run() {   	
        	lastShownReplycode1 = new String();
        	datalist1 = new Datalist();
        	readText.setText("");
        	progressBar(0);
        	dropimage();	
            edittexthandler.postDelayed(this, 10000);
        } 
    }; 
    
  
    
    final Handler edittext1handler = new Handler();
    Runnable runnable1 = new Runnable(){
        @Override
        public void run() {
        	lastShownReplycode2 = new String();
        	datalist2 = new Datalist();
        	readText1.setText("");
            edittext1handler.postDelayed(this, 10000);
        } 
    }; 
    

	   
    public void appendData(char[] data, int len)
    {
    	
    	StringBuffer temp = new StringBuffer();
    	temp.append(String.copyValueOf(data, 0, len));
    	
    	if ((tobeFilled==0) && ((temp.length() == 1) && (String.valueOf(temp).equals("A"))) 
    			|| ((temp.length() == 1) && (String.valueOf(temp).equals("B")))){
    		processData(temp.toString().toCharArray(),isSwitched);
    	}
    	
    	else if ((temp.length() < correctLength) && (temp.length() != tobeFilled)){
    		
    		tempnew.append(temp);
    		if (firstTime){
    			tobeFilled = correctLength - temp.length();
    			}
    		else 
    			{
    			  tobeFilled-=temp.length();
    			  firstTime=false;
    			}
    		}
    	
    	else if (temp.length() == tobeFilled){
    		
    		tempnew.append(temp);   		
    		if (tempnew.length() == correctLength){
    			processData(tempnew.toString().toCharArray(),isSwitched);
    			tempnew.setLength(0);
    			firstTime = true;
    		}else{
    		tempnew.setLength(0);
    		firstTime = true;
    		}    		
    	}
    	
    	else if (temp.length() == correctLength){
    		processData(temp.toString().toCharArray(),isSwitched);
    		tempnew.setLength(0);
    		firstTime= true;
    	}
    }
			
    public void processData(char[] dat, boolean isSwitched){
    	
    	
    	String degree = null;//degree
    	String replycode = null;//reply code
    	String signal = null;//signal strength for the bar
    	Float actualDegree;//used to record the degree value in current data package
    	
    	
    	if ((dat.length == 1)){
    		if (String.valueOf(dat).equals("A")){
    			Log.d(TAG,"setBrightness-A");
    			setBrightness(255);    		//Maximum the screen brightness
    		}
    		else if (String.valueOf(dat).equals("B")){
    			Log.d(TAG,"setBrightness-B");
    			setBrightness(102);        //Minimum the screen brightness
    		}
    	}
    	
    	else if ((dat.length == correctLength) && 
    			(isNumeric(String.copyValueOf(dat, 11, 2))) && //make sure signal strength is num.
    			(isNumeric(String.copyValueOf(dat, 14, 3))) && //make sure degree is num.
    			((String.copyValueOf(dat, 17, 1).equals(String.valueOf(LF))) || (String.copyValueOf(dat, 17, 1).equals(String.valueOf(CR))))&&
    			((String.copyValueOf(dat, 18, 1).equals(String.valueOf(LF))) || (String.copyValueOf(dat, 18, 1).equals(String.valueOf(CR))))){
    		
    		if((dat[0] == 'I' || dat[0] == 'L' || dat[0] == 'S') && (dat[1]==' ') && (dat[7]==' ') && (dat[10]==' ') && (dat[13]==' ')){
    		
    			replycode = String.copyValueOf(dat, 2, 5);
    			signal = String.copyValueOf(dat, 11, 2);
    			degree = String.copyValueOf(dat, 14, 3);
    			actualDegree = (float) (Float.valueOf(degree) * 1.8);
    			Integer S = Integer.valueOf(signal);
	
    			datalisttemp.setreplycode(replycode);
    			datalisttemp.setdegree(actualDegree);
    			datalisttemp.setsignal(S);
    			
    			
				if (("".equals(readText.getText().toString()))&&(!replycode.equals(lastShownReplycode2))){
					playSound_MaxVolume(1);
					showData(datalisttemp,99);
				
    				datalist1.setreplycode(datalisttemp.getreplycode());
    				datalist1.setdegree(datalisttemp.getdegree());
    				datalist1.setsignal(datalisttemp.getsignal());
    				
    				Log.d(TAG,"1111111");
    				
				}
				
				else if(replycode.equals(lastShownReplycode1)){
					
					
						showData(datalisttemp,1);
						Log.d(TAG,"2222222");
					
				}else if((!"".equals(readText.getText().toString()))&&(!replycode.equals(lastShownReplycode1))){

					if (datalist2.replycode == null){	
						datalist2.setreplycode(datalisttemp.getreplycode());
	    				datalist2.setdegree(datalisttemp.getdegree());
	    				datalist2.setsignal(datalisttemp.getsignal());
	    				showData(replycode,true);
	    				Log.d(TAG,"3333333");
					}else if (replycode.equals(lastShownReplycode2)){
						showData(replycode,false);
						Log.d(TAG,"888888");
					}	
				}else if (replycode.equals(lastShownReplycode2)){			
					showData(replycode,false);
					Log.d(TAG,"444444444");
				}
    		}
    	}
    }

    public void showData(Datalist datalist, int num){
    	
    	float degree = datalist.degree;
    	String replycode = datalist.replycode;
    	int signal = datalist.signal;
    	previousReplycode = replycode;
    	
    	if (!replycode.equals(lastShownReplycode1)){
    		rotate(degree);
    	}else{
    		rotate(calculatedDegree(degree));
    	}
    	showReplyCode(replycode,1,true,true);
    	progressBar(signal);
    	
    	if (num == 1){
    	playSound(signal + 1);
    	}
    }
    
    public void showData(String replycode, boolean alart){

		showReplyCode(replycode,2,true,true);
    	if (alart){
		playSound_MaxVolume(1);
    	}
    }
 
    public void progressBar(int signal){
    	rectangleProgressBar.setIndeterminate(false);
	    rectangleProgressBar.setProgress(signal);
    }
    
    public float calculatedDegree(float degree){
		
    	Float degreetemp = null;
    	if ((degree == 360) || (degree == 000)){
    		degreetemp = degree; 
	    }
    	else if (degree >= lastShownDegree){
    	
    		if ((degree - lastShownDegree) >= maxDegree){
		    	degreetemp = lastShownDegree + maxDegree;
		    	
			}else {
		    	degreetemp = degree;
		    }	    
    	}	
    	else if (degree < lastShownDegree){		    		
    		if (lastShownDegree - degree >= maxDegree){
	    		degreetemp = lastShownDegree - maxDegree;
	    		
    		}else{
    			degreetemp = degree;
    		}
    	}
		return degreetemp;
    }
    
    public void rotate(float r){
    	ImageView arror_imageView = (ImageView)findViewById(R.id.arror_imageView); 
    	arror_imageView.setVisibility(View.VISIBLE);
	    arror_imageView.setRotation(r);
	    if (r == 360){
	    	r = 000;
	    }
	    lastShownDegree = r;
	    
    }
    
    public void dropimage(){
    	ImageView arror_imageView = (ImageView)findViewById(R.id.arror_imageView);
    	arror_imageView.setVisibility(View.GONE);
    }
    
    public void showReplyCode(String replycode, int num, boolean record, boolean timer){
    	if (num == 1){
        	readText.setText(underline(replycode));
        	
        	if (timer){
        		edittexthandler.removeCallbacks(runnable);
        		edittexthandler.postDelayed(runnable,10000);
        		startTime = System.nanoTime();
        	}
        	
        	if (record){
        		lastShownReplycode1 = replycode;
        	}
        	
        }else if (num == 2) {
        	
        	readText1.setText(underline(replycode));
        	
        	if (timer){
        		
        		edittext1handler.removeCallbacks(runnable1);
        		edittext1handler.postDelayed(runnable1,10000);
        		startTime1 = System.nanoTime();
        	}
        	if (record){
        		lastShownReplycode2 = replycode;
        	}
        	
        }
    }
    
    public SpannableStringBuilder underline(String replycode){  
        SpannableStringBuilder underline = new SpannableStringBuilder();  
       	
        for (int i = 0; i < replycode.length(); i++){    	
        	if (Character.isDigit(replycode.charAt(i))){  
        		underline.append(Html.fromHtml("<u>" + replycode.charAt(i)+"</u>"));   		
        	   } 
        	else if (!Character.isDigit(replycode.charAt(i))){
        		underline.append(replycode.charAt(i));       	    
        	}
        }
		return underline;
    }  
    
    public void switchCode(String replycode1, String replycode2){
    	
    	if (!"".equals(readText1.getText().toString())){
    		
    		if (!"".equals(readText.getText().toString())){
    			
	    		showReplyCode(replycode2,1,true,false);
	    		Long timetogo = (System.nanoTime() - startTime1) / 1000000;
	    		edittexthandler.removeCallbacks(runnable);
        		edittexthandler.postDelayed(runnable,10000 - timetogo);

        		showReplyCode(replycode1,2,true,false);
        		Long timetogo2 = (System.nanoTime() - startTime) / 1000000;
        		edittext1handler.removeCallbacks(runnable1);
        		edittext1handler.postDelayed(runnable1,10000 - timetogo2);

	    		
	    		Datalist dl = new Datalist();
	    		
	    		dl.setreplycode(datalist1.getreplycode());
				dl.setdegree(datalist1.getdegree());
				dl.setsignal(datalist1.getsignal());
	    		
	    		datalist1.setreplycode(datalist2.getreplycode());
				datalist1.setdegree(datalist2.getdegree());
				datalist1.setsignal(datalist2.getsignal());
				
	    		datalist2.setreplycode(dl.getreplycode());
				datalist2.setdegree(dl.getdegree());
				datalist2.setsignal(dl.getsignal());
    		
    		}else if ("".equals(readText.getText().toString())){
    			
    			showReplyCode(replycode2,1,true,false);
    			
    			Long timetogo = System.currentTimeMillis() - startTime1;
    			
    			
	    		edittexthandler.removeCallbacks(runnable);
        		edittexthandler.postDelayed(runnable,10000 - timetogo);
    			
    			
    			showReplyCode("",2,true,false);
    			
    			Datalist dl = new Datalist();
	    		
	    		dl.setreplycode(datalist1.getreplycode());
				dl.setdegree(datalist1.getdegree());
				dl.setsignal(datalist1.getsignal());
	    		
	    		datalist1.setreplycode(datalist2.getreplycode());
				datalist1.setdegree(datalist2.getdegree());
				datalist1.setsignal(datalist2.getsignal());
				
	    		datalist2 = new Datalist();
	    		//lastShownReplycode2 = new String();
		
    		}		
    	}
    }

    public static boolean isNumeric(String str){  
    	  
    	for (int i = 0; i < str.length(); i++){  
    	   //System.out.println(str.charAt(i));  
    	   
    		if (!Character.isDigit(str.charAt(i))){  
    	    
    			return false;  
    		}  
    	 }  
    	  return true;  
    }  

	@Override
	protected void onResume() {
		super.onResume();		
		if( 2 == uartInterface.ResumeAccessory() )
		{
			cleanPreference();
			restorePreference();
		}
		if(false == bConfiged){
			bConfiged = true;
			//Log.d(TAG, String.valueOf(baudRate));
			uartInterface.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
			Log.d(TAG, "32");
			savePreference();
			Log.d(TAG, "33");
		}
	}
		
	protected void cleanPreference(){
		SharedPreferences.Editor editor = sharePrefSettings.edit();
		editor.remove("configed");
		editor.remove("baudRate");
		editor.remove("stopBit");
		editor.remove("dataBit");
		editor.remove("parity");
		editor.remove("flowControl");
		editor.commit();
	}

	protected void savePreference() {
		if(true == bConfiged){
			sharePrefSettings.edit().putString("configed", "TRUE").commit();
			sharePrefSettings.edit().putInt("baudRate", baudRate).commit();
			sharePrefSettings.edit().putInt("stopBit", stopBit).commit();
			sharePrefSettings.edit().putInt("dataBit", dataBit).commit();
			sharePrefSettings.edit().putInt("parity", parity).commit();			
			sharePrefSettings.edit().putInt("flowControl", flowControl).commit();			
		}
		else{
			sharePrefSettings.edit().putString("configed", "FALSE").commit();
		}
	}
	
	protected void restorePreference() {
		String key_name = sharePrefSettings.getString("configed", "");
		if(true == key_name.contains("TRUE")){
			bConfiged = true;
		}
		else{
			bConfiged = false;
        }
		
		baudRate = sharePrefSettings.getInt("baudRate", 2400);
		stopBit = (byte)sharePrefSettings.getInt("stopBit", 1);
		dataBit = (byte)sharePrefSettings.getInt("dataBit", 8);
		parity = (byte)sharePrefSettings.getInt("parity", 0);
		flowControl = (byte)sharePrefSettings.getInt("flowControl", 0);
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override  
    protected void onDestroy() {  
        soundPool.release();  
        soundPool = null;  
        uartInterface.DestroyAccessory(bConfiged);
        super.onDestroy();  
    }
}







