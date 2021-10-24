package com.github.nthily.swsclient.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShiftButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .width(120.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF0079D3)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides LocalContentAlpha.current, content = content)
    }
}