package jp.shiguredo.react.webrtckit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

final class Readables {

    private Readables() {
    }

    @Nullable
    static Integer integer(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.Number) return null;
        return json.getInt(key);
    }

    static int jint(@NonNull final ReadableMap json, @NonNull final String key) {
        return jint(json, key, 0);
    }

    static int jint(@NonNull final ReadableMap json, @NonNull final String key, int def) {
        if (!json.hasKey(key)) return def;
        if (json.getType(key) != ReadableType.Number) return def;
        return json.getInt(key);
    }

    @Nullable
    static Double doubles(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.Number) return null;
        return json.getDouble(key);
    }

    static double jdouble(@NonNull final ReadableMap json, @NonNull final String key) {
        return jdouble(json, key, 0.0f);
    }

    static double jdouble(@NonNull final ReadableMap json, @NonNull final String key, double def) {
        if (!json.hasKey(key)) return def;
        if (json.getType(key) != ReadableType.Number) return def;
        return json.getDouble(key);
    }

    @Nullable
    static Boolean booleans(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.Boolean) return null;
        return json.getBoolean(key);
    }

    @Nullable
    static String string(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.String) return null;
        return json.getString(key);
    }

    @Nullable
    static ReadableMap map(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.Map) return null;
        return json.getMap(key);
    }

    @Nullable
    static ReadableArray array(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return null;
        if (json.getType(key) != ReadableType.Array) return null;
        return json.getArray(key);
    }

    @NonNull
    static ReadableType type(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return ReadableType.Null;
        return json.getType(key);
    }

    static boolean isTruthy(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return false;
        return !json.isNull(key);
    }

    static boolean isFalsy(@NonNull final ReadableMap json, @NonNull final String key) {
        if (!json.hasKey(key)) return true;
        return json.isNull(key);
    }

}
