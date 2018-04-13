# Testing Energy Consumption on Android

## Using Android Batterystats & Battery Historian

Batterystats is a tool included in the Android framework that collects battery data on your device. Battery Historian converts the report from Batterystats into an HTML visualization that you can view in your browser.

More info on [Batterystats & Battery Historian](https://developer.android.com/studio/profile/battery-historian.html).

### Prerequisites

- docker
- git
- [adb](https://developer.android.com/studio/command-line/adb.html) (included in [Android SDK](https://developer.android.com/studio/index.html) Platform-Tools package)
- real Android device to measure battery consumption

### Setup

1. Install and run Battery Historian:
```
docker run -p 9999:9999 bhaavan/battery-historian
```
2. Navigate to Battery Historian in your browser to confirm that it is running:
http://localhost:9999/

### Measure battery consumption

Now it's time to measure battery consumption while performing a test / experiment. To get an idea what kind of test you should perform, check [types of experiments](./QA.md)



1. First, make sure your device is recognized:
```
adb devices
```
2. Reset old battery data that has been gathered so far
```
adb shell dumpsys batterystats --reset
```
3. Disconnect your device from your computer so that you are only drawing current from the device's battery.
4. Perform actions for which you would like to test energy consumption.
5. Reconnect your phone.
6. Make sure your phone is recognized:
```
adb devices
```
7. Create a bugreport in current directory. The report contains logs the battery stats we are interested in
```
adb bugreport
```

### Analyse battery consumption

Open the bugreport in Battery Historian:
1. Navigate to http://localhost:9999/
2. Click **Browse** and select the bugreport file
3. Click **Submit**

#### Android Components actively drawing energy

Historian chart describes Android components like WiFi, Camera, etc. that were actively drawing energy during the test. It also shows how battery level has was changing (**Show line overlay**).

One of the most interesting components to check are SyncManager and JobScheduler. Their visualizations can make us aware if your app performs syncs and executes jobs more frequently than necessary.

#### App specific data

When you scroll down to **App Selection** and select Status (im.status.ethereum) you will see Status stats like:
- Device estimated power use (and power use due to CPU usage)
- Mobile data transfered
- Wakelocks time (how many times the app needed to wake up screen or CPU to complete some work which is a really costly activity)
- Time spent in foreground and background


----

Here is full description of how to analyse power usage with [Battery Historian](https://developer.android.com/topic/performance/power/battery-historian.html)
