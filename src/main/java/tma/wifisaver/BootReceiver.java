package tma.wifisaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(context, WifiSwitchReceiver.class);

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, Constants.REQUEST_CODE, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, mPendingIntent);
        Log.i("WifiSaver","bootCompleted,broadcast 2 minute");
    }

}
