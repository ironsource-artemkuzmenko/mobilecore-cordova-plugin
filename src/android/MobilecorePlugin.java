package com.mobilecore.phonegap;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.ironsource.mobilcore.CallbackResponse;
import com.ironsource.mobilcore.MCEWidgetTextProperties;
import com.ironsource.mobilcore.MCISliderAPI;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.ironsource.mobilcore.OnReadyListener;

public class MobilecorePlugin extends CordovaPlugin implements CallbackResponse, OnReadyListener {
    public static final String TAG = "MCPhoneGap";
    private static final boolean DEBUG = true;

    public static final String WIDGET_PROPERTY_MAIN_TEXT = "MAIN_TEXT";
    public static final String WIDGET_PROPERTY_SECONDARY_TEXT = "SECONDARY_TEXT";
    public static final String WIDGET_PROPERTY_BADGE_TEXT = "BADGE_TEXT";

    public static final String ACTION_INIT = "initMobilecore";
    public static final String ACTION_SHOW_OFFERWALL = "showOfferwall";
    public static final String ACTION_SHOW_OFFERWALL_FORCE = "showOfferwallForce";
    public static final String ACTION_OPEN_URL = "openUrl";
    public static final String ACTION_REFRESH_OFFERS = "refreshOffers";
    public static final String ACTION_IS_OFFERWALL_READY = "isOfferwallReady";

    public static final String ACTION_SLIDER_SET_WIDGET_TEXT_PROPERTY = "setWidgetTextProperty";
    public static final String ACTION_SLIDER_GET_WIDGET_TEXT_PROPERTY = "getWidgetTextProperty";
    public static final String ACTION_SLIDER_SET_WIDGET_ICON_RESOURCE = "setWidgetIconResource";
    public static final String ACTION_SLIDER_OPEN_SLIDER_MENU = "openSliderMenu";
    public static final String ACTION_SLIDER_CLOSE_SLIDER_MENU = "closeSliderMenu";
    public static final String ACTION_SLIDER_TOGGLE_SLIDER_MENU = "toggleSliderMenu";
    public static final String ACTION_SLIDER_SHOW_WIDGET = "showWidget";
    public static final String ACTION_SLIDER_HIDE_WIDGET = "hideWidget";
    public static final String ACTION_SLIDER_SET_EMPHASIZED_WIDGET = "setEmphasizedWidget";
    public static final String ACTION_SLIDER_IS_SLIDER_MENU_OPEN = "isSliderMenuOpen";

    public static final String ACTION_LISTEN_WIDGET_CALLBACKS = "listenWidgetCallbacks";
    
    public static final String ACTION_SET_OFFERWALLREADY_LISTENER = "setOfferwallReadyListener";
    
    // Stickeez
    public static final String ACTION_STICKEEZ_SHOW_STICKEE = "showStickee";
    public static final String ACTION_STICKEEZ_HIDE_STICKEE = "hideStickee";
    public static final String ACTION_STICKEEZ_IS_STICKEE_READY = "isStickeeReady";
    public static final String ACTION_STICKEEZ_IS_STICKEE_SHOWING = "isStickeeShowing";
    public static final String ACTION_STICKEEZ_IS_STICKEE_SHOWING_OFFERS = "isStickeeShowingOffers";
    public static final String ACTION_STICKEEZ_SET_STICKEEZ_READY_LISTENER = "setStickeezReadyListener";
    
    //Direct to Market
    public static final String ACTION_DIRECT_TO_MARKET = "directToMarket";
    public static final String ACTION_DIRECT_TO_MARKET_IS_READY = "isDirectToMarketReady";
    public static final String ACTION_DIRECT_TO_MARKET_SET_READY_LISTENER = "setDirectToMarketReadyListener";
    
    // AD_UNITS consts
    public static final String ALL_UNITS = MobileCore.AD_UNITS.ALL_UNITS.toString();
    public static final String OFFERWALL = MobileCore.AD_UNITS.OFFERWALL.toString();
    public static final String SLIDER = MobileCore.AD_UNITS.SLIDER.toString();
    public static final String STICKEEZ = MobileCore.AD_UNITS.STICKEEZ.toString();
    public static final String DIRECT_TO_MARKET = MobileCore.AD_UNITS.DIRECT_TO_MARKET.toString();
    
