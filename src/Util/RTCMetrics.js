import { NativeModules } from 'react-native';

/** @private */
const { WebRTCModule } = NativeModules;

/** @private */
function nativeEnableMetrics() {
  WebRTCModule.enableMetrics();
}

/** @private */
function nativeGetAndResetMetrics(): Promise<Array<RTCMetricsSampleInfo>> {
  return WebRTCModule.getAndResetMetrics();
}

/**
 * メトリクスの計測を有効にします。
 * 
 * @returns {void}
 * 
 * @since 2.0.0
 */
export function enableMetrics() {
  nativeEnableMetrics();
}

/**
 * 計測中のメトリクスを取得します。
 * メトリクスはリセットされます。
 * 
 * @return {Promise<Array<RTCMetricsSampleInfo>>}
 * 
 * @since 2.0.0
 */
export function getAndResetMetrics(): Promise<Array<RTCMetricsSampleInfo>> {
  return nativeGetAndResetMetrics();
}

/**
 * メトリクスの情報を表します。
 * 
 * @since 2.0.0
 */
export class RTCMetricsSampleInfo {

  /**
   * ヒストグラム名
   */
  name: string;

  /**
   * 最小バケット値
   */
  min: number;

  /**
   * 最大バケット値
   */
  max: number;

  /**
   * バケット数
   */
  bucketCount: number;

  /**
   * サンプルのマップ。
   * マップのキーはサンプルの値、値はイベントの数です。
   */
  samples: Map<number, number>;

  /** @package */
  constructor(info: Object) {
    this.name = info.name;
    this.min = info.min;
    this.max = info.max;
    this.bucketCount = info.bucketCount;
    this.samples = info.samples;
  }

}