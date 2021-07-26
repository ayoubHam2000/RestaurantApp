package com.example.restaurantapp.services

import com.example.restaurantapp.Utilities.URL
import com.github.nkzawa.socketio.client.IO
import java.net.URISyntaxException

object Soket {

    fun sendSocket(){
        try {

            //if you are using a phone device you should connect to same
            // local network as your laptop and disable your pubic firewall as well
            val socket = IO.socket(URL)

            //create connection
            socket.connect()

            // emit the event join along side with the nickname
            socket.emit("join","123")


        }
        catch (e : URISyntaxException)
        {
            e.printStackTrace()

        }
    }

}