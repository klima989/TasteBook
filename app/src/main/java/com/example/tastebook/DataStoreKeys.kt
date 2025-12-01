package com.example.tastebook

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val ACCESS_TOKEN = stringPreferencesKey("access_token") // optional
    val SERVER_AUTH_CODE = stringPreferencesKey("server_token")
}