// Heim László
// hlim1626
// 522-es csoport

#include <iostream>

#include <cstdlib>
#include <cstring>

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <errno.h>

#define MES_LEN 1024
#define PORT 8100

int main(int argc, char * argv[]) {
    // Create networking variables:
    int sockfd;
    sockaddr_in serv_addr;
    hostent *server;

    // Check command line arguments:
    if (argc < 2) {
        std::cerr << "usage: " << argv[0] << " <hostname>\n";
        return 4;
    }

    // Open TCP socket descriptor
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        std::cerr << "Error opening socket: " << strerror(errno) << '\n';
        return 1;
    }

    // Get remote host name and create server:
    server = gethostbyname(argv[1]);

    if (server == NULL) {
        std::cerr << "Error, no such host!\n";
        return 1;
    }

    // Configure server:
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy(
        (char *)server->h_addr,
        (char *)&serv_addr.sin_addr.s_addr,
        server->h_length
    );
    serv_addr.sin_port = htons(PORT);

    // Connect to server:
    std::cout << "Connecting to server...\n";
    if (connect(sockfd, (sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        std::cerr << "Error connecting: " << strerror(errno) << '\n';
        return 1;
    }


    // Message:
    char buffer[MES_LEN];
    int n;
    std::cout << "Please input message: ";
    std::cin >> buffer;

    // Write message:
    n = write(sockfd, buffer, strlen(buffer));

    if (n < 0) {
        std::cerr << "Failed to write on socket!\n";
        close(sockfd);
        return 2;
    }

    std::cout << "Message written...\n";

    bzero(buffer, MES_LEN);
    // Read message:
    n = read(sockfd, buffer, MES_LEN - 1);

    if (n < 0) {
        std::cerr << "Failed to read from client " << strerror(errno) << '\n';
        close(sockfd);
        return 2;
    }
    std::cout << "Answer: " << buffer << '\n';

    // Close server:
    std::cout << "Closing socket...\n";
    close(sockfd);
}
