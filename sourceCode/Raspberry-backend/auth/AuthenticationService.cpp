/*
 * AuthenticationService.cpp
 *
 *  Created on: Jan 11, 2018
 *      Author: paris
 */

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/stat.h>
#include "string.h"
#include "lib/Auth.h"
#include "lib/MySqlConnector.h"


using namespace std;

AuthenticationService::AuthenticationService(){
	this->configure((char*)"mysql.conf");
	mysqlConnector = new MySqlConnector(database,password);
	cout << "New Authentication Service is running!" << endl;
}

AuthenticationService::~AuthenticationService(){
	delete[] database;
	delete[] password;
	delete mysqlConnector;
}

int AuthenticationService::authenticate(char* username, char* password){


	if (mysqlConnector->searchForUser(username,password) == 1){
		cout << "Authentication for user " << username
			 <<" : SUCCESS" << endl;
		return 1;
	}
	else {
		cout << "Authentication for user " << username
					 <<" : FAILURE" << endl;
		return 0;
	}
}

int AuthenticationService::addUser(char* username, char* password,char* path,char * paththumb){
	if(mysqlConnector->addUser(username,password,path,paththumb)==1){
		cout<<"User successfully added"<<endl;
		return 1;
	}
	else{
		cout<<"Error Occurred"<<endl;
		return -1;
	}

}

char* AuthenticationService::getUserData(char* username){
	char* path;
	path = mysqlConnector->getPath(username);
	if (strcmp(path,"error") != 0) return path;
	else return path;
}

void AuthenticationService::configure(char* filename){
	FILE* file;
	if ((file = fopen(filename, "r")) == NULL)
		perror("can't open file");

	char buf[255];
	while (!feof(file))
	{
		fgets(buf, 255+1, file);
		buf[strlen(buf)-1] = '\0'; //skip '\n' char
		char* split;
		split = strtok(buf," ");
		if (strcmp(buf,"database") == 0){
			split = strtok (NULL,"\n");
			this->database = new char[strlen(split)+1];
			strcpy(this->database,split);
		}
		else if (strcmp(buf,"password") == 0){
			split = strtok (NULL,"\n");
			this->password = new char[strlen(split)+1];
			strcpy(this->password,split);
		}
	}
}
