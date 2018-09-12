// @flow

import EventTarget from 'event-target-shim';

/**
 * @private
 */
export const MEDIA_STREAM_TRACK_EVENTS = [
  'started',
  'ended',
  'mute',
  'unmute',
  'overconstrained',
];

/**
 * @package
 */
export default class RTCMediaStreamTrackEventTarget extends EventTarget(MEDIA_STREAM_TRACK_EVENTS) { }