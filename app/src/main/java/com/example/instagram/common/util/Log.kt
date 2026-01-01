package com.example.instagram.common.util

import android.util.Log

fun Any.logThis(tag: String? = "***logged") {
    Log.d("$tag", "$this")
}