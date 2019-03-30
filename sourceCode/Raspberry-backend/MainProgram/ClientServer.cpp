/*
 * ClientServer.cpp
 *
 *  Created on: Jan 13, 2018
 *      Author: timos
 */
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <cstdlib>
#include <stdlib.h>
#include <cstring>
#include <unistd.h>
#include <arpa/inet.h>
#include <iostream>
#include <fcntl.h>
#include <sstream>
#include <unistd.h>
#include <signal.h>
#include <poll.h>


#include "../auth/lib/Auth.h"
#include "../auth/lib/MySqlConnector.h"
#include "../SockOperations/Socket_Handling.h"
#include "../FilesManager/FileManager.h"
#include "../Thread_Pool/threadPool.h"

using namespace std;
#define PORT 8081
#define NUMBER_OF_INSTRUCTIONS 5
#define THREADS 1
#define null NULL
#define port_for_pics 8000

int socket_fd;

char** msg = null;
int msg_lenght=0;

/*
*	-> Be careful with the array msg[], it size varies depending its size and/or null pointer
*	-> msg[] is used for storing the messages exchanged between the Server and the Client
*/

pthread_t thread_pool[THREADS];
pthread_t self;
pool_thread *my_pool;

void parent_died(int signum){
	if(self == pthread_self()){
		if(my_pool!=null)
			delete my_pool;
		sct_hnd::free_buffer(msg,&msg_lenght);
		close(socket_fd);
		cout<<"Main process exiting..."<<endl;
		exit(1);
	}
}


