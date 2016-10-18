with the root permissions, copy the file

android.hardware.usb.host.xml

into the folder

/system/etc/permission/

wth command

adb push "android.hardware.usb.host.xml" /system/etc/permission

and reboot