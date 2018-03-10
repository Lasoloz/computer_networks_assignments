/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/
#ifndef HTTP_SERVER_LOGGER
#define HTTP_SERVER_LOGGER

#include <iostream>
#include <string>
#include <mutex>

class HttpServerLogger {
    std::ostream *_out;
    bool _owned;
    std::mutex _srcMtx;

    template <typename Wr>
    inline void _write(Wr &arg);
    template <typename Wr, typename ...Wrs>
    inline void _write(Wr &arg, Wrs &...args);

public:
    HttpServerLogger(std::ostream *capturedStream);
    // HttpServerLogger(const std::string &filename);
    ~HttpServerLogger();

    HttpServerLogger(const HttpServerLogger&) = delete;
    HttpServerLogger& operator= (const HttpServerLogger&) = delete;

    // HttpServerLogger(HttpServerLogger &&hlogger);
    // HttpServerLogger& operator= (HttpServerLogger &&hlogger);

    template <typename ...Wrs>
    inline HttpServerLogger& write(Wrs &...args);

};


// Writer templated method:
template <typename ...Wrs>
inline HttpServerLogger& HttpServerLogger::write(Wrs &...args) {
    std::unique_lock<std::mutex> lck(_srcMtx);

    _write(args...);

    *_out << '\n';
}


// Private variadic templated methods:
template <typename Wr>
inline void HttpServerLogger::_write(Wr &arg) {
    *_out << arg;
}

template <typename Wr, typename ...Wrs>
inline void HttpServerLogger::_write(Wr &arg, Wrs &...args) {
    *_out << arg;
    _write(args...);
}


#endif
