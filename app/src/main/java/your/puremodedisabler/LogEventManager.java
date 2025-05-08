package your.puremodedisabler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;

public class LogEventManager {
    private static final int MAX_LOG_LINES = 50;
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
        if (logBuffer.size() >= MAX_LOG_LINES) {
            logBuffer.removeFirst();
        }
        logBuffer.add(message);
        logLiveData.setValue(logBuffer);
    }
}
