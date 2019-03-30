/*
 * threadPool.h
 *
 *  Created on: Feb 11, 2018
 *      Author: timos
 */

#include <pthread.h>
#include <iostream>
#include <stdio.h>
#include <queue>
#include <unistd.h>
#include <cstring>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <exception>
#include <map>
#include <list>
#include <experimental/filesystem> // C++-standard header file name

#include "../file_transfering/FileTransfering.h"
#include "../FilesManager/FileManager.h"
#include "../Exceptions/Exceptions.h"
#include "../Directory_Structure/Directory_Structure.h"

#define null NULL

#include <fstream>

using namespace std;

#ifndef THREAD_POOL_THREADPOOL_H_
#define THREAD_POOL_THREADPOOL_H_



enum Jobs { Delete=0,Upload,IndexFolder,SendPicture,RenameFile,Copy,Move,Download,CreateDirectory};

//Information needed by thread in order to execute their job

typedef struct{

	Jobs job_to_do;
	bool  memory_alloc[10];

	char* workingDirectoryPath; //0

	char* pic_name; //1
	char* new_pic_name; //2

	char*  folderPath; //3
	char* thumbPath; //4

	char* renamePath; //5
	char* renameThumPath; //6

	char* movePath; //7
	char* moveThumbPath; //8

	char* copyPath; //9
	char* copyThumbPath; //10

	int prev_fd;

	int main_port;
	int pipe_fd;

	bool multiplefiles;

}thread_task_info;


class pool_thread{

	//My threads are I/O bound, mostly they read/create/delete files and just send a verification code

private:
	queue<thread_task_info*> *the_queue;
	int number_of_workers;
	pthread_t* thread_pool;
	bool is_there_job;
	pthread_mutex_t m; // Protects the condition Variable itself
	pthread_cond_t cv = PTHREAD_COND_INITIALIZER;
	bool should_exit;
	bool initializing = true;
	tree* data_structure;

public:
	pool_thread(int number_of_workers);
	~pool_thread();
	void add_job(thread_task_info *job);
	void *threadPooling(void* args);
	static void* thread_pool_wrap(void* args); //Static member needed by pthread_create(), does not know (this)
	void kill_threads();
	thread_task_info* initialize_parameters(bool list_of_files,Jobs job,char* folderPath,char* thumbPath,int main_port,int socket_fd,int pipe_fd,char* path_extension,char* rename_extension,char* move_extension,char* copy_extension);
	void destroy_parameter(thread_task_info* previous_job);

	void sendFolderList(thread_task_info* previous_job);
	void deletefile(thread_task_info* previous_job);
	void createDirectory(thread_task_info* previous_job);
	void renameFile(thread_task_info* previous_job);
	void showPicture(thread_task_info* previous_job);
	void downloadFiles(thread_task_info* previous_job);
	void MoveFile(thread_task_info* previous_job);
	void CopyFile(thread_task_info* previous_job);
	char* get_pic_name(char* path_extension);
	char* get_working_directory(char* folderPath,char* path_extension);
	void download_folder(char* path_of_folder,int accept_fd,char* folder_name);
	void upload_pic(thread_task_info* previous_job);
};

#endif /* THREAD_POOL_THREADPOOL_H_ */
