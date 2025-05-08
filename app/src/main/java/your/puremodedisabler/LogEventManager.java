package your.puremodedisabler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;

public class LogEventManager {
    private static LogEventManager instance;
    private final MutableLiveData<LinkedList<String>> logBuffer = new MutableLiveData<>();

    public static LogEventManager getInstance() {
        if (instance == null) {
            instance = new LogEventManager();
        }
        return instance;
    }

    public LiveData<LinkedList<String>> getLogBuffer() {
        return logBuffer;
    }

    public void postLog(LinkedList<String> logs) {
        logBuffer.setValue(logs);
    }
}
