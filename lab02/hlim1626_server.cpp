/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
 *
 * Build és futtatás:
 * ```sh
 * $ mkdir build && cd build
 * $ cmake .. && make
 * $ cd ../resources/
 * $ ../build/lab02 <portno>
 * ```
 *
 * Sigint bezárja a szervert _graceful_ módon
*/
#include <iostream>
#include <memory>
#include <unistd.h>
#include <signal.h>
#include <mutex>

#include <http/HttpServer.hpp>
#include <http/HttpServerLogger.hpp>

std::mutex globalPtrMtx;
std::shared_ptr<HttpServer> globalPtr;

void close(const char *signame) {
    std::cout << "Received " << signame << "! Attempting to close server...\n";
    std::unique_lock<std::mutex> lck(globalPtrMtx);
    if (globalPtr) {
        globalPtr->stop();
        std::cout << "Server got request!\n";
    }
}

void sigHandler(int sig) {
    if (sig == SIGINT) {
        close("SIGINT");
    } else if (sig == SIGQUIT) {
        close("SIGQUIT");
    }
}

int main(int argc, char **argv) {
    // Set up signal:
    if (signal(SIGINT, sigHandler) == SIG_ERR) {
        std::cerr << "Failed to set signal handler for SIGINT!\n";
    }
    if (signal(SIGQUIT, sigHandler) == SIG_ERR) {
        std::cerr << "Faield to set signal handler for SIGQUIT!\n";
    }

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
        globalPtr = std::make_shared<HttpServer>(portno);
        // HttpServer server(portno);

        std::cout << "Attempting to start listening...\n";
        // server.listen();
        globalPtr->listen();

        // Lock before destructing server:
        globalPtrMtx.lock();
    } catch (TcpException &ex) {
        std::cerr << "Error occoured during initialization!\n";
    }

    // Unlock after finished:
    globalPtrMtx.unlock();
    std::cout << "Server closed gracefully!\n";
}
