package com.example.videoapp.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "istream_session";
    private static final String KEY_USER_ID = "userId";
    private static final int NO_USER = -1;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
