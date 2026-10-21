package com.example.databank;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Helper {
    static void hideKeyboard(View view, InputMethodManager imm) {
        if (view == null || imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
