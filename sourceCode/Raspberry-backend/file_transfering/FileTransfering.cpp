/*
 * FileTransfering.cpp
 *
 *  Created on: Jan 12, 2018
 *      Author: paris
 */
#include <stdio.h>
#include <sys/wait.h>	     /* sockets */
#include <sys/types.h>	     /* sockets */
#include <sys/socket.h>	     /* sockets */
#include <netinet/in.h>	     /* internet sockets */
#include <netdb.h>	         /* gethostbyaddr */
#include <unistd.h>	         /* fork */
#include <stdlib.h>	         /* exit */
#include <ctype.h>	         /* toupper */
#include <signal.h>          /* signal */
#include<iostream>
#include<string.h>
#include <fcntl.h>
#include <sys/stat.h>
#include<sys/shm.h>
#include<sys/sem.h>
#include<sys/ipc.h>
#include "FileTransfering.h"

#define null NULL
using namespace std;

void flts::sendFile(char*type,char* filename,char* path,int socket){
	char buf[255];
	int file;
	file = open(path, O_RDONLY);
	struct stat status;
	fstat(file, &status);
	if(filename!=null){
		write(socket,filename,strlen(filename)+1);
		read(socket,buf,sizeof(buf));
	}
	else
		strcpy(buf,(char*)"ok_name");

	if(strcmp(type,"image")==0){
		if(strcmp(buf,(char*)"ok_name")==0){
			int size = status.st_size;
			sprintf(buf,"%d",size);
			write(socket,buf,strlen(buf)+1);
			read(socket,buf,sizeof(buf)); // Make sure file was sent ok
			if(strcmp(buf,(char*)"ok_size")==0){
				ssize_t nread = 0;
				while (nread = read(file, buf, sizeof(buf)), nread > 0){
					char *out = buf;
					ssize_t nwritten;
					do {
						nwritten = write(socket, out, nread);

							if (nwritten >= 0){
								nread -= nwritten;
								out += nwritten;
							}
					} while (nread > 0);
				}
			}
		}
	}
	else if(strcmp(type,"folder")==0){
		if(strcmp(buf,(char*)"all_ok")==0)
			cout<<"Folder was downloaded"<<endl;
	}
}

int flts::receiveFile(int socket,char*path){
    
    int size,success=-1;
	char buf[1024]; //buffer to read

	//TODO Save at path defined by user
	if( access( path, F_OK ) != -1 ) {
		cout<<"File Already exists, inform client"<<endl;
		strcpy(buf,(char*)"File Already Exists\0");
		write(socket,buf,strlen(buf)+1);
		return  -1;
	}
	else {
		strcpy(buf,(char*)"Cont_size");
		write(socket, buf,strlen(buf)+1);
	    read(socket,buf,sizeof(buf));
	    size = atoi(buf);
//	    cout<<"Received file_size :" << size<<endl;

	    int file;
		file = open(path, O_CREAT | O_EXCL | O_WRONLY,0777);
		ssize_t nread = 0;
		ssize_t total = 0;

	    strcpy(buf,(char*)"Cont_File");
	    write(socket,buf,strlen(buf)+1);
		strcpy(buf,"");
		while (nread = read(socket, buf, sizeof(buf)),total<size){
			total += nread;
			char *out_ptr = buf;
			ssize_t nwritten;
			do {
				nwritten = write(file, out_ptr, nread);
				if (nwritten >= 0)
				{
					nread -= nwritten;
					out_ptr += nwritten;
				}
			} while (nread > 0);
        if(total == size) break;
		}
		close(file);
//	    cout << "bytes transfered : " << total << endl;
		cout << "file : "<< path << " has been transfered" << endl;
		strcpy(buf,(char*)"File_Received");
		write(socket,buf,strlen(buf)+1);
		success =  1;
	}
	return success;  //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
}

