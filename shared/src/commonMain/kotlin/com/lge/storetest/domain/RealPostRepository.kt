package com.lge.storetest.domain

import com.lge.storetest.data.PostOperations
import com.lge.storetest.data.PostStore
import com.lge.storetest.domain.PostExtensions.asPost
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

@Inject
class RealPostRepository(
    private val postStore: PostStore,
    private val postOperations: PostOperations // Direct API access for feature #5
) : PostRepository {

    // 1. Normal Store flow - for regular data streaming
    override suspend fun getPost(id: Int): Post? {
        return postStore.get<Int, Post, Boolean>(id)
    }

    // 2. Force fetch from server immediately
    override suspend fun getPostFresh(id: Int): Post? {
        return postStore.fresh<Int, Post, Boolean>(id)
    }

    // 3. For screen transitions - use this when navigating to fetch fresh data
    override suspend fun getPostOnScreenChange(id: Int): Post? {
        val readRequest = StoreReadRequest.cached(
            key = id,
            refresh = true // Force refresh on screen change
        )

        val response = postStore.stream<Int, Post, Boolean>(readRequest).first()
        return when (response) {
            is StoreReadResponse.Data -> response.value
            is StoreReadResponse.Error.Exception -> throw response.error
            is StoreReadResponse.Error.Message -> throw IllegalStateException(response.message)
            else -> null
        }
    }

    // 4. DB update only operation
    override suspend fun updatePost(
        postId: Int,
        title: String?,
        body: String?,
        userId: Int?
    ): Post {
        val prevPost = postStore.get<Int, Post, Boolean>(postId)

        val nextPost = prevPost.copy(
            title = title ?: prevPost.title,
            body = body ?: prevPost.body,
            userId = userId ?: prevPost.userId
        )

        val writeRequest = StoreWriteRequest.of<Int, Post, Boolean>(
            key = postId,
            value = nextPost
        )

        return when (postStore.write(writeRequest)) {
            is StoreWriteResponse.Error -> prevPost
            is StoreWriteResponse.Success -> nextPost
        }
    }

    // 5. Direct API call without Store (bypassing DB)
    override suspend fun getPostDirectFromServer(id: Int): Post? {
        return try {
            val networkModel = postOperations.getPost(id)
            networkModel?.asPost()
        } catch (e: Exception) {
            null
        }
    }

    // 5b. Update directly via API without using Store
    override suspend fun updatePostDirectToServer(
        postId: Int,
        title: String?,
        body: String?,
        userId: Int?
    ): Post? {
        val currentPost = getPostDirectFromServer(postId) ?: return null

        val updatedPost = currentPost.copy(
            title = title ?: currentPost.title,
            body = body ?: currentPost.body,
            userId = userId ?: currentPost.userId
        )

        return try {
            postOperations.updatePost(updatedPost.asNetworkModel()).asPost()
        } catch (e: Exception) {
            null
        }
    }
}