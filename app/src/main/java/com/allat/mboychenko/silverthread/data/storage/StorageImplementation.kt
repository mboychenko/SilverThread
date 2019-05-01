package com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.data.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.allat.mboychenko.silverthread.com.allat.mboychenko.silverthread.presentation.helpers.Storage
import org.json.JSONObject


class StorageImplementation(val context: Context) : Storage {

    private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getInt(key: String) = preferences.getInt(key, 0)

    override fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    override fun getString(key: String) = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    override fun getBooleanDefaultFalse(key: String) = preferences.getBoolean(key, false)

    override fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * ensure to call from io thread
     */
    @SuppressLint("ApplySharedPref")
    override fun putMap(key: String, map: Map<*, *>) {
        val jsonObject = JSONObject(map)
        val jsonString = jsonObject.toString()
        val editor = preferences.edit()
        editor.remove(key).commit()
        editor.putString(key, jsonString)
        editor.commit()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K: Any, T:Any> getMap(prefkey: String): Map<K, T> {
        try {
            val outputMap = mutableMapOf<K, T>()
            val jsonString = preferences.getString(prefkey, JSONObject().toString())
            val jsonObj = JSONObject(jsonString)

            val keysItr = jsonObj.keys()
            while (keysItr.hasNext()) {
                val key = keysItr.next()
                val value = jsonObj.get(key) as T
                outputMap[key as K] = value
            }

            return outputMap

        } catch(e: Exception) {
            Log.e(LOG_TAG, "Cant get requested map. Probably wrong type")
        }

        return emptyMap()
    }

    override fun remove(key: String) {
        val editor = preferences.edit()
        editor.remove(key)
        editor.apply()  //commit if immediately
    }

    override fun clear() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val LOG_TAG = "StorageImplementation"
    }
}


