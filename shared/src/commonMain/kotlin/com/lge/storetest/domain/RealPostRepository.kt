package com.lge.storetest.domain

import com.lge.storetest.data.PostStore
import kotlinx.datetime.LocalDateTime
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

@Inject
class RealPostRepository(
    private val postStore: PostStore
) : PostRepository {
    override suspend fun getPost(id: Int): Post? {
        return postStore.fresh<Int, Post, Boolean>(id)
    }

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

}