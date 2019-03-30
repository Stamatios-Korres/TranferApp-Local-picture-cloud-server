/*
 * threadPool.cpp
 *
 *  Created on: Feb 11, 2018
 *      Author: timos
 */

#include "threadPool.h"

namespace fs = std::experimental::filesystem;

pool_thread::pool_thread(int number){
	the_queue = new queue<thread_task_info*>;
	number_of_workers = number;
	thread_pool = new pthread_t[number_of_workers];
	should_exit = false;
	if( (pthread_cond_init(&cv, NULL)!=0) || (pthread_mutex_init(&m, NULL) != 0))
		cout<<"Problem creating Condition Variable, exiting ..."<<endl;
	for(int i=0;i<number_of_workers;i++){
		pthread_create(&thread_pool[i],null,pool_thread::thread_pool_wrap,this); // Why need this ?
	}
}

pool_thread::~pool_thread(){
	pool_thread::kill_threads();
	delete the_queue;
	delete thread_pool;
}

void pool_thread::kill_threads(){
		should_exit = true;
		pthread_mutex_lock(&m);
		pthread_cond_broadcast(&cv);
		pthread_mutex_unlock(&m);
		for(int i = 0;i<number_of_workers;i++){
			pthread_join(thread_pool[i],null);
		}
}

void pool_thread::add_job(thread_task_info* job){
	pthread_mutex_lock(&m);
	the_queue->push(job);
	pthread_cond_broadcast(&cv);
	pthread_mutex_unlock(&m);
}

void *pool_thread::thread_pool_wrap(void* args){
	((pool_thread *)args)->threadPooling(args);
	return 0;
}

void *pool_thread::threadPooling(void* args){
	while(1){
		thread_task_info* my_task=null;
		pthread_mutex_lock(&m);
		while( (the_queue->empty() && !should_exit))
			pthread_cond_wait(&cv, &m); //  pthread_cond_broadcast() || a pthread_cond_signal()
		if(should_exit)
			break;
		if(!the_queue->empty()){
			my_task = the_queue->front();
			the_queue->pop();
		}
		pthread_mutex_unlock(&m); // Job has been finished, thread is still available

		/*
		 * Here we do the job
		 *
		 */

		if(my_task->job_to_do==IndexFolder)
			pool_thread::sendFolderList(my_task);
		else if(my_task->job_to_do==Delete)
			pool_thread::deletefile(my_task);
		else if(my_task->job_to_do==CreateDirectory)
			pool_thread::createDirectory(my_task);
		else if(my_task->job_to_do==RenameFile)
			pool_thread::renameFile(my_task);
		else if(my_task->job_to_do ==SendPicture)
			pool_thread::showPicture(my_task);
		else if(my_task->job_to_do == Move)
			pool_thread::MoveFile(my_task);
		else if(my_task->job_to_do == Copy)
			pool_thread::CopyFile(my_task);
		else if(my_task->job_to_do == Download)
			pool_thread::downloadFiles(my_task);
		else if(my_task->job_to_do == Upload)
			pool_thread::upload_pic(my_task);

		pool_thread::destroy_parameter(my_task);
	}
	pthread_mutex_unlock(&m);
	return NULL;
}

thread_task_info* pool_thread::initialize_parameters(bool multiplefiles,Jobs job,char* folderPath,char* thumbPath,int main_port,int socket_fd,int pipe_fd,char* path_extension,char* rename_extension,char* move_extension,char*copy_extension){
	thread_task_info* return_job = new thread_task_info;
		if(job == RenameFile){ // Only in case of renaming we need the new name of the file
			return_job->renamePath = new char[strlen(folderPath)+strlen(rename_extension)+1];
			strcpy(return_job->renamePath,folderPath);
			strcat(return_job->renamePath,rename_extension);

			return_job->renameThumPath = new char[strlen(thumbPath)+strlen(rename_extension)+1];
			strcpy(return_job->renameThumPath,thumbPath);
			strcat(return_job->renameThumPath,rename_extension);
			return_job->pic_name = pool_thread::get_pic_name(path_extension);
			return_job->new_pic_name = pool_thread::get_pic_name(rename_extension);

		}
		else if(job == Delete || job == Upload)
			return_job->pic_name = pool_thread::get_pic_name(path_extension);
		if(job != SendPicture)
			return_job->workingDirectoryPath = pool_thread::get_working_directory(folderPath,path_extension);


		/* Action that can be performed in a group of items
		 * (1)Delete
		 * (2)Copy-Move outside of the app
		 * (3)Copy-Move inside of the app
		 */

		if(!multiplefiles){ // when we do not
			return_job->multiplefiles = false;
			if(path_extension!=null){
				return_job->folderPath = new char[strlen(folderPath)+strlen(path_extension) +1];
				strcpy(return_job->folderPath,folderPath);
				strcat(return_job->folderPath,path_extension);

				return_job->thumbPath = new char[strlen(thumbPath)+strlen(path_extension)+1];
				strcpy(return_job->thumbPath,thumbPath);
				strcat(return_job->thumbPath,path_extension);
			}
			else{
				return_job->folderPath = new char[strlen(folderPath)+2];
				strcpy(return_job->folderPath,folderPath);
				strcat(return_job->folderPath,(char*)"/");

				return_job->thumbPath = new char[strlen(thumbPath)+2];
				strcpy(return_job->thumbPath,thumbPath);
				strcat(return_job->thumbPath,(char*)"/");
			}
			if(job == Move){
				return_job->pic_name = pool_thread::get_pic_name(path_extension);

				return_job->movePath = new char[strlen(folderPath)+strlen(move_extension)+1];
				strcpy(return_job->movePath,folderPath);
				strcat(return_job->movePath,move_extension);

				return_job->moveThumbPath = new char[strlen(thumbPath)+strlen(move_extension)+1];
				strcpy(return_job->moveThumbPath,thumbPath);
				strcat(return_job->moveThumbPath,move_extension);
			}
			else if (job == Copy){
				return_job->pic_name = pool_thread::get_pic_name(path_extension);

				return_job->copyPath = new char[strlen(folderPath)+strlen(copy_extension)+1];
				strcpy(return_job->copyPath,folderPath);
				strcat(return_job->copyPath,copy_extension);

				return_job->copyThumbPath = new char[strlen(thumbPath)+strlen(copy_extension)+1];
				strcpy(return_job->copyThumbPath,thumbPath);
				strcat(return_job->copyThumbPath,copy_extension);

			}
		}
		else{  // Server will send each photo sequentiallly
			return_job->multiplefiles = true;

			return_job->folderPath = new char[strlen(folderPath)];
			strcpy(return_job->folderPath,folderPath);

			return_job->thumbPath = new char[strlen(thumbPath)];
			strcpy(return_job->thumbPath,thumbPath);

		}
	// Here I must create the folder under which the item exists 1)Find the number of '/' and cut the char* after the previous before last '/'//

	return_job->job_to_do =job;
	return_job->main_port = main_port;
	return_job->prev_fd = socket_fd;
	return_job->pipe_fd = pipe_fd;

//	cout<<"Initializing is complete"<<endl;

	return return_job;
}

