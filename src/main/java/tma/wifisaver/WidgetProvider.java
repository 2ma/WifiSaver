package tma.wifisaver;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    public static final String CLICK_ACTION = "tma.wifisaverclick";
    private static int grey;
    private static int blue;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(CLICK_ACTION)) {
            Intent wifiIntent = new Intent(context, MainActivity.class);
            wifiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(wifiIntent);

        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        blue = context.getResources().getColor(R.color.clock_blue);
        grey = context.getResources().getColor(R.color.grey);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId, appWidgetManager);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void updateAppWidget(Context context, int appWidgetId, AppWidgetManager appWidgetManager) {
        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //set text color depending on state
        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF, 0);
        boolean running = mSharedPreferences.getBoolean(Constants.SHARED_TIMER_STATE, false);

        mRemoteViews.setTextColor(R.id.widget_timer_view, running ? blue : grey );

        ComponentName mComponentName = new ComponentName(context, WakupReceiver.class);
        mRemoteViews.setTextColor(R.id.widget_screen_view, context.getPackageManager().getComponentEnabledSetting(mComponentName)== PackageManager.COMPONENT_ENABLED_STATE_ENABLED ? blue : grey );

        Intent clickIntent = new Intent(context, WidgetProvider.class);
        clickIntent.setAction(WidgetProvider.CLICK_ACTION);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.widget, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }
}
