/*
 * Exceptions.h
 *
 *  Created on: Feb 12, 2018
 *      Author: timos
 */

#ifndef EXCEPTIONS_EXCEPTIONS_H_
#define EXCEPTIONS_EXCEPTIONS_H_

#include <exception>
#include <string.h>

using namespace std;


class socket_Exception: public exception{
private:
	char *type_err;
public:
	 virtual const char* what() const throw();
	 socket_Exception(char * input);
	 ~socket_Exception()  throw ();
};



#endif /* EXCEPTIONS_EXCEPTIONS_H_ */
