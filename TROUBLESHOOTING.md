# Troubleshooting Guide

## App Crashes from Splash Screen

If your app crashes immediately after launching from the splash screen, try these solutions:

### 1. Check Logcat for Error Messages

In Android Studio:
- Open **Logcat** (View → Tool Windows → Logcat)
- Filter by your app package: `com.example.futsalmate`
- Look for red error messages (especially `FATAL EXCEPTION`)
- The error message will tell you exactly what's wrong

### 2. Common Issues and Fixes

#### Missing Resources
**Symptoms**: `Resources$NotFoundException` or `android.content.res.Resources$NotFoundException`

**Solution**: 
- Ensure all drawable resources exist in `res/drawable/`
- Check that all string resources are defined in `res/values/strings.xml`
- Verify all layout files exist

#### Network Security Configuration
**Symptoms**: Network-related crashes or API calls failing

**Solution**: 
- The `network_security_config.xml` has been added
- Ensure `AndroidManifest.xml` references it correctly
- For physical devices, you may need to add your computer's IP to the allowed domains

#### Missing Dependencies
**Symptoms**: `ClassNotFoundException` or `NoClassDefFoundError`

**Solution**:
- Sync Gradle: **File → Sync Project with Gradle Files**
- Clean and rebuild: **Build → Clean Project**, then **Build → Rebuild Project**
- Ensure all dependencies in `build.gradle.kts` are properly downloaded

#### JVM Target Mismatch
**Symptoms**: Build errors about JVM target compatibility

**Solution**: 
- Already fixed in `build.gradle.kts` with `kotlinOptions { jvmTarget = "11" }`
- If still occurring, ensure Java 11 is installed and configured

### 3. Quick Fixes to Try

1. **Clean and Rebuild**:
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```

2. **Invalidate Caches**:
   ```
   File → Invalidate Caches / Restart → Invalidate and Restart
   ```

3. **Sync Gradle**:
   ```
   File → Sync Project with Gradle Files
   ```

4. **Check Build Output**:
   - Look at the **Build** tab for any errors
   - Check for missing dependencies or compilation errors

### 4. Verify Configuration

#### AndroidManifest.xml
- ✅ Internet permission added
- ✅ Network security config referenced
- ✅ All activities declared

#### build.gradle.kts
- ✅ Retrofit dependencies added
- ✅ Kotlin JVM target set to 11
- ✅ Java compatibility set to 11

#### Network Security
- ✅ `network_security_config.xml` created
- ✅ HTTP allowed for development
- ✅ Base URL configured in `RetrofitClient.java`

### 5. Testing Steps

1. **Check if app builds successfully**:
   - Build → Make Project (Ctrl+F9)

2. **Run on emulator first**:
   - Use Android Emulator
   - Base URL should be: `http://10.0.2.2:8000/api/`

3. **Check Logcat when app crashes**:
   - The exact error will be shown
   - Look for the stack trace pointing to the problematic line

### 6. Common Error Messages

#### "Unable to instantiate activity"
- **Cause**: Activity class not found or has errors
- **Fix**: Check activity classes for compilation errors

#### "Resources$NotFoundException"
- **Cause**: Missing drawable, string, or layout resource
- **Fix**: Ensure all referenced resources exist

#### "NetworkSecurityException"
- **Cause**: HTTP blocked by Android security
- **Fix**: Network security config is already added, ensure it's referenced in manifest

#### "ClassNotFoundException: retrofit2.Retrofit"
- **Cause**: Retrofit dependency not loaded
- **Fix**: Sync Gradle and rebuild

### 7. Getting Detailed Error Information

To see the exact crash reason:

1. Connect device/emulator
2. Open Logcat in Android Studio
3. Run the app
4. When it crashes, look for the red error message
5. The stack trace will show exactly where it failed

### 8. Still Not Working?

If the app still crashes:

1. **Share the Logcat error message** - This will help identify the exact issue
2. **Check Android Studio Build output** - Look for any warnings or errors
3. **Verify Laravel backend is running** - The app might be trying to connect immediately
4. **Test with a minimal activity** - Create a simple test activity to isolate the issue

### 9. Network Configuration for Physical Devices

If testing on a physical device:

1. Find your computer's IP address:
   - Windows: `ipconfig` (look for IPv4 Address)
   - Mac/Linux: `ifconfig` (look for inet)

2. Update `RetrofitClient.java`:
   ```java
   private static final String BASE_URL = "http://YOUR_IP:8000/api/";
   ```

3. Ensure your phone and computer are on the same network

4. Update `network_security_config.xml` to include your IP if needed



