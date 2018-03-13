/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef HTTP_SERVER_INCLUDED
#define HTTP_SERVER_INCLUDED

#include <iostream>

#include <vector>
#include <memory>
#include <thread>
#include <mutex>
#include <chrono>

#include "../tcp/TcpListener.hpp"
#include "../tcp/TcpSocket.hpp"
#include "HttpServerLogger.hpp"
#include "req/Request.hpp"

class HttpServer {
    // inline size_t safeReadRunningCount();
    inline size_t cleanup();

    void launchWorker(TcpSocket);
    void worker(const int, TcpSocket);

public:
    enum class State {
        STOPPED  = 0,
        RUNNING  = 1,
        FINISHED = 2
    };

private:
    static std::shared_ptr<HttpServerLogger> _loggerPtr;
    static std::shared_ptr<HttpServerLogger>
    _accessLogPtr;

    TcpListener listener;

    size_t _max_thread_count;
    size_t _running_count;
    State _serverState;

    std::vector<std::unique_ptr<std::thread> > _answerThreads;
    std::vector<State> _threadStates;
    std::mutex _stateMutex;
    std::mutex _launchMutex;

public:
    HttpServer(const int portno=80, const size_t max_threads=10);

    void listen();
    void stop();

    static void setHttpServerLogger(
        std::shared_ptr<HttpServerLogger> loggerPtr
    );

    static void setHttpServerAccessLog(
        std::shared_ptr<HttpServerLogger> loggerPtr
    );

};



// Inline methods:
// inline size_t HttpServer::safeReadRunningCount() {
//     std::unique_lock<std::mutex> lck(_stateMutex);

//     return _running_count;
// }

inline size_t HttpServer::cleanup() {
    std::unique_lock<std::mutex> lck(_stateMutex);

    size_t freePos = _max_thread_count;

    for (size_t i = 0; i < _max_thread_count; ++i) {
        if (_threadStates[i] == HttpServer::State::STOPPED) {
            if (i < freePos) {
                freePos = i;
            }
            continue;
        } else if (_threadStates[i] != HttpServer::State::FINISHED) {
            continue;
        }
        // Else it is finished:
        _threadStates[i] = HttpServer::State::STOPPED;
        _answerThreads[i]->join();
        _answerThreads[i].reset();
        --_running_count;

        if (i < freePos) {
            freePos = i;
        }
    }

    return freePos;
}

#endif