int main(int args, char* argv[]){

	signal(SIGINT,parent_died);
	char buf[1024];
	struct pollfd fds[2];
	int secondary_port = port_for_pics;
	int main_port = atoi(argv[2]);
	self = pthread_self();
	AuthenticationService* Auth_Serv = new AuthenticationService();
	socket_fd = atoi(argv[1]);
	char  initPath[250] ="/home/timos/Desktop/GitLab/sourceCode/ServerFiles/"; //These directories Should point at the directories where the
	char  thumbPath[250] = "/home/timos/Desktop/GitLab/sourceCode/ServerFiles/";// photos are stored
	char token[25] = "Token:PrivateToken";
	int exit_value=0;

	thread_task_info* new_job;
	my_pool = new pool_thread(THREADS);

	int Authent_sts = 0;
	while(Authent_sts == 0 || Authent_sts ==-1){
		sct_hnd::free_buffer(msg,&msg_lenght);
		for(int i=0;i<1024;i++)
			buf[i] = ' ';
		cout<<"Waiting for credential"<<endl;
		int read_char = read(socket_fd,buf,1024);
		if(read_char<=0){
			if(read_char == -1)
				cout<<"Error occurred"<<endl;
			else
				cout<<"Connection was closed"<<endl;
			sct_hnd::reset(Auth_Serv,msg,&msg_lenght);
			exit_value = 25;
			close(socket_fd);
			exit(exit_value);
		}
		msg = sct_hnd::split_input(buf,&msg_lenght);
		Authent_sts = sct_hnd::authenticate_user(Auth_Serv,msg,initPath,thumbPath,socket_fd,main_port);
	}
	strcat(initPath,msg[1]);
	strcat(initPath,(char*)"/Pictures");
	strcat(thumbPath,msg[1]);
	strcat(thumbPath,(char*)"/ThumbnailPictures");
	sct_hnd::free_buffer(msg,&msg_lenght);

	cout<<"Token and Paths were created, informing user"<<endl;

	//Initialize the socket through which the pics will be send
	int pic_socket_fd;
	sockaddr_in address;
	struct linger so_linger;
		so_linger.l_onoff = 1;
		so_linger.l_linger = 0;
		while(1){
			try{
				if ((pic_socket_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
						throw socket_Exception((char*)"Cannot create the Socket Interface");
				if (setsockopt(pic_socket_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
					throw socket_Exception((char*)"Error setting the Socket's options");
				address.sin_family = AF_INET;
				address.sin_addr.s_addr = htonl(INADDR_ANY);
				address.sin_port = htons(secondary_port);
				if (bind(pic_socket_fd, (struct sockaddr *)&address,sizeof(address))==-1)
				{
					throw socket_Exception((char*)" Port already in use");
				}
				if (listen(pic_socket_fd, 5) < 0)
				{
					throw socket_Exception((char*)"Maximum level reached");
				}
				break;
			}
			catch(socket_Exception& e){
				secondary_port++;
			}
		}
	int size = sizeof(address);


	//Create actual token//
	write(socket_fd,token,strlen(token)+1);
	read(socket_fd,buf,1024);

	sprintf(buf,"%d",secondary_port); //Through this port, pictures will be send
	write(socket_fd,buf,strlen(buf)+1);
	memset(fds, 0 , sizeof(fds));

	fds[0].fd = socket_fd;
	fds[0].events = POLLIN;
	fds[1].fd = pic_socket_fd;
	fds[1].events = POLLIN;
	int fds_size = 2;
	bool should_exit = false;

	new_job = my_pool->initialize_parameters(false,IndexFolder,initPath,thumbPath,main_port,socket_fd,-1,null,null,null,null);
	my_pool->add_job(new_job);

	while(1){
		if(should_exit)
			break;
		 int rc = poll(fds, fds_size, -8);
		 if (rc < 0){
		       perror("  poll() failed");
		       break;
		 }

		 for (int socket_number = 0; socket_number < fds_size; socket_number++){
			 if(fds[socket_number].revents == 0)
				 continue;
			 if(fds[socket_number].revents != POLLIN){
				printf("  Error! revents = %d\n", fds[socket_number].revents);
				should_exit = 1;
				break;

			  }

			 // Initial socket, used for serving the client

			 if (fds[socket_number].fd == socket_fd){
//				 cout<<"Socket found as primary is: "<<socket_number<<endl;
				for(int i=0;i<1024;i++)
					buf[i] = ' ';
				int read_char = read(socket_fd,buf,1024);

				if(read_char<=0){ // Connection is dead
					if(read_char == -1)
						cout<<"Error occurred"<<endl;
					else
						cout<<"Connection was closed"<<endl;
					sct_hnd::reset(Auth_Serv,null,&msg_lenght);
					exit_value = 25;
					should_exit = 1;
					close(socket_fd);
					break;
				}
				msg = sct_hnd::split_input(buf,&msg_lenght);
				if(strcmp(msg[0],token)!=0){ // Wrong Credential by User
					cout<<"Authentication Problem,The user gave this credential "<<endl;
					sct_hnd::reset(Auth_Serv,msg,&msg_lenght);
					exit_value = 26;
					break;
				}
				else{
					for(int i=0;i<1024;i++){
						buf[i]=' ';
					}
					if(msg != NULL){

						if(strcmp(msg[1],(char*)"terminate_con")==0){
							sct_hnd::reset(Auth_Serv,msg,&msg_lenght);
							exit_value = 25;
							if(read(socket_fd,buf,sizeof(buf)+1)==0) //Wait for client to close first
								close(socket_fd);
							should_exit = 1;
							break;
						}
						bool many_files = (strcmp(msg[2],(char*)"list_of_files")==0) ? true:false;
						if(strcmp(msg[1],"download_req")==0){
							cout<<"Download Request"<<endl;
							new_job = my_pool->initialize_parameters(false,Download,initPath,thumbPath,main_port,socket_fd,-1,null,null,null,null);
							my_pool->add_job(new_job);
							//User want to download a picture
						}
						else if(strcmp(msg[1],"switch_curr_dir_req")==0){
							new_job = my_pool->initialize_parameters(many_files,IndexFolder,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,null,null);
							my_pool->add_job(new_job);
						}
						else if(strcmp(msg[1],"delete_file_req")==0){
							new_job = my_pool->initialize_parameters(many_files,Delete,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,null,null);
							my_pool->add_job(new_job);
						}
						else if(strcmp(msg[1],(char*)"mkdir_req")==0){
							new_job = my_pool->initialize_parameters(many_files,CreateDirectory,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,null,null);
							my_pool->add_job(new_job);
						}
						else if(strcmp(msg[1],(char*)"rename_file_req")==0){
							new_job = my_pool->initialize_parameters(many_files,RenameFile,initPath,thumbPath,main_port,socket_fd,-1,msg[2],msg[3],null,null);
							my_pool->add_job(new_job);
						}

						else if(strcmp(msg[1],"upload_pic_req")==0){
						//User want to download a picture

						}
						else if(strcmp(msg[1],"mv_file_req")==0){
						//User want to download a picture
							if(many_files)
								new_job = my_pool->initialize_parameters(many_files,Move,initPath,thumbPath,main_port,socket_fd,-1,null,null,null,null);
							else
								new_job = my_pool->initialize_parameters(many_files,Move,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,msg[3],null);
							my_pool->add_job(new_job);

						}
						else if(strcmp(msg[1],"cp_file_req")==0){
							if(many_files)
								new_job = my_pool->initialize_parameters(many_files,Copy,initPath,thumbPath,main_port,socket_fd,-1,null,null,null,null);
							else
								new_job = my_pool->initialize_parameters(many_files,Copy,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,null,msg[3]);
							my_pool->add_job(new_job);

						}
						else if(strcmp(msg[1],"upload_file_req")==0){
							cout<<"Upload request"<<endl;
							new_job = my_pool->initialize_parameters(many_files,Upload,initPath,thumbPath,main_port,socket_fd,-1,msg[2],null,null,null);
							my_pool->add_job(new_job);
						}

					}
					else{
						cout<<"Error with message"<<endl;
						strcpy(buf,"Error");
						write(socket_fd,buf,strlen(buf)+1);
					}
				}
				sct_hnd::free_buffer(msg,&msg_lenght);
				msg = null;
			 }
			 else if(fds[socket_number].fd == pic_socket_fd){
				 int new_socket;
				 if ((new_socket = accept(pic_socket_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
						perror("accept");
				 new_job = my_pool->initialize_parameters(false,SendPicture,initPath,thumbPath,port_for_pics,new_socket,-1,null,null,null,null);
				 my_pool->add_job(new_job);

			 }
		 }
	}
	delete my_pool;
	cout<<"Exiting... "<<exit_value<<endl;
	exit(exit_value);
}

