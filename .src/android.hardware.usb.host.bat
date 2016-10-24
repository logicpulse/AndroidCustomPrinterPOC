@ECHO OFF
CLS
adb root push android.hardware.usb.host.xml /system/etc/permissions
adb root remount
adb root cat /system/etc/permissions/android.hardware.usb.host.xml