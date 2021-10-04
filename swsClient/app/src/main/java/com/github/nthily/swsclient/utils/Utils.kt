package com.github.nthily.swsclient.utils

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object Utils {
    fun log(str: String) {
        Log.d(TAG, str)
    }
    @Composable
    fun SecondaryText(str: String) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(str)
        }
    }
}