void pool_thread::destroy_parameter(thread_task_info* previous_job){
	if(previous_job!=null){
		if(previous_job->job_to_do == RenameFile){
			delete[] previous_job->renamePath;
			delete[] previous_job->renameThumPath;
		}
		if(previous_job->job_to_do== Delete || previous_job->job_to_do == Upload)
			delete[] previous_job->pic_name;
		if(previous_job->job_to_do == Move && (!previous_job->multiplefiles)){
			delete[] previous_job->movePath;
			delete[] previous_job->moveThumbPath;

		}
		if(previous_job->job_to_do == Copy && (!previous_job->multiplefiles)){
			delete[] previous_job->copyPath;
			delete[] previous_job->copyThumbPath;
		}
		if(previous_job->job_to_do != SendPicture)
			delete [] previous_job->workingDirectoryPath;
		delete [] previous_job->folderPath;
		delete [] previous_job->thumbPath;
		delete previous_job;
	}
}

void pool_thread::sendFolderList(thread_task_info* previous_job){
	try{
		int port = previous_job->main_port;
		char * thumbPath = previous_job-> thumbPath;
		int prev_fd = previous_job->prev_fd;
		int my_port = port+1;
		struct sockaddr_in address;
		char buf[1024];
		int server_fd,accept_fd,size; //opt=1,size;
		struct linger so_linger;
		so_linger.l_onoff = 1;
		so_linger.l_linger = 0;
		while(1){
			/*
			 * This function should be moved to FilesManager
			 */
			try{
				if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
						throw socket_Exception((char*)"Cannot create the Socket Interface");
				if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
					throw socket_Exception((char*)"Error setting the Socket's options");
				address.sin_family = AF_INET;
				address.sin_addr.s_addr = htonl(INADDR_ANY);
				address.sin_port = htons(my_port);
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
//				cout<<"exception : "<<e.what()<<endl;
					my_port++;
			}
		}

		sprintf(buf,"%d",my_port);
		write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
		prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
		size = sizeof(address);
		if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
			perror("accept");
		DIR  *dirp;
		struct dirent *directory;
		dirp = opendir(thumbPath);
		index_of_directory * new_file = new index_of_directory();
		if(initializing){
			initializing = false;
			data_node* root = new data_node(previous_job->folderPath,new_file);
			data_structure = new tree(root);
		}
		int number = 0;
		if (dirp){
			while ((directory = readdir(dirp)) != NULL){
				int length = strlen(thumbPath)+strlen(directory->d_name) +1;
				char * file = new char[length];
				strcpy(file,thumbPath);
				strcat(file,directory->d_name);
				if(fileManaging::is_file(file) && (strstr(directory->d_name,".jpg") != NULL || strstr(directory->d_name,".png") != NULL) ){

						flts::sendFile((char*)"image",directory->d_name,file,accept_fd);
						read(accept_fd,buf,sizeof(buf));
							number++;
						delete[] file;
						length = strlen(previous_job->folderPath)+strlen(directory->d_name) +1;
						file = new char[length];
						strcpy(file,previous_job->folderPath);
						strcat(file,directory->d_name);
						data_structure->add_index(file,directory->d_name);
						delete[] file;
				}
				else{
					if(fileManaging::is_dir(file) == true && strstr(directory->d_name,".") == NULL && strstr(directory->d_name,".") == NULL ){
						flts::sendFile((char*)"folder",directory->d_name,file,accept_fd);
						strcpy(buf,(char*)" ");
						read(accept_fd,buf,sizeof(buf));
							number++;
						delete[] file;
						length = strlen(previous_job->folderPath)+strlen(directory->d_name) +2;
						file = new char[length];
						strcpy(file,previous_job->folderPath);
						strcat(file,directory->d_name);
						strcat(file,"/");
						index_of_directory* new_dir = new index_of_directory();
						data_node * new_empty_node = new data_node(file,new_dir);
						data_structure->add_node(new_empty_node);
					}
				}
			}

		}
		else{
			cout<<"Nothing to open"<<endl;
		}
		closedir(dirp);
		strcpy(buf,(char*)"end\0");
		write(accept_fd,buf,strlen(buf)+1);
		if(read(accept_fd,buf,sizeof(buf))==-1){ // Also set a timeout in case something has happened to the other peer
			close(accept_fd);
		}
	}
	catch(char* errcstr){
		cout << errcstr << endl;
	}
	catch(int err){
		cout << err << endl;
	}
	catch(string & errstr){
		cout << errstr << endl;
	}
	catch(exception & ex){
		cout << ex.what() << endl;
	}
};

