package com.vigor.betterclock.utils

import android.content.Context
import androidx.core.content.edit
import com.vigor.betterclock.R

class PrefUtils(context: Context) {

    val sharedPref = context.getSharedPreferences(
        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    fun save(key: String, value: Boolean) {
        sharedPref ?: return
        sharedPref.edit {
            putBoolean(key, value)
        }
    }

    var dnd: Boolean
        get() {
            sharedPref ?: throw Exception()
            return sharedPref.getBoolean("dnd", false)
        }
        set(value) {
            sharedPref.edit {
                putBoolean("dnd", value)
            }
        }

    var charge_icon: Boolean
        get() {
            sharedPref ?: throw Exception()
            return sharedPref.getBoolean("charge_icon", false)
        }
        set(value) {
            sharedPref.edit {
                putBoolean("charge_icon", value)
            }
        }

    var charge_animation: Boolean
        get() {
            sharedPref ?: throw Exception()
            return sharedPref.getBoolean("charge_animation", false)
        }
        set(value) {
            sharedPref.edit {
                putBoolean("charge_animation", value)
            }
        }
}