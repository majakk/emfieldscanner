package studio.coldstream.emfieldscanner;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SettingsActivity extends Activity{

	protected static final int SENSITIVITY_DEFAULT = 9;
	protected static final int AUTOLOGGING_DEFAULT = 0;
	
	CheckBox mycheck;
	SeekBar myseek1;
	SeekBar myseek2;
	TextView mytext1;
	TextView mytext2;
	
	private boolean wakelock;
	private int sensitivity;
	private int autologging;
	
	String[] al_text = {"Off","5s","10s","30s","1m","5m","10m"};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.settings);
	    
	    Intent data = this.getIntent();
	    wakelock = data.getBooleanExtra("SETTINGS_WAKELOCK", false);
		//Log.d(TAG, Boolean.toString(wakelock));
		sensitivity = data.getIntExtra("SETTINGS_SENSITIVITY", SENSITIVITY_DEFAULT);
		//Log.d(TAG, Integer.toString(sensitivity));
		autologging = data.getIntExtra("SETTINGS_AUTOLOGGING", AUTOLOGGING_DEFAULT);
		//Log.d(TAG, Integer.toString(autologging));
	    
	    mycheck = (CheckBox) findViewById(R.id.checkBox1);
	    mycheck.setChecked(wakelock);
	    mycheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				// TODO Auto-generated method stub
				wakelock = isChecked;
			}
	    	
	    });
	    
	    myseek1 = (SeekBar) findViewById(R.id.seekBar1);
	    mytext1 = (TextView) findViewById(R.id.textView1);
	    mytext1.setText("Sensitivity [" + sensitivity + "]");
	    myseek1.setProgress(sensitivity);
	    myseek1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            	//seekBar.setProgress(progress);
            	mytext1.setText("Sensitivity [" + progress + "]");
            	sensitivity = progress;
            }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
	    });
	    
	    myseek2 = (SeekBar) findViewById(R.id.seekBar2);
	    mytext2 = (TextView) findViewById(R.id.textView2);
	    mytext2.setText("Autologging [" + al_text[autologging] + "]");
	    myseek2.setProgress(autologging);
	    myseek2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            	//seekBar.setProgress(progress);
            	mytext2.setText("Autologging [" + al_text[progress] + "]");
            	autologging = progress;
            }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
	    });

	}
	 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Intent mainIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
		SettingsActivity.this.startActivity(mainIntent);
		SettingsActivity.this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (  Integer.valueOf(android.os.Build.VERSION.SDK) < 7 //Instead use android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
	            && keyCode == KeyEvent.KEYCODE_BACK
	            && event.getRepeatCount() == 0) {
	        // Take care of calling this method on earlier versions of
	        // the platform where it doesn't exist.
	        onBackPressed();
	    }
	    return super.onKeyDown(keyCode, event);
	}

	//@Override
	public void onBackPressed() {
	    // This will be called either automatically for you on 2.0
	    // or later, or by the code above on earlier versions of the
	    // platform.
		
		Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
		mainIntent.putExtra("SETTINGS_WAKELOCK", wakelock);
		mainIntent.putExtra("SETTINGS_SENSITIVITY", sensitivity);
		mainIntent.putExtra("SETTINGS_AUTOLOGGING", autologging);
		SettingsActivity.this.setResult(RESULT_OK, mainIntent);
        SettingsActivity.this.finish();
		
	    return;
	}
}
