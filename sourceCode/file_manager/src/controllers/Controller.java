package controllers;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import runnables.SmbRunnable;

public class Controller implements Initializable {

	@FXML
	private BorderPane mainPane;
	@FXML
	private ScrollPane filesPane;
	@FXML
	private Text dirPath;
	@FXML
	private TextField search;
	@FXML
	private MenuButton sortBtn;
	@FXML
	private Button logoutBackBtn;
	
	private FlowPane curflowPane;
	private Stage appStage;
	private boolean multiSelection = false;
	private VBox lastVb = null;
	private List<VBox> selectedVbs = null;
	private final Background focusBackground = new Background( new BackgroundFill(  Color.rgb(65, 107, 175), CornerRadii.EMPTY, Insets.EMPTY ) );
    private final Background unfocusBackground = new Background( new BackgroundFill( Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY ) );
	private Node prevPane;
	private List<Node> imagePanes;
	private List<String> folderPaths;
	private List<Node> tempPanes;
	private List<String> tempPaths;
	private String currentFolder;
	private String mainPath = "smd://192.168.2.6/Share/";
	NtlmPasswordAuthentication auth;
	Stage stage = new Stage();
	private Process proc = null;
	
	
	public Controller(Stage appStage) {
		this.appStage = appStage;
	
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		this.Authenticate(); // authentication for samba user
		this.setGlobalEventHandler(search);  
		
		appStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			filesPane.setFitToWidth(true);
		    //flowPane.setPrefWidth(filesPane.getWidth());
		});
		
		imagePanes = new ArrayList<>();
		folderPaths = new ArrayList<>();
		tempPanes = new ArrayList<>();
		tempPaths = new ArrayList<>();
		
		filesPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		filesPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		filesPane.setContent(curflowPane);
		filesPane.setFocusTraversable(false);
		
		currentFolder = mainPath;
		this.fetchFiles(null);
		
		filesPane.setOnKeyPressed(new  EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			if (event.getCode() == KeyCode.CONTROL) {
				if (!multiSelection) {
					System.out.println("Multiselection ON");
					if (lastVb != null) {
						lastVb.setBackground(unfocusBackground);
						lastVb = null;
					}
					multiSelection = true;
					selectedVbs = new ArrayList<>();
				}
				else {
					System.out.println("Multiselection OFF");
					multiSelection = false;
						if (selectedVbs != null) {
						for (VBox vb : selectedVbs) {
							vb.setBackground(unfocusBackground);
							//TODO release data
						}
						selectedVbs = null;
					}
				}
			}
		}
	});
		
		stage.initOwner(appStage);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                stage.close();
                System.gc();
            }
        });
	}
	

	private void fetchFiles(VBox vbf) {
		FlowPane flowPane = new FlowPane();
		flowPane.setFocusTraversable(false);
		flowPane.setAlignment(Pos.TOP_LEFT);
		flowPane.setPadding(new Insets(15,15,15,15));
		curflowPane = flowPane;
    	imagePanes.add(flowPane);
    	tempPanes.add(flowPane);
    	filesPane.setContent(curflowPane);
    	String folderName =  null;
    	if (vbf == null ) {
    		folderName = "";
    	}
    	else {
    		Text text = (Text)vbf.getChildren().get(1);
    		folderName = text.getText()+"/";
    	}
    	currentFolder = currentFolder + folderName;
    	dirPath.setText(currentFolder);
    	folderPaths.add(currentFolder);
    	tempPaths.add(currentFolder);
    	if (!currentFolder.equals(mainPath))
    			logoutBackBtn.setText("Back");
		
		
    	Thread thread = new Thread(new SmbRunnable(currentFolder, flowPane, Controller.this));
		thread.start();
	}
	
	public void folderEffects(Node parent,VBox vb) {
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem open = new MenuItem("Open");
        MenuItem move = new MenuItem("Move to...");
        MenuItem copy = new MenuItem("Copy to...");
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().addAll(open,copy, move, delete);
            
        vb.setFocusTraversable(false);
		vb.setBackground(unfocusBackground);
        
        vb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	if(mouseEvent.getButton().equals(MouseButton.SECONDARY) && !multiSelection){
            		//if (!vb.isFocused()) {
            			vb.requestFocus();
            			if (lastVb != null && lastVb != vb) {
            				lastVb.setBackground(unfocusBackground);
            				vb.setBackground(focusBackground);
            			}
            			else vb.setBackground(focusBackground);
            			lastVb = vb;
            		//}
            	}
            	else if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
            		if (multiSelection) {
            			//if (!vb.isFocused()) {
            				if (vb.getBackground() == unfocusBackground) {
		            			vb.setBackground(focusBackground);
		            			selectedVbs.add(vb);
		            			System.out.println("Added to list");
            				}
            				else {
            					vb.setBackground(unfocusBackground);
		            			selectedVbs.remove(vb);
		            			System.out.println("Removed from list");
            				}

            				//}
            		}
            		else {
	            		//if (!vb.isFocused()) {
	            			vb.requestFocus();
	            			if (lastVb != null) {
	            				lastVb.setBackground(unfocusBackground);
	            			}
	            			lastVb = vb;
	            			vb.setBackground(focusBackground);
	            		//}
	            		if(mouseEvent.getClickCount() == 2){
	                        Text name = (Text) vb.getChildren().get(1);
	                        int i = 0;
	                        boolean answer = false;
	                        System.out.println("switching directory : " + currentFolder+ name.getText());
	                        for (String folder : folderPaths) {
	                        	String folderUrl = currentFolder+name.getText()+"/";
	                        	if (folder.equals(folderUrl)) {
	                        		tempPanes.add(imagePanes.get(i));
	                        		tempPaths.add(folderUrl);
	                        		curflowPane = (FlowPane)imagePanes.get(i);
	                        		currentFolder = folderUrl;
	                        		dirPath.setText(currentFolder);
	                        		logoutBackBtn.setText("Back");
	                        		fadeEffect(500, imagePanes.get(i));
	                        		//add flowPane to main scrollPane
	                        		filesPane.setContent(imagePanes.get(i));
	                        		answer = true;
	                        		break;
	                        	}
	                        	i++;
	                        }
	                        if (!answer) {
		                        try {
		                        	fetchFiles(vb);
		                        	
		                        } catch(Exception e) {
		                        	e.printStackTrace();
		                        }
	                        }
	                    }
            		}
                }
            }
        });
        
        vb.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
        	if (!multiSelection) {
                delete.setOnAction(new EventHandler<ActionEvent>() {
        			@Override
        			public void handle(ActionEvent event) {
        				vb.setBackground(unfocusBackground);
        				String pathToDelete = null;
        				int i=0;
        				boolean deleted = false;
        				Text filename = (Text)vb.getChildren().get(1);
        				String name = currentFolder+filename.getText();
        				if(deleteFile(name)) {
        					System.out.println("File Deleted");
        					curflowPane.getChildren().remove(vb);
        				}
        			}
        		});
        		move.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        System.out.println("Cut...");
                    }
                });
                copy.setOnAction(new EventHandler<ActionEvent>() {

        			@Override
        			public void handle(ActionEvent arg0) {
        				// TODO Auto-generated method stub
        				Stage stage = new Stage();
        				stage.initOwner(appStage);
        				stage.initModality(Modality.WINDOW_MODAL);
        				DirectoryChooser directoryChooser = new DirectoryChooser();
        		        File selectedDirectory = 
        		                 directoryChooser.showDialog(stage);
        		         
        		        if(selectedDirectory == null){
        		            //labelSelectedDirectory.setText("No Directory selected");
        		        }else{
        		            //labelSelectedDirectory.setText(selectedDirectory.getAbsolutePath());
        		        }
        		        //stage.show();
        			}
        		});
        	}
        	else {
        		System.out.println("Global effect");
        	}
        	contextMenu.show(parent, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        vb.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            contextMenu.hide();
        });
        //if user clicks elsewhere hide menu
        parent.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
        	if (lastVb != null && parent.isFocused()) lastVb.setBackground(unfocusBackground);
            contextMenu.hide();
        });
	}
        		
	public void imageEffects(Node parent,VBox vb) {
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem open = new MenuItem("Open");
        MenuItem move = new MenuItem("Move to...");
        MenuItem copy = new MenuItem("Copy to...");
        MenuItem delete = new MenuItem("Delete");
        contextMenu.getItems().addAll(open,copy, move, delete);
                       
        vb.setFocusTraversable(false);
		vb.setBackground(unfocusBackground);
        
        vb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	if(mouseEvent.getButton().equals(MouseButton.SECONDARY) && !multiSelection){
            		//if (!vb.isFocused()) {
            			vb.requestFocus();
            			if (lastVb != null && lastVb != vb) {
            				lastVb.setBackground(unfocusBackground);
            				vb.setBackground(focusBackground);
            			}
            			else vb.setBackground(focusBackground);
            			lastVb = vb;
            		//}
            	}
            	else if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
            		if (multiSelection) {
            			//if (!vb.isFocused()) {
            				if (vb.getBackground() == unfocusBackground) {
		            			vb.setBackground(focusBackground);
		            			selectedVbs.add(vb);
		            			System.out.println("Added to list");
            				}
            				else {
            					vb.setBackground(unfocusBackground);
		            			selectedVbs.remove(vb);
		            			System.out.println("Removed from list");
            				}

            				//}
            		}
            		else {
	            		//if (!vb.isFocused()) {
	            			vb.requestFocus();
	            			if (lastVb != null) {
	            				lastVb.setBackground(unfocusBackground);
	            			}
	            			lastVb = vb;
	            			vb.setBackground(focusBackground);
	            		//}
	            		if(mouseEvent.getClickCount() == 2){
	                        System.out.println("Double clicked");
	                        String imgName = vb.getAccessibleText();
	                        String url = currentFolder+imgName;
	                        try {
	                        	if (proc != null) {
		                        	proc.destroy();
		                        	proc =  null;
	                        	}
	                        	String[] command = new String[] {"java", "-jar", "/home/paris/Desktop/image-viewer.jar", url};
	                        	ProcessBuilder pb = new ProcessBuilder(command);
	                        	pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
	        					proc = pb.start();
	        					command = null;
								//Runtime.getRuntime().exec("");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    }
            		}
                }
	          }
	    });
        
        vb.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
        	if (!multiSelection) {
                delete.setOnAction(new EventHandler<ActionEvent>() {
        			@Override
        			public void handle(ActionEvent event) {
        				vb.setBackground(unfocusBackground);
        				String pathToDelete = null;
        				int i=0;
        				boolean deleted = false;
        				System.out.println("About to delete: ... ");
        				System.out.println(vb.getAccessibleText());
        				Text filename = (Text)vb.getChildren().get(1);
        				String name = currentFolder+filename.getText();
        				SmbFile f;
						try {
							f = new SmbFile(name,auth);
							f.delete();
							curflowPane.getChildren().remove(vb);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//        				if(f.delete()) {
//        					System.out.println("Deleted");
//        					curflowPane.getChildren().remove(vb);
//        				}
						catch (SmbException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
        		});
        		move.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        System.out.println("Cut...");
                    }
                });
                copy.setOnAction(new EventHandler<ActionEvent>() {

        			@Override
        			public void handle(ActionEvent arg0) {
        				//prin to eixa na anoigei se neo stage paizei na trwge mnhmh
        				DirectoryChooser directoryChooser = new DirectoryChooser();
        		        File selectedDirectory = 
        		                 directoryChooser.showDialog(appStage);
        		         
        		        if(selectedDirectory == null){
        		            //labelSelectedDirectory.setText("No Directory selected");
        		        }else{
        		        	System.out.println(selectedDirectory.getAbsolutePath());
        		            //labelSelectedDirectory.setText(selectedDirectory.getAbsolutePath());
        		        }
        		        //stage.show();
        			}
        		});
        	}
        	else {
        		System.out.println("Global effect");
        	}
        	contextMenu.show(parent, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        vb.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            contextMenu.hide();
        });
        //if user clicks elsewhere hide menu
        parent.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
        	if (lastVb != null && parent.isFocused()) lastVb.setBackground(unfocusBackground);
            contextMenu.hide();
        });
	}
        
	@FXML
	private void showMenu(MouseEvent event) {
		VBox menuBox = new VBox();
		Button btn1 = new Button("Back");
		btn1.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				fadeEffect(500, prevPane);
				mainPane.setLeft(prevPane);
			}
		});
		btn1.getStyleClass().add("my-button");
		btn1.setPrefWidth(179.0);
		Button btn2 = new Button("Option 2");
		btn2.getStyleClass().add("my-button");
		btn2.setPrefWidth(179.0);
		menuBox.getChildren().addAll(btn1,btn2);
		menuBox.setBackground(focusBackground);
		fadeEffect(500, menuBox);
		prevPane = mainPane.getLeft();
		mainPane.setLeft(menuBox);
	}
	
	public void fadeEffect(int duration,Node node) {
		FadeTransition ft = new FadeTransition(Duration.millis(duration), node);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.play();
	}
	
	@FXML
	//handler for back button
	private void prevImagePane() {
		if (!currentFolder.equals(mainPath)) {
			Node prev = null;
			int i = 0;
			for (Node pane : tempPanes) {
				if (pane == curflowPane) {
					currentFolder = tempPaths.get(i-1);
					dirPath.setText(currentFolder);
					tempPaths.remove(i);
					curflowPane = (FlowPane)prev;
					tempPanes.remove(pane);
					pane = null;
					fadeEffect(500, prev);
					filesPane.setContent(prev);
					if (currentFolder.equals(mainPath)) {
						logoutBackBtn.setText("Logout");
					}
					else {
						logoutBackBtn.setText("Back");
					}
//					System.out.println("FlowPanes in list : " + tempPanes.size() + " ,paths : " + tempPaths.size());
//					System.out.println("ALL : " + imagePanes.size() );
					return;
				}
				prev = pane;
				i++;
			}
		}
	}
	
	private void search() {
		System.out.println("Select first image that her name conatains search query!");
	}

	private void setGlobalEventHandler(Node node) {
	    node.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	        if (ev.getCode() == KeyCode.ENTER) {
	        	if (node == search) {
	        		search();
				}
	           
	           ev.consume(); 
	        }
	    });
	}


	public boolean deleteFile(String pathToDelete) {
			boolean retValue = deletePics(pathToDelete);
			if(retValue == true) {
				System.out.println("Photos were deleted");
				File dir = new File(pathToDelete);
				for (File f : dir.listFiles()) {
					if(!deleteFile(f.getAbsolutePath())) {
						retValue = false;
						break;
					}
				}
				if(!dir.delete()) {
					retValue = false;
				}
			}
			return retValue;
		}
	
	public boolean deletePics(String pathToDelete) {
		boolean retValue = true;
		File dir = new File(pathToDelete);
		for (File f : dir.listFiles()) {
			if(!f.isDirectory()) {
				if(!f.delete()) {
					retValue = false;
				}
			}
		}
		return retValue;
	}

	
	private void Authenticate() {
		auth = new NtlmPasswordAuthentication("","paris", "1234");
	}
	
	public NtlmPasswordAuthentication getAuthtentication() {
		return auth;
	}
}
