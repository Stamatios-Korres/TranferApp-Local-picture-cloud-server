/*
 *  directory_structure.h
 *
 *  Created on: Mar 1, 2018
 *      Author: timos
 */

#include <iostream>
#include <list>
#include <stdio.h>
#include <cstring>
#include <list>
#include <unistd.h>


using namespace std;
#define null NULL

#ifndef DIRECTORY_STRUCTURE__DIRECTORY_STRUCTURE_H_
#define DIRECTORY_STRUCTURE__DIRECTORY_STRUCTURE_H_



#endif /* DIRECTORY_STRUCTURE__DIRECTORY_STRUCTURE_H_ */

class index_of_directory{
private:
	list<char*> indexes;
public:
	index_of_directory();
	~index_of_directory();
	list<char*> get_indexes();
	void add_index(char* new_index);
	void delete_index(char* index_to_delete);
	void modify_index(char* old_index_name,char* new_index_name);
	void clear_directory();
	void print_list(int level);
};

class data_node{
private:
	char * directory_name;
	int height;
	index_of_directory * files;
	list<data_node*> neighbors;
public:
	data_node(char* path_name,index_of_directory* containing_files);
	~data_node();
	int get_height();
	bool is_my_child(char* path);
	bool is_mine_path(char* path);
	bool is_my_direct_child(char* path);
	int compute_height(char* path);
	void add_child(data_node* new_child);
	void delete_child(char * path);
	void add_index(char* path);
	void delete_index(char* index_name);
	void modify_index(char *old_name,char* new_name);
	data_node* find_direction(char* path);
	char * get_directory_name();
	data_node* return_correct_child(char* path);
	data_node * get_next_child(char* prev_node);
	void print_children();
	list<data_node*> get_children();
	void print_indexes(int level);
	void clear_subtree();
	void set_node_name(char* new_name);
	list<char*>  get_list();
};

class tree{
private:
	data_node *root;
	void print_tree(data_node* start,int level);
public:
	void rename_index(char* absolute_path,char* old_name,char* new_name);
	tree(data_node *root_node);
	~tree();
	data_node* find_node(char* path);
	void add_node(data_node * new_node);
	void print_tree();
	void delete_node(char * path_of_node);
	void delete_node_from_parent(char * path);
	void change_name_node(char* old_node_name,char * new_node_name);
	void add_index(char* path,char* index_name);
	void delete_index(char* path,char* index_name);
	void change_name_index(char* path,char* old_index_name,char* new_index_name);
	void send_prev_next(char* absolute_path,char* pic_name,int socket_fd);
	void clean_tree();

};

