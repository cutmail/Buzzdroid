package org.tlab.buzzdroid;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
  private final static String TAG = Settings.class.getSimpleName();

  @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref);
      setBackLine();
    }

  private void setBackLine(){
    Preference prefBack = findPreference("back");
    prefBack.setOnPreferenceClickListener(new PreferenceClickListener());
  }

  private class PreferenceClickListener implements OnPreferenceClickListener {
    public boolean onPreferenceClick(Preference preference) {
      finish();
      return true;
    }
  }
}
