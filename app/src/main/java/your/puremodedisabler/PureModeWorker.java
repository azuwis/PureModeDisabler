package your.puremodedisabler;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

public class PureModeWorker extends JobService {
    private static final int JOB_ID = 1;

    @Override
    public boolean onStartJob(JobParameters params) {
        PureModeHelper.checkAndDisablePureMode(getContentResolver(), "onSchedule");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static void schedule(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler.getPendingJob(JOB_ID) != null) {
            return;
        }
        JobInfo job = new JobInfo.Builder(JOB_ID,
                new ComponentName(context, PureModeWorker.class))
                .setPeriodic(15 * 60 * 1000)
                .setPersisted(true)
                .build();
        scheduler.schedule(job);
    }
}
