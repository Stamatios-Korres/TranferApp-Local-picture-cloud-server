/*
 * FileManager.h
 *
 *  Created on: Jan 28, 2018
 *      Author: timos
 */
#include <sys/stat.h>
#include <cstring>
#include <fstream>
#include <sys/stat.h>
#include <dirent.h>
#include<iostream>
#include <sys/socket.h>
#include <unistd.h>

using namespace std;

#define null NULL

#ifndef MAINPROGRAM_LIB_FILEMANAGER_H_
#define MAINPROGRAM_LIB_FILEMANAGER_H_

#endif /* MAINPROGRAM_LIB_FILEMANAGER_H_ */

namespace fileManaging {
	bool is_file(const char* path);

	bool is_dir(const char* path);

	int Size_of_file(const char* path);

	bool delete_dir(const char* path);

	bool delete_file(const char* path);

	bool rename_path(const char* path);

}
