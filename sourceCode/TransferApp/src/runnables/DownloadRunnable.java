package runnables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import controllers.FileManagerCntrl;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

public class DownloadRunnable implements Runnable {
	
	public FileManagerCntrl controller;
	public String folder;
	public List<VBox> selectedVbs;
	public String absolutePath;
	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;
	private Socket client;
	private String token;
	private byte[] bytes = new byte[1024];
	private String ip;
	private String serverAnswer;
	private String requestToServer;;
	
	public DownloadRunnable(FileManagerCntrl controller, String folder,List<VBox> selectedVbs,String absolutePath) {
		
		this.controller = controller;
		this.folder = folder;
		this.selectedVbs = selectedVbs;
		this.absolutePath = absolutePath;
		this.out = controller.getOut();
		this.in = controller.getIn();
		this.ip = FileManagerCntrl.getIp();
		this.token = controller.getToken();
		
	}
	
	@Override
	public void run() {
		requestToServer = token + " download_req unknownNumber\0";
		try {
			out.write(requestToServer.getBytes());
			in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			int port = Integer.parseInt(serverAnswer);
			client = new Socket(ip, port);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			out = new DataOutputStream(outToServer);
			InputStream inFromServer = client.getInputStream();
			in = new DataInputStream(inFromServer);
			
			for(VBox iterator: selectedVbs) {
				if(iterator.getAccessibleRoleDescription().equals("picture")) {
					System.out.println("Picture download");
					requestToServer= new String(folder+iterator.getAccessibleText()+"\0");
					out.write(requestToServer.getBytes());
					
					bytes = new byte[1024]; 	// Type of file
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					
					requestToServer = new String("Ok_type\0");
					out.write(requestToServer.getBytes());
					
					bytes = new byte[1024]; 	// Read size of file
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					
					int filesize = Integer.parseInt(serverAnswer);
					System.out.println("file's size: "+filesize );
					requestToServer = new String("ok_size\0");
					out.write(requestToServer.getBytes());
					
					//Read Actual image 
					
					String pathSaved = absolutePath+"/"+iterator.getAccessibleText();
					download_item(pathSaved,filesize);
					requestToServer = new String("all_ok\0");
					out.write(requestToServer.getBytes());
				}
				else { //Folder
					System.out.println("Folder download");
					
					requestToServer= new String(folder+iterator.getAccessibleText()+"\0");
					out.write(requestToServer.getBytes());
					
					bytes = new byte[1024]; 	// Type of file
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					
					requestToServer = new String("Ok_type\0");
					out.write(requestToServer.getBytes());
					
					String folderPath = absolutePath+"/"+iterator.getAccessibleText();
					
					createDir(folderPath);
					
					DownloaFolder(folderPath,folder+iterator.getAccessibleText());
					
					requestToServer = new String("all_ok\0");
					out.write(requestToServer.getBytes());
					
				}
			}			
		 Platform.runLater(new Runnable() {
				@Override
				public void run() {
					controller.alert("Files were downloaded");
				}
			});
			requestToServer= new String("Files_downloaded\0");
			System.out.println("File were downloaded");
			out.write(requestToServer.getBytes());
			if(client.isClosed())
				client.close();
		}catch(NumberFormatException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void download_item(String pathSaved,int filesize) throws IOException {
		File file = new File(pathSaved);
		int currentsize = 0;
		int bytesread = 0;
		FileOutputStream fos = new FileOutputStream(file);
		while(currentsize<filesize) {
			bytesread =in.read(bytes);
			fos.write(bytes,0,bytesread);
			currentsize+=bytesread;
			bytes = new byte[1024];
		}
		fos.flush();
		fos.close();
	}

	public File createDir(String folderPath) {
		File theDir = new File(folderPath);
		try{
			if (!theDir.exists()) 
		        theDir.mkdir();
		}
		catch(SecurityException se){
	    	System.out.println("Security issues");
	        //handle it
	    	theDir= null;
	    }
		return theDir;
	}
	
	public void DownloaFolder(String SavePath,String folderPath) throws IOException {
		
		bytes = new byte[1024];
		
        in.read(bytes);
		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		serverAnswer = serverAnswer.trim();

		// 2 types of messages: 1) Folder 2)Picture
		
		while(!serverAnswer.equals("End")) {
			// Choose what type of file it is
			
			if(serverAnswer.equals("Folder")) {
				requestToServer = new String("Ok_type\0");
				out.write(requestToServer.getBytes());
				
				bytes = new byte[1024];
		        in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				
				String folderName = SavePath + "/" + serverAnswer;
				String tempPath = folderPath +"/" + serverAnswer;
				
				requestToServer = new String("ok_name\0");
				out.write(requestToServer.getBytes());

				createDir(folderName);								// To create Dir : 1) Path to Save 2) Path to request
				System.out.println("Requesting item: "+tempPath);
				System.out.println("Saving at Path: "+folderName);
				DownloaFolder(folderName,tempPath);
				
				requestToServer = new String("all_ok\0");
				out.write(requestToServer.getBytes());
			}
			else if(serverAnswer.equals("Image")) {
				
				requestToServer = new String("Ok_type\0");
				out.write(requestToServer.getBytes());
				
				// Read name of image 
				bytes = new byte[1024];
		        in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				String tempPath = SavePath +"/" + serverAnswer;

				requestToServer = new String("ok_name\0");
				out.write(requestToServer.getBytes());

				// Size of image 
				bytes = new byte[1024];
		        in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				
				int sizeOfImage = Integer.parseInt(serverAnswer);
				
				requestToServer = new String("ok_size\0");
				out.write(requestToServer.getBytes());
				
				download_item(tempPath, sizeOfImage);
				
				requestToServer = new String("all_ok\0");
				out.write(requestToServer.getBytes());
			   
			}
			bytes = new byte[1024];	
			in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
		}
		System.out.println("Folder is complete");
	}

}
