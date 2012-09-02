package co.tomlee.frapp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Handles frapp:// URL schemes to enable easy OAuth authorization.
 */
public class AuthorizationActivity extends Activity {

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
    	final Uri uri = getIntent().getData();
    	final String fragment = uri.getFragment();
    	
    	boolean success = false;
    	if (fragment != null) {
    		//
    		// The fragment should contain our access token, which we need to access the API.
    		//
    		final Map<String, String> params = parseAuthFragment(fragment);
    		final SharedPreferences prefs = getSharedPreferences(Pref.FILENAME, 0);
    		
    		//
    		// TODO check access token using an API call, show a "please wait" thingy.
    		//
    		// Do this before writing it to the preferences file so we don't
    		// let other apps overwrite legit access tokens.
    		//
    		
    		final Editor editor = prefs.edit();
    		editor.putString(Pref.PREF_ACCESS_TOKEN, params.get("access_token"));
    		editor.commit();
    		success = true;
    	}
    	
    	if (!success) {
    		new AlertDialog.Builder(this)
    			.setTitle("Error")
    			.setMessage("Unable to verify your access token. Please try again.")
    			.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
    	}
    	else {
    		startActivity(new Intent(this, PostsActivity.class));
    	}
    	
    	super.onResume();
    }
    
    /**
     * Parse the fragment sent back by app.net.
     * 
     * @param authFragment
     * @return
     */
    private Map<String, String> parseAuthFragment(final String authFragment) {
    	//
    	// XXX assuming the fragment's encoded like a query string.
    	//
    	final String[] parts = authFragment.split("&");
    	final LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
    	for (int i = 0; i < parts.length; i++) {
    		final String[] kv = parts[i].split("=");
    		if (kv.length != 2) continue;
    		try {
	    		final String key = URLDecoder.decode(kv[0], "utf-8");
	    		final String value = URLDecoder.decode(kv[1], "utf-8");
	    		result.put(key, value);
    		}
    		catch (UnsupportedEncodingException e) {
    			// TODO log this?
    		}
    	}
    	return result;
    }
}
