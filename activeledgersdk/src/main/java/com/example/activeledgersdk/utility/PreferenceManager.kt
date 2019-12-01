/*
 * MIT License (MIT)
 * Copyright (c) 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.example.activeledgersdk.utility

import android.content.Context
import android.content.SharedPreferences


class PreferenceManager {
    lateinit var sharedPref: SharedPreferences

    fun init() {
        val context = Utility.context
        sharedPref = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE)

    }

    //save a string to preferences
    fun saveString(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun saveBoolean(key: String, value: Boolean?) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value!!)
        editor.commit()
    }

    fun saveInt(key: String, value: Int) {
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun saveFloat(key: String, value: Float) {
        val editor = sharedPref.edit()
        editor.putFloat(key, value)
        editor.commit()
    }

    fun saveLong(key: String, value: Long) {
        val editor = sharedPref.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun getStringValueFromKey(key: String?): String? {
        return sharedPref.getString(key, null)
    }

    fun getLongValueFromKey(key: String): Long {
        return sharedPref.getLong(key, 0)
    }

    fun getBooleanValueFromKey(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun getIntValueFromKey(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun getFloatValueFromKey(key: String): Float {
        return sharedPref.getFloat(key, 0f)
    }

    companion object {

        internal val PREF_NAME = "SDK Pref File"
        internal var context: Context? = null
        private var instance: PreferenceManager? = null

        @Synchronized
        fun getInstance(): PreferenceManager {
            if (instance == null) {
                instance = PreferenceManager()
            }
            return instance as PreferenceManager
        }
    }

}
