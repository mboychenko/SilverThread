package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers

interface Storage {

    fun getInt(key: String): Int
    fun getIntDefault(key: String, default: Int): Int
    fun putInt(key: String, value: Int)

    fun getString(key: String): String?
    fun putString(key: String, value: String)

    fun getBooleanDefaultFalse(key: String): Boolean
    fun putBoolean(key: String, value: Boolean)

    /**
     * ensure to call from IO thread
     */
    fun putMap(key: String, map: Map<*, *>)
    fun <T: Any, K:Any> getMap(prefkey: String): Map<T, K>

    fun remove(key: String)
    fun clear()

}