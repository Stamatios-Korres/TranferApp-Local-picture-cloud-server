package controllers;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.sun.prism.Image;

import application.Main;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class AuthenticatedController implements Initializable{

	private DataOutputStream out;
	private DataInputStream in;
	private String path;
	private String token;
	
	@FXML
	private Button openBtn;
	@FXML
	private Button logoutBtn;
	
	public AuthenticatedController(DataOutputStream out, String token, DataInputStream in) {
		this.out = out;
		this.in = in;
		this.token = token;
		this.path = "null";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[1024];
		String ip = "192.168.2.3";
        try {
			in.read(bytes);
			String serverAnswer = new String(bytes, StandardCharsets.UTF_8);
			serverAnswer = serverAnswer.trim();
			System.out.println("Server : " + serverAnswer);
			int port = Integer.parseInt(serverAnswer);
			
			System.out.println("Connecting to " + ip + " on port " + port);
			Socket client = new Socket(ip, port);
			out =null;
			in = null;
			
			OutputStream outToServer = client.getOutputStream();
			out = new DataOutputStream(outToServer);
			InputStream inFromServer = client.getInputStream();
			in = new DataInputStream(inFromServer);
			System.out.println(
			"Just connected to " + client.getRemoteSocketAddress());
			int number = 0;
			while(true) {
				in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = new String(serverAnswer.trim());
				if(serverAnswer.equals("end\0")) {
					System.out.println("Ok files");
					break;
				}
				out.writeBytes("ok\0");
			}
			System.out.println("done !");
		 	BufferedImage img =ImageIO.read(ImageIO.createImageInputStream(client.getInputStream()));
		 	SwingFXUtils.toFXImage(img,null);
		 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@FXML
    void logout(ActionEvent event) {
    	try {
    		String request = "terminate_con\0";
	        out.write(request.getBytes());
    		
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass()
		    			.getResource("../fxmls/main.fxml"));
		    Parent root;
			root = fxmlLoader.load();
			Main.changeScene(root);
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	@FXML
    void openFolder(ActionEvent event) {
		try {
			String[] split = path.split("/");
			Runtime.getRuntime().exec("thunar "+ "smb://172.16.125.199/"+split[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
