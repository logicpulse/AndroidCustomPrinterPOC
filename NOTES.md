
---------------------------------------------------------------------------------------------------------------------

http://www.droidviews.com/push-pull-files-android-using-adb-commands/

adb shell
su
mount -o remount,rw /system
exit

adb push "android.hardware.usb.host.xml" /system/etc/permission

adb shell 
reboot

adb root

---------------------------------------------------------------------------------------------------------------------

add to libs and 

build.gradle

dependencies {
    compile files('libs/customandroidapi.jar')
}

and sync

---------------------------------------------------------------------------------------------------------------------

adb push C:\_Android\LogicPulseCustomPrinterPOC\app\build\outputs\apk\app-debug.apk /data/local/tmp/com.logicpulse.logicpulsecustomprinter
$ adb shell pm install -r "/data/local/tmp/com.logicpulse.logicpulsecustomprinter"

---------------------------------------------------------------------------------------------------------------------

cat /data/data/com.logicpulse.logicpulsecustomprinter/files/android.hardware.usb.host.xml
rm /data/data/com.logicpulse.logicpulsecustomprinter/files/android.hardware.usb.host.xml

rm /data/data/com.logicpulse.logicpulsecustomprinter/android.hardware.usb.host.xml
su -c mount -o remount,rw /system
su -c rm /system/etc/permissions/android.hardware.usb.host.xml
su -c chmod 644 /system/etc/permissions/android.hardware.usb.host.xml

ll /data/data/com.logicpulse.logicpulsecustomprinter/files/android.hardware.usb.host.xml
ll /system/etc/permissions/android.hardware.usb.host.xml

cp /data/data/com.logicpulse.logicpulsecustomprinter/files/android.hardware.usb.host.xml /system/etc/permissions/android.hardware.usb.host.xml

---------------------------------------------------------------------------------------------------------------------

@ECHO OFF
REM CLS
adb root remount
adb root push android.hardware.usb.host.xml /system/etc/permissions
adb root cat /system/etc/permissions/android.hardware.usb.host.xml
adb root chmod 644 /system/etc/permissions/android.hardware.usb.host.xml
PAUSE

adb push DemoCustomAndroidUSB.apk /data/local/tmp/
adb push USB-Device-Info---Android.apk

---------------------------------------------------------------------------------------------------------------------

//Sunday (Domingo)
//Monday (Segunda-feira)
//Tuesday (Terça-feira)
//Wednesday (Quarta-feira)
//Thursday (Quinta-feira)
//Friday (Sexta-feira)
//Saturday (Sábado)

---------------------------------------------------------------------------------------------------------------------

View Alarms

adb shell dumpsys alarm > dump.txt

---------------------------------------------------------------------------------------------------------------------
