/*
 * client_Management.cpp
 *
 *  Created on: Feb 9, 2018
 *      Author: timos
 */

#include "Client_Management.h"
#define null NULL

client_node::client_node(pid_t my_id,int port,int fd_socket){
	this->my_id = my_id;
	this->port = port;
	this->fd_socket = fd_socket;
	this->next = null;
	this->prev  = null;
//	cout<<"New client node was created!"<<endl;
}
client_node::~client_node(){
	// Without being sure, all members were statically allocated and thus,
	//	I don't have to do something explicitly
}
void client_node::kill_process(){
	kill(my_id,SIGINT);
}

pid_t client_node::get_pid(){
	return my_id;
}

client_node* client_node::get_next(){;
	return this->next;
}
client_node* client_node:: get_prev(){
	return this->prev;
}
void client_node::set_next(client_node* next){
	this->next = next;
}
void client_node::set_prev(client_node* prev){
	this->prev = prev;
}
/* ------------------------------------ --------------------------------------------------------------------------- */

client_list::client_list(){
//	cout<<"client list creating, initializing parameters"<<endl;
	head = null;
	tail = null;
	length = 0;
}

client_list::~client_list(){
	if(head!=null){
//		cout<<"killing processes"<<endl;
		client_list::reset_list();
	}
}

void client_list::reset_list(){
	client_node * traverse = head,*temp;
	if(length >0){
		while(traverse!=null){
			temp = traverse->get_next();
			traverse->kill_process();
			cout<<"dead ->" <<traverse->get_pid()<<endl;
			delete traverse;
			traverse = temp;
		}
	}
	head = null;
	tail = null;
}

client_node* client_list::return_client(pid_t pid){
		client_node* return_value = head;
		while(return_value!=null){
			if(return_value->get_pid() == pid)
				break;
			return_value =return_value->get_next();
		}
		return return_value;
}
client_node* client_list::get_head(){
	return head;
}

client_node* client_list::get_tail(){
	return tail;
}

void client_list::print_list(){
	client_node* traverse= head;
	int counter = 0;
	if(length == 0)
		cout<<endl<<"No thread is currently running"<<endl;
	else{
		while(traverse!=null){
			if(traverse->get_next()!=null)
				cout<<traverse->get_pid()<<"--->";
			else
				cout<<traverse->get_pid();
			traverse = traverse->get_next();
			counter++;
		}
		if(counter == length){
			cout<<"--[]"<<endl;
		}
	}
}

void client_list::add_client(pid_t my_id,int port,int fd_socket){
	client_node *new_client = new client_node(my_id,port,fd_socket);
	length++;
	client_node *temp;
	temp = head;
	if(head == null) // only 1 node
		tail = new_client;
	head = new_client;
	if(temp)
		temp->set_prev(new_client);
	new_client->set_next(temp);
	new_client->set_prev(null);
}

void client_list::remove_client(int pid){
	client_node* traverse = head;
	bool found = false;
	while(traverse!=null){
		if(traverse->get_pid() == pid){
			if(traverse == head && traverse == tail){
				head = tail = null;
					// Nothing to do
			}
			else if(traverse==head){ //head
					head = traverse->get_next();
					traverse->get_next()->set_prev(null);
			}
			else if(traverse==tail){ //tail
					tail = traverse->get_prev();
					traverse->get_prev()->set_next(null);
			}
			else{ //intermediate
				traverse->get_prev()->set_next(traverse->get_next()); // intermediate node
				traverse->get_next()->set_prev(traverse->get_prev());
			}
			traverse->kill_process();
			cout<<"killed and removed ->" <<traverse->get_pid()<<endl;
			delete traverse;
			found = true;
			break;
		}
		traverse = traverse-> get_next();
	}
	if(!found)
		cout<<"The node was not found"<<endl;
	length--;
}

void client_list::client_exited(int pid){
	client_node* traverse = head;
	bool found = false;
	while(traverse!=null){
			if(traverse->get_pid() == pid){
				if(traverse == head && traverse == tail){
					head = tail = null;
				}
				else if(traverse==head){ //head
						head = traverse->get_next();
						traverse->get_next()->set_prev(null);
				}
				else if(traverse==tail){ //tail
						tail = traverse->get_prev();
						traverse->get_prev()->set_next(null);
				}
				else{ //intermediate
					traverse->get_prev()->set_next(traverse->get_next()); // intermediate node
					traverse->get_next()->set_prev(traverse->get_prev());
				}
				cout<<"Removed ->" <<traverse->get_pid()<<endl;
				delete traverse;
				found = true;
				break;
			}
			traverse = traverse-> get_next();
		}
	if(!found)
		cout<<"The node was not found"<<endl;
	length--;
}
