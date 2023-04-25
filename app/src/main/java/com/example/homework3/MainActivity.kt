package com.example.homework3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.model.StateWrapper
import com.example.homework3.repository.NewsRepository
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
}

val newsRepository = NewsRepository()
val newsViewModel = NewsViewModel(newsRepository)
val newsDetailViewModel = NewsDetailViewModel()

@Composable
fun Navigation() {
    val navController = rememberNavController()

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
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
                NewsList(mainViewModel = newsViewModel) {
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
                NewsDetail(newsDetailViewModel)
            }

        }
    }
}

@Composable
fun NewsList(
    modifier: Modifier = Modifier,
    mainViewModel: NewsViewModel,
    onNavigateClick: (NewsItem) -> Unit
) {
    LaunchedEffect(Unit) {
        mainViewModel.fetchCardsInitially()
    }

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
            ErrorView(message = newsWrapper.message ?: stringResource(R.string.unknown))
        }
        DataStatus.SUCCESS -> {
            val news: List<NewsItem> = newsWrapper.data!!

            Column(
                modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Button(
                    onClick = {
                        mainViewModel.fetchCards()
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.refresh))
                }
                LazyColumn {
                    items(news) { newsItem ->
                        NewsItemRow(newsItem, onNavigateClick)
                    }
                }

            }
        }
        else -> {
            Text(text = stringResource(R.string.unknown))
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItemRow(newsItem: NewsItem, onNavigateClick: (NewsItem) -> Unit) {
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
//            Image(
//                modifier = Modifier
//                    .padding(end = 16.dp)
//                    .size(96.dp),
//                painter = rememberImagePainter(
//                    data = newsItem.imageUrl,
//                    builder = {
//                        crossfade(true)
//                    }
//                ),
//                contentDescription = newsItem.title
//            )
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

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(align = Alignment.Center),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB00020),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Homework3Theme {
        NewsList(mainViewModel = newsViewModel, onNavigateClick = {})
    }
}


