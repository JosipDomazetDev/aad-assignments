package com.example.homework3.ui.newslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.homework3.R
import com.example.homework3.model.*
import com.example.homework3.newsDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsItemEntry(
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
            // Highlight the first item
            HighlightedEntry(settings, newsItem)
        } else {
            SimpleEntry(settings, newsItem)
        }
    }
}

@Composable
private fun SimpleEntry(
    settings: SettingsData,
    newsItem: NewsItem
) {
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

@Composable
private fun HighlightedEntry(
    settings: SettingsData,
    newsItem: NewsItem
) {
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
}