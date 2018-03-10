/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef TCP_SOCKET_INCLUDED
#define TCP_SOCKET_INCLUDED

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#include "TcpException.hpp"

class TcpSocket {
    int _sockfd;
    sockaddr_storage _sockinfo;

public:
    TcpSocket(const int socket, sockaddr_storage sockinfo);
    ~TcpSocket();

    TcpSocket(const TcpSocket&) = delete;
    TcpSocket& operator= (const TcpSocket&) = delete;
    TcpSocket(TcpSocket &&other);
    TcpSocket& operator= (TcpSocket &&other);


    int sendMsg(const void *msg, const int len);
    int recvMsg(void *msg, const int len);
};

#endif
