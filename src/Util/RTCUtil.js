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


/**
 * @package
 * 
 * Merge custom constraints with the default one. The custom one take precedence.
 *
 * @param {Object} custom - custom webrtc constraints
 * @param {Object} def - default webrtc constraints
 * @return {Object} constraints - new instance of merged webrtc constraints
 */
export function mergedMediaConstraints(custom: Object, def: Object): Object {
    const constraints = (def ? Object.assign({}, def) : {});
    if (custom) {
        if (custom.mandatory) {
            constraints.mandatory = { ...constraints.mandatory, ...custom.mandatory };
        }
        if (custom.optional && Array.isArray(custom.optional)) {
            // `optional` is an array, webrtc only finds first and ignore the rest if duplicate.
            constraints.optional = custom.optional.concat(constraints.optional);
        }
        if (custom.facingMode) {
            constraints.facingMode = custom.facingMode.toString(); // string, 'user' or the default 'environment'
        }
    }
    return constraints;
}
