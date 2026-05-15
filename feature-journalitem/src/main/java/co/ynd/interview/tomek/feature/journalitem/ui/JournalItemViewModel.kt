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

package co.ynd.interview.tomek.feature.journalitem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import co.ynd.interview.tomek.core.data.JournalItemRepository
import co.ynd.interview.tomek.feature.journalitem.ui.JournalItemUiState.Error
import co.ynd.interview.tomek.feature.journalitem.ui.JournalItemUiState.Loading
import co.ynd.interview.tomek.feature.journalitem.ui.JournalItemUiState.Success
import javax.inject.Inject

@HiltViewModel
class JournalItemViewModel @Inject constructor(
    private val journalItemRepository: JournalItemRepository
) : ViewModel() {

    val uiState: StateFlow<JournalItemUiState> = journalItemRepository
        .journalItems.map<List<String>, JournalItemUiState> { Success(data = it) }
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addJournalItem(name: String) {
        viewModelScope.launch {
            journalItemRepository.add(name)
        }
    }
}

sealed interface JournalItemUiState {
    object Loading : JournalItemUiState
    data class Error(val throwable: Throwable) : JournalItemUiState
    data class Success(val data: List<String>) : JournalItemUiState
}
