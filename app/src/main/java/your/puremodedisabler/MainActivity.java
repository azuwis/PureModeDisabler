package your.puremodedisabler;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends ComponentActivity {
    private static final String PURE_MODE_SETTING = "pure_mode_state";

    private TextView logTextView;
    private LinkedList<String> logBuffer;
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

        LogEventManager.getInstance().getLogLiveData()
            .observe(this, this::updateLog);

        SettingsMonitorService.startService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLogDisplay();
    }

    private void setupLogDisplay() {
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        logTextView.setVerticalScrollBarEnabled(true);
    }

    // 添加新日志条目（线程安全）
    private synchronized void updateLog(final LinkedList<String> logs) {
        logBuffer = logs;
        runOnUiThread(() -> {
            updateLogDisplay();
        });
    }

    // 更新日志显示
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

        // 自动滚动到底部
        // final int scrollAmount = logTextView.getLayout() != null ?
        //         logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight() : 0;
        // if (scrollAmount > 0) {
        //     logTextView.scrollTo(0, scrollAmount);
        // }
    }

    private int getPureModeState() {
        try {
            return Settings.Secure.getInt(getContentResolver(), PURE_MODE_SETTING);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return 0; // 默认值
        } catch (SecurityException e) {
            logTextView.setText(e.toString());
            return -1;
        }
    }

    private void updatePureModeStatus() {
        int state = getPureModeState();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        String date = sdf.format(new Date());
        pureModeStatus = date + " - " + ((state == 1) ? "Status: Disabled" : "Status: Enabled");
    }
}
