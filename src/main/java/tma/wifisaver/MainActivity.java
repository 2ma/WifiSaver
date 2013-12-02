package tma.wifisaver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements NumberPickerFragment.NumberPickerDialogHandler {

    private Context mContext;
    private boolean timerState;
    private TextView timerSwitchText, screenSwitchText, timeText;
    private ImageView timerImage, screenImage;
    private int blue, grey,hour, min;
    private final static int ALARM_DISABLE = 0;
    private final static int ALARM_ENABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blue = getResources().getColor(R.color.clock_blue);
        grey = getResources().getColor(R.color.grey);
        timerSwitchText = (TextView) findViewById(R.id.timer_text);
        screenSwitchText = (TextView) findViewById(R.id.screen_text);
        timeText = (TextView) findViewById(R.id.time_text);
        timerImage = (ImageView) findViewById(R.id.timer_image);
        screenImage = (ImageView) findViewById(R.id.screen_image);
        mContext = getApplicationContext();


    }

    @Override
    protected void onResume() {
        super.onResume();

        // get settings
        SharedPreferences saveSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREF, 0);
        hour = saveSharedPreferences.getInt(Constants.SHARED_HOUR, 0);
        min = saveSharedPreferences.getInt(Constants.SHARED_MINUTE, 0);
        timerState = saveSharedPreferences.getBoolean(Constants.SHARED_TIMER_STATE, false);

        //set screen state
        screenSwitchState();

        //set timer state
        updateTimerState();


    }

    @Override
    protected void onPause() {
        super.onPause();
        updateWidget();
    }



    private void updateWidget() {
        Intent widgetUpdateIntent = new Intent(this, WidgetProvider.class);
        widgetUpdateIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetUpdateIntent);
    }




    private void updateTimerState(){
        if(timerState){
            timerSwitchText.setTextColor(blue);
            timerImage.setImageResource(R.drawable.timer_mini);
            timeText.setTextColor(blue);
        }else{
            timerImage.setImageResource(R.drawable.timer_mini_ns);
            timerSwitchText.setTextColor(grey);
            timeText.setTextColor(grey);
        }
        if (hour != 0 || min != 0) {
            timeText.setText((hour == 0 ? "00" : hour) + ":" + (min < 10 ? "0"+Integer.toString(min) :min ));
        } else {
            timeText.setText(getResources().getString(R.string.time_text_string));
        }
    }

    //check wake state
    private boolean isWakeupEnabled() {
        ComponentName mComponentName = new ComponentName(mContext, WakupReceiver.class);
        PackageManager mPackageManager = mContext.getPackageManager();
        return mPackageManager.getComponentEnabledSetting(mComponentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    //launch time picker to set time for timer
    public void getTime(View v) {
        NumberPickerFragment numberPickerFragment = new NumberPickerFragment();
        numberPickerFragment.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        getFragmentManager().beginTransaction().add(numberPickerFragment,"numberPicker").commit();
    }

    public void screenClick(View v) {
        setWakeup(isWakeupEnabled() ? ALARM_DISABLE : ALARM_ENABLE);
        screenSwitchState();
    }

    private void setWakeup(int state) {
        PackageManager mPackageManager = mContext.getPackageManager();
        ComponentName mComponentName = new ComponentName(mContext, WakupReceiver.class);
        if (state == ALARM_DISABLE) {
            mPackageManager.setComponentEnabledSetting(mComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            //if timer is not on,cancel the alarm
            if (!timerState) {
                cancelAlarm();
            }
        } else {
            mPackageManager.setComponentEnabledSetting(mComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            //start alarm if it's not on
            if (!timerState) {
                setAlarm();
            }
        }
    }

    private void screenSwitchState() {
        if (isWakeupEnabled()) {
            screenSwitchText.setTextColor(blue);
            screenImage.setImageResource(R.drawable.screen_mini);
        } else {
            screenImage.setImageResource(R.drawable.screen_mini_ns);
            screenSwitchText.setTextColor(grey);
        }
    }

    public void timerClick(View v) {
        if (timerState) {
            cancelTimer();
        } else {
            if (hour != 0 || min != 0) {
                setTimer();
            } else {
                getTime(null);
            }
        }
    }

    private void setTimer() {


        //save hour,min, and timer state
        SharedPreferences.Editor edit = mContext.getSharedPreferences(Constants.SHARED_PREF,0).edit();
        edit.putInt(Constants.SHARED_HOUR, hour);
        edit.putInt(Constants.SHARED_MINUTE, min);
        edit.putBoolean(Constants.SHARED_TIMER_STATE, true);
        edit.commit();

        timerState = true;

        updateTimerState();

        //set alarm
        setAlarm();


    }

    private void cancelTimer() {

        //save timer state
        timerState = false;
        SharedPreferences.Editor edit = mContext.getSharedPreferences(Constants.SHARED_PREF,0).edit();
        edit.putBoolean(Constants.SHARED_TIMER_STATE, false);
        edit.commit();
        updateTimerState();

        //if screen wake is on, the alarm shouldn't be canceled
        if (!isWakeupEnabled()) {
            cancelAlarm();
        }
    }

    //set alarm
    private void setAlarm() {
        PendingIntent mPendingIntent;
        Intent mIntent = new Intent(mContext, WifiSwitchReceiver.class);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(mContext, Constants.REQUEST_CODE, mIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, mPendingIntent);
    }

    //cancel alarm
    public void cancelAlarm() {
        PendingIntent mPendingIntent;
        Intent mIntent = new Intent(mContext, WifiSwitchReceiver.class);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mPendingIntent = PendingIntent.getBroadcast(mContext, Constants.REQUEST_CODE, mIntent,
                PendingIntent.FLAG_NO_CREATE);
        if (mPendingIntent != null) {
            alarmManager.cancel(mPendingIntent);
            mPendingIntent.cancel();
        }

    }

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        hour = hourOfDay;
        min = minute;
        setTimer();
    }
}