void pool_thread::deletefile(thread_task_info* previous_job){
	cout<<"Delete request"<<endl;
	int port = previous_job->main_port;
	char * thumb_del = previous_job-> thumbPath;
	char* pic_delete = previous_job->folderPath;
	int prev_fd = previous_job->prev_fd;
	int my_port = port+1;
	struct sockaddr_in address;
	char buf[1024];
	int server_fd,accept_fd,size;
	struct linger so_linger;
	so_linger.l_onoff = 1;
	so_linger.l_linger = 0;
	while(1){
		try{
			if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
					throw socket_Exception((char*)"Cannot create the Socket Interface");
			if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
				throw socket_Exception((char*)"Error setting the Socket's options");
			address.sin_family = AF_INET;
			address.sin_addr.s_addr = htonl(INADDR_ANY);
			address.sin_port = htons(my_port);
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
//			cout<<"exception : "<<e.what()<<endl;
				my_port++;
		}
	}
	sprintf(buf,"%d",my_port);
	write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
	prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
	size = sizeof(address);
	cout<<"begin listening to port: "<<my_port<<endl;
	if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
		perror("accept");
	bool deleted = false;
	if(!previous_job->multiplefiles){
		if(thumb_del!=NULL && pic_delete!=NULL){
			if(fileManaging::is_dir(thumb_del) && fileManaging::is_dir(pic_delete)){ // Deleting directory
				deleted = fileManaging::delete_dir(thumb_del);
				if(deleted){
					cout<<"Deleting 2nd directory: "<<pic_delete<<endl;
					deleted = fileManaging::delete_dir(pic_delete);
					if(deleted)
						data_structure->delete_node(pic_delete);
				}
			}
			else if(fileManaging::is_file(thumb_del) && fileManaging::is_file(pic_delete)){
				deleted = fileManaging::delete_file(thumb_del);
				if(deleted){
					deleted = fileManaging::delete_file(pic_delete);
					if(deleted)
						data_structure->delete_index(pic_delete,previous_job->pic_name);
				}
			}
		}
		if(deleted)
			strcpy(buf,(char*)"delete_ok");
		else{
			cout<<"Problem deleting"<<endl;
			strcpy(buf,(char*)"delete_failed");
		}
		write(accept_fd,buf,strlen(buf)+1);
	}
	else{
		read(accept_fd,buf,sizeof(buf));
		int number = 0;
		while(!strcmp(buf,(char*)"end")==0){
			deleted = false;
			cout<<buf<<endl;
			char * path1 = new char[strlen(pic_delete)+strlen(buf)+2];
			char * path2 = new char[strlen(thumb_del)+strlen(buf)+1];
			strcpy(path1,pic_delete);
			strcat(path1,buf);
			strcpy(path2,thumb_del);
			strcat(path2,buf);
			if(path2!=NULL && path1!=NULL){
				if(fileManaging::is_dir(path2) && fileManaging::is_dir(path1)){ // Deleting directory
					deleted = fileManaging::delete_dir(path2);
					if(deleted){
						cout<<"Deleting 2nd directory: "<<path1<<endl;
						deleted = fileManaging::delete_dir(path1);
						if(deleted){
							strcat(path1,"/");
							data_structure->delete_node(path1);
						}
					}

				}
				else if(fileManaging::is_file(path2) && fileManaging::is_file(path1)){
					deleted = fileManaging::delete_file(path2);
					if(deleted){
						deleted = fileManaging::delete_file(path1);
						if(deleted){
							char *pic_name_delete =get_pic_name(buf);
							data_structure->delete_index(path1,pic_name_delete);
							delete pic_name_delete;
						}
					}
				}
			}
			if(deleted){
				number++;
				strcpy(buf,(char*)"delete_ok");
			}
			else{
				strcpy(buf,(char*)"delete_failed");
				break;
			}
			write(accept_fd,buf,strlen(buf)+1);
			delete [] path1;
			delete [] path2;
			strcpy(buf,(char*)" ");
			if(!deleted)
				break;
			read(accept_fd,buf,sizeof(buf));
		}

	cout<<"Deleting is done, deleted "<<number<<" files"<<endl;
	}


	if(read(accept_fd,buf,sizeof(buf))==-1)
		close(accept_fd);
	}

