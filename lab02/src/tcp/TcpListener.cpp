/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#include <tcp/TcpListener.hpp>

TcpListener::TcpListener()
    : _sockfd(0)
    , _portno(80)
    , _sock_addrinfo(NULL)
    , _opened(false)
    , _listening(false)
    , _blocking(true)
{}

TcpListener::~TcpListener()
{
    closeSocket();
}

// Private methods:
addrinfo *TcpListener::getAddressInfo(
    const char *portStr, const TcpListener::ModeFlags modeFlags)
{
    addrinfo hints, *resultInfo;

    memset(&hints, 0, sizeof hints);
    // Check mode:
    if (modeFlags & M_AF_UNSPEC) {
        if (modeFlags & M_AF_IPV4) {
            hints.ai_family = AF_INET6;
        } else {
            hints.ai_family = AF_UNSPEC;
        }
    } else {
        // Assume using IPv4:
        hints.ai_family = AF_INET;
    }
    // Set to streaming sockets (TCP):
    hints.ai_socktype = SOCK_STREAM;
    // TODO: Later I should add the possibility to create non-passive listeners
    hints.ai_flags = AI_PASSIVE;

    // Get address info:
    int status;
    if ((status = getaddrinfo(NULL, portStr, &hints, &resultInfo)) != 0) {
        throw TcpException::createErrnoTcpException(
            "Failed to get address info"
        );
    }

    return resultInfo;
}


void TcpListener::closeSocket() {
    if (_opened) {
        freeaddrinfo(_sock_addrinfo);
        _sock_addrinfo = NULL;
    }

    if (_sockfd) {
        close(_sockfd);
    }
}

// Public methods:
void TcpListener::open(const int portno, const TcpListener::ModeFlags modeFlags)
{
    // Check port:
    if (portno < 0 || portno > 65535) {
        throw TcpException("Port number out of range!");
    }
    // Values to be used:
    std::string portStr = std::to_string(portno);

    // Close old socket:
    closeSocket();

    // Get host address info:
    addrinfo *info = getAddressInfo(portStr.c_str(), modeFlags);

    // Get socket file decriptor:
    _sockfd = socket(info->ai_family, info->ai_socktype, info->ai_protocol);
    if (_sockfd < 0) {
        throw TcpException::createErrnoTcpException("Failed to open socket");
    }

    int yes = 1;

    if (setsockopt(_sockfd, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof yes) == -1) {
        throw TcpException::createErrnoTcpException("Failed to reuse socket");
    }

    // Let's bind the socket to the address:
    if (bind(_sockfd, info->ai_addr, info->ai_addrlen) < 0) {
        closeSocket();
        throw TcpException::createErrnoTcpException(
            "Failed to bind address to socket"
        );
    }

    // Server opened:
    _opened = true;
    _sock_addrinfo = info;

    // Set blocking state:
    if (modeFlags & TcpListener::M_NONBLOCK) {
        try {
            setBlockingState(false);
        } catch (TcpException &ex) {
            closeSocket();
            throw;
        }
    }
}

void TcpListener::startListen(const int maxQueue) {
    if (listen(_sockfd, maxQueue) < 0) {
        throw TcpException::createErrnoTcpException(
            "Failed to start listening"
        );
    }

    _listening = true;
}


TcpSocket TcpListener::acceptConn() {
    sockaddr_storage client_addr;
    socklen_t addr_size = sizeof client_addr;
    int client_fd = accept(_sockfd, (sockaddr *)&client_addr, &addr_size);

    if (client_fd < 0) {
        throw TcpException::createErrnoTcpException("Failed to accept client");
    }

    return TcpSocket(client_fd, client_addr);
}


void TcpListener::setBlockingState(const bool blockingState) {
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
