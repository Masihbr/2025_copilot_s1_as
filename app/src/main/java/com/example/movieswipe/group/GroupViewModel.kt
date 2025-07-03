package com.example.movieswipe.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.movieswipe.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

sealed class GroupState {
    object Idle : GroupState()
    object Loading : GroupState()
    data class Success(val groupId: String, val inviteCode: String) : GroupState()
    data class Error(val message: String) : GroupState()
}

@HiltViewModel
class GroupViewModel @Inject constructor() : ViewModel() {
    private val _groupState = MutableStateFlow<GroupState>(GroupState.Idle)
    val groupState: StateFlow<GroupState> = _groupState

    var userId: String? = null // Set this after sign-in

    fun setUser(account: GoogleSignInAccount) {
        userId = account.id
    }

    fun createGroup(groupName: String) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    GroupApi.retrofit.createGroup(
                        CreateGroupRequest(name = groupName, ownerId = userId ?: "")
                    )
                }
                _groupState.value = GroupState.Success(response.groupId, response.inviteCode)
            } catch (e: Exception) {
                _groupState.value = GroupState.Error(e.localizedMessage ?: "Failed to create group")
            }
        }
    }

    fun joinGroup(inviteCode: String) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    GroupApi.retrofit.joinGroup(
                        JoinGroupRequest(inviteCode = inviteCode, userId = userId ?: "")
                    )
                }
                _groupState.value = GroupState.Success(response.groupId, response.inviteCode)
            } catch (e: Exception) {
                _groupState.value = GroupState.Error(e.localizedMessage ?: "Failed to join group")
            }
        }
    }

    fun reset() {
        _groupState.value = GroupState.Idle
    }
}
