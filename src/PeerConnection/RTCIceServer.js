/**
 * ICE サーバーの情報です。
 */
export default class RTCIceServer {

  /**
   * URL のリスト
   */
  urls: Array<string>

  /**
   * ユーザー名
   */
  username: string | null;

  /**
   * クレデンシャル
   */
  credential: string | null;

  /**
   * オブジェクトを生成します。
   */
  constructor(urls: Array<string>,
    username: string | null, credential: string | null) {
    this.urls = urls;
    this.username = username;
    this.credential = credential;
  }

  /**
   * @package
   */
  toJSON(): Object {
    var json = {};
    json.urls = [];
    this.urls.forEach(url => { json.urls.push(url) });
    if (this.username) {
      json.username = this.username;
    }
    if (this.credential) {
      json.credential = this.credential;
    }
    return json;
  }

}