package com.example.homework3

import SettingsViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.homework3.model.*
import com.example.homework3.ui.ErrorView
import com.example.homework3.viewmodel.NewsViewModel

@Composable
fun NewsView(
    mainViewModel: NewsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateClick: (NewsItem) -> Unit,
) {
    val settings: SettingsData = settingsViewModel.settings.collectAsState().value
    val newsObserveAsState: State<StateWrapper<List<NewsItem>>> =
        mainViewModel.newsItems.observeAsState(
            // Set to loading until actual state gets through
            initial = StateWrapper.loading()
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
        DataStatus.CACHED, DataStatus.SUCCESS -> {
            SuccessNewsView(
                newsWrapper,
                mainViewModel,
                settings,
                Modifier
                    .padding(16.dp),
                onNavigateClick
            )
        }
        else -> {
            Text(text = stringResource(R.string.unknown))
        }
    }

}

@Composable
private fun SuccessNewsView(
    newsWrapper: StateWrapper<List<NewsItem>>,
    mainViewModel: NewsViewModel,
    settings: SettingsData,
    modifier: Modifier,
    onNavigateClick: (NewsItem) -> Unit
) {
    if (newsWrapper.data == null || newsWrapper.data.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.no_news))
            ReloadButton(mainViewModel = mainViewModel)
        }
        return
    }

    val news: List<NewsItem> = newsWrapper.data
    ListView(mainViewModel, news, settings, modifier, onNavigateClick)
}

@Composable
private fun ListView(
    mainViewModel: NewsViewModel,
    news: List<NewsItem>,
    settings: SettingsData,
    modifier: Modifier,
    onNavigateClick: (NewsItem) -> Unit
) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItemRow(
    i: Int, newsItem: NewsItem, settings: SettingsData, onNavigateClick: (NewsItem) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                newsDetailViewModel.setCurrentNewsItem(newsItem)
                onNavigateClick(newsItem);
            }, elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ), shape = RoundedCornerShape(8.dp)

    ) {

        if (i == 0) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (settings.showImages) AsyncImage(
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
                            text = newsItem.title, style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis
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
                    if (settings.showImages) AsyncImage(
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