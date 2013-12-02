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
    private final static int DISABLE = 0;
    private final static int ENABLE = 1;
    private ComponentName componentName;
    private PackageManager packageManager;

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
        componentName = new ComponentName(mContext, WakupReceiver.class);
        packageManager = mContext.getPackageManager();
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
        screenButtonState();
        //set timer state
        timerButtonState();

        setTimerDigits();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateWidget();
    }



    private void updateWidget() {
        Intent widgetUpdateIntent = new Intent(this, WidgetProvider.class);
        widgetUpdateIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, WidgetProvider.class));
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetUpdateIntent);
    }

    private void screenButtonState() {
        if (isWakeupEnabled()) {
            screenSwitchText.setTextColor(blue);
            screenImage.setImageResource(R.drawable.screen_mini);
        } else {
            screenImage.setImageResource(R.drawable.screen_mini_ns);
            screenSwitchText.setTextColor(grey);
        }
    }


    private void timerButtonState(){
        if(timerState){
            timerSwitchText.setTextColor(blue);
            timerImage.setImageResource(R.drawable.timer_mini);
            timeText.setTextColor(blue);
        }else{
            timerImage.setImageResource(R.drawable.timer_mini_ns);
            timerSwitchText.setTextColor(grey);
            timeText.setTextColor(grey);
        }
    }

    private void setTimerDigits(){
        if (hour != 0 || min != 0) {
            timeText.setText((hour == 0 ? "00" : hour) + ":" + (min < 10 ? "0" + Integer.toString(min) : min ));
        } else {
            timeText.setText(getResources().getString(R.string.time_text_string));
        }
    }

    //check wake state
    private boolean isWakeupEnabled() {

        return packageManager.getComponentEnabledSetting(componentName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    //launch time picker to set time for timer
    public void getTime(View v) {
        NumberPickerFragment numberPickerFragment = new NumberPickerFragment();
        numberPickerFragment.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        getFragmentManager().beginTransaction().add(numberPickerFragment,"numberPicker").commit();
    }

    //screen button
    public void screenButtonClick(View v) {
        if (isWakeupEnabled()) {
            //disable wakeup receiver
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            //if timer is off,cancel the alarm
            if (!timerState) {
                changeAlarm(DISABLE);
            }
            //set button state
            screenImage.setImageResource(R.drawable.screen_mini_ns);
            screenSwitchText.setTextColor(grey);
        } else {
            //enable wakeup receiver
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            //set alarm
            changeAlarm(ENABLE);
            //set button state
            screenSwitchText.setTextColor(blue);
            screenImage.setImageResource(R.drawable.screen_mini);
        }
    }

    //timer button
    public void timerButtonClick(View v) {
        if (timerState) {
            //if timer is on,disable
            changeTimer(DISABLE);
        } else {
            //if time is entered enable
            if (hour != 0 || min != 0) {
                changeTimer(ENABLE);
            } else {
                getTime(null);
            }
        }
    }

    // disable/enable timer
    private void changeTimer(int state){
        SharedPreferences.Editor edit = mContext.getSharedPreferences(Constants.SHARED_PREF,0).edit();
        if(state==DISABLE){
            timerState=false;
            //set button state
            timerImage.setImageResource(R.drawable.timer_mini_ns);
            timerSwitchText.setTextColor(grey);
            timeText.setTextColor(grey);
            //disable alarm if wakeup is disabled
            if (!isWakeupEnabled()) {
                changeAlarm(DISABLE);
            }
        }else{
            timerState=true;
            edit.putInt(Constants.SHARED_HOUR, hour);
            edit.putInt(Constants.SHARED_MINUTE, min);
            //set button state
            timerSwitchText.setTextColor(blue);
            timerImage.setImageResource(R.drawable.timer_mini);
            timeText.setTextColor(blue);
            //set alarm
            changeAlarm(ENABLE);
        }
        edit.putBoolean(Constants.SHARED_TIMER_STATE, timerState);
        edit.commit();
    }

    // set/cancel alarm for broadcast
    private void changeAlarm(int state){
        PendingIntent pendingIntent;
        Intent intent = new Intent(mContext, WifiSwitchReceiver.class);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if(state==DISABLE){
            pendingIntent = PendingIntent.getBroadcast(mContext, Constants.REQUEST_CODE, intent,
                    PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }else{
            pendingIntent = PendingIntent.getBroadcast(mContext, Constants.REQUEST_CODE, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.BROADCAST_TIME, pendingIntent);
        }
    }

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        hour = hourOfDay;
        min = minute;
        setTimerDigits();
        changeTimer(ENABLE);
    }
}
