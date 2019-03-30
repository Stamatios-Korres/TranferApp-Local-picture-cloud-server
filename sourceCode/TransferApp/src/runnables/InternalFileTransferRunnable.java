package runnables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.geometry.Insets;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import controllers.FileManagerCntrl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class InternalFileTransferRunnable implements Runnable {
	
	// Responsible for operations Move and Copy inside our Application

	private String action;
	private List<VBox> myList;
	private FileManagerCntrl controller;
	private FlowPane curFlowPane; 
	private String ip = FileManagerCntrl.getIp();
	private DataOutputStream out;
	private DataInputStream in;
	private Socket client;
	private String token;
	private byte[] bytes = new byte[1024];
	private String requestToServer;
	private String serverAnswer;
	private String prevFolderPath;
	private FlowPane prevFlowPane;
	private String currentPath;
	
	public InternalFileTransferRunnable(String action,List<VBox> list,FlowPane prevFlowPane,String prevFolderPath,FileManagerCntrl controller,FlowPane workingFlowPane,String currentPath){
		this.action = action;
		this.myList = list;
		this.controller = controller;
		this.prevFlowPane = prevFlowPane;
		this.curFlowPane = workingFlowPane;
		this.prevFolderPath = prevFolderPath;
        out = this.controller.getOut();
        in = this.controller.getIn();
        this.token = this.controller.getToken();
        this.currentPath = currentPath;
	}
	
	public boolean check_if_subfolder(String prevFolder,String curFolder) {
		int height_prev=prevFolder.split("/").length-1;
		int height_cur=curFolder.split("/").length-1;
		if(height_cur<=height_prev)
			return false;
		else {
			char[] var1 = prevFolder.toCharArray();
			char[] var2 = curFolder.toCharArray();
			if(var1.length>=var2.length) {
				for(int i=0;i<var2.length;i++) {
					if(var2[i]!=var1[i])
						return  false;
				}
			}
			else{
				for(int i=0;i<var1.length;i++) {
					if(var2[i]!=var1[i])
						return false;
				}
			}
			return true;
		}
	}
	
	public void copy_item(VBox prevVb) {
		if(curFlowPane!=null) {
			if(prevVb.getAccessibleRoleDescription().equals("picture")) {
//				System.out.println("Image");
				VBox vb = new VBox();
				vb.setPadding(new Insets(controller.getPadding(), controller.getPadding(), controller.getPadding(), controller.getPadding()));
			    String shortName = prevVb.getAccessibleText();
			    vb.setAccessibleText(shortName);
			    if (shortName.length() > 10) {
			    	shortName = shortName.substring(0, 10);
			    	shortName += "...";
			    }
			    Text desc = new Text(60,10,shortName);
			    ImageView imageView = (ImageView) prevVb.getChildren().get(0);
			    Image img = imageView.getImage();
			    imageView = new ImageView(img);
			    imageView.setFitHeight(controller.getimageSize());
			    imageView.setFitWidth(controller.getimageSize());
			    imageView.setPreserveRatio(true);
			    desc.setWrappingWidth(controller.getimageSize());
			    vb.getChildren().addAll(imageView,desc);
			    controller.imageEffects(curFlowPane,vb);
			    controller.fadeEffect(500, vb);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						curFlowPane.getChildren().add(vb);
					}
				});		  
			}
			else {
//				System.out.println("Folder");
			    String shortName = prevVb.getAccessibleText();
			    VBox vb = new VBox();
				vb.setPadding(new Insets(controller.getPadding(), controller.getPadding(), controller.getPadding(), controller.getPadding()));
			    vb.setAccessibleText(shortName);
			    if (shortName.length() > 10) {
			    	shortName = shortName.substring(0, 10);
			    	shortName += "...";
			    }
			    Text desc = new Text(60,10,shortName);
			    Image img = new Image(controller.getClass().getResource("../img/folder.png").toExternalForm(),controller.getimageSize(),controller.getimageSize(),false,false);
			    ImageView imv = new ImageView(img);
			    desc.setWrappingWidth(controller.getimageSize());
			    vb.getChildren().addAll(imv,desc);
			    controller.folderEffects(curFlowPane,vb);
			    controller.fadeEffect(500, vb);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						curFlowPane.getChildren().add(0,vb);
					}
				});		
			}
		}
	}
	
	public void add_item(VBox prevVb) {
		if(prevVb.getAccessibleRoleDescription().equals("picture")) {
//			System.out.println("Image");
			if(curFlowPane!=null) {
				VBox vb = new VBox();
				vb.setPadding(new Insets(controller.getPadding(), controller.getPadding(), controller.getPadding(), controller.getPadding()));
			    String shortName = prevVb.getAccessibleText();
			    vb.setAccessibleText(shortName);
			    if (shortName.length() > 10) {
			    	shortName = shortName.substring(0, 10);
			    	shortName += "...";
			    }
			    Text desc = new Text(60,10,shortName);
			    ImageView imageView = (ImageView) prevVb.getChildren().get(0);
			    imageView.setFitHeight(controller.getimageSize());
			    imageView.setFitWidth(controller.getimageSize());
			    imageView.setPreserveRatio(true);
			    desc.setWrappingWidth(controller.getimageSize());
			  

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						vb.getChildren().addAll(imageView,desc);
					    controller.imageEffects(curFlowPane,vb);
					    controller.fadeEffect(500, vb);
						prevFlowPane.getChildren().remove(prevVb);
						curFlowPane.getChildren().add(vb);
					}
				});		 
			}
			else {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						prevFlowPane.getChildren().remove(prevVb);
					}
				});		
			}
			 
		}
		else {
//			System.out.println("Folder");
			if(curFlowPane!=null) {
			    String shortName = prevVb.getAccessibleText();
			    VBox vb = new VBox();
				vb.setPadding(new Insets(controller.getPadding(), controller.getPadding(), controller.getPadding(), controller.getPadding()));
			    vb.setAccessibleText(shortName);
				 if (shortName.length() > 10) {
			    	shortName = shortName.substring(0, 10);
			    	shortName += "...";
			    }
			    Text desc = new Text(60,10,shortName);
			    Image img = new Image(controller.getClass().getResource("../img/folder.png").toExternalForm(),controller.getimageSize(),controller.getimageSize(),false,false);
			    ImageView imv = new ImageView(img);
			    desc.setWrappingWidth(controller.getimageSize());
			    
			    
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						vb.getChildren().addAll(imv,desc);
					    controller.folderEffects(curFlowPane,vb);
					    controller.fadeEffect(500, vb);
						prevFlowPane.getChildren().remove(prevVb);
						curFlowPane.getChildren().add(0,vb);
					}
				});		
			}
			else {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						prevFlowPane.getChildren().remove(prevVb);
					}
				});		
			}
		}
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
    	try {
		 if(action.equals("Copy")) {
	        	System.out.println("Copy request");
	        	if(myList.size() >1) {
	        		requestToServer = new String(token +" "+ "cp_file_req"+" list_of_files");
	        		requestToServer+="\0";
	        		out.write(requestToServer.getBytes());
	        		
	        		in.read(bytes);
	        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
	        		int port = Integer.parseInt(serverAnswer);
		    		client = new Socket(ip, port);
		    		client.setSoLinger(true, 0);
		    		OutputStream outToServer = client.getOutputStream();
		    		out = new DataOutputStream(outToServer);
		    		InputStream inFromServer = client.getInputStream();
		    		in = new DataInputStream(inFromServer);
		    		Iterator<VBox> it = myList.iterator();
		    		while (it.hasNext()) {
		                VBox loop= (VBox) it.next();
		                if(loop.getAccessibleRoleDescription().equals("folder")){
		                	if(check_if_subfolder(prevFolderPath+loop.getAccessibleText(),currentPath+loop.getAccessibleText())) {
		                		Platform.runLater(new Runnable() {
									@Override
									public void run() {
										controller.alert("Can't move a folder inside its subfolder");
									}
								});
		                		break;
		                	}
		                }
		    			requestToServer = new String(prevFolderPath+loop.getAccessibleText());

		    			requestToServer+="\0";
		    			out.write(requestToServer.getBytes());
			    		
			    		bytes = new byte[1024];
			    		//Previous Position
			    		in.read(bytes);
		        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			    		serverAnswer = serverAnswer.trim();
			    		
			    		if(serverAnswer.equals("Ok_prev_location")) {
			    			requestToServer =  new String(currentPath+loop.getAccessibleText());
			    			requestToServer+="\0";
			    			out.write(requestToServer.getBytes());
				    		
				    		bytes = new byte[1024];
				    		//Next Position
				    		in.read(bytes);
			        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				    		serverAnswer = serverAnswer.trim();
			    			System.out.println("ServerAnswer: "+serverAnswer);
				    		if(serverAnswer.equals("file_copied")) 
				    			copy_item(loop);
				    		else {
				    			Platform.runLater(new Runnable() {
									@Override
									public void run() {
										controller.alert(serverAnswer+" "+loop.getAccessibleText());
									}
								});	
				    		}
			    		}
			    		else {
			    			Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.alert("Sorry, file : "+loop.getAccessibleText()+ "was not moved. Info:Server error moving the file: "+serverAnswer);
								}
							});	
			    		}
		    		}
		    		requestToServer = new String("End\0");
		    		out.write(requestToServer.getBytes());
	        	}
	        	else {
	        		String prevImagePath = prevFolderPath+myList.get(0).getAccessibleText();
	        		String curImagePath = currentPath+myList.get(0).getAccessibleText();
	        		
	        		if(myList.get(0).getAccessibleRoleDescription().equals("folder")){
	                	if(check_if_subfolder(prevImagePath,curImagePath)) {
	                		Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.alert("Can't move a folder inside its subfolder");
								}
							});	
	                		return;
	                	}
	                }
	        		requestToServer = new String(token +" "+ "cp_file_req"+" "+ prevImagePath+" "+ curImagePath);
	        		requestToServer+="\0";
		    		out.write(requestToServer.getBytes());
		    		
		    		bytes = new byte[1024];
		    		
		    		in.read(bytes);
	        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
		    		int port = Integer.parseInt(serverAnswer);
		    		client = new Socket(ip, port);
		    		client.setSoLinger(true, 0);
		    	
		    		
		    		OutputStream outToServer = client.getOutputStream();
		    		out = new DataOutputStream(outToServer);
		    		InputStream inFromServer = client.getInputStream();
		    		in = new DataInputStream(inFromServer);
		    		
		    		bytes = new byte[1024];
		    		in.read(bytes);
		    		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
		    		if(serverAnswer.equals("file_copied")) 
		    			copy_item(myList.get(0));
		    		else {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert(serverAnswer+" "+myList.get(0).getAccessibleText());
							}
						});	
		    		}
	    		}
	        }
	        else if(action.equals("Move")) {
	        	if(myList.size() >1) {
	        		requestToServer = new String(token +" "+ "mv_file_req"+" list_of_files");
		    		out.write(requestToServer.getBytes());
		    		
		    		bytes = new byte[1024];
		    		//TODO Bug when moving files around
		    		in.read(bytes);    
	        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
		    		
		    		int port = Integer.parseInt(serverAnswer);
		    		client = new Socket(ip, port);
		    		client.setSoLinger(true, 0);
		    		OutputStream outToServer = client.getOutputStream();
		    		out = new DataOutputStream(outToServer);
		    		InputStream inFromServer = client.getInputStream();
		    		in = new DataInputStream(inFromServer);
		    		Iterator<VBox> it = myList.iterator();
		    		while (it.hasNext()) {
		                VBox loop= (VBox) it.next();
		                if(loop.getAccessibleRoleDescription().equals("folder")){
		                	if(check_if_subfolder(prevFolderPath+loop.getAccessibleText(),currentPath+loop.getAccessibleText())) {
		                		Platform.runLater(new Runnable() {
									@Override
									public void run() {
										controller.alert("Can't move a folder inside its subfolder");
									}
								});	
		                		return;
		                	}
		                }
		    			requestToServer = new String(prevFolderPath+loop.getAccessibleText());
		    			requestToServer+="\0";
		    			out.write(requestToServer.getBytes());
			    		
			    		bytes = new byte[1024];
			    		in.read(bytes);
		        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			    		serverAnswer = serverAnswer.trim();
			    		
			    		if(serverAnswer.equals("Ok_prev_location")) {
			    			requestToServer =  new String(currentPath+loop.getAccessibleText());
			    			requestToServer+="\0";
			    			out.write(requestToServer.getBytes());
				    		
				    		bytes = new byte[1024];
				    		in.read(bytes);
			        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				    		serverAnswer = serverAnswer.trim();
				    		System.out.println(serverAnswer);
				    		if(serverAnswer.equals("file_moved")) 
				    			add_item(loop);
			    			else {
				    			Platform.runLater(new Runnable() {
									@Override
									public void run() {
										controller.alert(serverAnswer+" "+loop.getAccessibleText());
									}
								});	
				    		}
			    		}
			    		else {
			    			Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.alert("Sorry, file : "+loop.getAccessibleText()+ "was not moved. Info:Server error moving the file: "+serverAnswer);
								}
							});	
			    			break;
			    		}
		    		}
		    		requestToServer = new String("End\0");
		    		out.write(requestToServer.getBytes());

	        	}
	        	else {
	        		String prevImagePath = prevFolderPath+myList.get(0).getAccessibleText();
	        		String curImagePath = currentPath+myList.get(0).getAccessibleText();
	        		if(myList.get(0).getAccessibleRoleDescription().equals("folder")){
	                	if(check_if_subfolder(prevFolderPath+myList.get(0).getAccessibleText(),currentPath+myList.get(0).getAccessibleText())) {
			    			Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.alert("Can't move a folder inside its subfolder");
								}
							});	
	                		return;
	                	}
	                }
	        		requestToServer = new String(token +" "+ "mv_file_req"+" "+ prevImagePath+" "+ curImagePath);
		    		out.write(requestToServer.getBytes());
		    		
		    		bytes = new byte[1024];
		    		in.read(bytes);
	        		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
		    		int port = Integer.parseInt(serverAnswer);
		    		client = new Socket(ip, port);
		    		client.setSoLinger(true, 0);
		    	
		    		
		    		OutputStream outToServer = client.getOutputStream();
		    		out = new DataOutputStream(outToServer);
		    		InputStream inFromServer = client.getInputStream();
		    		in = new DataInputStream(inFromServer);

		    		
		    		bytes = new byte[1024];
		    		in.read(bytes);
		    		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		    		serverAnswer = serverAnswer.trim();
		    		if(serverAnswer.equals("file_moved")) 
		    			add_item(myList.get(0));
		    		else {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert(serverAnswer+" "+myList.get(0).getAccessibleText());
							}
						});	
		    		}
	        	}

    		}
				
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
