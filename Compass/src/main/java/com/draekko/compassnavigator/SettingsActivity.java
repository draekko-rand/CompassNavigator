/* =========================================================================

    Compass Navigator
    Copyright (C) 2019, 2022, Draekko, Benoit Touchette

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

   ========================================================================= */

package com.draekko.compassnavigator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity  extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static AppCompatActivity staticActivity;

    public static void startSettingsActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void startSettingsActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        staticActivity = this;
        setTheme(R.style.SettingsTheme);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    public static class SettingsFragment extends PreferenceFragment {

        private Settings settings;
        private SharedPreferences sharedPreferences;

        private final SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsChangeListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        Log.d(TAG, "onSharedPreferenceChanged");
                    }
                };

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            settings = new Settings();
            settings.load(sharedPreferences);

            addPreferencesFromResource(R.xml.prefs);

            Preference prefsEnableAltRose = findPreference(Settings.ENABLE_ALTROSE_KEY);
            prefsEnableAltRose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.setEnableAltRose((boolean)newValue);
                    settings.save(sharedPreferences);
                    return true;
                }
            });

            Preference prefsAutoUpManDeclination = findPreference(Settings.AUTOUPDECL_VALUE_KEY);
            prefsAutoUpManDeclination.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.setAutoUpdateManualDeclination((boolean)newValue);
                    settings.save(sharedPreferences);
                    return true;
                }
            });

            Preference prefsEnableNightMode = findPreference(Settings.ENABLE_NIGHTMODE_KEY);
            prefsEnableNightMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.setEnableNightMode((boolean)newValue);
                    settings.save(sharedPreferences);
                    return true;
                }
            });

            Preference prefsEnableGPSAutoDecl = findPreference(Settings.ENABLE_GPSDECL_KEY);
            prefsEnableGPSAutoDecl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.setEnableGpsDeclination((boolean)newValue);
                    settings.save(sharedPreferences);
                    return true;
                }
            });

            Preference prefsEnableManualDecl = findPreference(Settings.ENABLE_MANDECL_KEY);
            prefsEnableManualDecl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    settings.setEnableManualDeclination((boolean)newValue);
                    settings.save(sharedPreferences);
                    return true;
                }
            });

            Preference prefsManualDeclVal = findPreference(Settings.MANDECL_VALUE_KEY);
            prefsManualDeclVal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final EditText taskEditText = new EditText(staticActivity);
                    taskEditText.setText(String.valueOf(settings.getManualDeclinationValue()));
                    taskEditText.setMaxLines(1);
                    taskEditText.setGravity(Gravity.CENTER);
                    taskEditText.setInputType(
                            InputType.TYPE_NUMBER_FLAG_SIGNED |
                            InputType.TYPE_CLASS_NUMBER |
                            InputType.TYPE_NUMBER_FLAG_DECIMAL);

                    AlertDialog dialog = new AlertDialog.Builder(staticActivity)
                            .setTitle("Declination value")
                            .setMessage("Enter the declination value (East = positive value, West = negative values):")
                            .setView(taskEditText)
                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    float declination = Float.valueOf(String.valueOf(taskEditText.getText()));
                                    if (declination < -90 || declination > 90) {
                                        Toast.makeText(getContext(), "Invalid value was entered, value was not saved.", Toast.LENGTH_LONG);
                                        return;
                                    }
                                    settings.setManualDeclinationValue(rounded((float)declination));
                                    settings.save(sharedPreferences);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                    return true;
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeListener);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener);
        }
    }

    private static float rounded(float value) {
        float ret = 0.0f;
        int r1 = (int)(value * 10.0f);
        ret = (float)r1 / 10.0f;
        return ret;
    }
}
