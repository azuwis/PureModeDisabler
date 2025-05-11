package your.puremodedisabler;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class LogEventManager {
    private static final String TAG = "PureModeDisabler";
    private static final int MAX_LOG_LINES = 35;
    private static LogEventManager instance;
    private final MutableLiveData<LinkedList<String>> logLiveData = new MutableLiveData<>();
    private final LinkedList<String> logBuffer = new LinkedList<>();

    public static LogEventManager getInstance() {
        if (instance == null) {
            instance = new LogEventManager();
        }
        return instance;
    }

    public LiveData<LinkedList<String>> getLogLiveData() {
        return logLiveData;
    }

    public void postLog(String message) {
        Log.d(TAG, message);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        String logEntry = sdf.format(new Date()) + " - " + message;
        if (logBuffer.size() >= MAX_LOG_LINES) {
            logBuffer.removeFirst();
        }
        logBuffer.add(logEntry);
        logLiveData.setValue(logBuffer);
    }
}
