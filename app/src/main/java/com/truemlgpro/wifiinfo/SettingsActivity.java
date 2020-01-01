package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.*;
import android.support.v4.widget.*;
import android.widget.*;
import android.view.*;
import android.content.*;
import me.anwarshahriar.calligrapher.*;

public class SettingsActivity extends AppCompatActivity
{

	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private Context context;
	private TextView textview1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		textview1 = (TextView) findViewById(R.id.textview1);
		
		Calligrapher calligrapher = new Calligrapher(this);
		calligrapher.setFont(this, "fonts/GoogleSans-Medium.ttf", true);

		setSupportActionBar(toolbar);
		final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setDisplayShowHomeEnabled(true);
		actionbar.setElevation(20);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Back button pressed
					finish();
				}
			});
	}
	
}
