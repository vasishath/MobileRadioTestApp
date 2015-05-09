package com.vasishath.mobileradiotest;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.vasishath.mobileradiotest.util.MyLog;

public class App extends Application {

	private static final String TAG = "App";

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			final String packageName = getPackageName();
			final PackageManager pm = getPackageManager();
			final PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);

			MyLog.setPackageInfo(packageName);
			MyLog.setDebugSettings(true);
		} catch (Exception x) {
			Log.e(TAG, "Error initializing logging", x);
		}
	}
}
