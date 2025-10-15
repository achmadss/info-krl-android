package dev.achmad.comuline.util

import android.content.Context
import androidx.work.WorkManager

val Context.workManager: WorkManager
    get() = WorkManager.getInstance(this)

fun WorkManager.isRunning(tag: String): Boolean {
    val list = this.getWorkInfosByTag(tag).get()
    return list.any { !it.state.isFinished }
}