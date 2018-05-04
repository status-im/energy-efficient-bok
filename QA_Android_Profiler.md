# Using Android Studio Profiler

## Preparing debuggable release build

It's important to use performance profiling on **release** builds with all
optimizations on. Unfortunately, Android Studio Profiled requires your app to
be debuggable to use it. So we can't just build a release and assume it would
work.

We need to make a *debuggable release build*. That requires a patching a few
files.

#### `AndroidManifest.xml`

Add `debuggable:true` to the `<application/>` tag of `android/app/src/main/AndroidManifest.xml`.

```diff
diff --git a/android/app/src/main/AndroidManifest.xml
b/android/app/src/main/AndroidManifest.xml index f4ceb435..4b6108a6 100644 ---
a/android/app/src/main/AndroidManifest.xml +++
b/android/app/src/main/AndroidManifest.xml @@ -26,6 +26,7 @@

     <application
             android:allowBackup="true"
+            android:debuggable="true"
             android:label="@string/app_name"
             android:icon="@mipmap/ic_launcher"
             android:theme="@style/AppTheme"
```

### `build.gradle`

Linter will complain about the attribute we added, so we need to continue
building anyway.

```diff
diff --git a/android/app/build.gradle b/android/app/build.gradle
index 93ad2080..161b5bb5 100644
--- a/android/app/build.gradle
+++ b/android/app/build.gradle
@@ -186,6 +186,10 @@ android {
         }
     }

+    lintOptions {
+        abortOnError false
+    }
+
     sourceSets { main { jniLibs.srcDirs 'libs' } }
 }
```

### Keystore

Create a keystore in Android studio with the following parameters like in `android/gradle.properties` file.


### Try making a build

`make prod-build-android` 

`./android/gradlew assembleRelease`

`adb install -r ./android/app/build/outputs/apk/release/app-release.apk`


### Android Studio: Analyze APK project

1. "File -> Profile or Debug APK..."

1. Select `./android/app/build/outputs/apk/release/app-release.apk`

1. Select "app-release" in the projec view. "View -> Open Module Settings".

1. Go to "Dependencies" and select Module SDK.


### Android Studio: Profiling

rebuild the app if neeeded

`adb install -r ./android/app/build/outputs/apk/release/app-release.apk`

"Run -> Progfile 'app-release'" will show a profiler.

Android studio will *copy* this APK file to a separate directory, so it will
quickly become outdated. That is not a problem, because if we install a more
recent version of it via ADB, Android Studio will fail to install it's copy on
top and show an error dialog. Just click "Cancel" there.

