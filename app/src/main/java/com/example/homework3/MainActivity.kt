package com.example.homework3

import SettingsViewModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.homework3.model.*
import com.example.homework3.repository.NewsRepository
import com.example.homework3.repository.SettingsDataStore
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
    object NewsDetailScreen : Screen(route = "news/{id}")
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
    val settingsDataStore = SettingsDataStore(context.dataStore)

    if (!::settingsViewModel.isInitialized) {
        settingsViewModel = viewModel(initializer = { SettingsViewModel(settingsDataStore) })
    }

    if (!::newsViewModel.isInitialized) {
        newsViewModel =
            viewModel(initializer = { NewsViewModel(newsRepository, settingsDataStore) })
    }

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
                NewsList(mainViewModel = newsViewModel, settingsViewModel = settingsViewModel) {
                    navController.navigate(
                        route = Screen.NewsDetailScreen.route.replace(
                            "{id}",
                            it.id
                        )
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
                    reload(newsViewModel)
                }
            }

        }
    }
}

@Composable
fun NewsList(
    modifier: Modifier = Modifier,
    mainViewModel: NewsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateClick: (NewsItem) -> Unit,
) {
    LaunchedEffect(Unit) {
        mainViewModel.fetchCardsInitially()
    }
    val settings: SettingsData = settingsViewModel.settings.collectAsState().value
    val newsObserveAsState: State<StateWrapper<List<NewsItem>>> =
        mainViewModel.newsItems.observeAsState(
            initial = StateWrapper.init()
        )
    val newsWrapper: StateWrapper<List<NewsItem>> = newsObserveAsState.value


    when (newsWrapper.status) {
        DataStatus.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
        DataStatus.ERROR -> {
            ErrorView(
                mainViewModel = mainViewModel,
                message = newsWrapper.message ?: stringResource(R.string.unknown)
            )
        }
        DataStatus.SUCCESS -> {
            val news: List<NewsItem> = newsWrapper.data!!

            Column(
                modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                ReloadButton(mainViewModel)
                LazyColumn {
                    itemsIndexed(news) { i, newsItem ->
                        NewsItemRow(i, newsItem, settings, onNavigateClick)
                    }
                }

            }
        }
        else -> {
            Text(text = stringResource(R.string.unknown))
        }
    }

}

@Composable
private fun ReloadButton(mainViewModel: NewsViewModel) {
    Button(
        onClick = {
            reload(mainViewModel)
        },
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.refresh))
    }
}

private fun reload(mainViewModel: NewsViewModel) {
    Log.i(LogKeys.BASIC_KEY, "Reloading...")
    mainViewModel.fetchCards()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItemRow(
    i: Int,
    newsItem: NewsItem,
    settings: SettingsData,
    onNavigateClick: (NewsItem) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                newsDetailViewModel.setCurrentNewsItem(newsItem)
                onNavigateClick(newsItem);
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        shape = RoundedCornerShape(8.dp)

    ) {

        if (i == 0) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (settings.showImages)
                    AsyncImage(
                        model = newsItem.imageUrl,
                        contentDescription = stringResource(R.string.contentDesc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = newsItem.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "By ${newsItem.author} - ${newsItem.publicationDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (settings.showImages)
                        AsyncImage(
                            model = newsItem.imageUrl,
                            contentDescription = stringResource(R.string.contentDesc),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = newsItem.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = newsItem.author,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${newsItem.publicationDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun ErrorView(mainViewModel: NewsViewModel, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(align = Alignment.Center),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB00020),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        ReloadButton(mainViewModel)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Homework3Theme {
        NewsList(
            mainViewModel = newsViewModel,
            onNavigateClick = {},
            settingsViewModel = settingsViewModel
        )
    }
}


