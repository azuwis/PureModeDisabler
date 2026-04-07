package your.puremodedisabler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.provider.Settings;
import android.view.WindowInsetsController;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainActivity extends Activity {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private TextView logTextView;
    private List<String> logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getInsetsController().setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);

        logTextView = findViewById(R.id.logTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        logTextView.setVerticalScrollBarEnabled(true);

        findViewById(R.id.btnDisablePureMode).setOnClickListener(v -> {
            SettingsMonitorService.startService(this);
        });

        requestDisableBatteryOptimization();

        LogEventManager.getInstance().setListener(logs -> {
            logBuffer = logs;
            runOnUiThread(this::updateLogDisplay);
        });
        logBuffer = LogEventManager.getInstance().getLogs();
        updateLogDisplay();

        PureModeWorker.schedule(this);
        SettingsMonitorService.startService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogEventManager.getInstance().setListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLogDisplay();
    }

    private void requestDisableBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private String getPureModeStatus() {
        int state;
        try {
            state = Settings.Secure.getInt(getContentResolver(), PureModeHelper.PURE_MODE_STATE);
        } catch (Settings.SettingNotFoundException e) {
            LogEventManager.getInstance().postLog("info: Pure mode setting not found: " + e);
            state = 0;
        } catch (SecurityException e) {
            LogEventManager.getInstance().postLog("error: " + e);
            state = -1;
        }
        String date = LocalTime.now().format(TIME_FORMAT);
        return date + " - status: Pure mode " + ((state == 1) ? "disabled" : "enabled");
    }

    private void updateLogDisplay() {
        StringBuilder sb = new StringBuilder();
        if (logBuffer != null) {
            for (String entry : logBuffer) {
                sb.append(entry).append("\n");
            }
        }
        sb.append(getPureModeStatus()).append("\n");
        logTextView.setText(sb.toString());
    }
}
