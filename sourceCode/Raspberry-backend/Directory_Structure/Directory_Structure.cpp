/*
 *  directory_structure.cpp
 *
 *  Created on: Mar 1, 2018
 *      Author: timos
 */

#include "Directory_Structure.h"


//------------------------------------------------ INDEXES OF A PARTICULAR DIRECTORY ------------------------------------------------------------//


index_of_directory::index_of_directory(){
};

index_of_directory::~index_of_directory(){
	list<char*>::iterator it;
	for(it = indexes.begin();it!=indexes.end();it++){
//		cout<<"Pointer's value: "<<*it<<endl;
		delete[] *it; // Fuck me re C++ malakies
	}
	indexes.clear();
}

void index_of_directory::add_index(char* new_index){
	char * add_index = new char[strlen(new_index)];
	strcpy(add_index,new_index);
	indexes.push_back(add_index);
}

void index_of_directory::delete_index(char* index_to_delete){
	list<char*>::iterator it;
	for(it = indexes.begin();it!=indexes.end();it++)
		if(strcmp(index_to_delete,(*it))==0){
			delete[] *it;
			indexes.erase(it);
			break;
		}
};

void index_of_directory::modify_index(char* old_index_name,char* new_index_name){
	list<char*>::iterator it = indexes.begin();
	while(it!=indexes.end()){
		if(strcmp(*it,old_index_name)==0){
			char * new_name = new char[strlen(new_index_name)];
			strcpy(new_name,new_index_name);
			delete[] *it;
			*it = new_name;
			break;
		}
		it++;
	}
}

void index_of_directory::print_list(int level){
	list<char*>::iterator it = indexes.begin();
		while(it!=indexes.end()){
			for(int i=0;i<level;i++)
					cout<<"  ";
			cout<<" + ";
			cout<<*it<<endl;
			it++;
		}
}

void index_of_directory::clear_directory(){
	list<char*>::iterator it = indexes.begin();
	while(it!=indexes.end()){
		cout<<"Item found: "<<*it<<endl;
		delete[] *it;
		it++;
	}
	indexes.clear();
};

list<char*> index_of_directory::get_indexes(){
	return indexes;
}
//-----------------------------------------------     Tree Structure     -----------------------------------------------------------------------------//
tree::tree(data_node *root_node){
		root = root_node;
}

tree::~tree(){

};

void tree::print_tree(){
	cout<<root->get_directory_name()<<"  "<< root->get_height()<<endl;
	print_tree(root,1);
}

void tree::print_tree(data_node* start,int level){
	list<data_node*>::iterator it;
	list<data_node*> temp_neighbors = start->get_children();
	for(it = temp_neighbors.begin();it!=temp_neighbors.end();it++){
		for(int i=0;i<level;i++)
			cout<<"  ";
		cout<<"--"<<(*it)->get_directory_name()<<" "<< (*it)->get_height()<<endl;
		(*it)->print_indexes(level);
		print_tree((*it),level+1);
	}
}

void tree::add_node(data_node * new_node){
	char * path = new_node->get_directory_name();
	data_node* returned_node = root,*child;
	bool found=false; char * cur_path = null;
	while(!found){
		if(returned_node==null)
			return;
		if(returned_node->is_my_direct_child(path)== true)
			found = true;
		else{
			child = returned_node->get_next_child(cur_path);
			if(child!=null){
				cur_path = child->get_directory_name();
				if(child->is_my_direct_child(path)){
					returned_node = child;
					found = true;
				}
				else if(child->is_my_child(path)){
					returned_node = child;
					cur_path = null;
				}
				else
					child = returned_node->get_next_child(cur_path);
			}
			else
				return;
		}
	}
	returned_node->add_child(new_node);
}

void tree::delete_node(char * path_of_node){
	data_node* node_to_delete = find_node(path_of_node);
	delete_node_from_parent(path_of_node);
	delete node_to_delete;
}

