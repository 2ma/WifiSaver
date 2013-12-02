package tma.wifisaver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiSwitchReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int callState = mTelephonyManager.getCallState();
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (mPowerManager.isScreenOn() || callState != TelephonyManager.CALL_STATE_IDLE) {
            //if screen is turned on or the phone is used, try again 2 minutes later
            AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent mIntent = new Intent(context, WifiSwitchReceiver.class);
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, Constants.REQUEST_CODE, mIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, mPendingIntent);
            Log.i("WifiSaver","broadcast 2 min");
          } else {
            SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, 0);
            boolean running = mSharedPreferences.getBoolean(Constants.SHARED_TIMER_STATE, false);
            //if timer is enabled,launch service to handle it
            if (running) {
                context.startService(new Intent(context, WifiSwitchService.class));
            } else {

                WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                mWifiManager.setWifiEnabled(false);

                Log.i("WifiSaver", "wifi off");
            }
        }








    }
}
