/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#include "TcpSocket.hpp"

TcpSocket::TcpSocket(const int socket, sockaddr_storage sockinfo)
    : _sockfd(socket)
    , _sockinfo(sockinfo)
{}

TcpSocket::~TcpSocket() {
    if (_sockfd) {
        close(_sockfd);
    }
}

TcpSocket::TcpSocket(TcpSocket &&other)
    : _sockfd(other._sockfd)
    , _sockinfo(other._sockinfo)
{
    other._sockfd = 0;
}

TcpSocket& TcpSocket::operator= (TcpSocket &&other) {
    _sockfd = other._sockfd;
    _sockinfo = other._sockinfo;
    other._sockfd = 0;
}


int TcpSocket::sendMsg(const void *msg, const int len) {
    int bytes_sent = send(_sockfd, msg, len, 0);

    if (bytes_sent < 0) {
        throw TcpException::createErrnoTcpException("Failed to send message");
    }

    return bytes_sent;
}

int TcpSocket::recvMsg(void *msg, const int len) {
    int bytes_received = recv(_sockfd, msg, len, 0);

    if (bytes_received < 0) {
        throw TcpException::createErrnoTcpException(
            "Failed to receive message"
        );
    }

    return bytes_received;
}
