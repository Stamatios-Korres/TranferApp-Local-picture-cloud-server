/*
 * MySqlConnector.cpp
 *
 *  Created on: Jan 11, 2018
 *      Author: paris
 */
#include <iostream>
#include "mysql_driver.h"
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <cppconn/prepared_statement.h>
#include "lib/MySqlConnector.h"
#include "string.h"
#include <stdio.h>
#include <mysql_connection.h>


using namespace std;

MySqlConnector::MySqlConnector(char* database, char* password) {
	try {
		  sql::mysql::MySQL_Driver *driver;

		  /* Create a connection */
		  driver = sql::mysql::get_driver_instance();
		  con = driver->connect("tcp://127.0.0.1:3306", "root", password);
		  /* Connect to the MySQL <name-of-database> database */
		  con->setSchema(database);

		} catch (sql::SQLException &e) {
		  cout << "# ERR: SQLException in " << __FILE__;
		  cout << "# ERR: " << e.what();
		  cout << " (MySQL error code: " << e.getErrorCode();

		}
}

MySqlConnector::~MySqlConnector() {
	delete con;
}

//-1 ERROR , 0 NOT FOUND , 1 FOUND
int MySqlConnector::searchForUser(char* username, char* password){

	try {
		sql::ResultSet *res;
		sql::PreparedStatement *pstmt;

		pstmt = con->prepareStatement("select * from user where username = ? and password = ?");
		pstmt->setString(1,username);
		pstmt->setString(2, password);
		res = pstmt->executeQuery();

		if (res->rowsCount() > 0) {
			//user has been found
			delete res;
			delete pstmt;
			return 1;
		}
		//user has not been found
		else {
			delete res;
			delete pstmt;
			return 0;
		}

	} catch (sql::SQLException &e) {
		  cout << "# ERR: SQLException in " << __FILE__;
		  cout << "# ERR: " << e.what();
		  cout << " (MySQL error code: " << e.getErrorCode();
		  return -1;
	}
}

int MySqlConnector::addUser(char* username, char* password,char* path,char * paththumb){
	try{
	sql::ResultSet *res;
	sql::PreparedStatement *pstmt;
	char *result = new char[strlen("home/")+ strlen(username)+2];
	sprintf(result,"%s/%s/","home",username);
	pstmt = con->prepareStatement("insert into user(username,password,path,thumbnailsPath) values(?,?,?,?)");
	pstmt->setString(1,username);
	pstmt->setString(2, password);
	pstmt->setString(3,path);
	pstmt->setString(4,paththumb);
	res = pstmt->executeQuery();
		delete res;
		delete pstmt;
	return 1;
	}
	catch (sql::SQLException &e) {
		  cout << "# ERR: SQLException in " << __FILE__;
		  cout << "# ERR: " << e.what();
		  cout << " (MySQL error code: " << e.getErrorCode();
		  return -1;
	}

}

char* MySqlConnector::getPath(char* username){
	try {
			sql::ResultSet *res;
			sql::PreparedStatement *pstmt;

			pstmt = con->prepareStatement("select * from user where username = ?");
			pstmt->setString(1,username);
			res = pstmt->executeQuery();

			char* path = NULL;
			while (res->next()){
				path = new char[strlen(res->getString((string)"path").c_str())+1];
				strcpy(path, res->getString((string)"path").c_str());
				cout << "Files' path of user " << username
					 << " : "
					 << path
					 << endl;
			}

			if (res->rowsCount() > 0) {
				//user has been found
				delete res;
				delete pstmt;
				return path;
			}
			//user has not been found
			else {
				delete res;
				delete pstmt;
				return (char*)"error";
			}

		} catch (sql::SQLException &e) {
			  cout << "# ERR: SQLException in " << __FILE__;
			  cout << "# ERR: " << e.what();
			  cout << " (MySQL error code: " << e.getErrorCode();
			  return (char*)"error";
		}
}
