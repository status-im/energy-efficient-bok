# status-go-n-wrapper
Native simple wrapper for status-go for iOS and Android.

Used for testing energy efficiency of status-go on a device itself.

### Using iOS Wrapper

1. `make statusgo-ios`
2. `open ios/status-go-tester/status-go-tester.xcodeproj`
3. Build, run and trace.

---

# Energy Guide

##  What impacts battery performance?

### 1. CPU
That one is obvious, if the app consumes too much CPU, it won't be energy efficient. 

Is's important to test this data *on a device*. 

1. Desktop CPUs have different architecture and some algorithms that are hardware accelerated on a desktop-class CPU might cause bottlenecks in a mobile processor.

2. Mobile operating systems might have different syscalls implemented with different efficency.

Hence, everything should be profiled and optimized in an environment as close to what an end user will have.

**Instruments to triage CPU bottlenecks**
iOS: Instruments (Time Profiler)

### 2. Networking

One important trick that allows devices to live that long from a battery is that they try to switch to a low-power mode as soon as it is possible.

That relates not only to CPU but also to the networking stack. Radios that are used in the wireless connections are very resource-hungry. To workaround that limitation, system tries to turn them on fully only when it absolutely has to.  That is usually some timeout after the network packets were sent or received.

One important consequence of that fact is that if the packets are all sent and
received randomly, the radio will never have a chance to switch into a low
power mode.

To avoid that, networking should happen in "bursts", when during a short period of time a lots of packets are sent/received.

![](https://developer.apple.com/library/content/documentation/Performance/Conceptual/EnergyGuide-iOS/Art/new_chart_2x.png)
*source: [Apple: Energy & Networking](https://developer.apple.com/library/content/documentation/Performance/Conceptual/EnergyGuide-iOS/EnergyandNetworking.html)*

**Instruments to triage networking issues**

iOS: Xcode (Energy Efficiency, Network), Instruments (Network)

Android: TBD


## Profiling Process: iOS

**‼️** Instrument's "Energy Usage Log" [**seems to be broken** on iOS 10/11](https://forums.developer.apple.com/thread/70540) **‼️**

1. Profiling should happen using Wi-Fi debugging feature.
Otherwise, the device will be charging during the measuring process and that will skew the data.

2. Profiling should happen only on a device.

3. Profiling should happen on a **release** build configuration.
Different debugging mechanisms can add much additional overhead, hiding the
actual reasons on why something isn't efficient.

#### Tools
**Xcode: Debug Navigator**
![](./_assets/xcode-debug-nav.png)

What is "Overhead"? That is energy impact of "other system resources".
An example of that is a radio that is in high-power mode after networking data exchange.

**Instruments: Time Profiler, Network**





## Profiling Process: Android

TBD


# Read further

## iOS


- [Energy Efficiency Guide for iOS Apps](https://developer.apple.com/library/content/documentation/Performance/Conceptual/EnergyGuide-iOS/index.html#//apple_ref/doc/uid/TP40015243-CH3-SW1)

- WWDC sessions
    - 2017: 238
    - 2015: 708
    - 2014: 710, 712

