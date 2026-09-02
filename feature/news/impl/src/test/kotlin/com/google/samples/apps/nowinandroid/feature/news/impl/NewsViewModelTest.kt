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

import app.cash.turbine.test
import com.google.samples.apps.nowinandroid.core.model.data.NewsItem
import com.google.samples.apps.nowinandroid.core.testing.repository.TestNewsRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val newsRepository = TestNewsRepository()
    private lateinit var viewModel: NewsViewModel

    private val sampleNews = listOf(
        NewsItem(
            id = "1",
            title = "Android 15 Released",
            link = "https://example.com/1",
            imageUrl = "https://example.com/img1.png",
            source = "Google Blog",
            sourceIconUrl = "https://example.com/icon1.png",
            category = "Technology",
        ),
        NewsItem(
            id = "2",
            title = "Markets Rally on Tech Earnings",
            link = "https://example.com/2",
            imageUrl = "https://example.com/img2.png",
            source = "WSJ",
            sourceIconUrl = "https://example.com/icon2.png",
            category = "Business",
        ),
    )

    @Before
    fun setup() {
        viewModel = NewsViewModel(newsRepository)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(NewsUiState.Loading, viewModel.newsUiState.value)
    }

    @Test
    fun offlineLaunch_cachedArticlesLoadImmediately() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(sampleNews)
        advanceUntilIdle()

        val state = viewModel.newsUiState.value
        assertIs<NewsUiState.Success>(state)
        assertEquals(2, state.news.size)
        assertEquals("Android 15 Released", state.news[0].title)

        collectJob.cancel()
    }

    @Test
    fun failedRefresh_withCachedData_retainsArticlesAndEmitsSnackbar() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(sampleNews)
        advanceUntilIdle()

        newsRepository.syncNewsResult = Result.failure(Exception("Network Timeout"))

        viewModel.snackbarMessages.test {
            viewModel.refresh()
            advanceUntilIdle()

            val message = awaitItem()
            assertEquals("Unable to refresh. Showing offline data", message)

            val state = viewModel.newsUiState.value
            assertIs<NewsUiState.Success>(state)
            assertEquals(2, state.news.size)
        }

        collectJob.cancel()
    }

    @Test
    fun failedRefresh_withoutCachedData_emitsError() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(emptyList())
        newsRepository.syncNewsResult = Result.failure(Exception("No internet connection"))

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.newsUiState.value
        assertIs<NewsUiState.Error>(state)

        collectJob.cancel()
    }

    @Test
    fun emptySync_withoutCachedData_emitsEmpty() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(emptyList())
        newsRepository.syncNewsResult = Result.success(Unit)

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.newsUiState.value
        assertIs<NewsUiState.Empty>(state)

        collectJob.cancel()
    }

    @Test
    fun searchFilter_filtersArticlesByTitleAndSource() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(sampleNews)
        advanceUntilIdle()

        // Filter by title
        viewModel.onSearchQueryChanged("Android")
        advanceUntilIdle()

        var state = viewModel.newsUiState.value as NewsUiState.Success
        assertEquals(1, state.news.size)
        assertEquals("Android 15 Released", state.news[0].title)

        // Filter by source
        viewModel.onSearchQueryChanged("WSJ")
        advanceUntilIdle()

        state = viewModel.newsUiState.value as NewsUiState.Success
        assertEquals(1, state.news.size)
        assertEquals("Markets Rally on Tech Earnings", state.news[0].title)

        collectJob.cancel()
    }

    @Test
    fun categoryFilter_filtersArticlesByCategory() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.newsUiState.collect() }

        newsRepository.sendNews(sampleNews)
        advanceUntilIdle()

        viewModel.onCategorySelected("Business")
        advanceUntilIdle()

        val state = viewModel.newsUiState.value as NewsUiState.Success
        assertEquals(1, state.news.size)
        assertEquals("Business", state.news[0].category)

        collectJob.cancel()
    }
}
