package com.ape.sugarrequirement.util;

public class Constant {

	//ota apk and data
	public static final String DONT_OTA = "dont_ota";
	public static final String OTATAG = "otaapk";
	public static final String KEYTAG = "smart_key";
	public static final String APP_CONFIG = "/system/etc/app_config.xml";
	public static final String PRE_INSTALL_DIR = "/system/pre-install/";

	public static final String PARTNER_SETTINGS_OTA_PATH = "etc/settings-config-ota.xml";

	//qcom eyeprevent
	public static final String EYETAG = "eyeprevent";
	public static final String QCOM_EP_ACTION = "android.intent.action.QCOM_EYEPREVENT";
	public static final String QCOM_EP_ACTION_RECEIVER = "android.intent.action.QCOM_EYEPREVENT_RECEIVER";
	public static final String QCOM_EP_ACTION_SERVICE = "android.intent.action.QCOM_EYEPREVENT_SERVICE";
	public static final String MTK_EP_ACTION = "android.intent.action.MTK_EYEPREVENT";

	public static final String QCOM_STATUS_KEY = "persist.sys.eyep_status";

	public static final int QCOM_STATUS_VALUE_ON = 50;
	public static final int QCOM_STATUS_VALUE_OFF = 50;

	public static final String EYE_PREVENT_STATUS_QCOM = "eye_prevent_qcom_status";

	//smart key start
	public static final String SMART_KEY_EVENT = "com.android.start_smartkey";
	public static final String SMART_KEY_SERVICE = "com.android.smartkey.SERVICE";
	//settings status
	public static final String SUGAR_KEY_TOUCH_MODE_STATUS = "sugar_key_touch_mode_status";
	public static final String SUGAR_KEY_START_MODE_STATUS = "sugar_key_start_mode_status";

	//settings database name used in camera,can not change
	public static final String RECORDING_ENABLE_STATUS = "double_click_recording_enable_status";
	public static final String VOICE_RECORDING_STATUS = "sugar_voice_recording_status";
	public static final String CLICK_ANSWER_THE_PHONE_STATUS = "sugar_click_answer_the_phone_status";
	public static final String FALSE_TOUCH_STATUS = "sugar_false_touch_status";
	public static final String MM_NOT_EXIST_ACTION = "com.android.settings.sugarkey_mm_notexist";
	public static final String MM_NEED_LOGIN_ACTION = "com.android.settings.sugarkey_mm_needlogin";
	public static final String ALIPAY_NOT_EXIST_ACTION = "com.android.settings.sugarkey_alipay_notexist";
	public static final String ALIPAY_NEED_LOGIN_ACTION = "com.android.settings.sugarkey_alipay_needlogin";

	public static final String FRIST_SAVE = "frist_save_file";
	public static final String IS_FRIST_RUN = "frist_time_use_sugar_key";
}
