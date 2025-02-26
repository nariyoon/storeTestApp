package com.lge.storetest.domain

import com.lge.storetest.data.PostNetworkModel
import db.com.lge.storetest.data.db.PostEntity

object PostExtensions {

    fun PostNetworkModel.asPost(): Post {
        return Post(
            id = this.id,
            title = this.title,
            body = this.body,
            userId = this.userId
        )
    }


    fun Post.asNetworkModel(): PostNetworkModel {
        return PostNetworkModel(
            id = this.id,
            title = this.title,
            body = this.body,
            userId = this.userId
        )
    }

    fun Post.asPostEntity(): PostEntity {
        return PostEntity(
            id = this.id.toLong(),
            title = this.title,
            body = this.body,
            userId = this.userId?.toLong()
        )
    }

    fun PostEntity.asNetworkModel(): PostNetworkModel {
        return PostNetworkModel(
            id = this.id.toInt(),
            title = this.title,
            body = this.body,
            userId = this.userId?.toInt()
        )
    }

    fun PostEntity.asPost(): Post {
        return Post(
            id = this.id.toInt(),
            title = this.title,
            body = this.body,
            userId = this.userId?.toInt()
        )
    }


}