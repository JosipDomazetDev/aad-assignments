import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.homework3.model.SettingsData
import com.example.homework3.repository.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    val settings: StateFlow<SettingsData> = this.settingsDataStore.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        SettingsData()
    )

    fun saveSettings(settings: SettingsData, onSettingChanged: (SettingsData) -> Unit = {}) {
        viewModelScope.launch {
            settingsDataStore.saveSettings(settings)
            // Pass the callback into the viewModelScope
            onSettingChanged(settings)
        }
    }


    class SettingsViewModelFactory(private val settingsDataStore: SettingsDataStore) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(settingsDataStore = settingsDataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
