# Testing Energy Consumption on iOS

## Prerequisites

Xcode (9.x), cmake, docker, git

## 1. Testing status-go

Native simple wrapper for status-go for iOS and Android.

Used for testing energy efficiency of status-go on a device itself.

#### 1.1. Building iOS Wrapper

1. Check-out the latest status-go
1. set environment variable `$STATUS_GO_HOME`
1. `make statusgo-ios`
1. `open ios/status-go-tester/status-go-tester.xcodeproj`
1. You are ready to measure!

#### 1.2. Measuring Energy Efficiency

> ‼️ you should use Wifi debugging to be able to trace energy consumption!
> Here's how to enable it: [Enable Wireless Builds](https://medium.com/aaronn/enable-wireless-builds-debug-in-xcode-9-ios-11-4f4293a184bc)

1. Run the app with Xcode
1. Go to the Debug Navigator (press `⌘+7`)
1. Go to the "Energy" tab.

## 2. Testing Status app

if you didn't set it up yet, prepare your Xcode for building Status
