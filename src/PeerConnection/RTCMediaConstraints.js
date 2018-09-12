/**
 * メディアの制約を表すオブジェクトです。
 * 制約として指定できるパラメーターは環境によって異なります。
 */
export default class RTCMediaConstraints {

  /**
   * 必須となるパラメーターです。
   */
  mandatory: Map<string, string> | null;

  /**
   * 任意で指定できるパラメーターです。
   */
  optional: Array<Map<string, string>> | null;

  /**
   * @package
   */
  toJSON(): Object {
    json = {};
    if (this.mandatory != null) {
      json['mandotory'] = this.mandatory;
    }
    if (this.optional != null) {
      json['optional'] = this.optional;
    }
    return json;
  }

}