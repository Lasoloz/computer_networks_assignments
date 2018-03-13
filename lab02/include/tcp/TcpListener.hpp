/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef TCP_LISTENER_INCLUDED
#define TCP_LISTENER_INCLUDED

#include <string>

// #include <cstdio>

#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#include "TcpException.hpp"
#include "TcpSocket.hpp"


class TcpListener {
    int _sockfd;
    int _portno;
    addrinfo *_sock_addrinfo;

    bool _opened;
    bool _listening;

    bool _blocking;

public:
    // Sub-enum type for different settings:
    // 00000010 unspecified
    // 00000100 IPv4
    // 00000110 IPv6
    typedef int ModeFlags;
    static constexpr ModeFlags M_NONE = 0x0;
    static constexpr ModeFlags M_NONBLOCK = 0x1;
    static constexpr ModeFlags M_AF_UNSPEC = 0x2;
    static constexpr ModeFlags M_AF_IPV4 = 0x4;
    static constexpr ModeFlags M_AF_IPV6 = M_AF_UNSPEC | M_AF_IPV4;

private:
    // Local methods:
    addrinfo *getAddressInfo(const char *, const ModeFlags);

    void closeSocket();

public:
    TcpListener();
    ~TcpListener();
    TcpListener(const TcpListener &) = delete;
    TcpListener & operator=(const TcpListener &) = delete;

    void open(const int, const ModeFlags);
    void startListen(const int maxQueue = 10);
    TcpSocket acceptConn();

    // Additional setups:
    void setBlockingState(const bool blockingState);

    // Information:
    // const bool isListening() const;
    // const bool  isBlocking() const;
    // const std::string getLocalAddress() const;
};

#endif
