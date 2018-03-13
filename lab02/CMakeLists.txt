# Heim László
# hlim1626
# 522-es csoport
# Lab02

cmake_minimum_required(VERSION 2.6)
project(lab02)

set (CMAKE_CXX_STANDARD 11)

include_directories(include)

add_executable(
    lab02
    hlim1626_server.cpp
    src/tcp/TcpException.cpp
    src/tcp/TcpListener.cpp
    src/tcp/TcpSocket.cpp
    src/http/HttpServer.cpp
    src/http/HttpServerLogger.cpp
    src/http/req/Request.cpp
)

target_link_libraries(lab02 pthread)
