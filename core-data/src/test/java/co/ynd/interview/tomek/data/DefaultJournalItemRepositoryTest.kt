/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.ynd.interview.tomek.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import co.ynd.interview.tomek.core.data.DefaultJournalItemRepository
import co.ynd.interview.tomek.core.database.JournalItem
import co.ynd.interview.tomek.core.database.JournalItemDao

/**
 * Unit tests for [DefaultJournalItemRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultJournalItemRepositoryTest {

    @Test
    fun journalItems_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultJournalItemRepository(FakeJournalItemDao())

        repository.add("Repository")

        assertEquals(repository.journalItems.first().size, 1)
    }

}

private class FakeJournalItemDao : JournalItemDao {

    private val data = mutableListOf<JournalItem>()

    override fun getJournalItems(): Flow<List<JournalItem>> = flow {
        emit(data)
    }

    override suspend fun insertJournalItem(item: JournalItem) {
        data.add(0, item)
    }
}
