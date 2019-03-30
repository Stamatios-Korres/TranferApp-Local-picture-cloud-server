/*
 * MainThread.cpp

 *
 *  Created on: Jan 10, 2018
 *      Author: timos
 */
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <cstdlib>
#include <stdlib.h>
#include <cstring>
#include <unistd.h>
#include <string.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>
#include <fcntl.h>
#include <exception>
#include <errno.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/prctl.h>

#include "../SockOperations/Socket_Handling.h"
#include "../Client_Management/Client_Management.h"
#include "../Exceptions/Exceptions.h"

using namespace std;

#define MAX_CON 10


#define MAX_LENGHT 10
#define null NULL

client_list* my_clients; //Connections will be saved here

void signal_callback(int signum){
	int status;
	pid_t p;
	if( (p = waitpid(-1,&status,WNOHANG))!=-1){
		if (WIFEXITED(status)){
			if(WEXITSTATUS(status) == 25)
				cout<<"Connection closed ... " << endl;
			else if(WEXITSTATUS(status) == 26)
				cout<<"Authentication Problem... " << endl;
		}
		else
			cout<< "Child with p_id: "<<p<<" died"<<" with status: "<<status<<endl;
		my_clients->client_exited(p);
	}
}

void signal_callback_proc_close(int signum){
	my_clients->print_list();
	delete my_clients;
	exit(1);
}


int main(){

	int server_fd, socket_num;
	pid_t pid;

	/*
	 * Sockets options - necessary information
	 */
	signal(SIGCHLD,signal_callback);
	signal(SIGINT,signal_callback_proc_close);

	struct sockaddr_in address;
	int port = 8081;
	int opt =1,size;
	my_clients = new client_list();


	/*
	 * 	FIND AVAILABLE PORT
	 */

	while(1){
		try{
			if ((server_fd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) == 0)
					throw socket_Exception((char*)"Cannot create the Socket Interface");
			if (setsockopt(server_fd, SOL_SOCKET , SO_REUSEADDR, &opt, sizeof(opt))==-1)
				throw socket_Exception((char*)"Error setting the Socket's options");
			address.sin_family = AF_INET;
			address.sin_addr.s_addr = htonl(INADDR_ANY);
			address.sin_port = htons(port);
			if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
			{
				throw socket_Exception((char*)" Port already in use");
			}
			if (listen(server_fd, 5) < 0)
			{
				throw socket_Exception((char*)"Maximum level reached");
			}
			break;
		}
		catch(socket_Exception& e){
			cout<<"exception1 : "<<e.what()<<endl;
			if( strcmp(e.what(),(char*)"BIND_ERR")==0 )
				port++;
			else
				return -1;
		}
	}
	cout<<"Waiting for connections at port "<< port <<endl;
	size = sizeof(address);

	/*
	 * select() returns (1) -1 meaning error (2) 0 meaning no event occurred and
	 * (3) >0 #sockets that have events pending
	 */

	/*
	 *  ACCEPT NEW CONNECTIONS
	 */
	char socket_fd[50];
	while(1){
		if ((socket_num = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1){
			perror("accept");
			exit(EXIT_FAILURE);
		}
		cout<<"Accepted new connection"<<endl; 	//Pass arguments to child process
		char* proc_args[4];
		for(int i=0;i<4;i++){
			proc_args[i] = new char[50];
		}
		sprintf(proc_args[2],"%d",port);
		proc_args[3]= NULL;
		sprintf(socket_fd,"%d",socket_num);
		strcpy(proc_args[1],socket_fd);
		strcpy(proc_args[0],"./clientServer");
		pid = fork();
		if(pid == -1 ){ // mistake
			perror("Fork failed");
			return -1;
		}
		if(pid>0){
			my_clients->add_client(pid,port,socket_num);
			my_clients->print_list();
			socket_num = -1;
		}
		else if(pid == 0){ // child
			close(server_fd);
			char *name = new char[100];
			strcpy(name,"log_file_");
			char *pid= new char[20];
			sprintf(pid,"%d",getpid());
			strcat(name,pid);
			strcat(name,(char*)".txt");
			int fd = open(name,O_WRONLY | O_CREAT,0666);
			dup2(fd,STDOUT_FILENO);
			delete[] name;
			delete[] pid;
			cout<<"Process with id: "<<getpid()<<" was created"<<endl;
			execv(proc_args[0],(char *const*)proc_args);
		}

	}
	return 0;
}

