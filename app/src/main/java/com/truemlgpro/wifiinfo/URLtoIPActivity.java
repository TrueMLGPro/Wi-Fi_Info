package com.truemlgpro.wifiinfo;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.support.v7.app.*;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.*;
import android.support.design.widget.*;
import me.anwarshahriar.calligrapher.*;
import android.view.*;
import java.lang.ref.*;
import android.view.inputmethod.*;
import java.net.*;
import android.content.*;


public class URLtoIPActivity extends AppCompatActivity
{

	private static final int MIN_TEXT_LENGTH = 4;
    private static final String EMPTY_STRING = "";

    private TextInputLayout mTextInputLayout;
    private static EditText mEditText;
	private static TextView textview_ipFromURL;
	private Button convert_button;
	private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_to_ip_activity);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextInputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        mEditText = (EditText) findViewById(R.id.edittext_main);
		convert_button = (Button) findViewById(R.id.convert_button);
		textview_ipFromURL = (TextView) findViewById(R.id.textview_ipFromURL);
    	
		mEditText.setOnEditorActionListener(ActionListener.newInstance(this));
		
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
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
		
		textview_ipFromURL.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					String ip_converted = textview_ipFromURL.getText().toString();
					ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("Converted IP", ip_converted);
					cbm.setPrimaryClip(clip);

					Toast.makeText(getBaseContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
				}
			});
		
		convert_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!shouldShowError()) {
					try {
						String url = mEditText.getText().toString();
						try {
							InetAddress ipFromURL = InetAddress.getByName(new URL("https://" + url).getHost());
							String ip = ipFromURL.getHostAddress();
							textview_ipFromURL.setText("IP: " + ip);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							textview_ipFromURL.setText("IP: N/A");
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
						textview_ipFromURL.setText("IP: N/A");
					}
						hideError();
				} else {
						showError();
					}
				}
		});
		
    }

    private boolean shouldShowError() {
        int textLength = mEditText.getText().length();
        return textLength >= 0 && textLength < MIN_TEXT_LENGTH;
    }

    private void showError() {
        mTextInputLayout.setError("Field is too short...");
    }

    private void hideError() {
        mTextInputLayout.setError(EMPTY_STRING);
    }

    private static final class ActionListener implements TextView.OnEditorActionListener {
        private final WeakReference<URLtoIPActivity> urlToIPWeakReference;

        public static ActionListener newInstance(URLtoIPActivity urlToIPActivity) {
            WeakReference<URLtoIPActivity> urlToIPWeakReference = new WeakReference<>(urlToIPActivity);
            return new ActionListener(urlToIPWeakReference);
        }

        private ActionListener(WeakReference<URLtoIPActivity> urlToIPWeakReference) {
            this.urlToIPWeakReference = urlToIPWeakReference;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            URLtoIPActivity urlToIPActivity = urlToIPWeakReference.get();
            if (urlToIPActivity != null) {
                if (actionId == EditorInfo.IME_ACTION_GO && urlToIPActivity.shouldShowError()) {
                    urlToIPActivity.showError();
                } else {
                    urlToIPActivity.hideError();
					try {
						String url = mEditText.getText().toString();
						try {
							InetAddress ipFromURL = InetAddress.getByName(new URL("https://" + url).getHost());
							String ip = ipFromURL.getHostAddress();
							textview_ipFromURL.setText("IP: " + ip);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							textview_ipFromURL.setText("IP: N/A");
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
						textview_ipFromURL.setText("IP: N/A");
					}
                }
            }
            return true;
        }
    }
	
}
