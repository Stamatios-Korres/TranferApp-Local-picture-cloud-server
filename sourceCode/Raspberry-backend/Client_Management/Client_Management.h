/*
 * client_Management.h
 *
 *  Created on: Feb 9, 2018
 *      Author: timos
 */
#include <signal.h>
#include <stdio.h>
#include <exception>
#include <iostream>


using namespace std;

#ifndef CLIENT_MANAGEMENT_CLIENT_MANAGEMENT_H_
#define CLIENT_MANAGEMENT_CLIENT_MANAGEMENT_H_


class client_node{
private:
	pid_t my_id;
	int port;
	int fd_socket;
	client_node * next;
	client_node* prev;
public:
	client_node* get_next();
	client_node* get_prev();
	void set_next(client_node* next);
	void set_prev(client_node* prev);
	client_node(pid_t my_id,int port,int fd_socket);
	~client_node();
	void kill_process();
	pid_t get_pid();
};

class client_list{
private:
	int length;
	client_node * head;
	client_node *tail;
public:
	client_list();
	~client_list();
	void add_client(pid_t my_id,int port,int fd_socket);
	void remove_client(int pid);
	void client_exited(int pid);
	void reset_list();
	void print_list();
	client_node* return_client(pid_t pid);
	client_node* get_head();
	client_node* get_tail();


};
#endif /* CLIENT_MANAGEMENT_CLIENT_MANAGEMENT_H_ */
