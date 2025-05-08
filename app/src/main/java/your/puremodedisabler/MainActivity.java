package your.puremodedisabler;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import java.util.LinkedList;

public class MainActivity extends ComponentActivity {
    private TextView logTextView;

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

    private void setupLogDisplay() {
        logTextView.setMovementMethod(new ScrollingMovementMethod());
        logTextView.setVerticalScrollBarEnabled(true);
    }

    // 添加新日志条目（线程安全）
    private synchronized void updateLog(final LinkedList<String> logBuffer) {
        runOnUiThread(() -> {
            updateLogDisplay(logBuffer);
        });
    }

    // 更新日志显示
    private void updateLogDisplay(LinkedList<String> logBuffer) {
        StringBuilder sb = new StringBuilder();
        for (String entry : logBuffer) {
            sb.append(entry).append("\n");
        }
        logTextView.setText(sb.toString());

        // 自动滚动到底部
        // final int scrollAmount = logTextView.getLayout() != null ?
        //         logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight() : 0;
        // if (scrollAmount > 0) {
        //     logTextView.scrollTo(0, scrollAmount);
        // }
    }
}
