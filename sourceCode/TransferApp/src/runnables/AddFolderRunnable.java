package runnables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import controllers.FileManagerCntrl;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AddFolderRunnable implements Runnable{

	private String currentFolder;
	private FlowPane flowPane;
	private FileManagerCntrl controller;
	private byte[] bytes = new byte[1024];
	private String serverAnswer;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket client;
	private String ip = null;
	private String token =null;
	private int imageSize;
	
	public AddFolderRunnable(String ip,String currentFolder, FlowPane flowPane, FileManagerCntrl controller,DataInputStream in,DataOutputStream out) {
		this.currentFolder = currentFolder;
		this.flowPane = flowPane;
		this.controller = controller;
		this.token = this.controller.getToken();
		this.in = in;
		this.ip = ip;
		this.imageSize = this.controller.getimageSize();
		this.out = out;
	}
	
	@Override
	public void run() {
		addFolder(flowPane);
		// TODO Auto-generated method stub
		
	}
	
	public void addFolder(Node parent) {
		VBox vb = new VBox();
		vb.setAccessibleRoleDescription("folder");
		vb.setPadding(new Insets(10, 10, 10, 10));
		TextField textField = new TextField(); 
		controller.folderEffects(flowPane,vb);
		controller.fadeEffect(500, vb);
	    textField.setText("New Folder");  
	    textField.setPrefSize(100, 10);
	    textField.selectAll();
		Image img = new Image(controller.getClass().getResource("../img/folder.png").toExternalForm(),imageSize,imageSize,false,false);
	    ImageView imv = new ImageView(img);
	    vb.getChildren().addAll(imv,textField);
	      
	    Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	flowPane.getChildren().add(0,vb);	 
	        	textField.requestFocus();
	        }
	    });
	    
	    textField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	    	try {
	    		
		    	if(ev.getCode().equals(KeyCode.ENTER)) {
		    		if(textField.getText().length() ==0) {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert("Enter a name, field cannot be blank!");
								
							}
						});
		    		}
		    		else if(textField.getText().contains("/") ||  textField.getText().contains(" ") ||  textField.getText().contains(".")) {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert("Folder cannot contain the characters: / or . and any spaces");
							}
						});
		    		}
		    		else if(Check_if_exists_Alert( textField.getText() )){
		    			// Do nothing 
		    		}
		    		else {
		    			System.out.println("Ok sending request");
		    			Text newText = new Text();
		    			newText = new Text(60,10,textField.getText());
			    		String serverRequest = new String(token + " mkdir_req "+ currentFolder+newText.getText());
			    		serverRequest+="\0";
			    		System.out.println(serverRequest);
			    		out.write(serverRequest.getBytes());
			    		bytes = new byte[1024];
			    		System.out.println("Waiting answer ...");
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
						
						//Filename
						bytes = new byte[1024];	
						in.read(bytes);
						serverAnswer = new String(bytes, StandardCharsets.UTF_8);
						serverAnswer = serverAnswer.trim();
						if(serverAnswer.equals("all_ok")) {
							vb.setAccessibleText(newText.getText());
				    		vb.getChildren().remove(1);
				    		newText.setWrappingWidth(imageSize);
				    		vb.getChildren().add(newText);	   
						}
						else {
						    Platform.runLater(new Runnable() {
								@Override
								public void run() {
								   flowPane.getChildren().remove(vb);
								}
							});
						}
			    	}
		    	}
	    	}
			catch(IOException e) {
	    	}
	    });
	}
		
	public boolean Check_if_exists_Alert(String name) {
		boolean exists = false;
		for(Node vb :flowPane.getChildren()) {
			VBox box = (VBox) vb;
			if(box.getAccessibleRoleDescription().equals("folder")) {
				if(box.getAccessibleText()!=null) {
					if(box.getAccessibleText().equals(name)) {
						exists = true;
						Platform.runLater(new Runnable() {
					        @Override
					        public void run() {
					        	controller.alert("a folder with same name exists" );
					        }
					    });
					}
				}
			}
		}
		return exists;
	}
	
	public boolean Check_if_exists(String name) {
		boolean exists = false;
		for(Node vb :flowPane.getChildren()) {
			VBox box = (VBox) vb;
			if(box.getAccessibleRoleDescription().equals("folder")) {
				if(box.getAccessibleText()!=null) {
					if(box.getAccessibleText().equals(name)) 
						exists = true;
				}
			}
		}
		return exists;
	}

}
