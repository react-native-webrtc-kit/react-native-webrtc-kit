// @flow

var debugMode: boolean = false;

/**
 * @package
 */
export default class RTCLogger {

  static setDebugMode(flag: boolean): void {
    debugMode = flag;
  }

  static _message(f: (...any) => void, msg: Array<any>): void {
    if (debugMode) {
      f.apply(f, msg);
    }
  }

  static group(...msg: Array<any>): void {
    if (console.group !== undefined) {
      this._message(console.group, msg);
    }
  }

  static groupEnd(): void {
    if (debugMode && console.groupEnd !== undefined) {
      console.groupEnd();
    }
  }

  static log(...msg: Array<any>): void {
    if (console.log !== undefined) {
      this._message(console.log, msg);
    }
  }

  static error(...msg: Array<any>): void {
    if (console.error !== undefined) {
      this._message(console.error, msg);
    }
  }

}
