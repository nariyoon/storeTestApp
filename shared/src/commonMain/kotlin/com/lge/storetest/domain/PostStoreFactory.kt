package com.lge.storetest.domain

import com.lge.storetest.data.PostNetworkModel
import com.lge.storetest.data.PostOperations
import com.lge.storetest.data.PostStore
import com.lge.storetest.data.db.AppDatabase
import com.lge.storetest.data.db.PostEntity
import com.lge.storetest.data.db.PostFailedDelete
import com.lge.storetest.domain.PostExtensions.asNetworkModel
import com.lge.storetest.domain.PostExtensions.asPost
import com.lge.storetest.domain.PostExtensions.asPostEntity
import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

class PostStoreFactory(
    private val client: PostOperations,
    private val appDatabase: AppDatabase
) {

    fun create(): PostStore {
        return MutableStoreBuilder.from(
            fetcher = createFetcher(),
            sourceOfTruth = createSourceOfTruth(),
            converter = createConverter()
        ).build(
            updater = createUpdater(),
            bookkeeper = createBookkeeper()
        )
    }

    private fun createFetcher(): Fetcher<Int, PostNetworkModel> = Fetcher.of { id ->
        // Fetch post from the network
        client.getPost(id) ?: throw IllegalArgumentException("Post with ID $id not found.")
    }

    private fun createSourceOfTruth(): SourceOfTruth<Int, PostEntity, Post> =
        SourceOfTruth.of(
            reader = { id ->
                flow {
                    // Query the database for a post
                    emit(
                        appDatabase.postQueries.selectPostById(id.toLong()).executeAsOne().asPost()
                    )
                }
            },
            writer = { _, postEntity ->
                appDatabase.postQueries.insertPost(postEntity)
            }
        )


    private fun createConverter(): Converter<PostNetworkModel, PostEntity, Post> =
        Converter.Builder<PostNetworkModel, PostEntity, Post>()
            .fromOutputToLocal { post -> post.asPostEntity() }
            .fromNetworkToLocal { postNetworkModel -> postNetworkModel.asPost().asPostEntity()}
            .build()


    private fun createUpdater(): Updater<Int, Post, Boolean> =
        Updater.by(
            post = { _, updatedPost ->
                val networkModel = updatedPost.asNetworkModel()
                val success = client.updatePost(networkModel)
                UpdaterResult.Success.Typed(success)
            }
        )

    private fun createBookkeeper(): Bookkeeper<Int> =
        Bookkeeper.by(
            getLastFailedSync = { id ->
                appDatabase.postBookkeepingQueries.getFailedDeletes().executeAsOne().timestamp
            },
            setLastFailedSync = { id, timestamp ->
                appDatabase.postBookkeepingQueries.insertFailedDelete(PostFailedDelete(id.toLong(), timestamp)
                )
                true

            },
            clear = { id ->
                appDatabase.postBookkeepingQueries.clearFailedDelete(id.toLong())
                true
            },
            clearAll = {
                appDatabase.postBookkeepingQueries.clearAllFailedDeletes()
                true
            }
        )
}