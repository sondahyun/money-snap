package com.example.tripline

import android.content.Context

object TriplineUiStateStore {
    private const val PREF_NAME = "tripline_ui_state"
    private const val KEY_HAS_CURRENT_TRIP = "has_current_trip"
    private const val KEY_HAS_SCHEDULE = "has_schedule"

    fun hasCurrentTrip(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HAS_CURRENT_TRIP, true)
    }

    fun hasSchedule(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HAS_SCHEDULE, true)
    }

    fun selectExistingTrip(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_CURRENT_TRIP, true)
            .putBoolean(KEY_HAS_SCHEDULE, true)
            .apply()
    }

    fun createTrip(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_CURRENT_TRIP, true)
            .putBoolean(KEY_HAS_SCHEDULE, false)
            .apply()
    }

    fun markScheduleReady(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_CURRENT_TRIP, true)
            .putBoolean(KEY_HAS_SCHEDULE, true)
            .apply()
    }

    fun clearTrip(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_CURRENT_TRIP, false)
            .putBoolean(KEY_HAS_SCHEDULE, false)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
