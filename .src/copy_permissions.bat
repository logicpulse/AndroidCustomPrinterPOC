@ECHO OFF
REM CLS
adb root remount
adb root push android.hardware.usb.host.xml /system/etc/permissions
adb root cat /system/etc/permissions/android.hardware.usb.host.xml
adb root chmod 644 /system/etc/permissions/android.hardware.usb.host.xml
PAUSE