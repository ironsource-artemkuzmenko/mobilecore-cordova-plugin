package com.mobilecore.phonegap;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.LOG;
import org.apache.cordova.LinearLayoutSoftKeyboardDetect;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.hello.mobilecore.R;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;

public class MobilecoreActivity extends CordovaActivity {

	String TAG = "MainActivity CordovaInterface";

	CordovaWebView mainView;

	private final boolean WITH_SLIDER = true; // Switch this to false if you don't want to use mobileCore Slider
	private final String MOBILECORE_DEV_HASH = "3FCONBTOP58OJE0UTVFCE947OC88D"; // REPLACE THIS WITH YOUR OWN DEV TOKEN

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MobileCore.init(this, MOBILECORE_DEV_HASH, MobileCore.LOG_TYPE.DEBUG, AD_UNITS.ALL_UNITS); 
		
		super.init();

		loadUrl("file:///android_asset/www/index.html");
	}

	@SuppressWarnings("deprecation")
    @Override
	protected void createViews() {
		// This builds the view. We could probably get away with NOT having a LinearLayout, but I like having a bucket!

		LOG.d(TAG, "CordovaActivity.createViews()");
		
        if (WITH_SLIDER) {
        	return;
        }
		
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		root = new LinearLayoutSoftKeyboardDetect(this, width, height);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

		appView.setId(100);
		appView.setLayoutParams(new LinearLayout.LayoutParams(
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        ViewGroup.LayoutParams.MATCH_PARENT,
		        1.0F));

		// Add web view but make it invisible while loading URL
		appView.setVisibility(View.INVISIBLE);

		// need to remove appView from any existing parent before invoking root.addView(appView)
		ViewParent parent = appView.getParent();
		if ((parent != null) && (parent != root)) {
			LOG.d(TAG, "removing appView from existing parent");
			ViewGroup parentGroup = (ViewGroup) parent;
			parentGroup.removeView(appView);
		}
		root.addView((View) appView);
		
		setContentView(root);
		
		int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
		root.setBackgroundColor(backgroundColor);
	}

	@Override
    protected CordovaWebView makeWebView() {
		CordovaWebView webView = null;
		if (WITH_SLIDER) {
			webView = (CordovaWebView) findViewById(R.id.mainView);
		} else {
			webView = new CordovaWebView(MobilecoreActivity.this);
		}
        return webView;
    }	

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void init(CordovaWebView webView, CordovaWebViewClient webViewClient, CordovaChromeClient webChromeClient) {
        LOG.d(TAG, "CordovaActivity.init()");

        if(!preferences.getBoolean("ShowTitle", false))
        {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if(preferences.getBoolean("SetFullscreen", false))
        {
            Log.d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (preferences.getBoolean("Fullscreen", false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        if (WITH_SLIDER) {
        	MobileCore.getSlider().setContentViewWithSlider(this, R.layout.main);
        }
        
        appView = webView != null ? webView : makeWebView();
        if (appView.pluginManager == null) {
            appView.init(this, webViewClient != null ? webViewClient : makeWebViewClient(appView),
                    webChromeClient != null ? webChromeClient : makeChromeClient(appView),
                    pluginEntries, internalWhitelist, externalWhitelist, preferences);
        }

        // TODO: Have the views set this themselves.
        if (preferences.getBoolean("DisallowOverscroll", false)) {
            appView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        createViews();

        // TODO: Make this a preference (CB-6153)
        // Setup the hardware volume controls to handle volume control
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
}
