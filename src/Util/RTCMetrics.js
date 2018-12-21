import WebRTC from '../WebRTC';

export function enableMetrics() {
  WebRTC.enableMetrics();
}

export function getAndResetMetrics(): Promise<Array<RTCMetricsSampleInfo>> {
  return WebRTC.getAndResetMetrics();
}

export class RTCMetricsSampleInfo {

  name: string;

  min: number;

  max: number;

  bucketCount: number;

  samples: Map<number, number>;

  constructor(info: Object) {
    this.name = info.name;
    this.min = info.min;
    this.max = info.max;
    this.bucketCount = info.bucketCount;
    this.samples = info.samples;
  }

}