package com.example.unmannedstore.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ShowUtils {
    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void log(String msg) {
        Log.v("2", msg);
    }
}
