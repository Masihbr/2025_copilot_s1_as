package com.example.movieswipe.voting

import retrofit2.http.Body
import retrofit2.http.POST

interface VotingSessionService {
    @POST("voting-sessions/start")
    suspend fun startVotingSession(@Body request: StartVotingSessionRequest): StartVotingSessionResponse

    @POST("voting-sessions/vote")
    suspend fun vote(@Body request: VoteRequest): VoteResponse

    @POST("voting-sessions/end")
    suspend fun endVotingSession(@Body request: EndVotingSessionRequest): EndVotingSessionResponse
}

data class StartVotingSessionRequest(val groupId: String)
data class StartVotingSessionResponse(val sessionId: String, val movies: List<MovieDto>)
data class MovieDto(val tmdbId: Int, val title: String, val genres: List<String>, val posterPath: String?)
data class VoteRequest(val sessionId: String, val userId: String, val movieId: Int, val vote: String)
data class VoteResponse(val success: Boolean)
data class EndVotingSessionRequest(val sessionId: String)
data class EndVotingSessionResponse(val winningMovie: MovieDto?)

object VotingSessionApi {
    val retrofit: VotingSessionService = retrofit2.Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/")
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
        .create(VotingSessionService::class.java)
}

