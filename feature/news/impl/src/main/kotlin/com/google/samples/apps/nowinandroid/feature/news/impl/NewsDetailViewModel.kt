/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.news.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.samples.apps.nowinandroid.core.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.core.model.data.NewsItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface NewsDetailUiState {
    data object Loading : NewsDetailUiState
    data object Error : NewsDetailUiState
    data class Success(val newsItem: NewsItem) : NewsDetailUiState
}

@HiltViewModel(assistedFactory = NewsDetailViewModel.Factory::class)
class NewsDetailViewModel @AssistedInject constructor(
    newsRepository: NewsRepository,
    @Assisted val newsId: String,
) : ViewModel() {

    val uiState: StateFlow<NewsDetailUiState> = newsRepository.getNewsItem(newsId)
        .map { item ->
            if (item != null) {
                NewsDetailUiState.Success(item)
            } else {
                NewsDetailUiState.Error
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsDetailUiState.Loading,
        )

    @AssistedFactory
    interface Factory {
        fun create(newsId: String): NewsDetailViewModel
    }
}
