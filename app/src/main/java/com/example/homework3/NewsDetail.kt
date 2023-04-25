package com.example.homework3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.homework3.model.NewsItem
import com.example.homework3.viewmodel.NewsDetailViewModel


@Composable
fun NewsDetail(newsDetailViewModel: NewsDetailViewModel) {
    val newsItem: NewsItem? = newsDetailViewModel.getCurrentNewsItem().value

    if (newsItem == null) {
        Text(stringResource(R.string.nothing))
        return
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Unique Identifier: ${newsItem.id}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Title: ${newsItem.title}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Description: ${newsItem.description}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Image URL: ${newsItem.imageUrl}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Author: ${newsItem.author}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Publication Date: ${newsItem.publicationDate}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Full Article Link: ${newsItem.fullArticleLink}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Keywords: ${newsItem.keywords.joinToString(", ")}")
    }
}
