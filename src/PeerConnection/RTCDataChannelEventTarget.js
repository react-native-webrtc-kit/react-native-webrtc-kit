// @flow

import EventTarget from 'event-target-shim';

/**
 * @private
 */
export const DATA_CHANNEL_EVENTS = [
    'open',
    'message',
    'bufferedamountlow',
    'close',
    'error',
];

/**
 * @package
 */
export default class RTCDataChannelEventTarget extends EventTarget(DATA_CHANNEL_EVENTS) { }
