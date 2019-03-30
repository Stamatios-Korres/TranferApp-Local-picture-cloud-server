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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class FileRenameRunnable implements Runnable {
	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;
    private FileManagerCntrl controller;
	private byte[] bytes = new byte[1024];
	private Socket client;
	private String serverAnswer;
	private String ip;
	private String token;
	private String currentFolder;
	private VBox vb;
    private final Background unfocusBackground = new Background( new BackgroundFill( Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY ) );
    
	public FileRenameRunnable( VBox vb,String currentFolder,DataOutputStream out,DataInputStream in,FileManagerCntrl controller,FlowPane flowPane) {
		// TODO Auto-generated constructor stub
		this.out = out;
		this.in = in;
		this.controller = controller;
		this.token = this.controller.getToken();
		this.currentFolder = currentFolder;
		this.vb = vb;
	}
	
	@Override
	public void run() {
		renameFile();
	}
	
	public boolean Check_if_exists(String name) {
		boolean exists = false;
		for(Node vb :controller.getFlowPane().getChildren()) {
			VBox box = (VBox) vb;
			if(box.getAccessibleRoleDescription().equals("picture")) {
				if(box.getAccessibleText()!=null) {
					if(box.getAccessibleText().equals(name)) 
						exists = true;
				}
			}
		}
		return exists;
	}
	
	public void renameFile()  {
		String oldName = currentFolder + vb.getAccessibleText();
		String picName = vb.getAccessibleText();
		TextField textField = new TextField(); 
	    Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	vb.getChildren().remove(1);
	        	System.out.println(vb.getAccessibleRoleDescription());
	        	if(vb.getAccessibleRoleDescription().equals("picture")){
		    	    textField.setText(vb.getAccessibleText());  
		    	    textField.setPrefSize(100, 10);
		    	    textField.selectRange(0, vb.getAccessibleText().indexOf(".jpg"));
	        	}
	        	else { 
	        		textField.setText(vb.getAccessibleText());  
		    	    textField.setPrefSize(100, 10);
		    	    textField.selectAll();
	        	}
	    	    vb.getChildren().add(textField);
	        	textField.requestFocus();
	        }
	    });
	    textField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	    	try {
	    		if(ev.getCode().equals(KeyCode.ESCAPE)) {
	    			vb.getChildren().remove(1);
		    		Text desc = new Text(60,10,picName);
		    		desc.setWrappingWidth(controller.getimageSize());
		    		vb.getChildren().add(desc);	
	    		}
	    		else if(ev.getCode().equals(KeyCode.ENTER)) {
		    		ev.consume();
		    		Text newText ;
		    		if(textField.getText().length()== 0 ||textField.getText().equals(picName)) {
		    			vb.getChildren().remove(1);
			    		Text desc = new Text(60,10,picName);
			    		desc.setWrappingWidth(controller.getimageSize());
			    		vb.getChildren().add(desc);	
		    		}
		    		else if(textField.getText().contains(" ") || textField.getText().contains("/")) {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert("Name cannot contain blanks or the character /");
								// TODO Auto-generated method stub
								
							}
		    			});
		    		}
		    		else if(Check_if_exists(textField.getText())) {
		    			Platform.runLater(new Runnable() {
							@Override
							public void run() {
								controller.alert("Name already exists");
								// TODO Auto-generated method stub
								
							}
		    			});
		    		}
		    		else {
		    			newText = new Text(60,10,textField.getText());
			    		String newName = currentFolder+newText.getText();
			    		String requestToServer = new String(token + " rename_file_req " + oldName +" " + newName+ "\0");
			    		System.out.println(requestToServer);
						out.write(requestToServer.getBytes());
						
						//connect to new port
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
						
						bytes = new byte[1024];	
						in.read(bytes);
						serverAnswer = new String(bytes, StandardCharsets.UTF_8);
						serverAnswer = serverAnswer.trim();
						Platform.runLater(new Runnable() {
					        @Override
							public void run() {
								if(serverAnswer.equals("all_ok")) {
					    		vb.getChildren().remove(1);
					    		vb.setAccessibleText(newText.getText());
					    		newText.setWrappingWidth(controller.getimageSize());
					    		Text desc = new Text(60,10,newText.getText());
					    		vb.getChildren().add(desc);	 
					    		vb.setBackground(unfocusBackground);
							}
							else {
								vb.getChildren().remove(1);
					    		newText.setWrappingWidth(controller.getimageSize());
					    		Text desc = new Text(60,10,oldName);
					    		vb.getChildren().add(desc);	
							}
						 }
						 });
		    		}
		    	}
	    	}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
	    	}
	    });
	}
}
