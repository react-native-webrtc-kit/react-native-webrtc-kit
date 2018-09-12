package com.reactlibrary;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class WebRTCVideoViewManager extends SimpleViewManager<WebRTCVideoView> {

    @Override
    public String getName() {
        return "WebRTCVideoView";
    }

    @Override
    public WebRTCVideoView createViewInstance(final ThemedReactContext context) {
        return new WebRTCVideoView(context);
    }


    @ReactProp(name = "objectFit")
    public void setObjectFit(final WebRTCVideoView view, final String objectFit) {
        // TODO: implement
    }

    @ReactProp(name = "reactStreamId")
    public void setReactStreamId(final WebRTCVideoView view, final String reactStreamId) {
        // TODO: implement
    }

}
