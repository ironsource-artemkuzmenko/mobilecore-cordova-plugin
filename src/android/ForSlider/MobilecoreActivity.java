package com.mobilecore.phonegap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.hello.mobilecore.R;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.OnReadyListener;

public class MobilecoreActivity extends Activity implements CordovaInterface {

	String TAG = "MainActivity CordovaInterface";

	CordovaWebView mainView;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private CordovaPlugin activityResultCallback;
	@SuppressWarnings("unused")
	private Object activityResultKeepRunning;
	private Object keepRunning;
	
	private final String MOBILECORE_DEV_HASH = "69JU0DHY08GL1TOLW85R6W32GK6XA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);

		MobileCore.init(this, MOBILECORE_DEV_HASH, MobileCore.LOG_TYPE.PRODUCTION, AD_UNITS.ALL_UNITS);
		
		MobileCore.getSlider().setContentViewWithSlider(this, R.layout.main);

		if (MobileCore.isStickeeReady()) {
			MobileCore.showStickee(this);
		} else {
			MobileCore.setStickeezReadyListener(new OnReadyListener() {

				@Override
				public void onReady(AD_UNITS arg0) {
					MobileCore.showStickee(MobilecoreActivity.this);
				}
			});
		}

		mainView = (CordovaWebView) findViewById(R.id.mainView);
		mainView.loadUrl("file:///android_asset/www/index.html");
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	@Override
	public Object onMessage(String id, Object data) {
		LOG.d(TAG, "onMessage(" + id + "," + data + ")");
		if ("exit".equals(id)) {
			super.finish();
		}
		return null;
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
		this.activityResultCallback = plugin;
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		this.activityResultCallback = command;
		this.activityResultKeepRunning = this.keepRunning;

		// If multitasking turned on, then disable it for activities that return results
		if (command != null) {
			this.keepRunning = false;
		}

		// Start activity
		super.startActivityForResult(intent, requestCode);

	}

	@Override
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 *
	 * @param requestCode       The request code originally supplied to startActivityForResult(),
	 *                          allowing you to identify who this result came from.
	 * @param resultCode        The integer result code returned by the child activity through its setResult().
	 * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		CordovaPlugin callback = this.activityResultCallback;
		if (callback != null) {
			callback.onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	/**
	 * Called when the system is about to start resuming a previous activity.
	 */
	protected void onPause() {
		super.onPause();

		// Send pause event to JavaScript
		this.mainView.loadUrl("javascript:try{cordova.fireDocumentEvent('pause');}catch(e){console.log('exception firing pause event from native');};");

		// Forward to plugins
		if (this.mainView.pluginManager != null) {
			this.mainView.pluginManager.onPause(true);
		}
	}

	@Override
	/**
	 * Called when the activity receives a new intent
	 **/
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Forward to plugins
		if ((this.mainView != null) && (this.mainView.pluginManager != null)) {
			this.mainView.pluginManager.onNewIntent(intent);
		}
	}

	@Override
	/**
	 * Called when the activity will start interacting with the user.
	 */
	protected void onResume() {
		super.onResume();

		if (this.mainView == null) {
			return;
		}

		// Send resume event to JavaScript
		this.mainView.loadUrl("javascript:try{cordova.fireDocumentEvent('resume');}catch(e){console.log('exception firing resume event from native');};");

		// Forward to plugins
		if (this.mainView.pluginManager != null) {
			this.mainView.pluginManager.onResume(true);
		}

	}
}
