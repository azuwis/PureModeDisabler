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
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsMonitorService extends Service {
    private static final String TAG = "PureModeDisabler";
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
        Log.d(TAG, message);
        // 直接通过单例发送日志
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String logEntry = sdf.format(new Date()) + " - " + message;
        LogEventManager.getInstance().postLog(logEntry);
    }

    private void startMonitoring() {
        mSettingsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                sendLog("Check onChange");
                checkAndDisablePureMode();
            }
        };

        getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(PURE_MODE_SETTING),
                false,
                mSettingsObserver
        );

        sendLog("Check startMonitoring");
        checkAndDisablePureMode();
    }

    private void checkAndDisablePureMode() {
        try {
            int currentState = Settings.Secure.getInt(getContentResolver(), PURE_MODE_SETTING);
            if (currentState != 1) {
                Settings.Secure.putInt(getContentResolver(), PURE_MODE_SETTING, 1);
                sendLog("Pure mode disabled");
            }
        } catch (Settings.SettingNotFoundException e) {
            sendLog("Pure mode setting not found: " + e);
        } catch (SecurityException e) {
            sendLog("Missing WRITE_SECURE_SETTINGS permission: " + e);
            sendLog("Setup adb and run: adb shell pm grant your.puremodedisabler android.permission.WRITE_SECURE_SETTINGS");
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
        sendLog("Check onStartCommand");
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
