package your.puremodedisabler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

public class SettingsMonitorService extends Service {
    private static final String PURE_MODE_SETTING = "pure_mode_state";

    private ContentObserver mSettingsObserver;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        startMonitoring();
        setupAlarm();
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
                Settings.Secure.getUriFor(PURE_MODE_SETTING),
                false,
                mSettingsObserver
        );

        sendLog("check: onCreate");
        checkAndDisablePureMode();
    }

    private void checkAndDisablePureMode() {
        try {
            int currentState = Settings.Secure.getInt(getContentResolver(), PURE_MODE_SETTING);
            if (currentState != 1) {
                sendLog("action: Disabling pure mode");
                Settings.Secure.putInt(getContentResolver(), PURE_MODE_SETTING, 1);
            }
        } catch (Settings.SettingNotFoundException e) {
            sendLog("info: Pure mode setting not found: " + e);
        } catch (SecurityException e) {
            sendLog("info: Missing WRITE_SECURE_SETTINGS permission: " + e);
            sendLog("info: Setup adb and run: adb shell pm grant your.puremodedisabler android.permission.WRITE_SECURE_SETTINGS");
        }
    }

    private void setupAlarm() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SettingsMonitorService.class);
        mAlarmPendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 使用不精确重复闹钟
        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                mAlarmPendingIntent
        );
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
        if (mAlarmManager != null && mAlarmPendingIntent != null) {
            mAlarmManager.cancel(mAlarmPendingIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
