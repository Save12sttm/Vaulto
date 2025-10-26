package com.example.vaulto.util.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

@Composable
fun <T> rememberStable(calculation: () -> T): T {
    return remember { calculation() }
}