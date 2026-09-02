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
class NewsDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val newsRepository = TestNewsRepository()
    private lateinit var viewModel: NewsDetailViewModel

    private val sampleNews = listOf(
        NewsItem(
            id = "article-123",
            title = "Jetpack Compose 2.0 Announced",
            link = "https://developer.android.com",
            imageUrl = "https://developer.android.com/img.png",
            source = "Android Developers",
            sourceIconUrl = "https://developer.android.com/favicon.ico",
            category = "Technology",
        ),
    )

    @Before
    fun setup() {
        viewModel = NewsDetailViewModel(
            newsRepository = newsRepository,
            newsId = "article-123",
        )
    }

    @Test
    fun loadsArticleSuccessfully() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        newsRepository.sendNews(sampleNews)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<NewsDetailUiState.Success>(state)
        assertEquals("article-123", state.newsItem.id)
        assertEquals("Jetpack Compose 2.0 Announced", state.newsItem.title)

        collectJob.cancel()
    }

    @Test
    fun emitsErrorWhenNotFound() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        newsRepository.sendNews(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<NewsDetailUiState.Error>(state)

        collectJob.cancel()
    }
}
