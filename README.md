# Local picture cloud-Server

Ιn this work, we created a beta-production project for a Wi-Fi local Server. This repository contains code for installing and deploying a local server on a GNU\Linux device. It has three main components: 

* C/C++ code responsible for the Server to be installed in a linux device 
* Android-Java code for  mobile phones
* A Desktop-Java application for accessing the local Server from any device.



### Description 

Our experiment server was hosted on a Raspberry-Pi 3, which was connected on a Wi-Fi.  [ServerCode](https://gitlab.com/timos/Gitlab/tree/master/sourceCode/Raspberry-backend) contains the main functions of the Server.
Android mobiles [AndroidApplication](https://gitlab.com/timos/Gitlab/tree/master/sourceCode/PhotoSharing), granted wifi access, can use the Server to privately save photos currently on the phone. A list of files can be monitored and automatically update the Server everytime a client-Server connection can be established. Photos can also be automatically be removed from the mobile, for space efficiency. 
Given the correct credential, any connected device has access to the photos and can process, monitor, update, remove, download or upload photos [Desktop Application](https://gitlab.com/timos/Gitlab/tree/master/sourceCode/TransferApp). 


### Build With

* C/C++: [STL](http://www.cplusplus.com/reference/stl/), [pthread, processes & synchronization](https://randu.org/tutorials/threads/), [SocketApi](http://man7.org/linux/man-pages/man2/socket.2.html). .
* Java:  [JavaFx](https://openjfx.io/), [Android Java](https://developer.android.com/).


### Authors 

* **Korres  Stamatios**
* **Kolovos Paris**