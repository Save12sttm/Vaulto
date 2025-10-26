package com.example.vaulto.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ClipboardHelper {
    
    fun copyWithAutoClear(
        context: Context,
        label: String,
        text: String,
        scope: CoroutineScope,
        clearDelayMs: Long = 30000 // 30 seconds
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        
        // Auto-clear after delay
        scope.launch {
            delay(clearDelayMs)
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }
}