void pool_thread::createDirectory(thread_task_info* previous_job){
		int port = previous_job->main_port;
		char * thumb_create = previous_job-> thumbPath;
		char* path = previous_job->folderPath;
		int prev_fd = previous_job->prev_fd;
		int my_port = port+1;
		struct sockaddr_in address;
		char buf[1024];
		int server_fd,accept_fd,size;
		struct linger so_linger;
		so_linger.l_onoff = 1;
		so_linger.l_linger = 0;
		while(1){
			try{
				if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
						throw socket_Exception((char*)"Cannot create the Socket Interface");
				if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
				throw socket_Exception((char*)"Error setting the Socket's options");
				address.sin_family = AF_INET;
				address.sin_addr.s_addr = htonl(INADDR_ANY);
				address.sin_port = htons(my_port);
				if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
					throw socket_Exception((char*)" Port already in use");
				if (listen(server_fd, 5) < 0)
					throw socket_Exception((char*)"Maximum level reached");
				break;
				}
			catch(socket_Exception& e){
					my_port++;
			}
		}
		sprintf(buf,"%d",my_port);
		write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
		prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
		size = sizeof(address);
		if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
			perror("accept");
		if (mkdir(path,0777) == -1){
			cout << "Error creating directory!" << endl;
			strcpy(buf,(char*)"Dir_Err");
		}
		else{
			if (mkdir(thumb_create,0777) == -1){
				cout << "Error creating directory!" << endl;
				strcpy(buf,(char*)"Thumb_Dir_Err");
			}
			else{
				char *tree_folder = new char[strlen(path)+2];
				strcpy(tree_folder,path);
				strcat(tree_folder,"/");
				index_of_directory* new_dir = new index_of_directory();
				data_node * new_empty_node = new data_node(tree_folder,new_dir);
				data_structure->add_node(new_empty_node);
				delete[] tree_folder;
				strcpy(buf,(char*)"all_ok");
			}
		}
		write(accept_fd,buf,strlen(buf)+1);
		close(accept_fd);
}

void pool_thread::renameFile(thread_task_info* previous_job){
	int port = previous_job->main_port;
	int prev_fd = previous_job->prev_fd;
	int my_port = port+1;
	struct sockaddr_in address;
	char buf[1024];
	int server_fd,accept_fd,opt=1,size;
	while(1){
		try{
			if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
					throw socket_Exception((char*)"Cannot create the Socket Interface");
			if (setsockopt(server_fd, SOL_SOCKET , SO_REUSEADDR, &opt, sizeof(opt)))
				throw socket_Exception((char*)"Error setting the Socket's options");
			address.sin_family = AF_INET;
			address.sin_addr.s_addr = htonl(INADDR_ANY);
			address.sin_port = htons(my_port);
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
				my_port++;
		}
	}
	sprintf(buf,"%d",my_port);
	write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
	prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
	size = sizeof(address);
	if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
		perror("accept");
//	cout<<"new path is: "<<previous_job->renamePath<<endl;
	if(rename(previous_job->folderPath,previous_job->renamePath)==0){
		if(rename(previous_job->thumbPath,previous_job->renameThumPath)==0){
			strcpy(buf,"all_ok");
			if( fileManaging::is_dir(previous_job->renamePath)){
				data_structure->change_name_node(previous_job->folderPath,previous_job->renamePath);
			}
			else{
				data_structure->rename_index(previous_job->folderPath,previous_job->pic_name,previous_job->new_pic_name);
			}
		}
		else
			strcpy(buf,"thumb_err");
	}
	else
		strcpy(buf,"dir_err");
	write(accept_fd,buf,strlen(buf)+1);
	close(accept_fd);
}

char* pool_thread::get_pic_name(char* path_extension) {
	char * pic_name = null;
	if(path_extension!=null){
		int length = strlen(path_extension);
		int last_slash=0;
		for(int i=0;i<length;i++){
			if(path_extension[i]=='/')
				last_slash = i;
		}
		// [τ][ι][μ][ο][ς][/]][τ][ι][μ][\0]
		char image_name[length - last_slash -1];
		int i=0;
		for(i=0;i<length - last_slash;i++)
			image_name[i]= path_extension[i+last_slash+1];
		image_name[i] = '\0';
//		cout<<"Image's name is: "<<image_name<<endl;
		pic_name = new char[strlen(image_name)];
		strcpy(pic_name,image_name);
	}
	return pic_name;
}

