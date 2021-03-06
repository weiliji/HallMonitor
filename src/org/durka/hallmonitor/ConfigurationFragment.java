/* Copyright 2013 Alex Burka

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.durka.hallmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

public class ConfigurationFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);      

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    
	    getPreferenceManager().getSharedPreferences()
	    		.edit()
	    		.putBoolean("pref_enabled", Functions.Is.service_running(getActivity()))
	    		.putBoolean("pref_default_widget_enabled", Functions.Is.widget_enabled(getActivity(),"default"))
	    		.putBoolean("pref_media_widget_enabled", Functions.Is.widget_enabled(getActivity(),"media"))
	    		.commit();
	    
	    getPreferenceManager().getSharedPreferences()
        	.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    // don't unregister, because we still want to receive the notification when
	    // pref_enabled is changed in onActivityResult
	    // FIXME is it okay to just never unregister??
	    getPreferenceManager().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Call back handler for when a change is detected in one of the preferences
	 * Note that all actions aere delegated into the Functions.Actions Class.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		
		Log.d("CF-oSPC", "changed key " + key);
		
		// update display
		if (findPreference(key) instanceof CheckBoxPreference) {
			((CheckBoxPreference)findPreference(key)).setChecked(prefs.getBoolean(key, false));
		}
		
		// if the service is being enabled/disabled the key will be pref_enabled 
		if (key.equals("pref_enabled")) {
			
			if (prefs.getBoolean(key, false)) {
				Functions.Actions.start_service(getActivity());
			} else {
				Functions.Actions.stop_service(getActivity());
			}
			
		// if the default screen widget is being enabled/disabled the key will be pref_default_widget	
		} else if (key.equals("pref_default_widget")) {
				
			if (prefs.getBoolean(key, false)) {
				Functions.Actions.register_widget(getActivity(), "default");
			} else {
				Functions.Actions.unregister_widget(getActivity(), "default");
			}
			
		// if the media screen widget is being enabled/disabled the key will be pref_media_widget	
			} else if (key.equals("pref_media_widget")) {
					
				if (prefs.getBoolean(key, false)) {
					Functions.Actions.register_widget(getActivity(), "media");
				} else {
					Functions.Actions.unregister_widget(getActivity(), "media");
				}	
				
			// if the default screen widget is being enabled/disabled the key will be pref_widget	
		} else if (key.equals("pref_runasroot")) {
				
			if (prefs.getBoolean(key, false)) {
				if (!Functions.Actions.run_commands_as_root(new String[]{"whoami"}).equals("root")) {
					// if "whoami" doesn't work, refuse to set preference
					Toast.makeText(getActivity(), "Root access not granted - cannot enable root features!", Toast.LENGTH_SHORT).show();			  
					prefs.edit().putBoolean(key, false).commit();
				}
			}

		} else if (key.equals("pref_do_notifications")) {
			startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
			if (prefs.getBoolean(key, false)) {
				Toast.makeText(getActivity(), "check this box then", Toast.LENGTH_SHORT).show();
				//getActivity().startService(new Intent(getActivity(), NotificationService.class));
			} else {
				Toast.makeText(getActivity(), "okay uncheck the box", Toast.LENGTH_SHORT).show();
				//getActivity().startService(new Intent(getActivity(), NotificationService.class));
			}
		}
		
		
		// if the flash controls are being enabled/disabled the key will be pref_flash_controls	
		else if (key.equals("pref_flash_controls")) {
			
			if (prefs.getBoolean(key, false) ) {
				try {
					PackageManager packageManager = getActivity().getPackageManager();
					packageManager.getApplicationLogo("net.cactii.flash2");
				} catch (PackageManager.NameNotFoundException nfne) {
					// if the app isn't installed, just refuse to set the preference
					Toast.makeText(getActivity(), "Default torch application is not installed - cannot enable torch button!", Toast.LENGTH_SHORT).show();			  
					prefs.edit().putBoolean(key, false).commit();
				}
			}
		}
		
	}

}
