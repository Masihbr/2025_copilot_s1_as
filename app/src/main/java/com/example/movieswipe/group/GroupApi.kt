package com.example.movieswipe.group

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object GroupApi {
    private const val BASE_URL = "http://10.0.2.2:4000/" // Use 10.0.2.2 for localhost in Android emulator

    val retrofit: GroupService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GroupService::class.java)
}

interface GroupService {
    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): CreateGroupResponse

    @POST("groups/join")
    suspend fun joinGroup(@Body request: JoinGroupRequest): JoinGroupResponse
}

data class CreateGroupRequest(val name: String, val ownerId: String)
data class CreateGroupResponse(val groupId: String, val inviteCode: String)
data class JoinGroupRequest(val inviteCode: String, val userId: String)
data class JoinGroupResponse(val groupId: String, val inviteCode: String)

