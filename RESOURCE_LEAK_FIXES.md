# Resource Leak Fixes

This document describes the fixes applied to resolve resource leak issues detected during neutral cycle testing (DOC, Home, Notification, Text, and Scroll cycles).

## Issues Fixed

### 1. BilimiaoScanner Callback Leak

**File:** `app/src/main/java/com/a10miaomiao/bilimiao/comm/scanner/BilimiaoScanner.kt`

**Problem:** The `BilimiaoScanner` singleton object stores a `resultCallback` that references Activity contexts. When the scanner activity is cancelled or fails, the callback was not being cleared, leading to memory leaks across lifecycle changes.

**Fix:**
- Clear `resultCallback` in all exit paths of `onActivityResult()`:
  - When no intent data is received
  - When barcode/QR code data extraction fails
  - When result code is not OK (cancelled)
  - When no relevant extras are present

**Impact:** Prevents Activity context leaks when scanning operations are cancelled or fail during orientation changes and other lifecycle events.

---

### 2. PlayerDelegate2 BroadcastReceiver Leak

**File:** `app/src/main/java/com/a10miaomiao/bilimiao/comm/delegate/player/PlayerDelegate2.kt`

**Problem:** The `BroadcastReceiver` for audio events (headphone disconnect) was being registered in `onCreate()` but could fail to unregister properly if multiple lifecycle events occurred, or if the unregister call threw an exception.

**Fix:**
- Added `isBroadcastReceiverRegistered` flag to track registration state
- Only register the receiver if not already registered
- Catch `IllegalArgumentException` during registration (receiver already registered)
- Safely unregister with try-catch in `onDestroy()`
- Clear registration flag after successful unregistration

**Impact:** Prevents accumulation of BroadcastReceiver instances during rapid lifecycle changes (orientation changes, home button, etc.).

---

### 3. PlayerDelegate2 Theme Observer Leak

**File:** `app/src/main/java/com/a10miaomiao/bilimiao/comm/delegate/player/PlayerDelegate2.kt`

**Problem:** A new `Observer` was being created and registered every time `onCreate()` was called. Although LiveData observers are lifecycle-aware, the anonymous observer instances were accumulating in memory.

**Fix:**
- Store the observer in a field `themeObserver`
- Only create and register the observer once (when null)
- Clear the observer reference in `onDestroy()`

**Impact:** Prevents creation of multiple observer instances during lifecycle recreation, reducing memory pressure during orientation changes.

---

### 4. DanmakuSurfaceView SurfaceHolder Callback Leak

**File:** `DanmakuFlameMaster/src/main/java/master/flame/danmaku/ui/widget/DanmakuSurfaceView.java`

**Problem:** The `SurfaceHolder.Callback` was registered in `init()` via `mSurfaceHolder.addCallback(this)` but never removed, causing the DanmakuSurfaceView to remain in memory even after it should have been garbage collected.

**Fix:**
- Added `mSurfaceHolder.removeCallback(this)` in the `release()` method
- Added null check before removing callback

**Impact:** Ensures proper cleanup of Surface callbacks, preventing DanmakuSurfaceView instances from leaking during video playback lifecycle events.

---

## Testing Recommendations

To verify these fixes resolve the reported issues, repeat the neutral cycle tests:

1. **DOC (Double Orientation Change)** - Rotate device twice
2. **Home** - Press home button, then return to app
3. **Notification** - Open and close notification bar
4. **Text** - Input and clear text in fields
5. **Scroll** - Scroll down/right, then up/left

Execute each cycle 50 times and monitor:
- Memory usage (heap)
- Object counts for:
  - Activity instances
  - BroadcastReceiver instances
  - Observer instances
  - SurfaceHolder.Callback instances

Expected behavior: Resource metrics should remain stable without continuous growth.

## Related Issues

These fixes address the resource leak issues described in the bug report for:
- Android 11.0 on Google Pixel
- App version 2.4.6
- Activities experiencing leaks during lifecycle callbacks

The fixes ensure proper resource management during all Android lifecycle transitions, particularly those triggered by the neutral cycles used in automated testing.
