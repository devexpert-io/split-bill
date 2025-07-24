package io.devexpert.splitbill.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scan_counter")

class DataStoreScanCounterDataSource(private val context: Context) : ScanCounterDataSource {
    
    companion object {
        private val FIRST_USE_DATE_KEY = longPreferencesKey("first_use_date")
        private val SCANS_REMAINING_KEY = intPreferencesKey("scans_remaining")
        private const val MAX_SCANS_PER_MONTH = 5
    }
    
    override val scansRemaining: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SCANS_REMAINING_KEY] ?: MAX_SCANS_PER_MONTH
    }
    
    override suspend fun initializeOrResetIfNeeded() {
        val preferences = context.dataStore.data.first()
        val firstUseDate = preferences[FIRST_USE_DATE_KEY]
        val currentDateEpoch = LocalDate.now().toEpochDay()
        
        if (firstUseDate == null) {
            context.dataStore.edit { preferences ->
                preferences[FIRST_USE_DATE_KEY] = currentDateEpoch
                preferences[SCANS_REMAINING_KEY] = MAX_SCANS_PER_MONTH
            }
        } else {
            val firstUse = LocalDate.ofEpochDay(firstUseDate)
            val monthsElapsed = ChronoUnit.MONTHS.between(firstUse, LocalDate.now())
            
            if (monthsElapsed >= 1) {
                context.dataStore.edit { preferences ->
                    preferences[FIRST_USE_DATE_KEY] = currentDateEpoch
                    preferences[SCANS_REMAINING_KEY] = MAX_SCANS_PER_MONTH
                }
            }
        }
    }
    
    override suspend fun decrementScan() {
        context.dataStore.edit { preferences ->
            val current = preferences[SCANS_REMAINING_KEY] ?: MAX_SCANS_PER_MONTH
            if (current > 0) {
                preferences[SCANS_REMAINING_KEY] = current - 1
            }
        }
    }
}