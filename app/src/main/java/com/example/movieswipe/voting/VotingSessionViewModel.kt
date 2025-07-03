package com.example.movieswipe.voting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class VotingSessionState {
    object Idle : VotingSessionState()
    object Loading : VotingSessionState()
    data class Ready(val sessionId: String, val movies: List<MovieDto>) : VotingSessionState()
    data class Voted(val movieId: Int) : VotingSessionState()
    data class Ended(val winningMovie: MovieDto?) : VotingSessionState()
    data class Error(val message: String) : VotingSessionState()
}

@HiltViewModel
class VotingSessionViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<VotingSessionState>(VotingSessionState.Idle)
    val state: StateFlow<VotingSessionState> = _state

    var sessionId: String? = null
    var userId: String? = null

    fun startSession(groupId: String) {
        viewModelScope.launch {
            _state.value = VotingSessionState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    VotingSessionApi.retrofit.startVotingSession(StartVotingSessionRequest(groupId))
                }
                sessionId = response.sessionId
                _state.value = VotingSessionState.Ready(response.sessionId, response.movies)
            } catch (e: Exception) {
                _state.value = VotingSessionState.Error(e.localizedMessage ?: "Failed to start session")
            }
        }
    }

    fun vote(movieId: Int, vote: String) {
        val sId = sessionId ?: return
        val uId = userId ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    VotingSessionApi.retrofit.vote(VoteRequest(sId, uId, movieId, vote))
                }
                _state.value = VotingSessionState.Voted(movieId)
            } catch (e: Exception) {
                _state.value = VotingSessionState.Error(e.localizedMessage ?: "Failed to vote")
            }
        }
    }

    fun endSession() {
        val sId = sessionId ?: return
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    VotingSessionApi.retrofit.endVotingSession(EndVotingSessionRequest(sId))
                }
                _state.value = VotingSessionState.Ended(response.winningMovie)
            } catch (e: Exception) {
                _state.value = VotingSessionState.Error(e.localizedMessage ?: "Failed to end session")
            }
        }
    }

    fun setUser(userId: String) {
        this.userId = userId
    }

    fun reset() {
        _state.value = VotingSessionState.Idle
        sessionId = null
    }
}

