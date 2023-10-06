package com.example.fortnightly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fortnightly.repo.NewsRepository
import com.example.fortnightly.utils.Resource
import com.example.fortnightly.utils.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NewsArticlesViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val timeUtil: TimeUtil
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Refresh>()
    val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    var pendingScrollToTopAfterRefresh = false

    private val categories = listOf("general", "business", "sports", "science")

    init {
        viewModelScope.launch {
            repository.deleteArticlesOlderThen(
                timeUtil.getCurrentSystemTime() - TimeUnit.DAYS.toMillis(2)
            )
        }
    }

    val categoryArticles = categories.associateWith { category ->
        refreshTrigger.flatMapLatest { refresh ->
            repository.getArticles(
                category,
                refresh == Refresh.FORCE,
                onFetchSuccess = {
                    pendingScrollToTopAfterRefresh = true
                },
                onFetchFailed = { t ->
                    viewModelScope.launch {
                        eventChannel.send(Event.ShowErrorMessage(t))
                    }
                }
            )
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun onStart(category: String) {
        if (categoryArticles[category]?.value !is Resource.Loading) {
            normalRefresh(category)
        }
    }

    fun onManualRefresh(category: String) {
        if (categoryArticles[category]?.value !is Resource.Loading) {
            forceRefresh(category)
        }
    }

    private fun normalRefresh(category: String) {
        viewModelScope.launch {
            refreshTriggerChannel.send(Refresh.NORMAL)
        }
    }

    private fun forceRefresh(category: String) {
        viewModelScope.launch {
            refreshTriggerChannel.send(Refresh.FORCE)
        }
    }

    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }
}