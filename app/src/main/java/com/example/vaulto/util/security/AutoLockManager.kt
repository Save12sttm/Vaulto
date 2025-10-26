package com.example.vaulto.util.security

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoLockManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onLock: () -> Unit
) : DefaultLifecycleObserver {

    private var lockJob: Job? = null
    private var lockTimeoutMs: Long = 300000 // 5 minutes default
    
    fun setLockTimeout(milliseconds: Long) {
        lockTimeoutMs = milliseconds
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        startLockTimer()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        cancelLockTimer()
    }

    private fun startLockTimer() {
        lockJob?.cancel()
        lockJob = scope.launch {
            delay(lockTimeoutMs)
            onLock()
        }
    }

    private fun cancelLockTimer() {
        lockJob?.cancel()
        lockJob = null
    }

    fun reset() {
        cancelLockTimer()
    }
}