char* pool_thread::get_working_directory(char* folderPath,char* path_extension){
	char* workingDirectoryPath;
	if(path_extension!=null){
		char prev_absolute_path[strlen(folderPath)+strlen(path_extension)+1];
		strcpy (prev_absolute_path, folderPath);
		strcat (prev_absolute_path,path_extension);
		int cur=0,lenght =strlen(prev_absolute_path),i=0;
		while(i<lenght){
			if(prev_absolute_path[i] == '/')
				cur = i;
			i++;
		}
		if(cur == lenght - 1){
			workingDirectoryPath = new char[strlen(prev_absolute_path)];
			strcpy(workingDirectoryPath,prev_absolute_path);
		}
		else{
			prev_absolute_path[cur+1]='\0';
			workingDirectoryPath = new char[strlen(prev_absolute_path)];
			strncpy(workingDirectoryPath,prev_absolute_path,cur);
		}
	}
	else{
		char prev_absolute_path[strlen(folderPath)];
		strcpy (prev_absolute_path, folderPath);
		int cur=0,lenght =strlen(prev_absolute_path),i=0;
		while(i<lenght){
			if(prev_absolute_path[i] == '/')
				cur = i;
			i++;
		}
		if(cur == lenght - 1){
			cout<<"Case 1"<<endl;
			workingDirectoryPath = new char[strlen(prev_absolute_path)];
			strcpy(workingDirectoryPath,prev_absolute_path);
		}
		else{
			cout<<"Case 2"<<endl;
			prev_absolute_path[cur]='\0';
			workingDirectoryPath = new char[strlen(prev_absolute_path)];
			strncpy(workingDirectoryPath,prev_absolute_path,cur+1);
		}
	}
	return workingDirectoryPath;
}

void pool_thread::downloadFiles(thread_task_info* previous_job){
		int port = previous_job->main_port;
		int prev_fd = previous_job->prev_fd;
		int my_port = port+1;
		struct sockaddr_in address;
		char buf[1024];
		int server_fd,accept_fd,size;
		struct linger so_linger;
		so_linger.l_onoff = 1;
		so_linger.l_linger = 0;
		while(1){
			try{
				if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
						throw socket_Exception((char*)"Cannot create the Socket Interface");
				if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
				throw socket_Exception((char*)"Error setting the Socket's options");
				address.sin_family = AF_INET;
				address.sin_addr.s_addr = htonl(INADDR_ANY);
				address.sin_port = htons(my_port);
				if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
					throw socket_Exception((char*)" Port already in use");
				if (listen(server_fd, 5) < 0)
					throw socket_Exception((char*)"Maximum level reached");
				break;
				}
			catch(socket_Exception& e){
					my_port++;
			}
		}
		sprintf(buf,"%d",my_port);
		write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
		prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
		size = sizeof(address);
		if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
			perror("accept");

		//Downloading begins here

		int num_read = read(accept_fd,buf,sizeof(buf));
		while(strcmp(buf,"Files_downloaded")!=0 && num_read>0){
			char * filepath = new char[strlen(previous_job->folderPath)+strlen(buf)+1];
			strcpy(filepath,previous_job->folderPath);
			strncpy(filepath+strlen(filepath), buf+1, strlen(buf));


			if(fileManaging::is_file(filepath)){
				strcpy(buf,(char*)"Image");
				write(accept_fd,buf,strlen(buf)+1);
				read(accept_fd,buf,sizeof(buf)+1);
				if(strcmp(buf,"Ok_type")==0){
					flts::sendFile((char*)"image",null,filepath,accept_fd);
					read(accept_fd,buf,sizeof(buf));
					if(strcmp(buf,(char*)"all_ok")!=0){
						cout<<"Error with the file, abandoned mission"<<endl;
						break;
					}
				}
			}
			else{
				strcpy(buf,(char*)"Folder");
				write(accept_fd,buf,strlen(buf)+1);
				read(accept_fd,buf,sizeof(buf)+1);
				if(strcmp(buf,"Ok_type")==0){
					char * folderName = get_pic_name(filepath);
					download_folder(filepath,accept_fd,folderName);
					read(accept_fd,buf,sizeof(buf)+1);
					if(strcmp(buf,(char*)"all_ok")!=0){
						cout<<"Error with the directory, abandoned mission"<<endl;
						break;
					}
					delete[] folderName;
				}

			}
			delete[] filepath;
			num_read = read(accept_fd,buf,sizeof(buf));
		}
		close(accept_fd);
}

void pool_thread::showPicture(thread_task_info* previous_job){

	char buf[1024];
	for(int i = 0;i<1024;i++)
		buf[i]=' ';

	char* pic_relative_path,*pic_name,*pic_absolute_path;
	int socket_fd = previous_job->prev_fd;

	char prev_absolute_path[strlen(previous_job-> folderPath)];
	strncpy (prev_absolute_path,previous_job-> folderPath, strlen(previous_job-> folderPath)-1 );
	int pic_url_ok = read(socket_fd,buf,sizeof(buf)+1);

	while(pic_url_ok > 0){ // Read picture's relative URL
			pic_relative_path = new char[strlen(buf)+1];
			strcpy(pic_relative_path,buf);

			strcpy(buf,(char*)"pic_url_ok");
			write(socket_fd,buf,strlen(buf)+1);

				if(read(socket_fd,buf,sizeof(buf)+1)>0){ //Read picture's name

					pic_name = new char[strlen(buf)+1];
					strcpy(pic_name,buf);

					strcpy(buf,(char*)"pic_name_ok");
					write(socket_fd,buf,strlen(buf)+1);

					// Picture's absolute path

					pic_absolute_path = new char[ strlen(prev_absolute_path)+strlen(pic_relative_path)+1];
					strcpy(pic_absolute_path,prev_absolute_path);
					strcat(pic_absolute_path,pic_relative_path);


					data_structure->send_prev_next(pic_absolute_path,pic_name,socket_fd);

					flts::sendFile((char*)"image",null,pic_absolute_path,socket_fd);

					delete[] pic_name;
					delete[] pic_absolute_path;
				}
			delete[] pic_relative_path;
			pic_url_ok = read(socket_fd,buf,sizeof(buf)+1);
		}
	cout<<" Image viewer exiting ..."<<endl;
	}

