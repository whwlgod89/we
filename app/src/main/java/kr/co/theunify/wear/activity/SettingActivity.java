package kr.co.theunify.wear.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import kr.co.theunify.wear.Const;
import kr.co.theunify.wear.R;
import kr.co.theunify.wear.utils.ULog;

public class SettingActivity extends BaseActivity {
    private static final String TAG = "[" + SettingActivity.class.getSimpleName() + "]";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
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

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_settings);

            // 현재 설정값 표시 및 설정값 변경 표시 핸들러 연결
            bindPreferenceSummaryToValue(findPreference("pref_key_alarm_disconnected"));
            bindPreferenceSummaryToValue(findPreference("pref_key_alarm_find_phone"));
            bindPreferenceSummaryToValue(findPreference("pref_key_alarm_duration"));

            // 앱 재시작 처리
            Preference appRestart = findPreference("pref_key_app_control_restart");
            appRestart.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Context context = getActivity();
                    final AlertDialog dlgConfirm = new AlertDialog.Builder(context).create();
                    dlgConfirm.setMessage(getString(R.string.pref_confirm_app_control_restart));
                    dlgConfirm.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dlgConfirm.dismiss();
                            ULog.i(TAG, "RESTART..." + getActivity().getLocalClassName());
                            Const.mRestartApp = true;
                            getActivity().setResult(Const.RESULT_CODE_OF_RESTART_APP);
                            getActivity().finish();
                        }
                    });
                    dlgConfirm.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dlgConfirm.dismiss();
                        }
                    });

                    dlgConfirm.show();
                    return true;
                }
            });

            // 앱 종료 처리
            Preference appFinish = findPreference("pref_key_app_control_finish");
            appFinish.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Context context = getActivity();
                    final AlertDialog dlgConfirm = new AlertDialog.Builder(context).create();
                    dlgConfirm.setMessage(getString(R.string.pref_confirm_app_control_finish));
                    dlgConfirm.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dlgConfirm.dismiss();
                            ULog.i(TAG, "RESTART..." + getActivity().getLocalClassName());
                            Const.mFinishApp = true;
                            getActivity().setResult(Const.RESULT_CODE_OF_FINISH_APP);
                            getActivity().finish();
                        }
                    });
                    dlgConfirm.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dlgConfirm.dismiss();
                        }
                    });

                    dlgConfirm.show();
                    return true;
                }
            });

            // 도움말 선택시 브라우저 연결하고 설정은 종료
            Preference appHelp = findPreference("pref_key_show_guide");
            appHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://heytong.com/?page_id=425"));
                    startActivity(browserIntent);
                    getActivity().finish();
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
