package your.puremodedisabler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

public class SettingsMonitorService extends Service {
    private ContentObserver mSettingsObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        startMonitoring();
    }

    private void startMonitoring() {
        mSettingsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                LogEventManager.getInstance().postLog("check: onChange");
                PureModeHelper.checkAndDisablePureMode(getContentResolver());
            }
        };

        getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(PureModeHelper.PURE_MODE_STATE),
                false,
                mSettingsObserver
        );

        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(PureModeHelper.APP_CHECK_RISK),
                false,
                mSettingsObserver
        );

        LogEventManager.getInstance().postLog("check: onCreate");
        PureModeHelper.checkAndDisablePureMode(getContentResolver());
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, SettingsMonitorService.class);
        context.startService(serviceIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次被唤醒时重新检查状态
        LogEventManager.getInstance().postLog("check: onStartCommand");
        PureModeHelper.checkAndDisablePureMode(getContentResolver());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSettingsObserver != null) {
            getContentResolver().unregisterContentObserver(mSettingsObserver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
