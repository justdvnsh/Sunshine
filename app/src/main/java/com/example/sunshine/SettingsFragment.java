package com.example.sunshine;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        getPreferenceScreen().findPreference(getString(R.string.key_editText)).setSummary(getPreferenceScreen().findPreference(getString(R.string.key_editText)).getSharedPreferences().getString(getResources().getString(R.string.key_editText), getResources().getString(R.string.default_value_editText)));
        Preference preference = findPreference(getString(R.string.key_editText));
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        Toast error = Toast.makeText(getContext(), "Please enter a valid city", Toast.LENGTH_SHORT);

        String editTextKey = getString(R.string.key_editText);
        preference.setSummary(newValue.toString());
        if (preference.getKey().equals(editTextKey)) {
            String city = (String) newValue;
            try {
                int val = Integer.parseInt(city);
                error.show();
                return false;
            } catch (Exception e) {}

            try {
                float val = Float.parseFloat(city);
                error.show();
                return false;
            } catch(Exception e) { }
        }

        return true;
    }
}
