package com.example.homework3.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.homework3.model.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
         val NEWS_FEED_URL = stringPreferencesKey("news_feed_url")
         val SHOW_IMAGES = booleanPreferencesKey("show_images")
         val DOWNLOAD_IMAGES_IN_BACKGROUND = booleanPreferencesKey("download_images_in_background")
    }

    val settings: Flow<SettingsData> = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        SettingsData(
            newsFeedUrl = preferences[PreferencesKeys.NEWS_FEED_URL] ?: "https://www.engadget.com/rss.xml",
            showImages = preferences[PreferencesKeys.SHOW_IMAGES] ?: true,
            downloadImagesInBackground = preferences[PreferencesKeys.DOWNLOAD_IMAGES_IN_BACKGROUND] ?: true
        )
    }

    suspend fun saveSettings(settings: SettingsData) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NEWS_FEED_URL] = settings.newsFeedUrl
            preferences[PreferencesKeys.SHOW_IMAGES] = settings.showImages
            preferences[PreferencesKeys.DOWNLOAD_IMAGES_IN_BACKGROUND] = settings.downloadImagesInBackground
        }
    }
}

