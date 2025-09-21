export PACKAGE_NAME=com.softartdev.conwaysgameoflife
export PERM_NAME=android.permission.POST_NOTIFICATIONS
# The user keeps notifications enabled when the app is installed on a device that runs 12L or lower, then the device upgrades to Android 13 or higher:
adb shell pm grant $PACKAGE_NAME $PERM_NAME
adb shell pm set-permission-flags $PACKAGE_NAME $PERM_NAME user-set
adb shell pm clear-permission-flags $PACKAGE_NAME $PERM_NAME user-fixed
# Print the current permission status
adb shell dumpsys package $PACKAGE_NAME | grep $PERM_NAME:
