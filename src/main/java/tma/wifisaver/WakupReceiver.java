package tma.wifisaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WakupReceiver extends BroadcastReceiver {

    //receiver for handling the screen turning on (user present)

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        mWifiManager.setWifiEnabled(true);
        Intent mIntent = new Intent(context, WifiSwitchReceiver.class);

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, Constants.REQUEST_CODE, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, mPendingIntent);

        Log.i("WifiSaver", "wifi on, broadcast 2 min");


    }

}