void tree::change_name_node(char* old_node_name,char * new_node_name){
		data_node* node = find_node(old_node_name);
		node->set_node_name(new_node_name);
}

void tree::add_index(char* path,char* index_name){
	data_node* existing_node = find_node(path);
	existing_node->add_index(index_name);
}

void tree::clean_tree(){
	delete root;
}

void tree::send_prev_next(char* absolute_path,char* pic_name,int socket_fd){
	data_node* working_node = find_node(absolute_path);
	char* prevPic,*nextPic;
	prevPic = nextPic = null;
	list<char*> ::iterator prev,next,temp;
	list<char*> indexes =  working_node->get_list();
	list<char*> ::iterator it;
	for(it=indexes.begin();it!=indexes.end();it++){
		if(strcmp((*it),pic_name)==0){
			if(it != indexes.begin()){
				temp = it;
				prev = --it;
				prevPic = (*prev);
				it = temp;
			}
			temp = it;
			next = ++it;
			if(next!=indexes.end())
				nextPic = (*next);
			it = temp;
		}
	}
	cout<<"Requested Image: "<<pic_name<<endl;
	cout<<"Next Image: "<<nextPic<<endl;
	cout<<"PRevious Image: "<<prevPic<<endl;
	char buf[1024];
	 if(prevPic!=null)
		 strcpy(buf,prevPic);
	 else
		 strcpy(buf,(char*)"No_prev_available");
	 write(socket_fd,buf,strlen(buf)+1);
	 int chars = read(socket_fd,buf,sizeof(buf));
	 if(chars >0){
		 if(strcmp(buf,(char*)"Ok_first")==0){
			 if(nextPic!=null)
				 strcpy(buf,nextPic);
			 else
				 strcpy(buf,(char*)"No_next_available");
			 write(socket_fd,buf,strlen(buf)+1);
			 chars = read(socket_fd,buf,sizeof(buf));
			 if(chars >0){
//				 if(strcmp(buf,(char*)"Ok_second")==0){
//
//				 }
			 }
		 }
	 }
}

void tree::delete_index(char* path,char* index_name){
		data_node* responsible_node = find_node(path);
		responsible_node->delete_index(index_name);
}

void tree::rename_index(char* absolute_path,char* old_name,char* new_name){
	data_node* responsible_node =find_node(absolute_path);
	responsible_node->modify_index(old_name,new_name);
}

void tree::delete_node_from_parent(char * path){
	data_node* returned_node = root,*child;
	bool found=false; char * cur_path = null;
	while(!found && returned_node!=null){
		if(returned_node->is_my_direct_child(path)== true){
			returned_node->delete_child(path);
			break;
		}
		else{
			child = returned_node->get_next_child(cur_path);
			cur_path = child->get_directory_name();
			if(child->is_my_direct_child(path)){
				returned_node = child;
				returned_node->delete_child(path);
				break;
			}
			else if(child->is_my_child(path)){
				returned_node = child;
				cur_path = null;
			}
			else
				child = returned_node->get_next_child(cur_path);
		}
	}
}

data_node* tree::find_node(char * path){
	data_node* returned_node = root,*child;
	bool found=false; char * cur_path = null;
	while(!found ){
		if(returned_node==null)
			break;
		if(returned_node->is_mine_path(path)== true){
			found = true;
		}
		else{
			child = returned_node->get_next_child(cur_path);
			cur_path = child->get_directory_name();
			if(child->is_mine_path(path)){
				returned_node = child;
				found = true;
			}
			else if(child->is_my_child(path)){
				returned_node = child;
				cur_path = null;
			}
			else
				child = returned_node->get_next_child(cur_path);
		}
	}
	return returned_node;
}

//------------------------------------------------     Tree's node structure    -----------------------------------------------------------------------//

