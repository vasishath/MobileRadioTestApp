package com.vasishath.mobileradiotest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.vasishath.mobileradiotest.util.MyLog;

public class StartSyncService extends Service {

	/**
	 * Log tag
	 */
	private static final String TAG = "StartSyncService";

	/**
	 * Action to sync accounts in the background, applying all the usual logic
	 */
	public static final String ACTION_SYNC = "com.vasishath.mobileradiotest.ACTION_SYNC";

	public static void submitAccountSync(Context context) {
		final LockManager lm = LockManager.get(context);
		lm.acquireSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);

		final Intent serviceIntent = new Intent(ACTION_SYNC);
		serviceIntent.setClass(context, StartSyncService.class);

		if (context.startService(serviceIntent) == null) {
			// Failure
			lm.releaseSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);
		}
	}

	@Override
	public void onCreate() {
		MyLog.i(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*
		 * Do something useful based on the action
		 */
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(ACTION_SYNC)) {
					/*
					 * Sync request
					 */
					final TaskExecutor executor = TaskExecutor.get(this);
					final Task task = new Task(this, startId);
					executor.submit(task);

					final LockManager lm = LockManager.get(this);
					lm.releaseSpecialFlag(LockManager.LOCK_FLAG_STARTING_SYNC);
				}
			}
		}

		return START_NOT_STICKY;
	}
}
