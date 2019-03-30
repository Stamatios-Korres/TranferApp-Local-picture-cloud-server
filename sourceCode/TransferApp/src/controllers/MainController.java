package controllers;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;


import application.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import runnables.FileTransferRunnable;

//import file_manager;

public class MainController implements Initializable{

	private String ip;
	private static Socket client=null;
	private DataOutputStream out;
	private DataInputStream in;
	private static Stage appStage;
	private int port = 8081;
	
	@FXML
	private TextField loginUsername;
	@FXML
	private PasswordField loginPassword;
	@FXML
	private Button loginBtn;
	@FXML
    private TextField registerUsername;
    @FXML
    private PasswordField registerPassword1;
    @FXML
    private PasswordField registerPassword2;
    @FXML
    private Button registerBtn;
    @FXML
    private Text registerInfo;
    @FXML
    private Text loginInfo;
	
	public MainController() {
        try {
           client = new Socket("192.168.122.1", port);
           OutputStream outToServer = client.getOutputStream();
           out = new DataOutputStream(outToServer);
           InputStream inFromServer = client.getInputStream();
           in = new DataInputStream(inFromServer);
           System.out.println("Just connected to " + client.getRemoteSocketAddress());
           
        } catch (IOException e) {
           e.printStackTrace();
        }
	}
	
	public static void setStage(Stage stage) throws IOException {
		appStage = stage;
		appStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
             @Override
             public void handle(WindowEvent t) {
					try {
						if(client!=null) {
							if(client.isConnected())
								client.close();
						}
						System.out.println("The programmis officially closed");
	             		Platform.exit();
	             		System.exit(0);
					} catch (IOException e) {
						e.printStackTrace();
					}
             }
         });
	}
	
	private void setGlobalEventHandler(Node node) {
	    node.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	        if (ev.getCode() == KeyCode.ENTER) {
	        	if (node == loginUsername || node == loginPassword
	        			|| node == loginBtn) {
	        		loginBtn.fire();
				}
	        	else if (node == registerUsername || node == registerPassword1
	        			|| node == registerPassword2 || node == registerBtn) {
					registerBtn.fire();
				}
	           
	           ev.consume(); 
	        }
	    });
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setGlobalEventHandler(loginUsername);
		setGlobalEventHandler(loginPassword);
		setGlobalEventHandler(loginBtn);
		setGlobalEventHandler(registerUsername);
		setGlobalEventHandler(registerPassword1);
		setGlobalEventHandler(registerPassword2);
		setGlobalEventHandler(registerBtn);
	}
	
	@FXML
	void login(ActionEvent event) {
		if (loginInfo.isVisible())
    		loginInfo.setVisible(false);
		
        try {
        	String username = loginUsername.getText();
        	String password = loginPassword.getText();
        	if (username.equals("") || password.equals("")) {
        		loginInfo.setText("Error: empty fields!");
        		loginInfo.setFill(Color.RED);
        		loginInfo.setVisible(true);
        		return;
        	}
        	String message = "login_req "+username+" "+password+"\0";
            out.write(message.getBytes());
            //from C++ sockets read bytes
            byte[] buf = new byte[1024];
            in.read(buf);
            String serverAnswer = new String(buf, StandardCharsets.UTF_8);
            serverAnswer = serverAnswer.trim();
            System.out.println("Server says " + serverAnswer);
            
            if (serverAnswer.contains("Connection refused")) {
            	loginInfo.setText("Error: wrong username or password!");
        		loginInfo.setFill(Color.RED);
        		loginInfo.setVisible(true);
            }
            else if(serverAnswer.contains("Token:")){
            	String token = serverAnswer;
            	String replyToServer = new String("ok\0");
            	out.write(replyToServer.getBytes());
            	buf = new byte[1024];
                in.read(buf);
                serverAnswer = new String(buf, StandardCharsets.UTF_8);
                serverAnswer = serverAnswer.trim();
                System.out.println("Server says " + serverAnswer);
				int port_for_pics = Integer.parseInt(serverAnswer);
	            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
	        			.getResource("../fxmls/file_manager.fxml"));
	            FileManagerCntrl controller = new FileManagerCntrl(port_for_pics,client,ip,out,token,in,appStage);
	            fxmlLoader.setController(controller);
	            Parent root = fxmlLoader.load();
	        	Main.changeScene(root);
	        	
            }
        } catch (IOException e) {
           e.printStackTrace();
        }
       
	}
	
	@FXML
    void register(ActionEvent event) {
		if (registerInfo.isVisible())
    		registerInfo.setVisible(false);
		
		String username = registerUsername.getText();
    	String password1 = registerPassword1.getText();
    	String password2 = registerPassword2.getText();
    	if (username.equals("") || password1.equals("") || password2.equals("")) {
    		registerInfo.setText("Error: empty fields!");
    		registerInfo.setFill(Color.RED);
    		registerInfo.setVisible(true);
    		//registerUsername.requestFocus();
    		return;
    	}
    	if (!password1.equals(password2)) {
    		registerInfo.setText("Error: not matching passwords!");
    		registerInfo.setFill(Color.RED);
    		registerInfo.setVisible(true);
    		return;
    	}
    	try {
    	String message = "register_req "+username+" "+password1+"\0";
        out.write(message.getBytes());
        //from C++ sockets read bytes
        byte[] buf = new byte[1024];
        in.read(buf);
        String serverAnswer = new String(buf, StandardCharsets.UTF_8);
        serverAnswer = serverAnswer.trim();
        System.out.println("Server says " + serverAnswer);
        if(serverAnswer.contains("Token:")){
        	String token = serverAnswer;
        	String replyToServer = new String("ok\0");
        	out.write(replyToServer.getBytes());
        	buf = new byte[1024];
            in.read(buf);
            serverAnswer = new String(buf, StandardCharsets.UTF_8);
            serverAnswer = serverAnswer.trim();
            System.out.println("Server says " + serverAnswer);
			int port_for_pics = Integer.parseInt(serverAnswer);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
        			.getResource("../fxmls/file_manager.fxml"));
            FileManagerCntrl controller = new FileManagerCntrl(port_for_pics,client,ip,out,token,in,appStage);
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
        	Main.changeScene(root);
        	
        }
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
	
	@FXML
    void sendFile(ActionEvent event) {
		try {
			byte[] bytes = new byte[1024];
			//for C++ sockets add '\0' at the end of string
			String request = "transfer_req\0";
	        out.write(request.getBytes());
	        System.out.println("Sent : " + request);
	        
	        bytes = new byte[1024];
	        in.read(bytes);
			String serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			System.out.println("Server says " + serverAnswer);
			
			if (!serverAnswer.equals("error")) {
				int port = Integer.parseInt(serverAnswer);
				System.out.println("port : " + port);
				Thread transferThread = new Thread(new FileTransferRunnable(this.ip,port));
				transferThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
