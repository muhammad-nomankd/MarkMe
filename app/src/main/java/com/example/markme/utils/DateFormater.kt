package com.example.markme.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Date.toFormatedString(date: Date): String {

    val formater = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formater.format(date)
}