    //LOG_TYPE consts
    public static final String LOG_DEBUG = MobileCore.LOG_TYPE.DEBUG.toString();
    public static final String LOG_PRODUCTION = MobileCore.LOG_TYPE.PRODUCTION.toString();    
    
    
    private boolean isReceiverRegistered;
    BroadcastReceiver mSliderReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    String callbackId = intent.getStringExtra(MCISliderAPI.EXTRA_CALLBACK_ID);
	    if (DEBUG) { 
		Log.d(TAG, "mSliderReceiver onReceive, callbackId=" + callbackId);
	    }
	    if (!TextUtils.isEmpty(callbackId) && mWidgetCallbackCtx != null) {
		PluginResult result = new PluginResult(PluginResult.Status.OK, callbackId);
		result.setKeepCallback(true);
		mWidgetCallbackCtx.sendPluginResult(result);
	    }
	}
    };

    private CallbackContext mCallbackCtx;
    
    private CallbackContext mWidgetCallbackCtx;
    
    private CallbackContext mOfferwallReadyCtx;

    private MCEWidgetTextProperties mMCProperty;

    private MCEWidgetTextProperties mMCProperty1;

    private String mWigdetTextProperty;

    private boolean mIsSliderMenuOpen;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
	if (DEBUG) {
	    Log.d(TAG, "execute | action=" + action);
	}
	try {

	    // General:

	    if (ACTION_INIT.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String devHash = arg_object.getString("devHash");
		if (DEBUG) {
		    Log.d(TAG, "devHash=" + devHash);
		}

		final String logLevel = arg_object.getString("logLevel");
		LOG_TYPE logType;
		if (!TextUtils.isEmpty(logLevel) && LOG_DEBUG.equals(logLevel)) {
		    logType = LOG_TYPE.DEBUG;
		} else {
		    logType = LOG_TYPE.PRODUCTION;
		}
		final LOG_TYPE finalLogType = logType;

		JSONArray adUnitsArray = arg_object.optJSONArray("adUnits");
		if (DEBUG) {
		    Log.d(TAG, "adUnitsArray=" + adUnitsArray);
		}
		AD_UNITS[] mCAdUnits;
		if (adUnitsArray != null && adUnitsArray.length() > 0) {
		    List<AD_UNITS> units = new ArrayList<AD_UNITS>();
		    for (int i = 0; i < adUnitsArray.length(); i++) {
			String s = adUnitsArray.optString(i);
			if (!TextUtils.isEmpty(s)) {
			    if (ALL_UNITS.equals(s)) {
				units.add(AD_UNITS.ALL_UNITS);
			    } else if (OFFERWALL.equals(s)) {
				units.add(AD_UNITS.OFFERWALL);
			    } else if (SLIDER.equals(s)) {
				units.add(AD_UNITS.SLIDER);
			    } else if (STICKEEZ.equals(s)) {
				units.add(AD_UNITS.STICKEEZ);
			    } else if (DIRECT_TO_MARKET.equals(s)) {
				units.add(AD_UNITS.DIRECT_TO_MARKET);
			    }  else {
				continue;
			    }
			}
		    }
		    mCAdUnits = new AD_UNITS[units.size()];
		    mCAdUnits = units.toArray(mCAdUnits);
		} else {
		    mCAdUnits = new AD_UNITS[] { AD_UNITS.ALL_UNITS };
		}
		final AD_UNITS[] finalMCAdUnits = mCAdUnits;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			if (DEBUG) {
			    Log.d(TAG, "finalLogType=" + finalLogType);
			    for (int j = 0; j < finalMCAdUnits.length; j++) {
				Log.d(TAG, "finalMCAdUnits[" + j + "]=" + finalMCAdUnits[j]);

			    }
			}
			MobileCore.init(cordova.getActivity(), devHash, finalLogType, finalMCAdUnits);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SHOW_OFFERWALL.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.showOfferWall(cordova.getActivity(), MobilecorePlugin.this);
		    }
		});
		return true;
	    } else if (ACTION_SHOW_OFFERWALL_FORCE.equals(action)) {
		this.mCallbackCtx = callbackContext;
		JSONObject arg_object = args.getJSONObject(0);
		final boolean forceShow = arg_object.getBoolean("forceShow");
		if (DEBUG) {
		    Log.d(TAG, "forceShow=" + forceShow);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.showOfferWall(cordova.getActivity(), MobilecorePlugin.this, forceShow);
		    }
		});
		return true;
	    } else if (ACTION_REFRESH_OFFERS.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.refreshOffers();
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_OPEN_URL.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String url = arg_object.getString("url");
		final boolean internal = arg_object.getBoolean("internal");
		if (DEBUG) {
		    Log.d(TAG, "url=" + url + " | internal=" + internal);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.openUrl(cordova.getActivity(), url, internal);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SET_OFFERWALLREADY_LISTENER.equals(action)) {
		mOfferwallReadyCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.setOfferwallReadyListener(MobilecorePlugin.this);
		    }
		});
		return true;
	    } else if (ACTION_IS_OFFERWALL_READY.equals(action)) {
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			boolean isOfferwallReady = MobileCore.isOfferwallReady();
			if (DEBUG) {
			    Log.d(TAG, "isOfferwallReady=" + isOfferwallReady);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, isOfferwallReady);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;

	    }

	    // Stickeez:
	    else if (ACTION_STICKEEZ_IS_STICKEE_READY.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			boolean isStickeeReady = MobileCore.isStickeeReady();
			if (DEBUG) {
			    Log.d(TAG, "isStickeeReady=" + isStickeeReady);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, isStickeeReady);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;
	    } else if (ACTION_STICKEEZ_SHOW_STICKEE.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.showStickee(cordova.getActivity());
		    }
		});
		return true;
	    } else if (ACTION_STICKEEZ_HIDE_STICKEE.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.hideStickee();
		    }
		});
		return true;
	    } else if (ACTION_STICKEEZ_IS_STICKEE_SHOWING.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			boolean isStickeeShowing = MobileCore.isStickeeShowing();
			if (DEBUG) {
			    Log.d(TAG, "isStickeeShowing=" + isStickeeShowing);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, isStickeeShowing);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;
	    } else if (ACTION_STICKEEZ_IS_STICKEE_SHOWING_OFFERS.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			boolean isStickeeShowingOffers = MobileCore.isStickeeShowingOffers();
			if (DEBUG) {
			    Log.d(TAG, "isStickeeShowingOffers=" + isStickeeShowingOffers);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, isStickeeShowingOffers);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;
	    } else if (ACTION_STICKEEZ_SET_STICKEEZ_READY_LISTENER.equals(action)) {
		mOfferwallReadyCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.setStickeezReadyListener(MobilecorePlugin.this);
		    }
		});
		return true;
	    }
	    
	    // Direct to market
	    else if (ACTION_DIRECT_TO_MARKET.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.directToMarket(cordova.getActivity());
		    }
		});
		return true;
	    } else if (ACTION_DIRECT_TO_MARKET_IS_READY.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			boolean isDirectToMarketReady = MobileCore.isDirectToMarketReady();
			if (DEBUG) {
			    Log.d(TAG, "isDirectToMarketReady=" + isDirectToMarketReady);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, isDirectToMarketReady);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;
	    } else if (ACTION_DIRECT_TO_MARKET_SET_READY_LISTENER.equals(action)) {
		mOfferwallReadyCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.setDirectToMarketReadyListener(MobilecorePlugin.this);
		    }
		});
		return true;
	    }

	    // Slider:
	    else if (ACTION_LISTEN_WIDGET_CALLBACKS.equals(action)) {
		cordova.getActivity().registerReceiver(mSliderReceiver, new IntentFilter(MCISliderAPI.ACTION_WIDGET_CALLBACK));
		mWidgetCallbackCtx = callbackContext;
		isReceiverRegistered = true;
		return true;
	    } else if (ACTION_SLIDER_SET_WIDGET_TEXT_PROPERTY.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		final String property = arg_object.getString("property");
		final String text = arg_object.getString("text");
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId + " | property=" + property + " | text=" + text);
		}
		mMCProperty = null;
		if (WIDGET_PROPERTY_MAIN_TEXT.equals(property)) {
		    mMCProperty = MCEWidgetTextProperties.MAIN_TEXT;
		} else if (WIDGET_PROPERTY_SECONDARY_TEXT.equals(property)) {
		    mMCProperty = MCEWidgetTextProperties.SECONDARY_TEXT;
		} else if (WIDGET_PROPERTY_BADGE_TEXT.equals(property)) {
		    mMCProperty = MCEWidgetTextProperties.BADGE_TEXT;
		}
		if (mMCProperty != null) {
		    cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
			    MobileCore.getSlider().setWidgetTextProperty(widgetId, mMCProperty, text);
			}
		    });
		}
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_GET_WIDGET_TEXT_PROPERTY.equals(action)) {
		this.mCallbackCtx = callbackContext;
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		final String property = arg_object.getString("property");
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId + " | property=" + property);
		}
		mMCProperty1 = null;
		if (WIDGET_PROPERTY_MAIN_TEXT.equals(property)) {
		    mMCProperty1 = MCEWidgetTextProperties.MAIN_TEXT;
		} else if (WIDGET_PROPERTY_SECONDARY_TEXT.equals(property)) {
		    mMCProperty1 = MCEWidgetTextProperties.SECONDARY_TEXT;
		} else if (WIDGET_PROPERTY_BADGE_TEXT.equals(property)) {
		    mMCProperty1 = MCEWidgetTextProperties.BADGE_TEXT;
		}
		if (mMCProperty1 != null) {
		    cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
			    mWigdetTextProperty = MobileCore.getSlider().getWidgetTextProperty(widgetId, mMCProperty1);
			    if (DEBUG) {
				Log.d(TAG, "getWidgetTextProperty | wigdetTextProperty=" + mWigdetTextProperty);
			    }
			    PluginResult result = new PluginResult(PluginResult.Status.OK, mWigdetTextProperty);
			    result.setKeepCallback(false);
			    mCallbackCtx.sendPluginResult(result);
			}
		    });
		} else {
		    mWigdetTextProperty = "";
		}
		return true;
	    } else if (ACTION_SLIDER_SET_WIDGET_ICON_RESOURCE.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		final String resDrawableName = arg_object.getString("resDrawableName");
		final int resId = cordova.getActivity().getResources().getIdentifier(resDrawableName, "drawable", cordova.getActivity().getPackageName());
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId + " | resDrawableName=" + resDrawableName + " | resId=" + resId);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().setWidgetIconResource(widgetId, resId);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_OPEN_SLIDER_MENU.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final boolean animate = arg_object.getBoolean("animate");
		if (DEBUG) {
		    Log.d(TAG, "animate=" + animate);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().openSliderMenu(animate);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_CLOSE_SLIDER_MENU.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final boolean animate = arg_object.getBoolean("animate");
		if (DEBUG) {
		    Log.d(TAG, "animate=" + animate);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().closeSliderMenu(animate);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_TOGGLE_SLIDER_MENU.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final boolean animate = arg_object.getBoolean("animate");
		if (DEBUG) {
		    Log.d(TAG, "animate=" + animate);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().toggleSliderMenu(animate);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_SHOW_WIDGET.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().showWidget(widgetId);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_HIDE_WIDGET.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().hideWidget(widgetId);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_SET_EMPHASIZED_WIDGET.equals(action)) {
		JSONObject arg_object = args.getJSONObject(0);
		final String widgetId = arg_object.getString("widgetId");
		if (DEBUG) {
		    Log.d(TAG, "widgetId=" + widgetId);
		}
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			MobileCore.getSlider().setEmphasizedWidget(widgetId);
		    }
		});
		callbackContext.success();
		return true;
	    } else if (ACTION_SLIDER_IS_SLIDER_MENU_OPEN.equals(action)) {
		this.mCallbackCtx = callbackContext;
		cordova.getActivity().runOnUiThread(new Runnable() {
		    public void run() {
			mIsSliderMenuOpen = MobileCore.getSlider().isSliderMenuOpen();
			if (DEBUG) {
			    Log.d(TAG, "isSliderMenuOpen=" + mIsSliderMenuOpen);
			}
			PluginResult result = new PluginResult(PluginResult.Status.OK, mIsSliderMenuOpen);
			result.setKeepCallback(false);
			mCallbackCtx.sendPluginResult(result);
		    }
		});
		return true;
	    }
	    callbackContext.error("Invalid action");
	    return false;
	} catch (Exception e) {
	    System.err.println("Exception: " + e.getMessage());
	    callbackContext.error(e.getMessage());
	    return false;
	}
    }

    @Override
    public void onDestroy() {
	if (isReceiverRegistered) {
	    cordova.getActivity().unregisterReceiver(mSliderReceiver);
	    isReceiverRegistered = false;
	}
	super.onDestroy();
    }

    @Override
    public void onConfirmation(TYPE type) {
	String callbackString = type.toString();
	if (DEBUG) {
	    Log.d(TAG, "onConfirmation | callbackString=" + callbackString);
	}
	PluginResult result = new PluginResult(PluginResult.Status.OK, callbackString);
	result.setKeepCallback(true);
	mCallbackCtx.sendPluginResult(result);
    }

    public void onOfferwallReady() {
	if (DEBUG) {
	    Log.d(TAG, "onOfferWallReady");
	}
	PluginResult result = new PluginResult(PluginResult.Status.OK, "onOfferwallReady");
	result.setKeepCallback(true);
	mOfferwallReadyCtx.sendPluginResult(result);
    }

    public void onStickeezReady() {
	if (DEBUG) {
	    Log.d(TAG, "onStickeezReady");
	}
	PluginResult result = new PluginResult(PluginResult.Status.OK, "onStickeezReady");
	result.setKeepCallback(true);
	mOfferwallReadyCtx.sendPluginResult(result);
    }

    @Override
    public void onReady(AD_UNITS adUnit) {
	if (adUnit == AD_UNITS.OFFERWALL) {
	    onOfferwallReady();
	} else if (adUnit == AD_UNITS.STICKEEZ) {
	    onStickeezReady();
	}
    }

}
