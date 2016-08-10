package se.miun.daje1400.bathingsites;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    private EditTextPreference weatherURL, bathingSitesURL, bathsiteRadius;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        bathingSitesURL = (EditTextPreference) findPreference(getResources().getString(R.string.get_bathingsites_url));
        bathingSitesURL.setSummary(sharedPreferences.getString(getResources().getString(R.string.get_bathingsites_url),getResources().getString(R.string.default_bathingsites_url)));

        weatherURL = (EditTextPreference) findPreference(getResources().getString(R.string.get_weather_url));
        weatherURL.setSummary(sharedPreferences.getString(getResources().getString(R.string.get_weather_url),getResources().getString(R.string.default_weather_url)));

        bathsiteRadius = (EditTextPreference) findPreference(getResources().getString(R.string.radius_distance));
        bathsiteRadius.setSummary(sharedPreferences.getString(getResources().getString(R.string.radius_distance),getResources().getString(R.string.radius_default_value)));

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(Color.WHITE);
        getView().setPadding(0,(int)getResources().getDimension(R.dimen.twenty_dp), 0,0);
    }

    //Listener for when preference has changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key){
            case "Get weather URL": weatherURL.setSummary(sharedPreferences.getString(key,null));
                break;
            case "Get bathingsites URL": bathingSitesURL.setSummary(sharedPreferences.getString(key,null));
                break;
            case "Show bathsites in radius (km)": bathsiteRadius.setSummary(sharedPreferences.getString(key,null));
                break;
        }

    }

}

