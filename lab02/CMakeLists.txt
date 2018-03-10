# Heim László
# hlim1626
# 522-es csoport
# Lab02

cmake_minimum_required(VERSION 2.6)
project(lab02)

set (CMAKE_CXX_STANDARD 11)

add_executable(
    lab02
    hlim1626_server.cpp
    tcp/TcpException.cpp
    tcp/TcpListener.cpp
    tcp/TcpSocket.cpp
    http/HttpServer.cpp
    http/HttpServerLogger.cpp
)

target_link_libraries(lab02 pthread)
