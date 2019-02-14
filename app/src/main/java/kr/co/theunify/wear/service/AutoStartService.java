package kr.co.theunify.wear.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import kr.co.theunify.wear.utils.UPref;

public class AutoStartService extends BroadcastReceiver {
    private final String TAG = "[" + AutoStartService.class.getSimpleName() + "]";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "onReceive...");

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            String logstr = "";
            SharedPreferences pref1 = PreferenceManager.getDefaultSharedPreferences(context);
            logstr = "Default=" + pref1.toString();

            boolean bAutoStart1 = pref1.getBoolean("pref_key_start_on_boot", true);
            logstr = logstr + ", Default=" + bAutoStart1 ;

            Toast.makeText(context, logstr, Toast.LENGTH_LONG);

            Log.e(TAG, "BOOT_COMPLETED, AutoStart1=" + bAutoStart1);

            if(bAutoStart1) {
                Log.e(TAG, "Start SensorService...");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, SensorService.class));
                } else {
                    context.startService(new Intent(context, SensorService.class));
                }
            }
        } else if(intent.getAction().equals("kr.co.theunify.wear.service.SEND_BROAD_CAST")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, SensorService.class));
            } else {
                context.startService(new Intent(context, SensorService.class));
            }
        }
    }
}
