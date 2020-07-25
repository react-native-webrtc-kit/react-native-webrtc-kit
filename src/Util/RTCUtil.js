// @flow

/**
 * @package
 */
export function nativeBoolean(value: boolean | number): boolean {
    if (typeof value === 'boolean') {
        return value;
    } else {
        return value > 0 ? true : false;
    }
}
