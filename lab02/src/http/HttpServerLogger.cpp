/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * lab02
*/

#include <http/HttpServerLogger.hpp>

HttpServerLogger::HttpServerLogger(std::ostream *capturedStream) {
    _out = capturedStream;
    _owned = false;
}

// For future expansions:
// HttpServerLogger::HttpServerLogger(const std::string &filename) {
// }

HttpServerLogger::~HttpServerLogger() {
    if (_owned) {
        delete _out;
    }
}
