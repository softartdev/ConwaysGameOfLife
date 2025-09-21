export PACKAGE_NAME=com.softartdev.conwaysgameoflife
export PERM_NAME=android.permission.POST_NOTIFICATIONS
# App is newly installed on a device that runs Android 13 or higher:
adb shell pm revoke $PACKAGE_NAME $PERM_NAME
adb shell pm clear-permission-flags $PACKAGE_NAME $PERM_NAME user-set
adb shell pm clear-permission-flags $PACKAGE_NAME $PERM_NAME user-fixed
# Print the current permission status
adb shell dumpsys package $PACKAGE_NAME | grep $PERM_NAME:
