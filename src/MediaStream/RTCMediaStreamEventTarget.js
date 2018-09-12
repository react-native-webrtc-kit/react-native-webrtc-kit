// @flow

import EventTarget from 'event-target-shim';

/**
 * @private
 */
export const MEDIA_STREAM_EVENTS = [
  'active',
  'inactive',
  'addtrack',
  'removetrack',
];

/**
 * @package
 */
export default class RTCMediaStreamEventTarget extends EventTarget(MEDIA_STREAM_EVENTS) { }
