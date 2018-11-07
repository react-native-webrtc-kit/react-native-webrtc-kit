// @flow

import EventTarget from 'event-target-shim';

/**
 * @private
 */
export const PEER_CONNECTION_EVENTS = [
  'connectionstatechange',
  'icecandidate',
  'iceconnectionstatechange',
  'icegatheringstatechange',
  'identityresult',
  'idpassertionerror',
  'idpvalidationerror',
  'negotiationneeded',
  'peeridentity',
  'signalingstatechange',
  'addtrack',
  'removetrack',
  // DEPRECATED events
  'addstream',
  'removestream'
];

/**
 * @package
 */
export default class RTCPeerConnectionEventTarget extends EventTarget(PEER_CONNECTION_EVENTS) { }
