# Manual energy consumption tests

## Introduction

There are 2 important things about testing energy consumption.

First is repeatability. It means whether we can get the same results when testing the same scenario under the same conditions. If we can't, there is no way to compare if a new version of the app consumes less energy.

The second one is reproducibility. It measures ability to replicate results in different scenario or different devices to confirm some findings. For instance, we could use different devices and run different tests to observe less energy consumption between 2 versions of the app.

Having the 2 rules in mind, it's important to design the tests / experiment in such way that the energy consumption measurements can be both repeatable and reproducible. This way we will be able to understand if a new version of Status uses less energy or not.


## Types of tests for Status

### Before you start
As mentioned in the introduction, it's very important to measure only stats related to Status app. That's why, please clear device logs before each test session and dump them right afterwards.

E.g. for Android use `adb shell dumpsys batterystats --reset` to reset the battery logs. Then unplug the device from power. When last test step is done run `adb bugreport` to save the results. See more in the how to use the tools guide.

### 1. Release testing session (30 min)

#### Prerequisites

- 1 device under test
- Another device to communicate with the 1st device
- QR code of the special contact used in group chat test step:
<img src="https://user-images.githubusercontent.com/7532782/37644817-3d953cce-2c25-11e8-91c9-a90da0e47c09.png" width="200">

- ‼️ Make sure TestFairy is disabled during the test execution, it can significantly interfere with the test results.
- App is installed on both devices ready to be opened for the first time
- Device screen is on and the app is always in the foreground unless test steps says differently
- Use [timer](http://www.online-timers.com/timer-30-minutes) while performing the test steps. Try to stick to a duration of each step. When you finished before time, keep the app in the foreground


#### Test steps

|No|Step|Duration in minutes|Time left in minutes|Details|
|---|-----|----|---|---|
|1|Open the app|0|30|Open the app for the first time after installation|
|2|Create new account on both devices|2|28|Allow to share usage data|
|3|Use faucet to request ETH|1|27|Request ETH on both devices. It will be used in the next steps|
|4|Create 1-1 chat|1|26|Open app on the 2nd device and scan its contact QR code. Click "Add contact" on 2nd device|
|5|Exchange 50 messages|5|21|Send 25 messages and receive another 25|
|6|Put app into background, turn off device screen and receive 20 notifications|3|18|Go to background, turn off the device screen and receive 20 messages in the last chat|
|7|Open chat from the latest notification and request ETH|2|16|Request small amount of ETH like 0.00001. Send ETH from another device and wait until ETH is received|
|8|Send ETH from Wallet|2|14|Go to Wallet and send 0.0001 ETH to 2nd device. Then wait until ETH is received on the 2nd device|  
|9|Join public chat|1|13|Join public chat like #status-performance on both devices|
|10|Exchange 50 messages|5|8|Send 25 messages and receive another 25|
|11|Add new contact by scanning QR code|0.5|7.5|Scan QR code of the special contact (see Prerequisites section)|
|12|Create group chat|0.5|7|Create a group chat with 2nd device and the special contact that was just added|
|13|Exchange 50 messages in group chat|5|2|Send 25 messages and then receive another 25 sending them from the 2nd device|
|14|Put app into background and do nothing|2|0||
