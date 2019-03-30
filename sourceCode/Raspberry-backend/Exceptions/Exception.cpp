/*
 * Exception.cpp
 *
 *  Created on: Feb 12, 2018
 *      Author: timos
 */

#include "Exceptions.h"


socket_Exception::~socket_Exception() throw (){
	delete[] type_err;
}
socket_Exception::socket_Exception(char * input){
			type_err = new char[strlen(input) +1];
			strcpy(type_err,input);
}
const char* socket_Exception::what() const throw(){
	return type_err;
}
