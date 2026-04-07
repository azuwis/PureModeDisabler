package your.puremodedisabler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PURE_MODE_STATE = "pure_mode_state";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private TextView logTextView;
    private List<String> logBuffer;
    private String pureModeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        logTextView = findViewById(R.id.logTextView);
        setupLogDisplay();

        findViewById(R.id.btnDisablePureMode).setOnClickListener(v -> {
            SettingsMonitorService.startService(this);
        });

        requestDisableBatteryOptimization();

        LogEventManager.getInstance().setListener(this::updateLog);
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
            startActivityForResult(intent, 1001);
        }
    }

    private void setupLogDisplay() {
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        logTextView.setVerticalScrollBarEnabled(true);
    }

    private void updateLog(final List<String> logs) {
        logBuffer = logs;
        runOnUiThread(this::updateLogDisplay);
    }

    private void updateLogDisplay() {
        updatePureModeStatus();
        StringBuilder sb = new StringBuilder();
        if (logBuffer != null) {
            for (String entry : logBuffer) {
                sb.append(entry).append("\n");
            }
        }
        if (pureModeStatus != null) {
            sb.append(pureModeStatus).append("\n");
        }
        logTextView.setText(sb.toString());
    }

    private int getPureModeState() {
        try {
            return Settings.Secure.getInt(getContentResolver(), PURE_MODE_STATE);
        } catch (Settings.SettingNotFoundException e) {
            LogEventManager.getInstance().postLog("info: Pure mode setting not found: " + e);
            return 0;
        } catch (SecurityException e) {
            LogEventManager.getInstance().postLog("error: " + e);
            return -1;
        }
    }

    private void updatePureModeStatus() {
        int state = getPureModeState();
        String date = LocalTime.now().format(TIME_FORMAT);
        pureModeStatus = date + " - status: Pure mode " + ((state == 1) ? "disabled" : "enabled");
    }
}
