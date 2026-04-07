package your.puremodedisabler;

import android.content.ContentResolver;
import android.provider.Settings;

public class PureModeHelper {
    static final String PURE_MODE_STATE = "pure_mode_state";
    static final String APP_CHECK_RISK = "app_check_risk";

    static void checkAndDisablePureMode(ContentResolver resolver) {
        try {
            int currentState = Settings.Secure.getInt(resolver, PURE_MODE_STATE);
            if (currentState != 1) {
                LogEventManager.getInstance().postLog("action: Disabling pure mode");
                Settings.Secure.putInt(resolver, PURE_MODE_STATE, 1);
            }
        } catch (Settings.SettingNotFoundException e) {
            LogEventManager.getInstance().postLog("info: Pure mode setting not found: " + e);
        } catch (SecurityException e) {
            logPermissionError(e);
        }

        try {
            int currentState = Settings.Global.getInt(resolver, APP_CHECK_RISK);
            if (currentState != 0) {
                LogEventManager.getInstance().postLog("action: Disabling app check risk");
                Settings.Global.putInt(resolver, APP_CHECK_RISK, 0);
            }
        } catch (Settings.SettingNotFoundException e) {
            LogEventManager.getInstance().postLog("info: App check risk setting not found: " + e);
        } catch (SecurityException e) {
            logPermissionError(e);
        }
    }

    private static void logPermissionError(SecurityException e) {
        LogEventManager.getInstance().postLog("info: Missing WRITE_SECURE_SETTINGS permission: " + e);
        LogEventManager.getInstance().postLog("info: Setup adb and run: adb shell pm grant your.puremodedisabler android.permission.WRITE_SECURE_SETTINGS");
    }
}
