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
import controllers.FileManagerCntrl;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class FolderIndexRunnable implements Runnable {

	private String currentFolder;
	private FlowPane flowPane;
	private FileManagerCntrl controller;
	private byte[] bytes = new byte[1024];
	private String ReplytoServer;
	private String serverAnswer;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket client;
	private String ip = null;
	private String token =null;
	private int imageSize;
	private int padding;
	private boolean firsttime;
	
	public FolderIndexRunnable(boolean firsttime,String ip,String currentFolder, FlowPane flowPane, FileManagerCntrl controller,DataInputStream in,DataOutputStream out) {
		this.currentFolder = currentFolder;
		this.flowPane = flowPane;
		this.controller = controller;
		this.token = this.controller.getToken();
		this.in = in;
		this.ip = ip;
		this.imageSize = this.controller.getimageSize();
		this.padding = this.controller.getPadding();
		this.firsttime = firsttime;
		this.out = out;
	}
	
	@Override
	public void run() {
		/*
		 * Here we must create connection with new Server and receive List of files
		 * 
		 */
		try {
			System.out.println("Sending request to change directory : "+ currentFolder);
			
			if(!firsttime) {
	    		String request = token+" switch_curr_dir_req " + currentFolder;
	    		out.write(request.getBytes());
			}
		
			if (!currentFolder.endsWith("/"))
				currentFolder += "/";
			
			in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			
			int port = Integer.parseInt(serverAnswer);
			client = new Socket(ip, port);
			client.setSoLinger(true, 0);
//			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			
			OutputStream outToServer = client.getOutputStream();
			out = new DataOutputStream(outToServer);
			InputStream inFromServer = client.getInputStream();
			
			in = new DataInputStream(inFromServer);
			//Filename
			bytes = new byte[1024];	
			in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();

			while(!serverAnswer.equals("end")) {
				if(serverAnswer.endsWith("jpg") || serverAnswer.endsWith("png")) { //Image
					VBox vb = new VBox();
					vb.setPadding(new Insets(padding, padding, padding, padding));
				    String shortName = serverAnswer;
				    if (serverAnswer.length() > 10) {
				    	shortName = serverAnswer.substring(0, 10);
				    	shortName += "...";
				    }
				    Text desc = new Text(60,10,shortName);
				    vb.setAccessibleText(serverAnswer);
				    ReplytoServer = new String("ok_name\0");
					out.write(ReplytoServer.getBytes());
					
					// Size of image 
					bytes = new byte[1024];
			        in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					int sizeOfIMage = Integer.parseInt(serverAnswer);
					ReplytoServer = new String("ok_size\0");
					out.write(ReplytoServer.getBytes());
					
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
				    imv.setFitHeight(imageSize);
				    imv.setFitWidth(imageSize);
				    imv.setPreserveRatio(true);
				    desc.setWrappingWidth(imageSize);
				    
//				    
				    vb.getChildren().addAll(imv,desc);
				    controller.imageEffects(flowPane,vb);
				    controller.fadeEffect(500, vb);
				    
				    Platform.runLater(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							flowPane.getChildren().add(vb);
						}
					});
				    ReplytoServer = new String("all_ok\0");
					out.write(ReplytoServer.getBytes());
				    bytes = new byte[1024];
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
				}
				else { // Folder 
					
					VBox vb = new VBox();

					vb.setPadding(new Insets(10, 10, 10, 10));
				    String shortName = serverAnswer;
				    if (serverAnswer.length() > 10) {
				    	shortName = serverAnswer.substring(0, 10);
				    	shortName += "...";
				    }
				    Text desc = new Text(60,10,shortName);
				    vb.setAccessibleText(serverAnswer);
				    
				    ReplytoServer = new String("ok_name\0");
					out.write(ReplytoServer.getBytes());
					
					Image img = new Image(controller.getClass().getResource("../img/folder.png").toExternalForm(),imageSize,imageSize,false,false);
				    ImageView imv = new ImageView(img);
				    desc.setWrappingWidth(imageSize);
				    
				    vb.getChildren().addAll(imv,desc);
				    controller.folderEffects(flowPane,vb);
				    controller.fadeEffect(500, vb);
				    Platform.runLater(new Runnable() {
						@Override
						public void run() {
							flowPane.getChildren().add(0,vb);

						}
					});
				    
				    ReplytoServer = new String("all_ok\0");
					out.write(ReplytoServer.getBytes());
				    bytes = new byte[1024];
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
				}
			}
			if(client.isConnected()) {
				client.close();
			}
			System.out.println("Index Transfer is complete");
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (NumberFormatException e) {
			System.out.println(e.getMessage());
		}
		catch(ArrayIndexOutOfBoundsException e) {
			e. printStackTrace();
		}
		finally {
			out = null;
			in = null;
		}
		return;
	}


}
