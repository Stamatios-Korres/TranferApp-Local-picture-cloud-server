package runnables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import controllers.FileManagerCntrl;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class DeleteFileRunnable  implements Runnable {

	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;
	private Socket client;
    private FileManagerCntrl controller;
    private FlowPane flowPane;
	private byte[] bytes = new byte[1024];
	private String serverAnswer;
	private String ip;
	private String token;
	private String name;
	private VBox vb;
    private boolean many;
    private List<VBox> selectedVbs;
    
	public DeleteFileRunnable(boolean many,List<VBox> selectedVbs,VBox vb,String name,DataOutputStream out,DataInputStream in,FileManagerCntrl controller,FlowPane flowPane) {
		// TODO Auto-generated constructor stub
		this.out = out;
		this.in = in;
		this.controller = controller;
		this.flowPane = flowPane;
		this.token = this.controller.getToken();
		this.name = name;
		this.vb = vb;
		this.many = many;
		this.selectedVbs = selectedVbs;
	}
	@Override
	public void run() {
		try {
			if(!many) {
				String requestToServer = token + " delete_file_req " + " " + name+"\0";
				System.out.println(requestToServer);
				out.write(requestToServer.getBytes());

				in.read(bytes);
				
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				int port = Integer.parseInt(serverAnswer);
//				System.out.println("ServerAnswer: "+serverAnswer);
				client = new Socket(ip, port);
//				System.out.println("Just connected to " + client.getRemoteSocketAddress());
				OutputStream outToServer = client.getOutputStream();
				out = new DataOutputStream(outToServer);
				InputStream inFromServer = client.getInputStream();
				in = new DataInputStream(inFromServer);
				
				bytes = new byte[1024];
				in.read(bytes);
				String serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				if(serverAnswer.equals("delete_ok")) {
				        Platform.runLater(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								flowPane.getChildren().remove(vb);
								controller.resetImagests();
							}
						});
				}
				client.close();
				System.out.println("Delete is done");
			}
			else {
				String requestToServer = token + " delete_file_req " + "list_of_files"+"\0";
				out.write(requestToServer.getBytes());
				
				in.read(bytes);
				serverAnswer = new String(bytes, StandardCharsets.UTF_8);
				serverAnswer = serverAnswer.trim();
				int port = Integer.parseInt(serverAnswer);
				client = new Socket(ip, port);
//				System.out.println("Just connected to " + client.getRemoteSocketAddress());
				OutputStream outToServer = client.getOutputStream();
				out = new DataOutputStream(outToServer);
				InputStream inFromServer = client.getInputStream();
				in = new DataInputStream(inFromServer);
				boolean problem = false;
				
				List<VBox> all = new  ArrayList<>();
				for(VBox toBeRemoved: selectedVbs) {
					String filename = name +  toBeRemoved.getAccessibleText();
					filename = filename+'\0';
					out.write(filename.getBytes());
					in.read(bytes);
					serverAnswer = new String(bytes, StandardCharsets.UTF_8);
					serverAnswer = serverAnswer.trim();
					if(!serverAnswer.equals("delete_ok")) {
						problem = true;
						break;
					}
					else {
				        Platform.runLater(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								flowPane.getChildren().remove(toBeRemoved);
								controller.resetImagests();
							}
						});
					}
					all.add(toBeRemoved);
					
				}
				if(!problem) {
					selectedVbs.removeAll(all);
					String filename = new String("end\0");
					out.write(filename.getBytes());
				}
				System.out.println("Multiple delete is done");
				client.close();
			}
			// TODO Auto-generated method stub
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
;