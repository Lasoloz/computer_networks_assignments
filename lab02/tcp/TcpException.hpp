/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef TCP_EXCEPTION_INCLUDED
#define TCP_EXCEPTION_INCLUDED

#include <stdexcept>
#include <string>

// For special tcp exceptions:
#include <sstream>
#include <cstring>
#include <errno.h>

class TcpException : public std::runtime_error {

public:
    TcpException(const std::string &message) noexcept;

    static TcpException createErrnoTcpException(const char *baseMsgStr) {
        std::ostringstream result;
        result << baseMsgStr << " with underlying error: " << strerror(errno);
        return TcpException(result.str());
    }
};

#endif
