package com.lge.storetest.data

import com.lge.storetest.domain.Post
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.tatarka.inject.annotations.Inject

internal object TestServerEndpoints {
    private const val ROOT_API_URL = "https://jsonplaceholder.typicode.com"
    fun getPost(id: Int) = "$ROOT_API_URL/posts/$id"
    fun getAllPost() = "$ROOT_API_URL/posts"
    fun updatePost(id: Int) = "$ROOT_API_URL/posts/$id"
}

@Inject
class RealPostOperations(
    private val httpClient: HttpClient = httpClient()
) : PostOperations {

    override suspend fun getPost(id: Int): PostNetworkModel {
        val url = TestServerEndpoints.getPost(id)
        val response = httpClient.get(url)
        return response.body()
    }

    override suspend fun getAllPost(): List<PostNetworkModel> {
        val url = TestServerEndpoints.getAllPost()
        val response = httpClient.get(url)
        return response.body()
    }

    override suspend fun updatePost(post: PostNetworkModel): Post {
        val url = TestServerEndpoints.updatePost(post.id)
        val response = httpClient.put(url) {
            setBody(post)
        }
        return response.body()
    }


}

@Inject
class RealTestServerClient(
    private val postOperations: PostOperations
) : TestServerApi,
    PostOperations by postOperations

