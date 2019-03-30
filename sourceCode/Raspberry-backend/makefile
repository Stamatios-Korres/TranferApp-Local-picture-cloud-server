all: mainThread clientServer

CC = g++
CXXFLAGS  = -g -Wall 

#---------------------Raspberry Platform---------------------------------#

mainThread: Client_Management.o MainProgram/MainThread.cpp Socket_Handling.o FileTransfering.o  FilesManager.o  AuthenticationService.o Exception.o 
	$(CC) $(CXXFLAGS) -o mainThread  MainProgram/MainThread.cpp Client_Management.o Socket_Handling.o MySqlConnector.o FilesManager.o Exception.o FileTransfering.o AuthenticationService.o -lmysqlcppconn -std=c++11 
	
clientServer:  Socket_Handling.o  Exception.o Socket_Handling.o FileTransfering.o  Directory_Structure.o  FilesManager.o  AuthenticationService.o threadPool.o
	$(CC) $(CXXFLAGS) -o clientServer MainProgram/ClientServer.cpp  Exception.o Socket_Handling.o MySqlConnector.o Directory_Structure.o  FilesManager.o threadPool.o FileTransfering.o AuthenticationService.o -pthread -lmysqlcppconn -std=c++11 -lstdc++fs  
	
Client_Management.o : Client_Management/Client_Management.cpp 
	$(CC) $(CXXFLAGS) -c  Client_Management/Client_Management.cpp

threadPool.o: Thread_Pool/threadPool.cpp
	$(CC) $(CXXFLAGS) -c Thread_Pool/threadPool.cpp -std=c++11 
	
Socket_Handling.o: SockOperations/Socket_Handling.cpp 
	$(CC) $(CXXFLAGS) -c auth/MySqlConnector.cpp SockOperations/Socket_Handling.cpp  -std=c++11
	
Exception.o: Exceptions/Exception.cpp
	$(CC) $(CXXFLAGS) -c  Exceptions/Exception.cpp -std=c++11

FileTransfering.o: file_transfering/FileTransfering.cpp 
	$(CC) $(CXXFLAGS) -c file_transfering/FileTransfering.cpp

AuthenticationService.o: auth/AuthenticationService.cpp 
	$(CC) $(CXXFLAGS) -c auth/AuthenticationService.cpp 

MySqlConnector.o: auth/MySqlConnector.cpp
	$(CC) $(CXXFLAGS) -c auth/MySqlConnector.cpp

FilesManager.o: FilesManager/FilesManager.cpp
	$(CC) $(CXXFLAGS) -c FilesManager/FilesManager.cpp
	
Directory_Structure.o: Directory_Structure/Directory_Structure.cpp
	$(CC) $(CXXFLAGS) -c Directory_Structure/Directory_Structure.cpp

clean:
	rm -f  *o mainThread clientServer *.o *.txt
	
