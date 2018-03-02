# Heim László
# hlim1626
# 522-es csoport

import socket
import sys

HOST = 'localhost'
PORT = 8100
MSG_LEN = 1024


# Echoer
class Echoer:
    # Start up TCP server:
    def __init__(self, host, port):
        try:
            self.port = port
            # Create streaming TCP socket
            self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # Bind the socket to port:
            self.serversocket.bind((host, port))
            # Start listening and queue up max. 1 users
            self.serversocket.listen(1)

            self.runs = True
        except OverflowError:
            print('Caught `OverflowError`. Port number might be incorrect!')
            raise RuntimeError()
        except OSError:
            print('Address might be in use!')
            raise RuntimeError()
        except:
            print('Unexpected exception!')
            raise RuntimeError()


    # Test if server is running:
    def isRunning(self):
        return self.runs


    # Close server socket:
    def close(self):
        self.serversocket.close()


    # Accept request...
    def blockUntilAccept(self):
        conn, addr = self.serversocket.accept()
        print('Client accepted from `{}`'.format(addr))

        self.__waitForMessage(conn, addr)


    # ... And process it
    def __waitForMessage(self, conn, addr):
        data = conn.recv(MSG_LEN)
        if not data:
            print('Failed to receive data from `{}`'.format(addr))
            return

        if data == b'exit':
            self.runs = False
            print('Exit request from `{}`'.format(addr))

        conn.send(data.upper())

        conn.close()
        print('Connection closed with `{}`'.format(addr))


# Main:
def main():
    # Start server:
    try:
        print('Starting server...')
        srv = Echoer(HOST, PORT)

        # Wait for incoming requests:
        print('Waiting for incoming requests...')
        while srv.isRunning():
            srv.blockUntilAccept()

        # Close server
        srv.close()
        print('Server finished!')
    except:
        print('Exiting...')


# Main:
if __name__ == '__main__':
    main()
