package runnables;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import controllers.FileManagerCntrl;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UploadRunnable implements Runnable {

	private File absoluteFileUrl;
	private FileManagerCntrl controller;
	private String picName; 
	private String currentFolder;
	
	// For Data exchange 
	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;
	private Socket client;
	private String token;
	private byte[] bytes = new byte[1024];
	private String ip; 
	private String requestToServer;
	private String serverAnswer;
	private FlowPane curflowPane;
	
	public UploadRunnable(FlowPane curflowPane,FileManagerCntrl controller,String currentFolder,String picName,File absoluteFileUrl) {
		// TODO Auto-generated constructor stub
		this.controller = controller;
		this.curflowPane = curflowPane;
		this.absoluteFileUrl = absoluteFileUrl;
		this.picName = picName;
		this.currentFolder = currentFolder;
		this.token = controller.getToken();
		this.in = controller.getIn();
		this.out = controller.getOut();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Uploading request");
		try {
			requestToServer = new String(token+" upload_file_req " + currentFolder + picName+"\0");
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
			
			bytes = new byte[1024];
			in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			if(serverAnswer.equals("Cont_size")) {
				requestToServer = Long.toString(absoluteFileUrl.length());
				out.write(requestToServer.getBytes());
				
				bytes = new byte[1024];
				in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				if(serverAnswer.equals("Cont_File")){
					upload_file(absoluteFileUrl);
					bytes = new byte[1024];
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					if(serverAnswer.equals("File_Received")) {
						add_vbox_pic(picName);
					}
				}
			}
		}catch(FileNotFoundException e) {
			System.out.println("File was not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void upload_file(File file) throws IOException {
	 FileInputStream fileIn = new FileInputStream(file);
        int count;
        while ((count = fileIn.read(bytes)) > 0) 
        	out.write(bytes,0,count);
		fileIn.close();
	}

	public void add_vbox_pic(String picName) throws IOException {
		
		VBox vb = new VBox();
		vb.setPadding(new Insets(controller.getPadding(), controller.getPadding(), controller.getPadding(), controller.getPadding()));
	    String shortName = serverAnswer;
	    if (picName.length() > 10) {
	    	shortName = picName.substring(0, 10);
	    	shortName += "...";
	    }
	    Text desc = new Text(60,10,shortName);
	    vb.setAccessibleText(picName);
	    
		bytes = new byte[1024];
        in.read(bytes);
		serverAnswer = new String(bytes, StandardCharsets.UTF_8);
		serverAnswer = serverAnswer.trim();
		int sizeOfIMage = Integer.parseInt(serverAnswer);
		requestToServer = new String("ok_size\0");
		out.write(requestToServer.getBytes());
		
		//Read Actual image 
		byte[] imageBytes = new byte[sizeOfIMage];
		bytes = new byte[1024];
		int currentsize =0;
		int readBytes;
		while(currentsize<sizeOfIMage) {
			readBytes =in.read(bytes);
			System.arraycopy(bytes, 0, imageBytes, currentsize,readBytes);
			currentsize+=readBytes;
			bytes = new byte[1024];
		}
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        Image img = SwingFXUtils.toFXImage(image, null);
	    ImageView imv = new ImageView(img);
	    imv.setFitHeight(controller.getimageSize());
	    imv.setFitWidth(controller.getimageSize());
	    imv.setPreserveRatio(true);
	    desc.setWrappingWidth(controller.getimageSize());
	    
	    vb.getChildren().addAll(imv,desc);
	    controller.imageEffects(curflowPane,vb);
	    controller.fadeEffect(500, vb);
	    
	    Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				curflowPane.getChildren().add(vb);
			}
		});
	}
}