void pool_thread::MoveFile(thread_task_info* previous_job){
	int port = previous_job->main_port;
	int prev_fd = previous_job->prev_fd;
	int my_port = port+1;
	struct sockaddr_in address;
	char buf[1024];
	int server_fd,accept_fd,size;
	struct linger so_linger;
	so_linger.l_onoff = 1;
	so_linger.l_linger = 0;
	while(1){
		try{
			if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
					throw socket_Exception((char*)"Cannot create the Socket Interface");
			if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
			throw socket_Exception((char*)"Error setting the Socket's options");
			address.sin_family = AF_INET;
			address.sin_addr.s_addr = htonl(INADDR_ANY);
			address.sin_port = htons(my_port);
			if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
				throw socket_Exception((char*)" Port already in use");
			if (listen(server_fd, 5) < 0)
				throw socket_Exception((char*)"Maximum level reached");
			break;
			}
		catch(socket_Exception& e){
				my_port++;
		}
	}
	sprintf(buf,"%d",my_port);
	write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
	prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
	size = sizeof(address);
	if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
		perror("accept");
	if(!previous_job->multiplefiles){
		if(fs::exists(previous_job->movePath)){
			cout<<"Exists already, error:"<<previous_job->movePath <<endl;
			strcpy(buf,(char*)"file_exists");
		}
		else if(rename(previous_job->folderPath,previous_job->movePath)==0){
				if(rename(previous_job->thumbPath,previous_job->moveThumbPath)==0){
					strcpy(buf,(char*)"file_moved");
					if(fileManaging::is_file(previous_job->movePath)){
						data_structure->delete_index(previous_job->folderPath,previous_job->pic_name);
						data_structure->add_index(previous_job->movePath,previous_job->pic_name);
					}
					else{
						char * path_to_delete = new char[strlen(previous_job->folderPath)+2];
						strcpy(path_to_delete,previous_job->folderPath);
						strcat(path_to_delete,"/");

						data_structure->delete_node(path_to_delete);
						data_structure->print_tree();
						delete [] path_to_delete;

						index_of_directory* index = new index_of_directory();
						char* new_path = new char[strlen(previous_job->movePath)+2];
						strcpy(new_path,previous_job->movePath);
						strcat(new_path,"/");


						data_node* moving_item = new data_node(new_path,index);
						data_structure->add_node(moving_item);
						delete[] new_path;
					}
				}
				else
					strcpy(buf,(char*)"Problem,file2");
		}
		else
			strcpy(buf,(char*)"Problem,file1");
		write(accept_fd,buf,strlen(buf)+1);
	}
	else{
		int read_bytes = read(accept_fd,buf,sizeof(buf)+1);
		while(strcmp(buf,(char*)"End")!=0 && read_bytes>0 ){

			char *old_location_normal = new char[strlen(previous_job->folderPath)+strlen(buf)+1];
			strcpy(old_location_normal,previous_job->folderPath);
			strcat(old_location_normal,buf);

			char *old_location_thumb= new char[strlen(previous_job->thumbPath)+strlen(buf)+1];
			strcpy(old_location_thumb,previous_job->thumbPath);
			strcat(old_location_thumb,buf);

			strcpy(buf,(char*)"Ok_prev_location");
			write(accept_fd,buf,strlen(buf)+1);

			if(read(accept_fd,buf,sizeof(buf)+1)>0){

				char *new_location_normal = new char[strlen(previous_job->folderPath)+strlen(buf)+1];
				strcpy(new_location_normal,previous_job->folderPath);
				strcat(new_location_normal,buf);

				char *new_location_thumb = new char[strlen(previous_job->thumbPath)+strlen(buf)+1];
				strcpy(new_location_thumb,previous_job->thumbPath);
				strcat(new_location_thumb,buf);
				if(fs::exists(new_location_normal)){
					cout<<"Exists already, error"<<endl;
					strcpy(buf,(char*)"file_exists");
				}
				else if(rename(old_location_normal,new_location_normal)==0){
					if(rename(old_location_thumb,new_location_thumb)==0){
						strcpy(buf,(char*)"file_moved");

						if(fileManaging::is_file(new_location_normal)){
							cout<<old_location_normal<<endl;
							char* pic_name = pool_thread::get_pic_name(old_location_normal);

							cout<<"picture's name is: "<<pic_name<<endl;
							cout<<"Old name's directory: "<<old_location_normal<<endl;
							cout<<"New name's directory: "<<new_location_normal<<endl;

							data_structure->delete_index(old_location_normal,pic_name);
							cout<<"successfully deleted"<<endl;
							data_structure->add_index(new_location_normal,pic_name);
							cout<<"New pic added"<<pic_name<<endl;

						}
						else{
							char * path_to_delete = new char[strlen(old_location_normal)+2];
							strcpy(path_to_delete,old_location_normal);
							strcat(path_to_delete,"/");
							data_structure->delete_node(path_to_delete);
							delete [] path_to_delete;
							index_of_directory* index = new index_of_directory();
							char* new_path = new char[strlen(new_location_normal)+2];
							strcpy(new_path,new_location_normal);
							strcat(new_path,"/");
							data_node* moving_item = new data_node(new_path,index);
							data_structure->add_node(moving_item);
							delete[] new_path;
						}
					}
					else
						strcpy(buf,(char*)"Problem,file2");
				}
				else
					strcpy(buf,(char*)"Problem,file1");
				write(accept_fd,buf,strlen(buf)+1);
				delete[] new_location_normal;
				delete[] new_location_thumb;
			}

		delete[] old_location_normal;
		delete[] old_location_thumb;
		read_bytes = read(accept_fd,buf,sizeof(buf)+1);
		}
	}
}

