package com.example.homework3

import SettingsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homework3.model.SettingsData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onUrlChanged: (SettingsData) -> Unit
) {
    val settings: SettingsData = settingsViewModel.settings.collectAsState().value

    Column {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        Divider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "News Feed URL",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = settings.newsFeedUrl,
                onValueChange = {
                    settingsViewModel.saveSettings(settings.copy(newsFeedUrl = it), onUrlChanged)

                },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = settings.showImages,
                    onCheckedChange = { newValue ->
                        settingsViewModel.saveSettings(settings.copy(showImages = newValue))
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Show Images",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = settings.downloadImagesInBackground,
                    onCheckedChange = { newValue ->
                        settingsViewModel.saveSettings(settings.copy(downloadImagesInBackground = newValue))
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Download Images in Background",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
