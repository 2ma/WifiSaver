package tma.wifisaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

//receiver for handling boot

public class BootReceiver extends BroadcastReceiver {

    //after boot set an alarm if it was running before

    @Override
    public void onReceive(Context context, Intent intent) {

            boolean timerState = context.getSharedPreferences(Constants.SHARED_PREF,0).getBoolean(Constants.SHARED_TIMER_STATE,false);

            ComponentName componentName = new ComponentName(context, WakupReceiver.class);
            PackageManager packageManager = context.getPackageManager();
            boolean screenState = packageManager.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

            if(timerState || screenState){

                AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent mIntent = new Intent(context, WifiSwitchReceiver.class);
                PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, Constants.REQUEST_CODE, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, mPendingIntent);
                Log.i("WifiSaver","bootCompleted,broadcast 2 minute");
            }
    }

}
