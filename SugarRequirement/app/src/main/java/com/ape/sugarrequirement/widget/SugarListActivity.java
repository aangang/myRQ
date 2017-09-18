package com.ape.sugarrequirement.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ape.sugarrequirement.R;
import com.ape.sugarrequirement.util.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SugarListActivity extends Activity{

    public static final String LIST_KEY = "list";
    public static final String MULTI_CHECK ="MULTI_CHECK";
    public static final String RESULT_CHECKED = "checked";
    ListView mListView;
    View contentView;
    List<String> mEntryList;
    List<String> mEntryValueList;
    SugarListPreferenceInfo mPreferenceInfo;
    ListPreferenceAdapter mAdapter = null;
    SparseBooleanArray mCheckArray;
    boolean isMultiCheck = false;
    private static final int MENU_KEGUARD_MANAGER_ID = 0;
    private boolean mDisableListeners;
    private int mEntryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Ape_Settings);
        super.onCreate(savedInstanceState);
        Intent bundle = getIntent();
        if (null != bundle) {
            mPreferenceInfo = bundle.getParcelableExtra(LIST_KEY);
            isMultiCheck = bundle.getBooleanExtra(MULTI_CHECK,false);
            //get Strings title from xml resource id
            initListTitleString();
            initTitleChecked();
        }
        setContentView(R.layout.sugar_check_list_fragment);
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ListPreferenceAdapter();
        mListView.setAdapter(mAdapter);
        //mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(mItemClickListener);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setTitle(getString(R.string.gsm_umts_network_preferences_title));
    }

    public void initListTitleString() {
        mEntryId = mPreferenceInfo.getEntriesId();
        if (mEntryId == -1){
            mEntryList = mPreferenceInfo.getEntriesList();
        } else {
            String[] entryArray = getResources().getStringArray(mEntryId);
            mEntryList = Arrays.asList(entryArray);
        }
        //Log.d(LOG_TAG, "onCreate--> mEntryList: " + mEntryList.size());
    }

    public void initTitleChecked() {
        mCheckArray = new SparseBooleanArray();
        SparseBooleanArray mDefaultCheckedArray = null;
        mDefaultCheckedArray = mPreferenceInfo.getCheckArray();
        for (int i = 0; i < mEntryList.size(); i++) {
            boolean temp = (null == mDefaultCheckedArray)?false: mDefaultCheckedArray.get(i, false);
            //Log.d(LOG_TAG, "temp: " + temp);
            mCheckArray.append(i, temp);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Log.d(LOG_TAG, "onItemClick() called with: parent = [" + parent + "], view = [" + view + "], position = [" + position + "], id = [" + id + "]");
            if (!isMultiCheck) {
                RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio);
                radioButton.setChecked(true);

            } else {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                checkBox.setChecked(true);
            }
            if(!isMultiCheck) {
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 100);
            }

        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1:
                    ArrayList<Integer> checkPosition = new ArrayList<>();
                    //set back result before finish()
                    for (int i = 0; i < mCheckArray.size(); i++) {
                        if (mCheckArray.get(i, false)) {
                            checkPosition.add(i);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putIntegerArrayListExtra(RESULT_CHECKED, checkPosition);
                    setResult(mPreferenceInfo.getRequestCode(), intent);
                    if(!isMultiCheck) {
                        finish();
                    }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(requestCode == 2){
            mDisableListeners = true;
            updateInputMethodPreferenceViews();
            mAdapter.notifyDataSetChanged();
            mDisableListeners = false;
        }*/
    }

    private class ListPreferenceAdapter extends BaseAdapter {
        private CompoundButton mPreCheckedButton = null;

        public ListPreferenceAdapter() {
        }

        @Override
        public int getCount() {
            return mEntryList.size();
        }

        @Override
        public Object getItem(int position) {
            return mEntryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(SugarListActivity.this).inflate(R.layout.sugar_list_check_preference_item, null);
                viewHolder.mTitle = (TextView) convertView.findViewById(R.id.title);
                viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                viewHolder.mRadioButton = (RadioButton) convertView.findViewById(R.id.radio);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mTitle.setText(mEntryList.get(position));
            if (isMultiCheck) {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                viewHolder.mRadioButton.setVisibility(View.GONE);
                viewHolder.mCheckBox.setChecked(mCheckArray.get(position));
                viewHolder.mCheckBox.setOnCheckedChangeListener(mListener);
                viewHolder.mCheckBox.setTag(position);
            } else {
                //Init mPreCheckedButton is default checked data
                if (mPreferenceInfo.getCheckArray().get(position, false)) {
                    mPreCheckedButton = viewHolder.mRadioButton;
                }
                viewHolder.mCheckBox.setVisibility(View.GONE);
                viewHolder.mRadioButton.setVisibility(View.VISIBLE);
                viewHolder.mRadioButton.setTag(position);
                viewHolder.mRadioButton.setChecked(mCheckArray.get(position));
                viewHolder.mRadioButton.setOnCheckedChangeListener(mListener);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView mTitle;
            TextView mSummary;
            CheckBox mCheckBox;
            RadioButton mRadioButton;
        }

        private void handleCheckBoxSelect(ViewHolder viewHolder) {
        }

        private void handleRadioButtonSelect(ViewHolder viewHolder) {
        }

        private CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.d(LOG_TAG, "onCheckedChanged--> position: "+ buttonView.getTag() + " isChecked: "+ isChecked);
                if (isMultiCheck) {
                    mCheckArray.append((Integer) buttonView.getTag(), isChecked);
                } else {
                    //Log.d(LOG_TAG, "onCheckedChanged--> isChecked: " + isChecked);
                    if (isChecked) {
                        int position = (int) buttonView.getTag();
                        //Log.d(LOG_TAG, "onCheckedChanged--> position: " + position);
                        //Clear last checked button
                        if (null != mPreCheckedButton) {
                            int tag = (int) mPreCheckedButton.getTag();
                            mCheckArray.append(tag, false);
                            mPreCheckedButton.setChecked(false);
                        }
                        mCheckArray.append((Integer) buttonView.getTag(), true);
                        mPreCheckedButton = buttonView;
                    }
                }

                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 0);
            }
        };
    }

    interface OnSelectChangeListener {
        void onChange(int position);
    }
}
