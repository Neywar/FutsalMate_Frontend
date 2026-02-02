# Quick Fix for App Crash

## Step 1: Check Logcat for Error

1. In Android Studio, open **Logcat** (bottom panel or View → Tool Windows → Logcat)
2. Filter by: `package:com.example.futsalmate` or search for `SplashActivity`
3. Run the app
4. When it crashes, look for **RED error messages**
5. Copy the entire error message (especially the part that says what class/resource is missing)

## Step 2: Most Common Issues

### Issue 1: Missing Gson Dependency
**Error**: `ClassNotFoundException: com.google.gson.Gson`

**Fix**: Already added in build.gradle.kts. Try:
- File → Sync Project with Gradle Files
- Build → Clean Project
- Build → Rebuild Project

### Issue 2: Missing Resource
**Error**: `Resources$NotFoundException` or `Resource ID #0x7f...`

**Fix**: Check if these files exist:
- `res/drawable/bg_splash_gradient.xml`
- `res/drawable/ic_futsal_logo.png` (or .xml)
- `res/drawable/progress_splash.xml`

### Issue 3: Network Security Config
**Error**: Network-related errors

**Fix**: Already configured. Verify:
- `res/xml/network_security_config.xml` exists
- `AndroidManifest.xml` references it correctly

### Issue 4: Class Not Found
**Error**: `ClassNotFoundException: retrofit2...` or `ClassNotFoundException: okhttp3...`

**Fix**:
1. File → Sync Project with Gradle Files
2. Check Build tab for errors
3. If errors persist, try:
   - File → Invalidate Caches / Restart
   - Delete `.gradle` folder in project root
   - Re-sync Gradle

## Step 3: Test with Minimal Code

If still crashing, temporarily simplify SplashActivity to test:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Minimal test - just show a simple text view
    TextView tv = new TextView(this);
    tv.setText("Test");
    setContentView(tv);
    
    new Handler().postDelayed(() -> {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }, 2000);
}
```

If this works, the issue is with the splash layout resources.

## Step 4: Verify Dependencies

Run this in terminal (in project root):
```bash
./gradlew app:dependencies
```

Check if Retrofit, OkHttp, and Gson are listed.

## What to Share for Help

If still not working, share:
1. The **complete error message from Logcat** (red text)
2. The **Build output** (any errors/warnings)
3. Whether the **app builds successfully** (no compilation errors)



