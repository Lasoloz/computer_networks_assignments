/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab02
*/

#include <http/req/Request.hpp>

#define BUF_SIZE 1024
#define MES_LEN BUF_SIZE * sizeof(char)


constexpr const char Request::ERR_404_HEAD[];


Request::Request(std::string path) : _path(path) {}


void Request::answerRequest(TcpSocket &sock) {
    if (_path == "/") {
        const char buf[] = "HTTP/1.1 200 OK\r\n"
            "Content-Type: text/plain\r\n\r\n"
            "The mini HTTP server is working!\n";

        if (sock.sendMsg(buf, sizeof(buf) - 1) < 0) {
            throw std::runtime_error("Packet send failed");
        }
    } else {
        std::string relPath = checkFileExistence();

        if (relPath.size() == 0) {
            sendNotFound(sock);
            throw std::runtime_error("Requested file does not exist");
        }

        std::pair<std::ifstream, std::string> result =
            matchFileExtension(relPath, sock);

        // Send response head:
        std::ostringstream oss;
        oss << "HTTP/1.1 200 OK\r\nContent-Type: " << result.second
            << "\r\n\r\n ";

        std::string head = oss.str();
        sock.sendMsg(head.c_str(), head.size() - 1);

        // Send response content:
        sendContent(result.first, sock);
    }
}


Request Request::parseReqest(TcpSocket &sock) {
    bool invalidMethod = false;

    // Use stringstream to parse small parts of the buffer:
    std::stringstream requestCollector;
    char buf[BUF_SIZE];
    bool receiving = true;

    do {
        int received = sock.recvMsg(buf, MES_LEN);
        if (received < 0) {
            throw std::runtime_error("Packet receive failed");
        }

        requestCollector << buf;

        if (received < MES_LEN) {
            receiving = false;
        }
    } while (receiving);


    std::string line;
    // Get method:
    std::getline(requestCollector, line, ' ');
    if (line != "GET") {
        throw std::runtime_error("Invalid method in http request");
    }

    // Get path:
    std::getline(requestCollector, line, ' ');
    std::string path = line;

    // Get protocol type:
    std::getline(requestCollector, line, '\r');
    if (line != "HTTP/1.1") {
        throw std::runtime_error("Invalid protocol definition");
    }

    return Request(path);
}


std::ostream& operator<< (std::ostream &os, const Request &req) {
    os << "Request{ path=" << req._path << " }";
    return os;
}



// Private:
void Request::sendNotFound(TcpSocket &sock) {
    std::ostringstream oss;
    oss << Request::ERR_404_HEAD <<
    "<!DOCTYPE html><html><head><title>404 Not Found</title></head>"
    "<body><h1>Error 404: Not Found</h1><p>The requested resource (<code>" <<
    _path << "</code>) was not found</p></body></html>";

    std::string answer = oss.str();
    if (sock.sendMsg(answer.c_str(), answer.size() - 1) < 0) {
        throw std::runtime_error("Failed to send 404 error");
    }
}

std::pair<std::ifstream, std::string> Request::matchFileExtension(
    std::string &relPath,
    TcpSocket &sock
) {
    // Find extension:
    std::regex extension(
        ".*\\.([0-9A-Za-z]+)$",
        std::regex_constants::icase
    );

    std::cmatch m;
    std::regex_match(relPath.c_str(), m, extension);

    if (m.size() < 2) {
        sendNotFound(sock);
        throw std::runtime_error(
            "Requested file has wrong or no extension (match size)"
        );
    }

    // Check extension:
    std::string ext(m[1]);
    std::string mime;
    bool binary = false;
    if (ext == "txt") {
        mime = "text/plain";
    } else if (ext == "htm" || ext == "html") {
        mime = "text/html";
    } else if (ext == "jpg" || ext == "jpeg") {
        mime = "image/jpeg";
        binary = true;
    } else if (ext == "png") {
        mime = "image/png";
        binary = true;
    } else if (ext == "gif") {// `g` like in gift or graphics
        mime = "image/gif";
        binary = true;
    } else if (ext == "flv") {
        mime = "application/x-shockwave-flash";
        binary = true;
    } else if (ext == "css") {
        mime = "text/css";
    } else if (ext == "ogv") {
        mime = "video/ogg";
        binary = true;
    } else {
        sendNotFound(sock);
        throw std::runtime_error("Extension not supported");
    }

    // Try opening file:
    try {
        std::ifstream input;
        if (binary) {
            input.open(relPath, std::ios_base::binary);
        } else {
            input.open(relPath);
        }

        // Return pair:
        return std::pair<std::ifstream, std::string>(
            std::move(input), std::move(mime)
        );
    } catch (std::exception &ex) {
        throw std::runtime_error("Failed to open file");
    }

}


std::string Request::checkFileExistence() const {
    std::string fname = _path.substr(1);

    struct stat buffer;

    if (stat(fname.c_str(), &buffer) == 0) {
        return fname;
    } else {
        return "";
    }
}


void Request::sendContent(std::ifstream &ifile, TcpSocket &sock) {
    // Length of file:
    ifile.seekg(0, ifile.end);
    int length = ifile.tellg();
    ifile.seekg(0, ifile.beg);

    int chunkcount = 0;
    int diff = length - chunkcount;
    char buf[BUF_SIZE];;

    while (diff > 0) {
        int currentChunkSize = std::max(diff, BUF_SIZE);

        int bytecount = ifile.readsome(buf, BUF_SIZE);

        sock.sendMsg(buf, bytecount * sizeof(char));

        chunkcount += bytecount;

        diff = length - chunkcount;
    }
}