data_node::data_node(char* path_name,index_of_directory* containing_files){
	int length = strlen(path_name);
	directory_name = new char[length];
	strcpy(directory_name,path_name);
	height = compute_height(path_name);
	files = containing_files;
};

data_node::~data_node(){
	delete[] directory_name;
//	cout<<"Deleted directory name"<<endl;
	delete files;
//	cout<<"Deleted files"<<endl;
	clear_subtree();
//	cout<<"Cleared Subtree"<<endl;
	neighbors.clear();
//	cout<<"Neighbors are also cleared"<<endl;
}

void data_node::set_node_name(char* new_name){
	delete[] directory_name;
	directory_name = new char[strlen(new_name)];
	strcpy(directory_name,new_name);
}

void data_node::clear_subtree(){
	list<data_node*>::iterator it;
	for(it = neighbors.begin();it!=neighbors.end();it++)
		delete *it;
}

int data_node::compute_height(char *path){
	int height =0,length=strlen(path);
	for(int i=0;i<length;i++){
		if(path[i] == '/')
				height++;
	}
	return height;
}

int data_node::get_height(){
	return height;
};

void data_node::print_indexes(int level){
	files->print_list(level);
}

void data_node::add_index(char* path){
	files->add_index(path);
}

data_node* data_node::return_correct_child(char* path){
	char *current_path =null;
	data_node* child;
	bool found = false;
	if(neighbors.empty())
		child = null;
	else{
		while(found == false){
			child = get_next_child(current_path);
			if(child ==null)
				break;
			char * child_path_name =child->get_directory_name();
			unsigned int j=strlen(directory_name);
			while(j<strlen(path)){
				if(path[j] != child_path_name[j]){
					found = false;
					break;
				}
				if(path[j]!='/'){
					found = true;
					break;
				}
				j++;
			}
			if(found)
				break;
			else
				current_path = child_path_name;
		}
	}
	return child;
}

void data_node::print_children(){
	list<data_node*>::iterator it;
	for(it = neighbors.begin();it!=neighbors.end();it++)
		cout<<" --"<<(*it)->get_directory_name()<<endl;
}

bool data_node::is_mine_path(char * path_name){
	if(is_my_child(path_name) && (height == (compute_height(path_name)) ) ){
		return true;
	}
	else{
		return false;
	}
}

void data_node::delete_index(char* index_name){
	files->delete_index(index_name);
}

data_node * data_node::get_next_child(char* prev_path){
	data_node* return_node = null;
	if(prev_path == null){
		if(!neighbors.empty())
			return_node = neighbors.front();
			else
			return_node =  null;
	}
	else{
		list<data_node*>::iterator it;
		for(it = neighbors.begin();it!=neighbors.end();it++){
			if( strcmp((*it)->directory_name,prev_path) ==0){
					it++;
					return_node = *it;
					break;
			}
		}
	}
	return return_node;
}

list<data_node*> data_node::get_children(){
	return neighbors;
}

void data_node::modify_index(char *old_name,char* new_name){
	files->modify_index(old_name,new_name);
}

bool data_node::is_my_direct_child(char* path){
	if((compute_height(path)==height+1) && (is_my_child(path)))
		return true;
	else
		return false;
}

bool data_node::is_my_child(char* path){
	bool return_value=true;
	for(unsigned int i=0;i<strlen(directory_name);i++){
		if(directory_name[i] != path[i]){
			return_value = false;
			break;
		}
	}
	return return_value;
};

char* data_node::get_directory_name(){
	return directory_name;
}

void data_node::add_child(data_node* new_child){
	neighbors.push_back(new_child);
}

void data_node::delete_child(char * path){
	list<data_node*>::iterator it;
	for(it = neighbors.begin();it!=neighbors.end();it++)
		if(strcmp(path,(*it)->get_directory_name())==0){
			neighbors.erase(it);
			break;
		}
}

list<char*> data_node::get_list(){
	files->print_list(0);
	return files->get_indexes();
}
