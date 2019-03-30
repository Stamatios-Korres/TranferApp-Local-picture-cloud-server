/*
 * Socket_Handling.cpp
 *
 *  Created on: Jan 13, 2018
 *      Author: timos
 */

#include "Socket_Handling.h"

#define NUMBER_OF_INSTRUCTIONS 5
#define null NULL

char** sct_hnd::split_input(char * input,int *size){
	char* split;
	char** tokens;
	tokens = new char*[NUMBER_OF_INSTRUCTIONS];
	int pos=0;
	for(int i=0;i<NUMBER_OF_INSTRUCTIONS;i++)
		tokens[i] = null;
	split = strtok(input," ");
	while(split != NULL && pos <5){
		tokens[pos] = new char[strlen(split) +1];
		strcpy(tokens[pos],split);
		pos++;
		split = strtok (NULL," \0");
	}
	size =&pos;
	return tokens;
	}

int sct_hnd::authenticate_user(AuthenticationService* Auth_Serv,char ** msg,char* path,char * paththumb,int socket_fd,int main_port){
		char buf[1024];
		int ret_value = 0;
		for(int i=0;i<1024;i++){
			buf[i]=' ';
		}
		if(msg != NULL){
			if(strcmp(msg[0],"register_req")==0){
				char finalPath[250];
				char finalPath1[250];

				strcpy(finalPath,path);
				strcat(finalPath,msg[1]);

				strcpy(finalPath1,paththumb);
				strcat(finalPath1,msg[1]);
				cout<<"Normal Path"<<finalPath<<endl;
				cout<<"Thumb Path"<<finalPath1<<endl;
				int success = Auth_Serv->addUser(msg[1],msg[2],path,paththumb);
				if(success == 1){
					if (mkdir(finalPath,0777) == -1){
						cout << "Error creating directory!" << endl;
						ret_value=-1;
						strcpy(buf,(char*)"Dir_Err");
					}
					else{
						strcat(finalPath,"/Pictures");
						if (mkdir(finalPath,0777) == -1){
							cout << "Error creating directory!" << endl;
							ret_value=-1;
							strcpy(buf,(char*)"Dir_Err");
						}
						else {
							cout <<"Directory " << finalPath << " successfully created" << endl;;
							strcat(finalPath1,"/ThumbnailPictures");
							if (mkdir(finalPath1,0777) == -1){
								cout << "Error creating directory!" << endl;
								ret_value=-1;
								strcpy(buf,(char*)"Thumb_Dir_Err");
							}
							else{
								cout << "Thumbnails directory " << finalPath1 << " successfully created" << endl;;
								strcpy(buf,"[");
								strcat(buf,msg[1]);
								strcat(buf,"]\n");
								strcat(buf,"path = ");
								strcat(buf,path);
								strcat(buf,"\n");
								return 1;
							}
						}
					}
				}
				else{
					strcpy(buf,(char*)"Sorry, username already exists");
					cout<<"Username already exists"<<endl;
				}
			}
			else if(strcmp(msg[0],"login_req")==0){
				int success = Auth_Serv->authenticate(msg[1],msg[2]);
				if(success == 1)
					return 1;
				else if(success ==0){
					strcpy(buf,(char*)"Connection refused, wrong credential");
					cout<<"User Gave wrong credential"<<endl;
				}
			}
			else{
				strcpy(buf,(char*)"Unauthorized");
				cout<<"Unauthorized"<<endl;
			}

		}
		write(socket_fd,buf,strlen(buf)+1);
		return ret_value;
	}

void sct_hnd::free_buffer(char** msg,int* size){
	for(int i = 0; i < *size; i++){
		if(msg[i])
			delete[] msg[i];
	}
	delete[] msg;
	*size = 0;
}

void sct_hnd:: reset(AuthenticationService* Auth_Serv,char** msg,int *size){
	cout<<"Process with p_id: "<<getpid()<<" closing socket"<<endl;
	sct_hnd::free_buffer(msg,size);
	delete Auth_Serv;
}



//mogrify -resize 60x60 -quality 50 -path ../../ThumbnailPictures/tim/  *.jpg





