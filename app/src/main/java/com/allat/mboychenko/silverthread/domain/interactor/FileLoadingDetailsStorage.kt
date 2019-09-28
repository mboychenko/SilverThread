package com.allat.mboychenko.silverthread.domain.interactor

interface FileLoadingDetailsStorage {

    fun getLoadingIds(): Map<String, Long>
    fun putAllLoadingIds(map: Map<String, Long>)
    fun putLoadingId(url: String, id: Long)
    fun removeIdFromLoadings(id: Long)
    fun cleanLoadingIds()

}