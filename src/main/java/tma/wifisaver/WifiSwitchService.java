package tma.wifisaver;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class WifiSwitchService extends IntentService {


    //service to turn wifi on,wait 30s,turn wifi off(if screen is off),set alarm

    public WifiSwitchService() {
        super("WifiSwitchService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        WifiManager mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        PowerManager mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock mWakeLock=null;

        try {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiSaverWakeLock");
            mWakeLock.acquire();
            WifiManager.WifiLock mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WifiSaverWifiLock");
            mWifiManager.setWifiEnabled(true);
            mWifiLock.acquire();
            SystemClock.sleep(30000);
            mWifiLock.release();
            if (!mPowerManager.isScreenOn()) {
                mWifiManager.setWifiEnabled(false);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            Context context = getApplicationContext();
            AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, 0);
            int hour = mSharedPreferences.getInt(Constants.SHARED_HOUR, 0);
            int min = mSharedPreferences.getInt(Constants.SHARED_MINUTE, 2);
            int wakeupTime = (hour * 60 + min) * 60000;

            Intent mIntent = new Intent(context, WifiSwitchReceiver.class);
            PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, Constants.REQUEST_CODE, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + wakeupTime, mPendingIntent);
            Log.i("WifiSaver","service off, 2 min broadcast");
            if (mWakeLock!=null)mWakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
