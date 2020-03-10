package com.truemlgpro.wifiinfo;

import android.support.v7.app.*;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.widget.*;
import me.anwarshahriar.calligrapher.*;
import android.animation.*;
import android.view.*;

public class SplashActivity extends AppCompatActivity
{

	private TextView splash_text;
	private TextView dev_name;
	private ImageView splash_logo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		
		splash_text = (TextView) findViewById(R.id.splash_text);
		splash_logo = (ImageView) findViewById(R.id.splash_logo);
		dev_name = (TextView) findViewById(R.id.dev_name);
		
		ObjectAnimator.ofFloat(splash_logo, View.ALPHA, 0.0f, 1.0f).setDuration(2500).start();
		ObjectAnimator.ofFloat(splash_logo, View.SCALE_X, 0.1f, 0.75f).setDuration(2000).start();
		ObjectAnimator.ofFloat(splash_logo, View.SCALE_Y, 0.1f, 0.75f).setDuration(2000).start();
		
		ObjectAnimator.ofFloat(splash_text, View.ALPHA, 0.0f, 1.0f).setDuration(2500).start();
		ObjectAnimator.ofFloat(splash_text, View.SCALE_X, 0.25f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(splash_text, View.SCALE_Y, 0.25f, 1.0f).setDuration(2250).start();
		
		ObjectAnimator.ofFloat(dev_name, View.ALPHA, 0.0f, 1.0f).setDuration(2750).start();
		ObjectAnimator.ofFloat(dev_name, View.SCALE_X, 0.25f, 1.0f).setDuration(2250).start();
		ObjectAnimator.ofFloat(dev_name, View.SCALE_Y, 0.25f, 1.0f).setDuration(2250).start();
		
		Calligrapher calligrapher = new Calligrapher(this);
		calligrapher.setFont(this, "fonts/GoogleSans-Medium.ttf", true);
		
		Thread splashThread = new Thread() {
		@Override
			public void run() {
				try {
					sleep(3000);
					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(intent);
					finish();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		splashThread.start();
	}


	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}
	
}
