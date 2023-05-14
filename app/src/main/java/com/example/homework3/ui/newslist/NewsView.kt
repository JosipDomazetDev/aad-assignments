package com.example.homework3

import SettingsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.homework3.model.DataStatus
import com.example.homework3.model.NewsItem
import com.example.homework3.model.SettingsData
import com.example.homework3.model.StateWrapper
import com.example.homework3.ui.ErrorView
import com.example.homework3.ui.newslist.NewsItemEntry
import com.example.homework3.viewmodel.NewsViewModel

@Composable
fun NewsView(
    mainViewModel: NewsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateClick: (NewsItem) -> Unit,
) {
    val settings: SettingsData = settingsViewModel.settings.collectAsState().value
    val newsObserveAsState: State<StateWrapper<List<NewsItem>>> =
        mainViewModel.newsItemsMerged.observeAsState(
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
                NewsItemEntry(i, newsItem, settings, onNavigateClick)
            }
        }

    }
}

