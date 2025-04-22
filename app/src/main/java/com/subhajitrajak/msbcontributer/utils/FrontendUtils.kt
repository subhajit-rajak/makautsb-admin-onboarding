package com.subhajitrajak.msbcontributer.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun log(message: String) {
    Log.e("Personal", message)
}