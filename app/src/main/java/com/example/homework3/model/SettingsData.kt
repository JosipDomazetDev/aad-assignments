package com.example.homework3.model


data class SettingsData(
    var newsFeedUrl: String = "",
    var showImages: Boolean = true,
    var downloadImagesInBackground: Boolean = false
)
