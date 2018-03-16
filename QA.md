# Manual energy consumption tests

## Introduction

There are 2 important things about testing energy consumption.

First is repeatability. It means whether we can get the same results when testing the same scenario under the same conditions. If we can't, there is no way to compare if a new version of the app consumes less energy.

The second one is reproducibility. It measures ability to replicate results in different scenario or different devices to confirm some findings. For instance, we could use different devices and run different tests to observe less energy consumption between 2 versions of the app.

Having the 2 rules in mind, it's important to design the tests / experiment in such way that the energy consumption measurements can be both repeatable and reproducible. This way we will be able to understand if a new version of Status uses less energy or not.


## Types of tests for Status

### Before you start
As mentioned in the introduction, it's very important to measure only stats related to Status app. That's why, please clear device logs before each test session and dump them right afterwards.

E.g. for Android use `adb shell dumpsys batterystats --reset` to reset the battery logs and when last test step is done run `adb bugreport` to save the results. See more in the how to use the tools guide.

### 1. Release testing session (30 min)

#### Prerequisites

- 1 device under test
- Another device to communicate with the 1st device
- QR code of a special contact used in group chat test step:
<img src="https://user-images.githubusercontent.com/7532782/37519339-04a31648-2919-11e8-95f1-e66e236c8c7f.png" width="200">

#### Test steps

| No | Step | Time in minutes | Details |
|---|-----|----|---|
|1|Open the app|0|Open the app for the first time after installation|
|2|Create new account|1|Share usage data|
|3|Create 1-1 chat|1|Scan QR code of another device|
|4|Exchange 100 messages|5|Send 50 messages and receive another 50|
|4|Start a new 1-1 chat and exchange 50 messages|3|Send 25 messages and receive another 25|
|5|Go to background and receive 20 notifications|5|Receive 20 messages for the latest chat while staying in the background|
|5|Open chat from the latest notification and request ETH|2|Send ETH from another device and wait until ETH is received|
|6|Send ETH|2|Wait until ETH is received on another device|  
|7|Join public chat|1|Join public chat like #status-performance on both devices|
|8|Exchange 100 messages|5|Send 50 messages and receive another 50|
|9|Add new contact by scanning QR code|1|Scan QR code of the special contact. See Prerequisites section|
|9|Create group chat|1|Create a group chat with 2nd device and the special contact|
|10|Exchange 50 messages in group chat|3|Send 50 messages and then receive another 50 sending them from the 2nd device|