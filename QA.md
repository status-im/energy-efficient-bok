# Manual energy consumption tests

There are 2 important things about testing energy consumption.

First is repeatability. It means whether we can get the same results when testing the same scenario under the same conditions. If we can't, there is no way to compare if a new version of the app consumes less energy.

The second one is reproducibility. It measures ability to replicate results in different scenario or different devices to confirm some findings. For instance, we could use different devices and run different tests to observe less energy consumption between 2 versions of the app.

Having the 2 rules in mind, it's important to design the tests / experiment in such way that the energy consumption measurements can be both repeatable and reproducible. This way we will be able to understand if a new version of Status uses less energy or not.


## Types of tests for Status

### 30 min release testing session

#### Setup
- Set screen brightness to 30%
- Use WiFi
- describe other conditions..

| No | Step | Time in minutes | Details |
|---|-----|----|---|
|1|Open the app|0|App was just installed|
|2|Create new account|1|Share usage data|
|3|Create 1-1 chat|1|Scan QR code|
|4|Exchange 100 messages|5|Send 50 messages and receive another 50|
|5|Go to background and receive 20 notifications|5|Receive 20 messages while staying in the background|
|5|Request and receive ETH|1||
|6|Send ETH|1||  
|7|..|..|..|