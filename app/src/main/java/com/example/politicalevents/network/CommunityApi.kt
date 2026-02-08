package com.example.politicalevents.network


import com.example.politicalevents.Discussion
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

data class CreateDiscussionRequest(
    val title: String,
    val category: String,
    val author: String,
    val user_points: Int
)

data class CreateCommentRequest(
    val discussion_id: Int,
    val author: String,
    val text: String,
    val user_points: Int
)

interface CommunityApi {

    @GET("community/discussions")
    suspend fun getDiscussions(): List<Discussion>

    @POST("community/discussions")
    suspend fun createDiscussion(
        @Body body: CreateDiscussionRequest
    )

    @POST("community/comments")
    suspend fun addComment(
        @Body body: CreateCommentRequest
    )
}
