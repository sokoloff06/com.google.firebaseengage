import time
import subprocess
#subprocess.check_output(['ls', '-l'])  # All that is technically needed...
count = 1
while True:
    print(count)
    count = count + 1
    subprocess.run("./gradlew connectedAndroidTest", shell=True)
    subprocess.run("adb uninstall com.google.firebaseengage", shell=True)