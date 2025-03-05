package com.example.foundlah

class Message {
    var text: String? = null
        private set
    var sender: String? = null
        private set

    var timestamp: Long? = null
        private set

    constructor()

    constructor(text: String?, sender: String?, timestamp: Long?) {
        this.text = text
        this.sender = sender
        this.timestamp = timestamp
    }
}