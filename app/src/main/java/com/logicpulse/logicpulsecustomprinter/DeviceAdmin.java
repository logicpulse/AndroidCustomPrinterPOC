package com.logicpulse.logicpulsecustomprinter;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.logicpulse.logicpulsecustomprinter.App.Singleton;

/**
 * Created by mario.monteiro on 07/10/2016.
 *
 * Device Administration
 * https://developer.android.com/guide/topics/admin/device-admin.html
 *
 * DeviceAdminReceiver
 * https://developer.android.com/reference/android/app/admin/DeviceAdminReceiver.html
 */

public class DeviceAdmin extends DeviceAdminReceiver {

    private static Singleton mApp = Singleton.getInstance();

    void showToast(Context context, String msg) {
        String status = String.format("%s %s", context.getString(R.string.admin_receiver_status), msg);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled));
        mApp.getMainActivity().setAdminActive(true);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_receiver_status_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_disabled));
        mApp.getMainActivity().setAdminActive(false);
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
    }
}
