/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#include <tcp/TcpSocket.hpp>

TcpSocket::TcpSocket(const int socket, sockaddr_storage sockinfo)
    : _sockfd(socket)
    , _sockinfo(sockinfo)
    , _blocking(true)
{
    int flag = 1;
    setsockopt(_sockfd, IPPROTO_TCP, TCP_NODELAY, &flag, sizeof(int));
}

TcpSocket::~TcpSocket() {
    if (_sockfd) {
        shutdown(_sockfd, 2);
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


// Copy-paste... should reformat class structure...
void TcpSocket::setBlockingState(const bool blockingState) {
    if (blockingState != _blocking) {
        const int flags = fcntl(_sockfd, F_GETFL, 0);
        if (flags < 0) {
            throw TcpException::createErrnoTcpException(
                "Failed to get socket flags"
            );
        }

        int resultValue = fcntl(
            _sockfd,
            F_SETFL,
            blockingState ? flags ^ O_NONBLOCK : flags | O_NONBLOCK
        );

        if (resultValue < 0) {
            throw TcpException::createErrnoTcpException(
                "Failed to set blocking state"
            );
        }

        _blocking = blockingState;
    }
}
