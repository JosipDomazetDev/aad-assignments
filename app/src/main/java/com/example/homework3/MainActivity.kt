package com.example.homework3

import SettingsViewModel
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homework3.model.DataStatus
import com.example.homework3.repository.*
import com.example.homework3.repository.NewsDataRepository
import com.example.homework3.repository.db.NewsDatabase
import com.example.homework3.ui.theme.Homework3Theme
import com.example.homework3.viewmodel.NewsDetailViewModel
import com.example.homework3.viewmodel.NewsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Homework3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

}

sealed class Screen(val route: String) {
    object NewsListScreen : Screen(route = "news")
    object NewsDetailScreen : Screen(route = "news/detail")
    object SettingsScreen : Screen(route = "settings")
}


private val Context.dataStore by preferencesDataStore(name = "settings")

val newsRepository = NewsRepository()

val newsDetailViewModel = NewsDetailViewModel()
lateinit var newsViewModel: NewsViewModel
lateinit var settingsViewModel: SettingsViewModel

@Composable
fun Navigation() {
    val context = LocalContext.current
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    val settingsDataStore = SettingsDataStore(context.dataStore)

    settingsViewModel = ViewModelProvider(
        viewModelStoreOwner,
        SettingsViewModel.SettingsViewModelFactory(settingsDataStore)
    ).get()

    val newsDatabase = NewsDatabase.getInstance(context)
    val newsDataRepository = NewsDataRepository(newsDatabase)

    newsViewModel = ViewModelProvider(
        viewModelStoreOwner,
        NewsViewModel.NewsViewModelFactory(
            newsRepository, newsDataRepository, settingsDataStore
        )
    ).get()

    val navController = rememberNavController()
    var mDisplayMenu by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = Screen.NewsListScreen.route) {
        composable(Screen.NewsListScreen.route) {
            Column {
                SmallTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.app_name),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        val status = newsViewModel.newsItems.observeAsState().value?.status

                        if (status == DataStatus.CACHED)
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = stringResource(id = R.string.sync),
                                tint = Color.Gray,
                            )
                        if (status == DataStatus.SUCCESS)
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = stringResource(id = R.string.refresh),
                                tint = Color.Gray,
                            )
                        // Creating Icon button for dropdown menu
                        IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                            Icon(
                                Icons.Default.MoreVert,
                                "",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Creating a dropdown menu
                        DropdownMenu(
                            expanded = mDisplayMenu,
                            onDismissRequest = { mDisplayMenu = false }
                        ) {

                            // Creating dropdown menu item, on click
                            // would create a Toast message
                            DropdownMenuItem(onClick = {
                                navController.navigate(
                                    route = Screen.SettingsScreen.route
                                )
                            }, text = { Text("Settings") })

                        }

                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                )
                NewsView(mainViewModel = newsViewModel, settingsViewModel = settingsViewModel) {
                    navController.navigate(
                        route = Screen.NewsDetailScreen.route
                    )

                }
            }
        }

        composable(Screen.NewsDetailScreen.route) {
            Column {
                SmallTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.app_name),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }, colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .clickable {
                                        navController.popBackStack()
                                    }
                            )
                        }
                    }
                )
                NewsDetail(newsDetailViewModel, settingsViewModel)
            }

        }

        composable(Screen.SettingsScreen.route) {
            Column {
                SmallTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.app_name),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(all = 8.dp)
                                    .clickable {
                                        navController.popBackStack()
                                    }
                            )
                        }
                    },
                )
                SettingsScreen(settingsViewModel = settingsViewModel) {
                    newsViewModel.reload()
                }
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Homework3Theme {
        NewsView(
            mainViewModel = newsViewModel,
            onNavigateClick = {},
            settingsViewModel = settingsViewModel
        )
    }
}


