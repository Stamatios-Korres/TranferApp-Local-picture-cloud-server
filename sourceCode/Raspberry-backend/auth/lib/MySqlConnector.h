/*
 * MySqlConnector.h
 *
 *  Created on: Jan 11, 2018
 *      Author: paris
 */

#ifndef MYSQLCONNECTOR_H_
#define MYSQLCONNECTOR_H_

#include "mysql_connection.h"

class MySqlConnector {
private:
	  sql::Connection *con;
public:
	MySqlConnector(char* database, char* password);
	virtual ~MySqlConnector();
	int searchForUser(char* username, char* password);
	char* getPath(char* username);
	int addUser(char* username,char* password,char* path,char * paththumb);
};

#endif /* MYSQLCONNECTOR_H_ */
