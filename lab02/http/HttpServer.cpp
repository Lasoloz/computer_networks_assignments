/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/

#include "HttpServer.hpp"

#define SERVER_LISTEN_INTERVAL 500
#define HTTP_SERVER_HEAD "[HttpServer] "
#define HTTP_SERVER_THR_HEAD "[HttpServer:"

// Static member:
std::shared_ptr<HttpServerLogger> HttpServer::_loggerPtr  =
    std::make_shared<HttpServerLogger>(&std::cerr);
std::shared_ptr<HttpServerLogger> HttpServer::_accessLogPtr =
    std::make_shared<HttpServerLogger>(&std::cout);


// Constructor:
HttpServer::HttpServer(const int portno, const size_t max_threads)
    : _max_thread_count(max_threads)
    , _running_count(0)
    , _serverState(HttpServer::State::STOPPED)
{
    listener.open(portno, TcpListener::M_NONBLOCK);

    _answerThreads.resize(max_threads);
    _threadStates.resize(max_threads);
}


// Public methods:
void HttpServer::listen() {
    listener.startListen();
    _serverState = HttpServer::State::RUNNING;

    while (_serverState == HttpServer::State::RUNNING) {
        try {
            TcpSocket sock = listener.acceptConn();

            HttpServer::_loggerPtr->write(
                HTTP_SERVER_HEAD,
                "Accepted connection, attempting to start thread..."
            );
            // We don't need cleanup here, because `launchWorker` automatically
            // does that
            launchWorker(std::move(sock));
        } catch (...) {
            #ifdef DEBUG_BUILD
            HttpServer::_loggerPtr->write(
                HTTP_SERVER_HEAD,
                "No connection available. Waiting for ",
                SERVER_LISTEN_INTERVAL,
                "ms..."
            );
            #endif
            std::this_thread::sleep_for(
                std::chrono::milliseconds(SERVER_LISTEN_INTERVAL)
            );
            // Clean up finished workers:
            cleanup();
        }
    }
}


// Private methods:
void HttpServer::launchWorker(TcpSocket sock) {
    // Lock launch mutex:
    std::unique_lock<std::mutex> launchLck(_launchMutex);

    // Search for a free worker slot:
    size_t freePos = cleanup();
    while (freePos >= _max_thread_count) {
        freePos = cleanup();
    }

    HttpServer::_loggerPtr->write(
        HTTP_SERVER_HEAD,
        "Found thread #",
        freePos,
        " ready for work."
    );

    // Launch new worker thread:
    std::unique_lock<std::mutex> stateLck(_stateMutex);
    _answerThreads[freePos].reset(new std::thread(&HttpServer::worker, this));
    _threadStates[freePos] = HttpServer::State::RUNNING;
    ++_running_count;

    HttpServer::_loggerPtr->write(
        HTTP_SERVER_HEAD,
        "New thread #",
        freePos,
        " successfully started!"
    );
}


void HttpServer::worker() {
    std::thread::id tid = std::this_thread::get_id();
    HttpServer::_accessLogPtr->write(
        HTTP_SERVER_THR_HEAD,
        tid,
        "] Welcome from the thread"
    );
    HttpServer::_accessLogPtr->write(
        HTTP_SERVER_THR_HEAD,
        tid,
        "] Doing some heavy calculations..."
    );
    std::this_thread::sleep_for(std::chrono::seconds(3));
}


void HttpServer::setHttpServerLogger(
    std::shared_ptr<HttpServerLogger> loggerPtr
) {
    HttpServer::_loggerPtr = loggerPtr;
}

void HttpServer::setHttpServerAccessLog(
    std::shared_ptr<HttpServerLogger> loggerPtr
) {
    HttpServer::_accessLogPtr = loggerPtr;
}
