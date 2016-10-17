package com.logicpulse.logicpulsecustomprinter.App;

import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

import com.logicpulse.logicpulsecustomprinter.MainActivity;
import com.logicpulse.logicpulsecustomprinter.R;

/**
 * Created by mario on 05/02/2015.
 */
public class Singleton extends Application {

  //Constants
  public static String TAG;
  //Singleton
  private static Singleton ourInstance = new Singleton();
  //Activity
  private static MainActivity mainActivity;

  // Interaction with the DevicePolicyManager
  private ComponentName deviceAdmin;
  private Boolean deviceAdminActive;
  private DevicePolicyManager devicePolicyManager;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  public static Singleton getInstance() {
    return ourInstance;
  }

  public static MainActivity getMainActivity() {
    return mainActivity;
  }

  public static void setMainActivity(MainActivity mainActivity) {
    Singleton.mainActivity = mainActivity;
  }

  public static String getTAG() {
    return TAG;
  }

  public static void setTAG(String TAG) {
    Singleton.TAG = TAG;
  }

  public ComponentName getDeviceAdmin() {
    return deviceAdmin;
  }

  public void setDeviceAdmin(ComponentName deviceAdmin) {
    this.deviceAdmin = deviceAdmin;
  }

  public Boolean getDeviceAdminActive() {
    return deviceAdminActive;
  }

  public void setDeviceAdminActive(Boolean deviceAdminActive) {
    this.deviceAdminActive = deviceAdminActive;
  }

  public DevicePolicyManager getDevicePolicyManager() {
    return devicePolicyManager;
  }

  public void setDevicePolicyManager(DevicePolicyManager devicePolicyManager) {
    this.devicePolicyManager = devicePolicyManager;
  }
}