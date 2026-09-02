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

import com.google.samples.apps.nowinandroid.core.model.data.NewsItem

/**
 * UI State for the News Screen.
 */
sealed interface NewsUiState {
    /**
     * Initial loading state when local database has no data and initial sync is running.
     */
    data object Loading : NewsUiState

    /**
     * Full-screen error state when network request fails and no local cached data is available.
     */
    data class Error(val message: String? = null) : NewsUiState

    /**
     * Empty state when sync succeeded but zero news articles are returned and local cache is empty.
     */
    data object Empty : NewsUiState

    /**
     * Success state when cached news items are available.
     * @param news List of news articles to display.
     * @param isRefreshing True if a pull-to-refresh sync is currently in progress.
     */
    data class Success(
        val news: List<NewsItem>,
        val isRefreshing: Boolean = false,
    ) : NewsUiState
}
