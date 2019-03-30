/*
 * FileTransfering.h
 *
 *  Created on: Jan 12, 2018
 *      Author: paris
 */

#ifndef FILETRANSFERING_H_
#define FILETRANSFERING_H_

namespace flts {
    void sendFile(char* type,char* filename,char* path,int socket);
    int receiveFile(int socket,char*path);
}

#endif /* FILETRANSFERING_H_ */
