package com.aamovies.admin.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Movie(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("year") @set:PropertyName("year") var year: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("language") @set:PropertyName("language") var language: String = "",
    @get:PropertyName("quality") @set:PropertyName("quality") var quality: String = "",
    @get:PropertyName("poster") @set:PropertyName("poster") var poster: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = 0L,
    @get:PropertyName("trending") @set:PropertyName("trending") var trending: Boolean = false,
    @get:PropertyName("pinned") @set:PropertyName("pinned") var pinned: Boolean = false,
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "Movie",
    @get:PropertyName("genre") @set:PropertyName("genre") var genre: String = "",
    @get:PropertyName("upcoming") @set:PropertyName("upcoming") var upcoming: Boolean = false,
    @get:PropertyName("featured") @set:PropertyName("featured") var featured: Boolean = false,
    @get:PropertyName("screenshots") @set:PropertyName("screenshots")
    var screenshots: Map<String, String> = emptyMap(),
    @get:PropertyName("downloadLinks") @set:PropertyName("downloadLinks")
    var downloadLinks: Map<String, DownloadLink> = emptyMap()
)

@IgnoreExtraProperties
data class DownloadLink(
    @get:PropertyName("label") @set:PropertyName("label") var label: String = "",
    @get:PropertyName("url") @set:PropertyName("url") var url: String = "",
    @get:PropertyName("size") @set:PropertyName("size") var size: String = ""
)

@IgnoreExtraProperties
data class Category(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("movieCount") @set:PropertyName("movieCount") var movieCount: Int = 0,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = 0L
)
