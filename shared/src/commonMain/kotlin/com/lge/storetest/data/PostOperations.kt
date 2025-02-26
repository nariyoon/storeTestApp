package com.lge.storetest.data

import com.lge.storetest.domain.Post

interface PostOperations {
    suspend fun getPost(id: Int): PostNetworkModel?
    suspend fun getAllPost() :List<PostNetworkModel>
    suspend fun updatePost(post: PostNetworkModel): Post
}