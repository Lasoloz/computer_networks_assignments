/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#include <iostream>
#include <memory>

#include "http/HttpServer.hpp"
#include "http/HttpServerLogger.hpp"

int main(int argc, char **argv) {
    int portno = 80;
    if (argc < 2) {
        std::cout << "No port specified! Using default `80`.\n";
        std::cout << " Call `" << argv[0]
            << " <PORTNO>` to start on specified port!\n";
    } else {
        portno = std::atoi(argv[1]);
        std::cout << "Using port `" << portno << "`\n";
    }

    HttpServer::setHttpServerLogger(
        std::make_shared<HttpServerLogger>(&std::cerr)
    );

    try {
        HttpServer server(portno);

        std::cout << "Attempting to start listening...\n";
        server.listen();
    } catch (TcpException &ex) {
        std::cerr << "Error occoured during initialization!\n";
    }
}
