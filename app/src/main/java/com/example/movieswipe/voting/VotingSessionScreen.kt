package com.example.movieswipe.voting

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun VotingSessionScreen(
    movies: List<MovieDto>,
    onVote: (movieId: Int, vote: String) -> Unit,
    onEndSession: () -> Unit,
    votingEnded: Boolean = false,
    winningMovie: MovieDto? = null
) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentMovie = movies.getOrNull(currentIndex)

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (votingEnded) {
            Text("Voting Ended!")
            if (winningMovie != null) {
                Text("Selected Movie: ${winningMovie.title}")
                winningMovie.posterPath?.let {
                    Image(
                        painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${it}"),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text("No movie selected.")
            }
            Button(onClick = onEndSession) { Text("Back to Group") }
        } else if (currentMovie != null) {
            Text(currentMovie.title)
            currentMovie.posterPath?.let {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${it}"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("Genres: ${currentMovie.genres.joinToString(", ")}")
            RowButtons(
                onYes = {
                    onVote(currentMovie.tmdbId, "yes")
                    currentIndex++
                },
                onNo = {
                    onVote(currentMovie.tmdbId, "no")
                    currentIndex++
                },
                enabled = currentIndex < movies.size
            )
            if (currentIndex >= movies.size - 1) {
                Button(onClick = onEndSession) { Text("End Voting Session") }
            }
        } else {
            Text("No movies to vote on.")
        }
    }
}

@Composable
fun RowButtons(onYes: () -> Unit, onNo: () -> Unit, enabled: Boolean) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onYes, enabled = enabled) { Text("Yes") }
        Button(onClick = onNo, enabled = enabled) { Text("No") }
    }
}

