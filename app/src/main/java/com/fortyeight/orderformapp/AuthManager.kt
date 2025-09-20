package com.fortyeight.orderformapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthManager(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USERNAME = stringPreferencesKey("username")
        private val USER_ROLES = stringPreferencesKey("user_roles")
    }
    
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    val userToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN]
    }
    
    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }
    
    val userRoles: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLES]
    }
    
    suspend fun saveLoginData(token: String, username: String, roles: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_TOKEN] = token
            preferences[USERNAME] = username
            preferences[USER_ROLES] = roles.joinToString(",")
        }
    }

    suspend fun clearLoginData() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false // Setting boolean to false is fine
            preferences.remove(USER_TOKEN)    // Remove the key
            preferences.remove(USERNAME)      // Remove the key
            preferences.remove(USER_ROLES)    // Remove the key
        }
    }


    suspend fun getCurrentToken(): String? {
        var token: String? = null
        context.dataStore.data.map { preferences ->
            preferences[USER_TOKEN]
        }.collect { token = it }
        return token
    }
}
