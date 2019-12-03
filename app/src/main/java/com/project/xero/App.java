package com.project.xero;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.project.xero.Common.Common;
import com.project.xero.Model.User;

/**
 * Created by Rajat Sangrame on 3/12/19.
 * http://github.com/rajatsangrame
 */
public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();


        if (Common.curUser == null) {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                    Context.MODE_PRIVATE);
            String json = preferences.getString("USER", "{}");

            Gson gson = new Gson();
            Common.curUser = gson.fromJson(json, User.class);

            Log.i(TAG, "onCreate: " + json);

        }
    }
}
