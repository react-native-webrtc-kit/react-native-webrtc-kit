// @flow

import EventTarget from 'event-target-shim';

/**
 * @private
 * RTCDataChannel で発火しうるイベントです。
 * cf: https://www.w3.org/TR/webrtc/#dom-rtcdatachannel
 */
export const DATA_CHANNEL_EVENTS = [
    'open',
    'message',
    'close',
    'closing',
    'bufferedamountlow'
    //  XXX(kdxu): objc では error 時に発火する delegate は実装されていない (Android は未調査)
    // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/objc/api/peerconnection/RTCDataChannel.h#39
    // 'error',
];

/**
 * @package
 */
export default class RTCDataChannelEventTarget extends EventTarget(DATA_CHANNEL_EVENTS) { }
