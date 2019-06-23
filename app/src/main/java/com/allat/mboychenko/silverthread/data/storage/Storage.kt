package com.allat.mboychenko.silverthread.data.storage

interface Storage {

    fun getInt(key: String): Int
    fun getIntDefault(key: String, default: Int): Int
    fun putInt(key: String, value: Int)

    fun getLong(key: String): Long
    fun getLongDefault(key: String, default: Long): Long
    fun putLong(key: String, value: Long)

    fun getString(key: String): String?
    fun putString(key: String, value: String)

    fun getStringSet(key: String): Set<String>
    fun putStringSet(key: String, set: Set<String>)

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