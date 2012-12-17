package studio.coldstream.emfieldscanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener{
    /** Called when the activity is first created. */
	private static final String TAG = "FSP";
	private SensorManager sensorManager;
	private double myAzimuth = 0;
	private double myPitch = 0;
	private double myRoll = 0;
	private double abssum = 0;
	private double sensisum = 0;
	
	protected static final int SENSITIVITY_MAX = 9;
	protected static final int SENSITIVITY_DEFAULT = 9;
	protected static final int AUTOLOGGING_DEFAULT = 0;
	
	protected static final int TICK = 0x9001;
	
	int[] al_value = {0,5,10,30,60,300,600};
	
	private boolean wakelock = false;
	private int sensitivity = SENSITIVITY_DEFAULT;
	private int autologging = AUTOLOGGING_DEFAULT;
	private int isensitivity;
	Thread thread1;
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	DisplayMetrics metrics;
	
	private LinearLayout my_ll;
	
	List<TextView> my_meter;
	
	TextView statusView1;
	TextView statusView2;
	TextView statusView3;
	TextView clockView;
	TextView logView;
	Button lb;
	
	String currentvalue;
	List<String> my_logdata;
	private double sensidata[];
	int sensicount;
	
	StringBuilder sb;
	
	int autocounter;
	int rcalcounter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        sensorManager.registerListener(this,
	            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD ),
	            SensorManager.SENSOR_DELAY_UI);
        
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        
        sensidata = new double[(SENSITIVITY_MAX * 10) + 1];
        for(int i = 0; i < (SENSITIVITY_MAX * 10); i++)
        	sensidata[i] = 0;
        //my_meter = new TextView[4];
        my_meter = new LinkedList<TextView>();
        my_logdata = new LinkedList<String>();
        my_ll = (LinearLayout)findViewById(R.id.mainll1);
        //LayoutParams my_params = new ViewGroup.LayoutParams(this, null);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int frick = (int) Math.ceil(metrics.widthPixels / 240);
        //int frick = (int) Math.ceil(240 / 240); //test low resolution
        
        for(int i = 0; i <= 40; i++){
        	my_meter.add(new TextView(this));
        	my_meter.get(i).setTextSize(1.0f);
        	my_meter.get(i).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1f));
        	if(i%2 == 0)
        		my_meter.get(i).setBackgroundColor(Color.rgb(136 - i , 136 - i , 238 - i));
        	        		
        	//my_meter.get(i).setWidth(40 + ((40-i) * (40 - i)) / 16);
        	my_meter.get(i).setWidth((40 + ((40-i) * (40 - i)) / 16) * frick);
        	//my_meter.get(i).setWidth(frick + ((frick-i) * (frick - i)) / 16);
        	my_ll.addView(my_meter.get(i));
        }
        
        logView = (TextView)findViewById(R.id.logview);
        
        lb = (Button)findViewById(R.id.logButton);
		lb.setOnClickListener(new OnClickListener() 
        {
			public void onClick(View v) 
            {                
    			my_logdata.add(currentvalue);
    			sb = new StringBuilder();
    			sb.append("Logs [" + my_logdata.size() + "]\n");
    			for(int i = my_logdata.size(); i > 0; i--){
    				sb.append(my_logdata.get(i-1));
    				sb.append("\n");
    			}
    			logView.setText(sb.toString());
    			Log.d(TAG, "Click!");
            }
        });
		
		statusView1 = (TextView)findViewById(R.id.statusText1);
		statusView2 = (TextView)findViewById(R.id.statusText2);
		statusView3 = (TextView)findViewById(R.id.statusText3);
		
		statusView1.setTextColor(Color.rgb(20 , 20 , 80));
		statusView2.setTextColor(Color.rgb(20 , 20 , 80));
		statusView3.setTextColor(Color.rgb(20 , 20 , 80));
		        
		myTimer();
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Intent mainIntent = new Intent(MainActivity.this,MainActivity.class);
		MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();
	}
    
    public void myTimer(){
		thread1 = new Thread()
	    {
	        public void run() {
	            try {
	                while(true) {	        			
	        			sleep(1000);
	        			Message m = new Message();
                    	m.what = TICK;                            
                    	messageHandler.sendMessage(m); 
	                }
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    };
	    thread1.start();
	}
    
    private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			//handle messages
			
			case TICK:
				if(autologging > 0 && autocounter++ == al_value[autologging]){
					autocounter = 0;
					Log.d(TAG, "Click?");
					lb.performClick();
				}				
				
				if(wakelock)					
					statusView1.setTextColor(Color.rgb(136 , 136 , 238));
				else
					statusView1.setTextColor(Color.rgb(20 , 20 , 80));
					
				if(autologging > 0)	
					statusView2.setTextColor(Color.rgb(136 , 136 , 238));
				else
					statusView2.setTextColor(Color.rgb(20 , 20 , 80));
					
				if(sensisum >= 1000){	
					rcalcounter = 10;
					statusView3.setTextColor(Color.rgb(238, 136 , 136));
				}
				else{
					if(rcalcounter > 0){
						rcalcounter--;					
					}
					else
						statusView3.setTextColor(Color.rgb(20 , 20 , 80));
				}
				
				
				break;
			default:
				//break;
			}
		}
	};
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
	      if(accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
	    	  //Log.d(TAG, "Unreliable 1");
	    	  ;
	}

	public void onSensorChanged(SensorEvent event) {
		if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			;//Log.d(TAG, "Unreliable 2");
		//Create some kind of sensitivity (how many to average over?) 1 - 10?
		myAzimuth = Math.round(event.values[0] * event.values[0]);
		myPitch = Math.round(event.values[1] * event.values[1]);
		myRoll = Math.round(event.values[2] * event.values[2]);
		abssum = Math.sqrt(myAzimuth + myPitch + myRoll);
		//Log.d(TAG,"onSensorChanged:"+abssum);
		
		sensisum = 0;
		isensitivity = 1 + (SENSITIVITY_MAX - sensitivity) * 10;
		sensidata[sensicount++ % isensitivity] = abssum;
		for(int i = 0; i < isensitivity; i++){
			sensisum = sensisum + sensidata[i];
		}
		sensisum = sensisum / isensitivity;
		
		DecimalFormat maxDigitsFormatter = new DecimalFormat("#####.0");
		
		clockView = (TextView)findViewById(R.id.mainText);
        Typeface font = Typeface.createFromAsset(getAssets(), "digit.ttf");
	    clockView.setTypeface(font);
	    currentvalue = String.valueOf(maxDigitsFormatter.format(sensisum));
		clockView.setText(currentvalue + " ");
        
		//Find the baseline and remember it (like the average of the first 100 values or so). 
		//If new baseline larger than double that - make the user reset thr sensor by shaking or doing the figure 8.
		 for(int i = 0; i <= 40; i++){
			 if(i%2 == 0){
					 if(975 - sensisum <= i * 25)
						 my_meter.get(i).setBackgroundColor(Color.rgb(136 - i , 136 - i , 238 - i));					 
					 else
						 my_meter.get(i).setBackgroundColor(Color.rgb(20 , 20 , 80));
					 
					 if(sensisum >= 1000)
						 my_meter.get(i).setBackgroundColor(Color.rgb(238 - i , 136 - i , 136 - i));
			 }
			 else
				 	my_meter.get(i).setBackgroundColor(Color.rgb(34 , 34 , 102));
		 }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(my_logdata.size() > 0)
	    	menu.findItem(R.id.exportlog).setEnabled(true);
	    else
	    	menu.findItem(R.id.exportlog).setEnabled(false);
		return true;
	}
	//Menu item "exportlog" should only be visible when logdata exists! android:visible=["visible" | "invisible" | "gone"]
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.exportlog:
	    	//Toast.makeText(this, "Export Log", Toast.LENGTH_LONG).show();
	    	writeToFile();
	        return true;
	    case R.id.guide:
	        showGuide();
	    	//Toast.makeText(this, "Guide", Toast.LENGTH_LONG).show();
	        return true;
	    case R.id.settings:
	        showSettings();
	    	//Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
	        return true;
	    case R.id.about:
	        showAbout();
	    	//Toast.makeText(this, "About", Toast.LENGTH_LONG).show();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void showAbout(){
		/*Intent myIntent = new Intent(this, AboutActivity.class);
        startActivityForResult(myIntent, 0);*/
        
        Intent mainIntent = new Intent(MainActivity.this,AboutActivity.class);
		/*MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();*/
        MainActivity.this.startActivityForResult(mainIntent, -1);
        
		return;
	}
	
	public void showGuide(){
		/*Intent myIntent = new Intent(this, AboutActivity.class);
        startActivityForResult(myIntent, 0);*/
        
        Intent mainIntent = new Intent(MainActivity.this,GuideActivity.class);
		/*MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();*/
        MainActivity.this.startActivityForResult(mainIntent, -1);

		return;
	}
	
	public void showSettings(){
		Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		myIntent.putExtra("SETTINGS_WAKELOCK", wakelock);
		myIntent.putExtra("SETTINGS_SENSITIVITY", sensitivity);
		myIntent.putExtra("SETTINGS_AUTOLOGGING", autologging);
		MainActivity.this.startActivityForResult(myIntent, 0);
        
        /*Intent mainIntent = new Intent(MainActivity.this,SettingsActivity.class);
		MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();*/

		return;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){
			
			wakelock = data.getBooleanExtra("SETTINGS_WAKELOCK", false);
			Log.d(TAG, Boolean.toString(wakelock));
			sensitivity = data.getIntExtra("SETTINGS_SENSITIVITY", SENSITIVITY_DEFAULT);
			Log.d(TAG, Integer.toString(sensitivity));
			autologging = data.getIntExtra("SETTINGS_AUTOLOGGING", AUTOLOGGING_DEFAULT);
			Log.d(TAG, Integer.toString(autologging));
			
			for(int i = 0; i < (SENSITIVITY_MAX * 10); i++)
	        	sensidata[i] = abssum;
			
			if(wakelock){
		         wl.acquire();
		    }else{
	            if(wl.isHeld()){
	            	wl.release();
	            }
	            //wl = null;
	        }

		}
	}
	
	public boolean writeToFile(){
		  FileOutputStream fos = null;
		  OutputStreamWriter out = null;
		  boolean result;
		  
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	      sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
	      String dateName =  sdf.format(new Date());
	      Log.d(TAG, dateName);
		  try{
			  File root = Environment.getExternalStorageDirectory();
		
			  if( root.canWrite() ){
				  fos = new FileOutputStream( root + "/" + dateName + ".log");
				  out = new OutputStreamWriter( fos );
				    
				  for(int i = 0; i < my_logdata.size(); i++){
					  out.write(my_logdata.get(i));
					  out.write("\r\n");
				  }
				  
				  out.flush();
				  result = true;
				  out.close();
				  Toast.makeText(this, "File Saved as:\n" + root + "/" + dateName + ".log", 5).show();
			  }
			  else{
				  result = false;
			  }
		  }catch( IOException e ){
			  e.printStackTrace();
			  result = false;
		  }

		  return result;
	}
	
	@Override
	protected void onStop() {
		/* may as well just finish since saving the state is not important for this toy app */
		Log.d(TAG, "onStop");
		//sensorManager.unregisterListener(this);
		//finish();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		sensorManager.unregisterListener(this);
		if(wl.isHeld()){
        	wl.release();
        }
        //wl = null;
		//finish();
		super.onStop();
	}
	
}