package com.lge.storetest.domain

interface PostRepository {
    // 1. Standard fetch through Store flow
    suspend fun getPost(id: Int): Post?

    // 2. Force refresh from server through Store
    suspend fun getPostFresh(id: Int): Post?

    // 3. Fetch on screen change
    suspend fun getPostOnScreenChange(id: Int): Post?

    // 4. Update post in DB through Store
    suspend fun updatePost(
        postId: Int,
        title: String? = null,
        body: String? = null,
        userId: Int? = null
    ): Post

    // 5. Bypass Store/DB and fetch directly from API
    suspend fun getPostDirectFromServer(id: Int): Post?

    // 5b. Update directly on server via API
    suspend fun updatePostDirectToServer(
        postId: Int,
        title: String? = null,
        body: String? = null,
        userId: Int? = null
    ): Post?
}