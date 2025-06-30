package dev.romle.roamnoteapp.model

import java.io.Serializable

data class ForumPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String = "",
    val username: String = "",
    val timestamp: Long = 0L
) : Serializable {

    class Builder {
        private var id: String = ""
        private var title: String = ""
        private var content: String = ""
        private var userId: String = ""
        private var username: String = ""
        private var timestamp: Long = System.currentTimeMillis()

        fun id(id: String) = apply { this.id = id }
        fun title(title: String) = apply { this.title = title }
        fun content(content: String) = apply { this.content = content }
        fun userId(userId: String) = apply { this.userId = userId }
        fun username(username: String) = apply { this.username = username }
        fun timestamp(timestamp: Long) = apply { this.timestamp = timestamp }

        fun build() = ForumPost(id, title, content, userId, username, timestamp)
    }

    constructor() : this("", "", "", "", "", 0L)
}
