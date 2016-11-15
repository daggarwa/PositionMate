Installing PositionMate on phone(Android version 2.2 or higher):

1.Find the PositionMate.apk file under PositionMate_Source_and_Binaries\PositionMate\bin\ folder.
2.Copy the .apk file to your Android’s memory card and insert the card into your phone.
3.The Apps Installer will display the apk files on the memory card.
4.Click and install your apk files on memory card or phone.


Installing Applications With Android SDK
 
1.Download and install the Google Android SDK program and the Android USB drivers. The download links are as follows: http://code.google.com/android/intro/installing.html
http://dl.google.com/android/android_usb_windows.zip
2.You need to modify your Android’s settings to allow the installation of applications from other sources. Under “Settings,” select “Application Settings” and then enable “Unknown Sources.” Also under “Settings,” select “SD Card” and “Phone Storage,” and finally enable “Disable Use for USB Storage”
3.This last step is easy. Open Command Prompt and type the following: adb install <1>/<2>.apk
4.However, when you type the command, replace <1> with the path to your APK file and replace <2> with the name of the APK file.

You’re done! Your application is now ready for your use.