void pool_thread::CopyFile(thread_task_info* previous_job){
	int port = previous_job->main_port;
	int prev_fd = previous_job->prev_fd;
	int my_port = port+1;
	struct sockaddr_in address;
	char buf[1024];
	int server_fd,accept_fd,size;
	struct linger so_linger;
	so_linger.l_onoff = 1;
	so_linger.l_linger = 0;
	while(1){
		try{
			if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
					throw socket_Exception((char*)"Cannot create the Socket Interface");
			if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
			throw socket_Exception((char*)"Error setting the Socket's options");
			address.sin_family = AF_INET;
			address.sin_addr.s_addr = htonl(INADDR_ANY);
			address.sin_port = htons(my_port);
			if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
				throw socket_Exception((char*)" Port already in use");
			if (listen(server_fd, 5) < 0)
				throw socket_Exception((char*)"Maximum level reached");
			break;
			}
		catch(socket_Exception& e){
				my_port++;
		}
	}

	sprintf(buf,"%d",my_port);
	write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
	prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
	size = sizeof(address);
	if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
		perror("accept");

		if(!previous_job->multiplefiles){
			try {
				if(fileManaging::is_file(previous_job->folderPath)){
					fs::copy(previous_job->folderPath,previous_job->copyPath);

					fs::copy(previous_job->thumbPath,previous_job->copyThumbPath);
					strcpy(buf,(char*)"file_copied");
					data_structure->add_index(previous_job->copyPath,previous_job->pic_name);
				}
				else{
					fs::copy(previous_job->folderPath,previous_job->copyPath,fs::copy_options::recursive);
					fs::copy(previous_job->thumbPath,previous_job->copyThumbPath,fs::copy_options::recursive);
					index_of_directory* index = new index_of_directory();
					char* new_path = new char[strlen(previous_job->copyPath)+2];
					strcpy(new_path,previous_job->copyPath);
					strcat(new_path,"/");
					strcpy(buf,(char*)"file_copied");
					data_node* moving_item = new data_node(new_path,index);
					data_structure->add_node(moving_item);
					delete[] new_path;
				}
				write(accept_fd,buf,strlen(buf)+1);
			} catch(fs::filesystem_error& e) {
				cout << "Could not copy sandbox/abc: " << e.what() << '\n';
				strcpy(buf,(char*)"File_exists");
				write(accept_fd,buf,strlen(buf)+1);
			}
		}
		else{
			int read_bytes = read(accept_fd,buf,sizeof(buf)+1);
			while(strcmp(buf,(char*)"End")!=0 && read_bytes>0 ){
				try{
					char *old_location_normal = new char[strlen(previous_job->folderPath)+strlen(buf)+1];
					strcpy(old_location_normal,previous_job->folderPath);
					strcat(old_location_normal,buf);

					char *old_location_thumb= new char[strlen(previous_job->thumbPath)+strlen(buf)+1];
					strcpy(old_location_thumb,previous_job->thumbPath);
					strcat(old_location_thumb,buf);

					strcpy(buf,(char*)"Ok_prev_location");
					write(accept_fd,buf,strlen(buf)+1);

					if(read(accept_fd,buf,sizeof(buf)+1)>0){

						char *new_location_normal = new char[strlen(previous_job->folderPath)+strlen(buf)+1];
						strcpy(new_location_normal,previous_job->folderPath);
						strcat(new_location_normal,buf);

						char *new_location_thumb = new char[strlen(previous_job->thumbPath)+strlen(buf)+1];
						strcpy(new_location_thumb,previous_job->thumbPath);
						strcat(new_location_thumb,buf);

						if(fileManaging::is_file(new_location_normal)){
							char* pic_name = pool_thread::get_pic_name(old_location_normal);
							cout<<"About to copy: "<<old_location_normal<<endl;
							fs::copy(old_location_normal,new_location_normal);
							fs::copy(old_location_thumb,new_location_thumb);
							data_structure->add_index(new_location_normal,pic_name);
							strcpy(buf,(char*)"file_copied");
							cout<<pic_name<<" copied"<<endl;
							delete[] pic_name;

						}
						else{
							fs::copy(old_location_normal,new_location_normal,fs::copy_options::recursive);
							fs::copy(old_location_thumb,new_location_thumb,fs::copy_options::recursive);
							char * path_to_delete = new char[strlen(old_location_normal)+2];
							strcpy(path_to_delete,old_location_normal);
							strcat(path_to_delete,"/");
							delete [] path_to_delete;

							index_of_directory* index = new index_of_directory();
							char* new_path = new char[strlen(new_location_normal)+2];
							strcpy(new_path,new_location_normal);
							strcat(new_path,"/");
							data_node* moving_item = new data_node(new_path,index);
							data_structure->add_node(moving_item);
							strcpy(buf,(char*)"file_copied");
							cout<<new_path<<" copied"<<endl;

							delete[] new_path;
					}
					delete[] new_location_normal;
					delete[] new_location_thumb;
					}
				delete[] old_location_normal;
				delete[] old_location_thumb;
				write(accept_fd,buf,strlen(buf)+1);
				read_bytes = read(accept_fd,buf,sizeof(buf)+1);
				} catch(fs::filesystem_error& e) {
					cout << "Could not copy sandbox/abc: " << e.what() << '\n';
					strcpy(buf,(char*)"File_exists");
					write(accept_fd,buf,strlen(buf)+1);
					read_bytes = read(accept_fd,buf,sizeof(buf)+1);
				}
			}
		}
}

