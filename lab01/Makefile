# Heim László
# hlim1626
# 522-es csoport

CPP=g++
CPPFLAGS=-g -Wall -Wpedantic

.PHONY: clean


hlim1626_client: hlim1626_client.o
	$(CPP) hlim1626_client.o -o hlim1626_client

hlim1626_client.o: hlim1626_client.cpp
	$(CPP) -std=c++11 -c hlim1626_client.cpp -o hlim1626_client.o $(CPPFLAGS)


clean:
	rm -f hlim1626_client hlim1626_client.o
