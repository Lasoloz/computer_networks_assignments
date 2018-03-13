/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef REQUEST_INCLUDED
#define REQUEST_INCLUDED

#include <fstream>
#include <string>
#include <sstream>
#include <stdexcept>
#include <regex>
#include <utility>
#include <algorithm>

#include <unistd.h>
#include <sys/stat.h>

#include "../../tcp/TcpSocket.hpp"

class Request {
    static constexpr const char ERR_404_HEAD[] = "HTTP/1.1 404 NOT FOUND\r\n"
    "Content-Type: text/html\r\n\r\n";

    std::string _path;

    Request(std::string);

    void sendNotFound(TcpSocket &);

    std::pair<std::ifstream, std::string> matchFileExtension(
        std::string &,
        TcpSocket &
    );

    std::string checkFileExistence() const;

    void sendContent(std::ifstream &, TcpSocket &);

public:
    void answerRequest(TcpSocket &sock);

    static Request parseReqest(TcpSocket &sock);

    friend std::ostream& operator<<(std::ostream &os, const Request &req);

};

#endif
