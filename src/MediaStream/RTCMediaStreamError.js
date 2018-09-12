// @flow

export default class RTCMediaStreamError {

  /**
   * エラーの名前
   */
  name: string;

  /**
   * エラーメッセージ
   */
  message: string;

  /**
   * @private
   */
  constructor(error: Object) {
    this.name = error.name;
    this.message = error.message;
  }
}
