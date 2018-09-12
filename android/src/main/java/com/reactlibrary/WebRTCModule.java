
package com.reactlibrary;

import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class WebRTCModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public WebRTCModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "WebRTCModule";
    }

    @ReactMethod
    public void hellworld(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
