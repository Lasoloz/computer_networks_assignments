/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.server;

class ClientThread {
    public Thread thread;
    public ClientHandler handler;

    ClientThread(ClientHandler handler) {
        this.handler = handler;

        thread = new Thread(handler);
    }
}
