package com.example.movieswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movieswipe.auth.GoogleSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.movieswipe.voting.VotingSessionScreen
import com.example.movieswipe.voting.VotingSessionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSwipeTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MovieSwipeAppRoot()
                }
            }
        }
    }
}

@Composable
fun MovieSwipeAppRoot() {
    val navController = rememberNavController()
    var signedInAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (signedInAccount == null) "signin" else "home"
    ) {
        composable("signin") {
            GoogleSignInButton(
                onSignInSuccess = { account ->
                    signedInAccount = account
                    navController.navigate("home") {
                        popUpTo("signin") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            val groupViewModel: com.example.movieswipe.group.GroupViewModel = hiltViewModel()
            val groupState by groupViewModel.groupState.collectAsState()
            signedInAccount?.let { groupViewModel.setUser(it) }
            com.example.movieswipe.group.GroupManagementScreen(
                onCreateGroup = { groupName ->
                    groupViewModel.createGroup(groupName)
                },
                onJoinGroup = { inviteCode ->
                    groupViewModel.joinGroup(inviteCode)
                }
            )
            when (groupState) {
                is com.example.movieswipe.group.GroupState.Loading -> androidx.compose.material3.Text("Loading...")
                is com.example.movieswipe.group.GroupState.Success -> {
                    val group = groupState as com.example.movieswipe.group.GroupState.Success
                    // Show button to start voting session
                    val votingSessionViewModel: VotingSessionViewModel = viewModel()
                    votingSessionViewModel.setUser(groupViewModel.userId ?: "")
                    androidx.compose.material3.Button(onClick = {
                        votingSessionViewModel.startSession(group.groupId)
                    }) {
                        androidx.compose.material3.Text("Start Voting Session")
                    }
                    val votingState by votingSessionViewModel.state.collectAsState()
                    if (votingState is com.example.movieswipe.voting.VotingSessionState.Ready) {
                        val readyState = votingState as com.example.movieswipe.voting.VotingSessionState.Ready
                        VotingSessionScreen(
                            movies = readyState.movies,
                            onVote = { movieId, vote ->
                                votingSessionViewModel.vote(movieId, vote)
                            },
                            onEndSession = {
                                votingSessionViewModel.endSession()
                            },
                            votingEnded = votingState is com.example.movieswipe.voting.VotingSessionState.Ended,
                            winningMovie = (votingState as? com.example.movieswipe.voting.VotingSessionState.Ended)?.winningMovie
                        )
                    } else if (votingState is com.example.movieswipe.voting.VotingSessionState.Ended) {
                        val endedState = votingState as com.example.movieswipe.voting.VotingSessionState.Ended
                        VotingSessionScreen(
                            movies = emptyList(),
                            onVote = { _, _ -> },
                            onEndSession = { votingSessionViewModel.reset() },
                            votingEnded = true,
                            winningMovie = endedState.winningMovie
                        )
                    }
                }
                is com.example.movieswipe.group.GroupState.Error -> {
                    val error = groupState as com.example.movieswipe.group.GroupState.Error
                    androidx.compose.material3.Text("Error: ${error.message}")
                }
                else -> {}
            }
        }
    }
}