void pool_thread::download_folder(char* path_of_folder,int accept_fd,char* folder_name){
		DIR  *dirp;
		struct dirent *directory;
		dirp = opendir(path_of_folder);
		char buf[1024];
		if (dirp){
			while ((directory = readdir(dirp)) != NULL){
				int length = strlen(path_of_folder)+strlen(directory->d_name) +2;
				char * file = new char[length];
				strcpy(file,path_of_folder);
				strcat(file,"/");
				strcat(file,directory->d_name);

				if(fileManaging::is_file(file) && (strstr(directory->d_name,".jpg") != NULL || strstr(directory->d_name,".png") != NULL) ){
					strcpy(buf,(char*)"Image");
					write(accept_fd,buf,strlen(buf)+1);
					read(accept_fd,buf,sizeof(buf)+1);
					flts::sendFile((char*)"image",directory->d_name,file,accept_fd);
					read(accept_fd,buf,sizeof(buf));
					if(strcmp(buf,(char*)"all_ok")!=0){
						cout<<"Error with the file, abandoned mission"<<endl;
						break;
					}
				}
				else if(fileManaging::is_dir(file) == true && strstr(directory->d_name,".") == NULL && strstr(directory->d_name,".") == NULL ){
						strcpy(buf,(char*)"Folder");
						write(accept_fd,buf,strlen(buf)+1);
						read(accept_fd,buf,sizeof(buf)+1);
						if(strcmp(buf,"Ok_type")==0){
							strcpy(buf,directory->d_name);
							write(accept_fd,buf,strlen(buf)+1);
							read(accept_fd,buf,sizeof(buf)+1);
							if(strcmp(buf,"ok_name")==0){
									download_folder(file,accept_fd,directory->d_name);
									read(accept_fd,buf,sizeof(buf)+1);
									if(strcmp(buf,(char*)"all_ok")!=0){
										cout<<"Error with the directory, abandoned mission"<<endl;
										break;
								}
							}
						}
					}
				delete[] file;
			}
		}
		strcpy(buf,(char*)"End");
		write(accept_fd,buf,strlen(buf)+1);
		closedir(dirp);
};

void pool_thread::upload_pic(thread_task_info* previous_job){
		int port = previous_job->main_port;
		int prev_fd = previous_job->prev_fd;
		int my_port = port+1;
		struct sockaddr_in address;
		char buf[1024];
		int server_fd,accept_fd,size;
		struct linger so_linger;
		so_linger.l_onoff = 1;
		so_linger.l_linger = 0;
		while(1){
			try{
				if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
						throw socket_Exception((char*)"Cannot create the Socket Interface");
				if (setsockopt(server_fd, SOL_SOCKET , SO_LINGER,&so_linger,sizeof so_linger))
				throw socket_Exception((char*)"Error setting the Socket's options");
				address.sin_family = AF_INET;
				address.sin_addr.s_addr = htonl(INADDR_ANY);
				address.sin_port = htons(my_port);
				if (bind(server_fd, (struct sockaddr *)&address,sizeof(address))==-1)
					throw socket_Exception((char*)" Port already in use");
				if (listen(server_fd, 5) < 0)
					throw socket_Exception((char*)"Maximum level reached");
				break;
				}
			catch(socket_Exception& e){
					my_port++;
			}
		}
		sprintf(buf,"%d",my_port);
		write(prev_fd,buf,strlen(buf)+1); //Should send data back to main thread
		prev_fd = 0 ; // Be sure the sockets doesn't send anything through the initial socket
		size = sizeof(address);
		if ((accept_fd = accept(server_fd, (struct sockaddr *)&address,(socklen_t*)&size)) == -1)
			perror("accept");
		flts::receiveFile(accept_fd,previous_job->folderPath);
		data_structure->add_index(previous_job->folderPath,previous_job->pic_name);

		char command[8196];
		cout<<previous_job->thumbPath<<endl;
		strcpy(command,"mogrify -resize 60x60 -quality 50 -path ");
		char* directory = get_working_directory(previous_job->thumbPath,null);

		strcat(command,directory);
		strcat(command,"  ");
		strcat(command,previous_job->folderPath);
		cout<<command<<endl;
		system(command);
		flts::sendFile("image",null,previous_job->thumbPath,accept_fd);
		delete[] directory;

}
