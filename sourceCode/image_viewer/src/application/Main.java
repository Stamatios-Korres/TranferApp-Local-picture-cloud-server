package application;
	
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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	private static String url;
	private static String pic_url;
	private static String ip;
	private static String pic_name;
	private static int port;
	private static String token;
	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;	
    private String serverAnswer;
    private String replyToServer;
	private byte[] bytes;
	private Socket client;
	private String nextImage = null;
	private String prevImage = null;
	public BorderPane root ;
	public Stage appStage = new Stage();
	
	
	public void setGlobalEventHandler(Scene scene){
		scene.setOnKeyPressed(new  EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				boolean changed = false;
				if(event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT ){
					if (event.getCode() == KeyCode.RIGHT) {	
						if(nextImage !=null) {
							changed = true;
							pic_url = url+nextImage;
							pic_name = nextImage;
						}
					}
					else if(event.getCode()==KeyCode.LEFT) {
						if(prevImage!=null) {
							changed = true;

							pic_url = url+prevImage;
							pic_name = prevImage;
						}
					}
					if(changed) {
						nextImage = prevImage = null;
						appStage.setTitle(pic_name );
						request_image(scene);
					}
				}
				else if(event.getCode() == KeyCode.ESCAPE) {
					try {
						exit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void request_image(Scene scene) {
		try {
			replyToServer = pic_url+"\0";
			out.write(replyToServer.getBytes());
	
			
			bytes = new byte[1024];
	        in.read(bytes);
			serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			
			if(serverAnswer.equals("pic_url_ok")) {
	
					replyToServer = new String(pic_name+"\0");
					out.write(replyToServer.getBytes());
					
					bytes = new byte[1024];
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					
					if(serverAnswer.equals("pic_name_ok")) {
						
						bytes = new byte[1024];
						in.read(bytes);
						serverAnswer = new String(bytes, StandardCharsets.UTF_8);
						serverAnswer = serverAnswer.trim();
						
						if(!serverAnswer.equals("No_prev_available"))
							prevImage = serverAnswer;
						
						replyToServer = new String("Ok_first\0");
						out.write(replyToServer.getBytes());
						
						bytes = new byte[1024];
						in.read(bytes);
						serverAnswer = new String(bytes, StandardCharsets.UTF_8);
						serverAnswer = serverAnswer.trim();
						
						if(!serverAnswer.equals("No_next_available"))
							nextImage = serverAnswer;
						
						replyToServer = new String("Ok_second\0");
						out.write(replyToServer.getBytes());

						bytes = new byte[1024];
						in.read(bytes);
						serverAnswer = new String(bytes, StandardCharsets.UTF_8);
						serverAnswer = serverAnswer.trim();
						int sizeOfIMage = Integer.parseInt(serverAnswer);
						
						replyToServer = new String("ok_size\0");
						out.write(replyToServer.getBytes());
						
						byte[] imageBytes = new byte[sizeOfIMage];
						int currentsize =0;
						int readBytes = 0 ;
						while(currentsize<sizeOfIMage) {
							readBytes =in.read(bytes);
							System.arraycopy(bytes, 0, imageBytes, currentsize,readBytes);
							currentsize+=readBytes;
							bytes = new byte[1024];
						}
				        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
				        Image img = SwingFXUtils.toFXImage(image, null);
				        ImageView imgv = new ImageView(img);
						imgv.setFitHeight(1000);
						imgv.setFitWidth(1000);
						imgv.setPreserveRatio(true);
						root.setCenter(imgv);
					}
					appStage.setScene(scene);
					appStage.show();
			}
		}catch(IOException e) {
			
		}
	}
	
	public void exit() throws IOException {
		if(client.isConnected())
			client.close();
		appStage.close();        	
        System.gc();
	}
	@Override
	
public void start(Stage primaryStage) { 

		try {
			appStage = primaryStage;
			appStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	             @Override
	             public void handle(WindowEvent t) {
						try {
							if(client.isConnected())
								client.close();
		             		Platform.exit();
		             		System.exit(0);
						} catch (IOException e) {
							e.printStackTrace();
						}
	             }
			});
			root = new BorderPane();
			Scene scene = new Scene(root,1000,1000);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			this.setGlobalEventHandler(scene);
			client = new Socket(ip,port);
			System.out.println("Connected to: "+ client.getRemoteSocketAddress());
			
			OutputStream outToServer = client.getOutputStream();
			out = new DataOutputStream(outToServer);
			InputStream inFromServer = client.getInputStream();
			in = new DataInputStream(inFromServer);
			appStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		        @Override
		        public void handle(WindowEvent t) {
		        	try {
		        		if(client.isConnected())
							client.close();
		        		appStage.close();
		        		System.gc();
		        	} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		    });
			appStage.setTitle(pic_name);
			request_image(scene);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//TODO: Token is not used;
		token = args[0];
		url = args[1];
		pic_url = args[2];
		ip = args[3];
		port = Integer.parseInt(args[4]);
		pic_name = args[5];
		launch(args);
	}
}
