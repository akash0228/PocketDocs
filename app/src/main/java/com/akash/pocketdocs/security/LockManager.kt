package com.akash.pocketdocs.security

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.first
import java.io.File

object LockManager {
    private lateinit var masterKey: MasterKey
    private lateinit var dataStore: androidx.datastore.core.DataStore<Preferences>

    private val PIN_KEY = stringPreferencesKey("PIN")
    private val BIOMETRIC_KEY = booleanPreferencesKey("BIOMETRIC_ENABLED")

    fun init(context: Context) {
        masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = {File(context.filesDir, "secure_prefs.preferences_pb")},
        )
    }

    suspend fun savePin(pin : String) {
        val encryptedPin = encryptWithMasterKey(pin)
        dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
        }
    }

    //TODO add encryption logic
    private fun encryptWithMasterKey(plainText: String): String {
        return plainText
    }

    suspend fun getPin(): String? {
        val prefs = dataStore.data.first()
        val encrypted = prefs[PIN_KEY] ?: return null
        return decryptWithMasterKey(encrypted)
    }

    //TODO add decryption logic
    private fun decryptWithMasterKey(cipherText: String): String? {
        return cipherText
    }

    suspend fun isPinSet(): Boolean {
        return !getPin().isNullOrEmpty()
    }

    suspend fun setBioMetricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BIOMETRIC_KEY] = enabled
        }
    }

    suspend fun isBioMetricEnabled(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[BIOMETRIC_KEY]?:false
    }
}