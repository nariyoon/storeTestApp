package com.lge.storetest.data

import com.lge.storetest.domain.Post
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store

typealias PostStore = MutableStore<Int, Post>
//typealias PostStore = Store<Int, Post>
