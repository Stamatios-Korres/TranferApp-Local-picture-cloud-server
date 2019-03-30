/*
 * FilesManager.cpp
 *
 *  Created on: Jan 28, 2018
 *      Author: timos
 */
#include "FileManager.h"


	bool fileManaging::is_file(const char* path) {
	    struct stat buf;
	    stat(path, &buf);
	    return S_ISREG(buf.st_mode);
	}

	bool fileManaging::is_dir(const char* path) {
	    struct stat buf;
	    stat(path, &buf);
	    return S_ISDIR(buf.st_mode);
	}

	int fileManaging::Size_of_file(const char* path){
		struct stat filestatus;
		stat( path, &filestatus );
		return filestatus.st_size;
	}

	bool fileManaging::delete_dir(const char* path) {
		bool ret_value = true;
		DIR  *dirp;
		struct dirent *directory;
		dirp = opendir(path);
//		cout<<"Path to delete is: "<<path<<endl;
		try{
			if (dirp){
				while ((directory = readdir(dirp)) != NULL){
					if(strcmp(directory->d_name,".")==0 ||strcmp(directory->d_name,"..")==0)
						continue;
					char* final_path = new char[strlen(path)+2+strlen(directory->d_name)];
					strcpy(final_path,path);
					strcat(final_path,"/");
					strcat(final_path,directory->d_name);
					if( fileManaging::is_dir(final_path) ){
						ret_value = fileManaging::delete_dir(final_path);
						if(!ret_value){
							break;
						}
					}
					else if( fileManaging::is_file(final_path) ){
						ret_value = fileManaging::delete_file(final_path);
						if(!ret_value){
							break;
						}
					}
					delete[] final_path;
				}
			}
			if(remove(path)!=0)
				ret_value = false;
			}
		catch(bad_alloc& exc){
				cout<<"Exception: "<<exc.what()<<endl;
		}
		closedir(dirp);
		return ret_value;
	}

	bool fileManaging::delete_file(const char* pic_path){
		if(remove(pic_path)==0)
				return true;
		else
			return false;
	}

	bool fileManaging::rename_path(const char* pic_path){
		bool renamed = false;
		return renamed;
	}



