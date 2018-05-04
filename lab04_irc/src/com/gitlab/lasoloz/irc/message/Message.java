/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.message;

import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    private MessageType type;
    private String[] messageList;

    public Message(MessageType type, String[] messageList) {
        this.type = type;
        this.messageList = messageList.clone();
    }

    public Message(MessageType type) {
        this.type = type;
    }


    public MessageType getType() {
        return type;
    }

    public String[] getMessageList() {
        return messageList;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", messageList=" + Arrays.toString(messageList) +
                '}';
    }
}
