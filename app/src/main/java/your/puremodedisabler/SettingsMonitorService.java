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
    private static final String PURE_MODE_STATE = "pure_mode_state";
    private static final String APP_CHECK_RISK = "app_check_risk";

    private ContentObserver mSettingsObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        startMonitoring();
    }

    private void sendLog(String message) {
        LogEventManager.getInstance().postLog(message);
    }

    private void startMonitoring() {
        mSettingsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                sendLog("check: onChange");
                checkAndDisablePureMode();
            }
        };

        getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(PURE_MODE_STATE),
                false,
                mSettingsObserver
        );

        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(APP_CHECK_RISK),
                false,
                mSettingsObserver
        );

        sendLog("check: onCreate");
        checkAndDisablePureMode();
    }

    private void checkAndDisablePureMode() {
        try {
            int currentState = Settings.Secure.getInt(getContentResolver(), PURE_MODE_STATE);
            if (currentState != 1) {
                sendLog("action: Disabling pure mode");
                Settings.Secure.putInt(getContentResolver(), PURE_MODE_STATE, 1);
            }
        } catch (Settings.SettingNotFoundException e) {
            sendLog("info: Pure mode setting not found: " + e);
        } catch (SecurityException e) {
            sendLog("info: Missing WRITE_SECURE_SETTINGS permission: " + e);
            sendLog("info: Setup adb and run: adb shell pm grant your.puremodedisabler android.permission.WRITE_SECURE_SETTINGS");
        }

        try {
            int currentState = Settings.Global.getInt(getContentResolver(), APP_CHECK_RISK);
            if (currentState != 0) {
                sendLog("action: Disabling app check risk");
                Settings.Global.putInt(getContentResolver(), APP_CHECK_RISK, 0);
            }
        } catch (Settings.SettingNotFoundException e) {
            sendLog("info: App check risk setting not found: " + e);
        } catch (SecurityException e) {
            sendLog("info: Missing WRITE_SECURE_SETTINGS permission: " + e);
            sendLog("info: Setup adb and run: adb shell pm grant your.puremodedisabler android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, SettingsMonitorService.class);
        context.startService(serviceIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次被唤醒时重新检查状态
        sendLog("check: onStartCommand");
        checkAndDisablePureMode();
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
