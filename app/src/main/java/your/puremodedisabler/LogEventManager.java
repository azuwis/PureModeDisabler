package your.puremodedisabler;

import android.util.Log;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LogEventManager {
    private static final String TAG = "PureModeDisabler";
    private static final int MAX_LOG_LINES = 35;
    private static final LogEventManager instance = new LogEventManager();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final LinkedList<String> logBuffer = new LinkedList<>();
    private Listener listener;

    public interface Listener {
        void onLogChanged(List<String> logs);
    }

    public static LogEventManager getInstance() {
        return instance;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public List<String> getLogs() {
        return new ArrayList<>(logBuffer);
    }

    public void postLog(String message) {
        Log.d(TAG, message);
        String logEntry = LocalTime.now().format(TIME_FORMAT) + " - " + message;
        if (logBuffer.size() >= MAX_LOG_LINES) {
            logBuffer.removeFirst();
        }
        logBuffer.add(logEntry);
        if (listener != null) {
            listener.onLogChanged(new ArrayList<>(logBuffer));
        }
    }
}
