package com.ape.sugarrequirement.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 6/20/17.
 */
public class SugarListPreferenceInfo implements Parcelable {

    private int entriesId = -1;
    private int requestCode = 1;
    private SparseBooleanArray mCheckArray = null;
    private SugarListActivity.OnSelectChangeListener mSelectChangeListener;

    private List<String> entriesList;

    public SugarListPreferenceInfo() {

    }

    public SugarListActivity.OnSelectChangeListener getSelectChangeListener() {
        return mSelectChangeListener;
    }

    public void setSelectChangeListener(SugarListActivity.OnSelectChangeListener selectChangeListener) {
        mSelectChangeListener = selectChangeListener;
    }

    public SparseBooleanArray getCheckArray() {
        return mCheckArray;
    }
    public void setCheckArray(SparseBooleanArray checkArray) {
        mCheckArray = checkArray;
    }

    public int getEntriesId() {
        return entriesId;
    }

    public void setEntriesId(int entriesId) {
        this.entriesId = entriesId;
    }

    public void setEntriesList(List<String> entriesList){
        this.entriesList = entriesList;
    }

    public List<String> getEntriesList(){
        return this.entriesList;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    private SugarListPreferenceInfo(Parcel in) {

    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(entriesId);
        dest.writeInt(requestCode);
        dest.writeSparseBooleanArray(mCheckArray);
        dest.writeValue(mSelectChangeListener);
    }

    public static final Parcelable.Creator<SugarListPreferenceInfo> CREATOR = new Parcelable.Creator<SugarListPreferenceInfo>() {

        @Override
        public SugarListPreferenceInfo createFromParcel(Parcel source) {
            SugarListPreferenceInfo info = new SugarListPreferenceInfo();
            info.entriesId = source.readInt();
            info.requestCode = source.readInt();
            info.mCheckArray = source.readSparseBooleanArray();
            info.mSelectChangeListener = (SugarListActivity.OnSelectChangeListener)
                    source.readValue(SugarListActivity.OnSelectChangeListener.class.getClassLoader());
            List<String> list = new ArrayList();
            source.readStringList(list);
            info.entriesList = list;
            return info;
        }

        @Override
        public SugarListPreferenceInfo[] newArray(int size) {
            return new SugarListPreferenceInfo[size];
        }
    };
}
