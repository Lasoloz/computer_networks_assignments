/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#include "TcpException.hpp"

TcpException::TcpException(const std::string &message) noexcept
    : std::runtime_error(message)
{}
