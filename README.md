# AutoUpdater
[![](https://jitpack.io/v/CSAbhiOnline/AutoUpdater.svg)](https://jitpack.io/#CSAbhiOnline/AutoUpdater)

AutoUpdater is a simple Android library that automatically checks for updates and downloads APK files directly from a provided URL. It offers functionality to check for new versions, download APKs, track download progress, and install updates.

## Features

- **Check for Updates**: Compares the current app version with the latest available version from a JSON file.
- **Download APK**: Downloads the latest APK to the device.
- **Progress Tracking**: Monitors and displays the download progress in percentage.
- **Auto Install**: Installs the APK once the download is complete.

## How it Works

1. The `checkForUpdate()` method fetches the ```latest_version``` from a JSON file hosted on a server and compares with app's ```versionName```. If they don't match, then returns an ```UpdateFeatures``` object which contains ```latest_version```, ```changelog```, ```apk_url```. If there is no update, returns null.
2. If an update is available, the APK file is downloaded using `downloadApk()`, and progress is reported via a callback. Once the APK is downloaded, it is automatically installed.

## Installation

1. Add the necessary dependencies in your `build.gradle.kts`: 

```groovy
implementation ("com.github.CSAbhiOnline:AutoUpdater:1.0.0")
```

## Permissions
1. Add Permissions to `AndroidManifest.xml`

Include the following permissions in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```
2. Set Up `FileProvider` in `AndroidManifest.xml`
Add the following provider within the `<application>` tag in `AndroidManifest.xml`:
```xml
<provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
</provider>
```
3. Create `file_paths.xml` in `res/xml` Directory
Create an XML file named `file_paths.xml` in the res/xml directory with the following content:
```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="downloads" path="Download/"/>
</paths>
```
## Usage
1. The JSON file should contain the following structure:
```json
{
    "latest_version": "1.2.0",
    "changelog": "Bug fixes and performance improvements.",
    "apk_url": "https://example.com/app-latest.apk"
}
```
2. Create an instance of `AutoUpdaterManager`:
```kotlin
val autoUpdaterManager = AutoUpdaterManager(LocalContext.current)
```
3. The `checkForUpdate(jsonfileURL)` method fetches the ```latest_version``` from a JSON file hosted on a server and compares with app's ```versionName```. If they don't match, then returns an ```UpdateFeatures``` object which contains ```latest_version```, ```changelog```, ```apk_url```. If there is no update, returns null.
```kotlin
                    var update: UpdateFeatures? by remember {
                        mutableStateOf(null)
                    }
                    val autoUpdaterManager = AutoUpdaterManager(LocalContext.current)
                    val coroutineScope = rememberCoroutineScope()
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                update =
                                    autoUpdaterManager.checkForUpdate(JSONfileURL = "https://your/json/file/URL")

                            }
                        }
                    }
                    if (update == null) {
                        Text("No updates available!")
                    } else {
                        Text("Latest version: ${update!!.latestversion}")
                        Text("Changelog: ${update!!.changelog}")
                        Text("Apk URL: ${update!!.apk_url}")
                      }
```
4. If update is available, call `downloadapk(context: Context,apkUrl: String,apkName: String,onProgressUpdate: (Int) -> Unit)`. Do not use any symbols in `apkName` string.
   ```kotlin
    coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    autoUpdaterManager.downloadapk(
                                        this@MainActivity,
                                        update!!.apk_url,
                                        "version/*:${update!!.latestversion}*/"
                                    ) {
                                        progress = it
                                    }
                                }
   }
   ```
You can track the progress of download from `onProgressUpdate: (Int) -> Unit)` lambda from 0 to 100.

5. The latest version app will be automatically installed after the download completes, and the user will see a prompt to install it.
