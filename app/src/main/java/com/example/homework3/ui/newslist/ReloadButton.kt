package com.example.homework3

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.homework3.viewmodel.NewsViewModel

@Composable
fun ReloadButton(mainViewModel: NewsViewModel) {
    Button(
        onClick = {
            mainViewModel.reload(isSoftMode = true)
        },
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.refresh))
    }
}

