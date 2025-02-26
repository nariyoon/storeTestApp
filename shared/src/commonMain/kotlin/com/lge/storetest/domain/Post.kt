package com.lge.storetest.domain

data class Post(
    val id: Int,
    val title: String?,
    val body: String?,
    val userId: Int?
)
