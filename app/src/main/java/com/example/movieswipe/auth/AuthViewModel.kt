package com.example.movieswipe.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val account: GoogleSignInAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                val account = task.getResult(ApiException::class.java)
                authState = AuthState.Success(account)
            } catch (e: Exception) {
                authState = AuthState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun reset() {
        authState = AuthState.Idle
    }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .build()
    return GoogleSignIn.getClient(context, gso)
}

