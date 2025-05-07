package your.puremodedisabler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LogEventManager {
    private static LogEventManager instance;
    private final MutableLiveData<String> logLiveData = new MutableLiveData<>();

    public static LogEventManager getInstance() {
        if (instance == null) {
            instance = new LogEventManager();
        }
        return instance;
    }

    public LiveData<String> getLogLiveData() {
        return logLiveData;
    }

    public void postLog(String message) {
        logLiveData.postValue(message);
    }
}
