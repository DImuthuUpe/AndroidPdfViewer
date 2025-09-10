# 16 KB Page Size Support

This Android PDF Viewer library has been updated to support 16 KB page sizes, which is required for Google Play compatibility starting November 1st, 2025.

## What Changed

### Build Configuration Updates

1. **Android Gradle Plugin**: Already using AGP 8.13.0 (✅ above required 8.5.1)
2. **NDK Version**: Updated to use NDK r28+ for 16 KB support
3. **Packaging Options**: Configured to use uncompressed shared libraries for proper 16 KB alignment
4. **Gradle Properties**: Added configuration for 16 KB compatibility

### Key Changes Made

#### android-pdf-viewer/build.gradle
```gradle
// 16 KB page size support configuration
packagingOptions {
    jniLibs {
        useLegacyPackaging false  // Use uncompressed shared libraries for 16 KB alignment
    }
}

// Enable 16 KB page size support for native libraries
ndkVersion "28.0.12433566"  // Use NDK r28+ for 16 KB support
```

#### sample/build.gradle
```gradle
packagingOptions {
    // ... existing exclusions ...
    
    // 16 KB page size support configuration
    jniLibs {
        useLegacyPackaging false  // Use uncompressed shared libraries for 16 KB alignment
    }
}

// Enable 16 KB page size support for native libraries
ndkVersion "28.0.12433566"  // Use NDK r28+ for 16 KB support
```

#### gradle.properties
```properties
# 16 KB page size support
android.bundle.enableUncompressedNativeLibs=false
android.enableR8.fullMode=true
```

## Native Dependencies

This library uses `pdfium-android:1.9.0`, which contains native libraries. The configuration ensures these libraries are properly aligned for 16 KB page sizes.

## Verification

### Using the Provided Scripts

#### Linux/macOS
```bash
./check_16kb_alignment.sh sample/build/outputs/apk/debug/sample-debug.apk
```

#### Windows PowerShell
```powershell
.\check_16kb_alignment.ps1 -ApkFile "sample\build\outputs\apk\debug\sample-debug.apk"
```

#### Windows Batch Script
```batch
.\realign_apk.bat "sample\build\outputs\apk\debug\sample-debug.apk"
```

#### Python Script (Cross-platform)
```bash
python fix_16kb_alignment.py "sample/build/outputs/apk/debug/sample-debug.apk"
```

### Manual Verification

1. **Check APK alignment**:
   ```bash
   zipalign -c -p -v 4 your-app.apk
   ```

2. **Test on 16 KB device**:
   ```bash
   adb shell getconf PAGE_SIZE
   # Should return 16384
   ```

### Fixing Alignment Issues

If your APK fails 16 KB alignment checks, use the provided realignment scripts:

1. **Copy your APK** to avoid file lock issues:
   ```bash
   cp sample/build/outputs/apk/debug/sample-debug.apk sample-debug-copy.apk
   ```

2. **Run the realignment script**:
   ```bash
   .\realign_apk.bat "sample-debug-copy.apk"
   ```

3. **Verify the fix**:
   ```bash
   zipalign -c -p -v 4 sample-debug-copy.apk
   ```

## Testing on 16 KB Devices

### Android Emulator
1. Download Android 15 system image with 16 KB page size support
2. Create virtual device with the 16 KB system image
3. Test your app on the emulator

### Physical Devices
- Pixel 8 and 8 Pro (Android 15 QPR1+)
- Pixel 8a (Android 15 QPR1+)
- Pixel 9, 9 Pro, and 9 Pro XL (Android 15 QPR2 Beta 2+)

Enable "Boot with 16KB page size" in Developer Options.

## Benefits

Devices with 16 KB page sizes provide:
- 3.16% lower app launch times on average
- 4.56% reduction in power draw during app launch
- 4.48% faster camera launch (hot starts)
- 6.60% faster camera launch (cold starts)
- 8% improved system boot time

## Compatibility

- ✅ **AGP Version**: 8.13.0 (above required 8.5.1)
- ✅ **NDK Version**: r28+ (16 KB aligned by default)
- ✅ **Native Libraries**: Configured for 16 KB alignment
- ✅ **Packaging**: Uncompressed shared libraries for proper alignment

## Resources

- [Android 16 KB Page Size Guide](https://developer.android.com/guide/practices/page-sizes)
- [Google Play 16 KB Requirement](https://android-developers.googleblog.com/2025/05/prepare-play-apps-for-devices-with-16kb-page-size.html)
- [APK Analyzer Tool](https://developer.android.com/studio/build/analyze-apk)

## Troubleshooting

If you encounter issues:

1. **Verify NDK version**: Ensure you're using NDK r28 or higher
2. **Check AGP version**: Must be 8.5.1 or higher
3. **Run alignment check**: Use the provided scripts to verify APK alignment
4. **Test on 16 KB device**: Use emulator or physical device with 16 KB support

## Support

For issues related to 16 KB page size support, please check:
1. The alignment verification scripts
2. Android Studio's APK Analyzer
3. The official Android documentation linked above
