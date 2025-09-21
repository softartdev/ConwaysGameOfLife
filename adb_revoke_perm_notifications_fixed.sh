export PACKAGE_NAME=com.softartdev.conwaysgameoflife
export PERM_NAME=android.permission.POST_NOTIFICATIONS
# The user manually disables notifications:
adb shell pm revoke $PACKAGE_NAME $PERM_NAME
adb shell pm set-permission-flags $PACKAGE_NAME $PERM_NAME user-set
adb shell pm set-permission-flags $PACKAGE_NAME $PERM_NAME user-fixed
# Print the current permission status
adb shell dumpsys package $PACKAGE_NAME | grep $PERM_NAME:
