package io.devexpert.splitbill

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

// Extension para crear el DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scan_counter")

class ScanCounter(private val context: Context) {
    
    companion object {
        private val FIRST_USE_DATE_KEY = longPreferencesKey("first_use_date")
        private val SCANS_REMAINING_KEY = intPreferencesKey("scans_remaining")
        private const val MAX_SCANS_PER_MONTH = 5
    }
    
    // Flow que emite el número de escaneos restantes
    val scansRemaining: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SCANS_REMAINING_KEY] ?: MAX_SCANS_PER_MONTH
    }
    
    // Inicializar el contador al abrir la app
    suspend fun initializeOrResetIfNeeded() {
        val preferences = context.dataStore.data.first()
        val firstUseDate = preferences[FIRST_USE_DATE_KEY]
        val currentDateEpoch = LocalDate.now().toEpochDay()
        
        if (firstUseDate == null) {
            // Primera vez usando la app
            context.dataStore.edit { preferences ->
                preferences[FIRST_USE_DATE_KEY] = currentDateEpoch
                preferences[SCANS_REMAINING_KEY] = MAX_SCANS_PER_MONTH
            }
        } else {
            // Verificar si ha pasado un mes
            val firstUse = LocalDate.ofEpochDay(firstUseDate)
            val monthsElapsed = ChronoUnit.MONTHS.between(firstUse, LocalDate.now())
            
            if (monthsElapsed >= 1) {
                // Ha pasado al menos un mes, resetear
                context.dataStore.edit { preferences ->
                    preferences[FIRST_USE_DATE_KEY] = currentDateEpoch
                    preferences[SCANS_REMAINING_KEY] = MAX_SCANS_PER_MONTH
                }
            }
        }
    }
    
    // Decrementar el contador de escaneos
    suspend fun decrementScan() {
        context.dataStore.edit { preferences ->
            val current = preferences[SCANS_REMAINING_KEY] ?: MAX_SCANS_PER_MONTH
            if (current > 0) {
                preferences[SCANS_REMAINING_KEY] = current - 1
            }
        }
    }

} 