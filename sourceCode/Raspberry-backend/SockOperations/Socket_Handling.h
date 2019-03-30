
#include <mutex>
#include <pthread.h>
#include<stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <cstdlib>
#include <stdlib.h>
#include <cstring>
#include <unistd.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>
#include <fcntl.h>
#include <exception>
#include <iostream>
#include <sys/stat.h>
#include <errno.h>
#include <dirent.h>

#include "../auth/lib/Auth.h"
#include "../file_transfering/FileTransfering.h"
#include "../FilesManager/FileManager.h"
#include "../Exceptions/Exceptions.h"
using namespace std;



//Too many different structs




/*
 * Includes all function related to the sockets of our Local Server
*/


namespace sct_hnd{

	void free_buffer(char ** msg,int* size);

	char** split_input(char * input,int *size);

	int authenticate_user(AuthenticationService* Auth_Serv,char ** msg,char* path,char * paththumb,int socket_fd,int main_port);

	void* sendFolderList(void *FileInformation);

	void reset(AuthenticationService* Auth_Serv,char** msg,int *size);

	void* thread_transfer_file(void *args);

	void* deletefile(void *FileInformation);


}
