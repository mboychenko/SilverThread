package com.allat.mboychenko.silverthread.presentation.helpers

import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.ExecutionException

fun WorkManager.isWorkScheduled(workTag: String): Boolean {
    val statuses = getWorkInfosForUniqueWork(workTag)
    return try {
        var running = false
        val workInfoList = statuses.get()
        for (workInfo in workInfoList) {
            val state = workInfo.state
            running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
        }
        running
    } catch (e: ExecutionException) {
        e.printStackTrace()
        false
    } catch (e: InterruptedException) {
        e.printStackTrace()
        false
    }
}