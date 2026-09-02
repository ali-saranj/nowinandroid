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

package com.google.samples.apps.nowinandroid.core.data.testdoubles

import com.google.samples.apps.nowinandroid.core.database.dao.NewsDao
import com.google.samples.apps.nowinandroid.core.database.model.NewsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestNewsDao : NewsDao {
    private val entitiesState = MutableStateFlow<List<NewsEntity>>(emptyList())

    override fun getNews(): Flow<List<NewsEntity>> = entitiesState

    override fun getNewsItem(id: String): Flow<NewsEntity?> =
        entitiesState.map { entities -> entities.firstOrNull { it.id == id } }

    override suspend fun upsertNews(news: List<NewsEntity>) {
        entitiesState.update { old ->
            val oldMap = old.associateBy { it.id }.toMutableMap()
            news.forEach { oldMap[it.id] = it }
            oldMap.values.toList()
        }
    }

    override suspend fun deleteAll() {
        entitiesState.value = emptyList()
    }
}
