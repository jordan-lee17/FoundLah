package com.example.foundlah

class Convo {
    var user1: String? = null
        private set
    var user2: String? = null
        private set
    val chatroomID: String?
        get() = if (user1!!.compareTo(user2!!) < 0) {
            user1 + "_" + user2
        } else {
            user2 + "_" + user1
        }

    fun setUsers(users: List<String?>?) {
        if (users != null && users.size == 2) {
            this.user1 = users[0]
            this.user2 = users[1]
        } else {
            this.user1 = "Unknown"
            this.user2 = "Unknown"
        }
    }
}
