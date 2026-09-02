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

package com.google.samples.apps.nowinandroid.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.google.samples.apps.nowinandroid.core.database.model.NewsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [NewsEntity] access
 */
@Dao
interface NewsDao {
    @Query(
        value = """
            SELECT * FROM news_articles
            ORDER BY rowid ASC
        """,
    )
    fun getNews(): Flow<List<NewsEntity>>

    @Query(
        value = """
            SELECT * FROM news_articles
            WHERE id = :id
        """,
    )
    fun getNewsItem(id: String): Flow<NewsEntity?>

    @Upsert
    suspend fun upsertNews(news: List<NewsEntity>)

    @Query("DELETE FROM news_articles")
    suspend fun deleteAll()
}
