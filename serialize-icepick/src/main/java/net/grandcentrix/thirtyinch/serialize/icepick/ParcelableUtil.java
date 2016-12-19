package net.grandcentrix.thirtyinch.serialize.icepick;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * http://stackoverflow.com/a/18000094
 *
 * Created by rwondratschek on 12/14/16.
 */
@SuppressWarnings("WeakerAccess")
/*package*/ final class ParcelableUtil {

    public static byte[] marshal(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshal(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshal(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshal(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
