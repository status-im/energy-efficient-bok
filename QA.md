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

### List of tests

Battery tests are listed in [TestRail](https://ethstatus.testrail.net/index.php?/suites/view/15&group_by=cases:section_id&group_id=526&group_order=asc) under Battery Consumption section.