package me.yuqirong.plugin.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用于存储插件化过程中的数据
 * @author Zhouyu
 * @date 2018/8/16
 */
public class PluginDataStorage implements Parcelable {

    public String packageName;
    public String className;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.className);
    }

    public PluginDataStorage() {
    }

    protected PluginDataStorage(Parcel in) {
        this.packageName = in.readString();
        this.className = in.readString();
    }

    public static final Creator<PluginDataStorage> CREATOR = new Creator<PluginDataStorage>() {
        @Override
        public PluginDataStorage createFromParcel(Parcel source) {
            return new PluginDataStorage(source);
        }

        @Override
        public PluginDataStorage[] newArray(int size) {
            return new PluginDataStorage[size];
        }
    };

}
