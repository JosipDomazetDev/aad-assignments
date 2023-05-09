import androidx.lifecycle.ViewModel
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

    fun saveSettings(settings: SettingsData, onSettingChanged: (SettingsData) -> Unit = {} ) {
        viewModelScope.launch {
            settingsDataStore.saveSettings(settings)
            // Pass the callback into the viewModelScope
            onSettingChanged(settings)
        }
    }


//    // Define ViewModel factory in a companion object
//    companion object {
//        lateinit var SettingsDataStore: SettingsDataStore
//
//        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(
//                modelClass: Class<T>,
//                extras: CreationExtras
//            ): T {
//                // Get the Application object from extras
//
//
//                val application = checkNotNull(extras[APPLICATION_KEY])
//                // Create a SavedStateHandle for this ViewModel from extras
//                val savedStateHandle = extras.createSavedStateHandle()
//
//
//                val context = application.applicationContext
//                val settingsDataStore = SettingsDataStore(context.dataStore)
//
//                application.applicationContext
//                return SettingsViewModel(
//                    SettingsDataStore
//
//                ) as T
//            }
//        }
//    }
}
