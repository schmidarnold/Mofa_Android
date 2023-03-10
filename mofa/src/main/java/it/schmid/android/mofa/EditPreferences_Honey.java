package it.schmid.android.mofa;

import java.util.List;



import android.annotation.SuppressLint;
import android.preference.PreferenceActivity;



public class EditPreferences_Honey extends PreferenceActivity {

	@SuppressLint("NewApi")
	@Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.prference_headers, target);

	  }
	@Override
	protected boolean isValidFragment(String fragmentName) {
		return WorkingJournalPreferenceFragment.class.getName().equals(fragmentName) ||
				PreferenceContentFragmentASA.class.getName().equals(fragmentName) ||
				PreferenceFragmentASA.class.getName().equals(fragmentName);
	}

}
