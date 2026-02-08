package com.example.politicalevents.network


import okhttp3.*

class CommunityWebSocket(
    private val onUpdate: () -> Unit
) : WebSocketListener() {

    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder()
            .url(ApiConfig.WS_URL)
            .build()
        client.newWebSocket(request, this)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onUpdate() // re-fetch discussions
    }
}
