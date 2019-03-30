/*
 * auth.h
 *
 *  Created on: Jan 11, 2018
 *      Author: paris
 */

#ifndef AUTH_H_
#define AUTH_H_

#include "MySqlConnector.h"

class AuthenticationService{
private:
	MySqlConnector* mysqlConnector;
	char* database;
	char* password;
public:
	AuthenticationService();
	~AuthenticationService();
	int authenticate(char* username,char* password);
	char* getUserData(char* username);
	int addUser(char* username, char* password,char* path,char * paththumb);
	void configure(char* filename);
};




#endif /* AUTH_H_ */
