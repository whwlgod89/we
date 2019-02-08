package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.utils.ULog;
import kr.co.theunify.wear.utils.Utils;
import kr.co.theunify.wear.view.TitlebarView;

public class SettingActivity extends BaseActivity {

    private static final String TAG = "[" + SettingActivity.class.getSimpleName() + "]";

    //********************************************************************************
    //  Layout Member Variable
    //********************************************************************************

    @BindView(R.id.v_titlebar)  TitlebarView v_titlebar;

    @BindView(R.id.content)     LinearLayout content;

    //********************************************************************************
    //  Member Variable
    //********************************************************************************

    private Context mCotext;

    private static  String mAppVersion;

    //********************************************************************************
    //  LifeCycle Functions
    //********************************************************************************


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_setting);
        mCotext = this;
        ButterKnife.bind(this);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ULog.i(TAG, "onResume() - RESTART=" + Const.mRestartApp + ", FINISH=" + Const.mFinishApp + "..........");
//        if(Const.mRestartApp) {
//            setResult(Const.RESULT_CODE_OF_RESTART_APP);
//            finish();
//        }
//        else if(Const.mFinishApp) {
//            setResult(Const.RESULT_CODE_OF_FINISH_APP);
//            finish();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        ULog.i(TAG, "onBackPressed() - RESTART=" + Const.mRestartApp + ", FINISH=" + Const.mFinishApp + "..........");
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    //********************************************************************************
    //  Override Event Functions
    //********************************************************************************

    /**
     * 뒤로 이동 버튼 클릭 시
     */
    @OnClick(R.id.img_back)
    public void onClickImgBack() {
        onBackPressed();
    }


    //********************************************************************************
    //  User Define Functions
    //********************************************************************************

    /**
     * 사용자 뷰 초기화
     */
    private void initView() {
        initTitle();

        mAppVersion = "v"+Utils.getVersionName(mCotext);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(R.id.content, new PrefsFragment()).commit();
    }

    private void initTitle() {
        v_titlebar.setTitleVisible(View.VISIBLE);
        v_titlebar.setTitle(getString(R.string.title_setting));
        v_titlebar.setBackVisible(View.VISIBLE);
    }



    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_settings);

            // display version info
            Preference versionPref =  findPreference("pref_key_app_version");
            versionPref.setSummary( mAppVersion);

            // 현재 설정값 표시 및 설정값 변경 표시 핸들러 연결
            bindPreferenceSummaryToValue(findPreference("pref_key_alarm_find_phone"));
            bindPreferenceSummaryToValue(findPreference("pref_key_alarm_duration"));
         //   bindPreferenceSummaryToValue(findPreference("pref_alarm_connected"));

            // 앱 재시작 처리

            // 앱 종료 처리
            Preference appFinish = findPreference("pref_key_app_control_finish");
            appFinish.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Context context = getActivity();
                    ((BaseActivity)getActivity()).showAlertPopup("", getString(R.string.pref_confirm_app_control_finish),
                            getResources().getString(R.string.ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ULog.i(TAG, "RESTART..." + getActivity().getLocalClassName());
                                    Const.mFinishApp = true;
                                    getActivity().setResult(Const.RESULT_CODE_OF_FINISH_APP);
                                    getActivity().finish();
                                }
                            }, getString(R.string.cancel));
                    return true;
                }
            });

        }

        private static void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else if (preference instanceof RingtonePreference) {
                    // For ringtone preferences, look up the correct display value
                    // using RingtoneManager.
                    if (TextUtils.isEmpty(stringValue)) {
                        // Empty values correspond to 'silent' (no ringtone).
                        preference.setSummary(R.string.pref_summary_ringtone_silent);

                    } else {
                        Ringtone ringtone = RingtoneManager.getRingtone(
                                preference.getContext(), Uri.parse(stringValue));

                        if (ringtone == null) {
                            // Clear the summary if there was a lookup error.
                            preference.setSummary(null);
                        } else {
                            // Set the summary to reflect the new ringtone display
                            // name.
                            String name = ringtone.getTitle(preference.getContext());
                            preference.setSummary(name);
                        }
                    }
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
                return true;
            }
        };
    }
}
