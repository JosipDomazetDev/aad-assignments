package com.example.homework3

import SettingsViewModel
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.example.homework3.model.NewsItem
import com.example.homework3.model.SettingsData
import com.example.homework3.viewmodel.NewsDetailViewModel


@Composable
fun NewsDetail(newsDetailViewModel: NewsDetailViewModel, settingsViewModel: SettingsViewModel) {
    val settings: SettingsData = settingsViewModel.settings.collectAsState().value

    val newsItem: NewsItem? = newsDetailViewModel.getCurrentNewsItem().value

    if (newsItem == null) {
        CircularProgressIndicator()
        return
    }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NewsImage(newsItem = newsItem, settings)
        NewsDescription(newsItem)
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            onClick = {
                uriHandler.openUri(newsItem.fullArticleLink)
            }
        ) {
            Text(text = "Full Story")
        }
    }
}

@Composable
private fun NewsDescription(newsItem: NewsItem) {
    Html(text = newsItem.description)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        style = MaterialTheme.typography.bodySmall,
        text = "Keywords: ${newsItem.keywords.joinToString(", ")}"
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun Html(text: String) {
    AndroidView(factory = { context ->
        TextView(context).apply {
            setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
        }
    })
}


@Composable
fun NewsImage(newsItem: NewsItem, settings: SettingsData) {
    var showProgressBar by remember { mutableStateOf(true) }

    if (showProgressBar && settings.showImages)
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )

    Box(modifier = Modifier.aspectRatio(16f / 9f)) {
        if (settings.showImages)
            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = stringResource(R.string.contentDesc),
                contentScale = ContentScale.Crop,
                onLoading = {
                    showProgressBar = true
                }, onSuccess = {
                    showProgressBar = false
                },
                onError = {
                    showProgressBar = false
                },
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
    Spacer(modifier = Modifier.height(16.dp))
}