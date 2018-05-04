/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.server;

import com.gitlab.lasoloz.irc.message.Message;

public class MessageBundle {
    private Message message;
    private ClientHandler handler;


    public MessageBundle(Message message, ClientHandler handler) {
        this.message = message;
        this.handler = handler;
    }


    public Message getMessage() {
        return message;
    }

    public ClientHandler getHandler() {
        return handler;
    }
}
