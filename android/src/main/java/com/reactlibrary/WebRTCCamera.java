package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.webrtc.Camera1Enumerator;

final class WebRTCCamera {

    private final Camera1Enumerator cameraEnumerator;

    WebRTCCamera() {
        this.cameraEnumerator = new Camera1Enumerator(true);
    }

    @Nullable
    String getSuitableDeviceNameForFacingMode(@Nullable final String facingMode) {
        if (facingMode == null) {
            return null;
        }
        // TODO:
        return "todo";
    }

    void startCapture(@NonNull final String deviceName, final int width, final int height, final int framerate) {
        // TODO
    }

    void startCaptureWithAllDevices() {
        // TODO
    }

    void stopCapture() {
        // TODO
    }
}
