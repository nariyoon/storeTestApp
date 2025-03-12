package com.lge.storetest.domain

interface PostRepository {
    suspend fun getPost(id: Int): Post?
    suspend fun updatePost(
        postId: Int,
        title: String? = null,
        body: String? = null,
        userId: Int? = null
    ): Post
}