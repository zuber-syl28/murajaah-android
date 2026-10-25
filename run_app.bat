@echo off
set JAVA_HOME=C:\Users\szube\.jdks\jdk-21.0.12.1+1
cd /d "W:\Firstmate01\Projects\murajaah-android"
echo Installing & Launching Murajaah on Emulator...
call gradlew.bat installDebug
"C:\Users\szube\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n com.sylergy.murajaah/.MainActivity
echo Murajaah is running!
pause
