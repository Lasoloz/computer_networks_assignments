/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.message;

public enum MessageType {
    LOGIN,
    LOGOUT,
    LOGIN_NOTIF,
    LOGOUT_NOTIF,
    ACCEPT,
    REJECT,
    BROADCAST,
    WHISPER,
    BROADCAST_INCOMING,
    WHISPER_INCOMING
}
