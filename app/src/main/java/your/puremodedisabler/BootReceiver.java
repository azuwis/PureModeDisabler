package your.puremodedisabler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PureModeWorker.schedule(context);
        SettingsMonitorService.startService(context);
    }
}
