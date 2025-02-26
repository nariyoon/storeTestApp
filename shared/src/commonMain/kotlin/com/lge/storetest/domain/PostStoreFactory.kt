package com.lge.storetest.domain

import com.lge.storetest.data.PostNetworkModel
import com.lge.storetest.data.PostOperations
import com.lge.storetest.data.PostStore
import com.lge.storetest.data.db.AppDatabase
import com.lge.storetest.domain.PostExtensions.asNetworkModel
import com.lge.storetest.domain.PostExtensions.asPost
import com.lge.storetest.domain.PostExtensions.asPostEntity
import db.com.lge.storetest.data.db.PostEntity
import kotlinx.coroutines.channels.Channel
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
        TODO()
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
                if (success) {
                    UpdaterResult.Success.Typed(success)
                } else {
                    UpdaterResult.Error.Message("Something went wrong.")
                }
            }
        )

    private fun createBookkeeper(): Bookkeeper<Int> =
        Bookkeeper.by(
            getLastFailedSync = { id ->
                appDatabase.postBookkeepingQueries
                    .selectMostRecentFailedSync(id).executeAsOneOrNull()?.let { failedSync ->
                        timestampToEpochMilliseconds(timestamp = failedSync.timestamp)
                    }
            },
            setLastFailedSync = { id, timestamp ->
                try {
                    trailsDatabase.postBookkeepingQueries.insertFailedSync(
                        PostFailedSync(
                            post_id = id,
                            timestamp = epochMillisecondsToTimestamp(timestamp)
                        )
                    )
                    true
                } catch (e: SQLException) {
                    // Handle the exception
                    false
                }
            },
            clear = { id ->
                try {
                    appDatabase.postBookkeepingQueries.clearByPostId(id)
                    true
                } catch (e: SQLException) {
                    // Handle the exception
                    false
                }
            },
            clearAll = {
                try {
                    appDatabase.postBookkeepingQueries.clearAll()
                    true
                } catch (e: SQLException) {
                    // Handle the exception
                    false
                }
            }
        )
}