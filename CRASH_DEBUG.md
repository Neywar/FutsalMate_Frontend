# Debugging App Crash from Splash Screen

## To Find the Exact Error:

1. **Open Logcat in Android Studio**:
   - View → Tool Windows → Logcat
   - Filter by package: `com.example.futsalmate`
   - Filter by level: `Error` or `Fatal`

2. **Run the app and when it crashes, look for**:
   - Red error messages
   - `FATAL EXCEPTION` entries
   - `android.content.res.Resources$NotFoundException` (missing resources)
   - `java.lang.ClassNotFoundException` (missing classes)
   - `android.view.InflateException` (layout issues)

3. **Common error patterns**:

   **Missing Resource:**
   ```
   Resources$NotFoundException: Resource ID #0x7f...
   ```
   → Check if drawable/string resource exists

   **Class Not Found:**
   ```
   ClassNotFoundException: Didn't find class "...RetrofitClient"
   ```
   → Dependency issue, sync Gradle

   **Layout Inflate Error:**
   ```
   android.view.InflateException: Binary XML file line #X
   ```
   → Issue with layout XML file

## Quick Test:

Try temporarily commenting out the RetrofitClient import in LoginActivity to see if that's the issue:

```java
// import com.example.futsalmate.api.RetrofitClient;
```

If the app works after this, the issue is with Retrofit dependencies.


