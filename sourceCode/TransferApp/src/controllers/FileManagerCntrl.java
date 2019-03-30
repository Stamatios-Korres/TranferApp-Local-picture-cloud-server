package controllers;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import application.Main;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import runnables.AddFolderRunnable;
import runnables.DeleteFileRunnable;
import runnables.DownloadRunnable;
import runnables.FileRenameRunnable;
import runnables.FolderIndexRunnable;
import runnables.InternalFileTransferRunnable;
import runnables.UploadRunnable;

public class FileManagerCntrl implements Initializable {

	@FXML
	private BorderPane mainPane;
	@FXML
	private ScrollPane filesPane;
	@FXML
	private TextField dirPath;
	private JFXButton logoutBackBtn = new JFXButton("Logout");	
	private FlowPane curflowPane;
	private Stage appStage;
	private final Background focusBackground = new Background( new BackgroundFill(  Color.rgb(65, 107, 175), CornerRadii.EMPTY, Insets.EMPTY ) );
    private final Background unfocusBackground = new Background( new BackgroundFill( Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY ) );
    private JFXTextField search = new JFXTextField();
	private Node prevPane;
	private List<Node> imagePanes;
	private List<String> folderPaths;
	private List<Node> tempPanes;
	private List<String> tempPaths;
	private String currentFolder;
	private String mainPath = "/";
	@FXML
	private VBox optionsBox; 
	Stage stage = new Stage();
	private Process proc = null;
	private String token = null;
	private DataOutputStream out; // Data taken from the previous application 
    private DataInputStream in;
    private static String ip;
    private int port_for_pics;
    private boolean init = true; // used to fetch files the first time
    private int imageSize = 60;
    private int optionSize = 100;
    private int padding = 10;
    private int totalPadding = 15;
    private int firstTime = 1;
    private int curPos = 0;
    private int prevPos = 0;
    private Socket client;
	final ContextMenu menuOption = new ContextMenu();
    private MenuItem create = new MenuItem("Create Folder");
    private MenuItem paste  = new MenuItem("Paste");
    Text popText = new Text();
    PopOver popOver = new PopOver(popText);
    private boolean multiSelection = false;
	private VBox lastVb = null;
	private List<VBox> selectedVbs = new ArrayList<>();
	private List<VBox> copyVbs = null;
	private List<VBox> moveVbs = null;
	private String takenFolder = null;
	private FlowPane takenFlowPane = null;
	
	public FileManagerCntrl(int port_for_pics,Socket client,String ip,DataOutputStream out, String token, DataInputStream in, Stage appStage) {
		this.client = client;
		this.out = out;
		this.in = in;
		this.token = token;
		this.appStage = appStage;
		this.setIp(ip);
		this.port_for_pics = port_for_pics;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		 search.setPromptText("Search...");
		 search.setAlignment(Pos.CENTER);
		 search.setPrefWidth(optionSize);
		 search.setBackground(new Background(new BackgroundFill(Color.WHITE,
				 CornerRadii.EMPTY, Insets.EMPTY)));
		 
		 JFXButton refreshBtn = new JFXButton();
		 String btnName = "Refresh";
		 refreshBtn.setAlignment(Pos.CENTER_LEFT);
		 refreshBtn.setText(btnName);
		 refreshBtn.setPrefWidth(optionSize+50);
		 refreshBtn.setGraphic(new ImageView(
				 new Image("/img/autorenew.png")));
		 refreshBtn.setGraphicTextGap(10);
		 
		 JFXButton downloadBtn = new JFXButton();
		 btnName = "Download";
		 downloadBtn.setText(btnName);
		 downloadBtn.setAlignment(Pos.CENTER_LEFT);
		 downloadBtn.setPrefWidth(optionSize+50);
		 downloadBtn.setGraphic(new ImageView(
				 new Image("/img/arrow-down-bold-circle-outline.png")));
		 downloadBtn.setGraphicTextGap(10);
		 downloadBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(selectedVbs!=null) {
					if(selectedVbs.size()==0)
						alert("No selected items");
					else {
						DirectoryChooser directoryChooser = new DirectoryChooser();
				        File selectedDirectory = 
				                 directoryChooser.showDialog(appStage);
				        if(selectedDirectory == null)
				        	alert("No Directory selected");
				        else{
				        	System.out.println(selectedDirectory.getAbsolutePath());
				        	Thread thread = new Thread(new DownloadRunnable(FileManagerCntrl.this,currentFolder,selectedVbs,selectedDirectory.getAbsolutePath()));
		    				thread.start();
				            //labelSelectedDirectory.setText(selectedDirectory.getAbsolutePath());
				        }			
					}
				}
				else
					alert("No selected items");
				
	        }
		});		
		 
		 JFXButton uploadBtn = new JFXButton();
		 btnName = "Upload";
		 uploadBtn.setText(btnName);
		 uploadBtn.setAlignment(Pos.CENTER_LEFT);
		 uploadBtn.setPrefWidth(optionSize+50);
		 ImageView uploadPic = 	new ImageView(new Image("/img/uploadButton.png",30,30,false,false));
		 uploadBtn.setGraphic(uploadPic);
		 uploadBtn.setGraphicTextGap(10);
		 uploadBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					FileChooser fileChooser = new FileChooser();
		                // Set extension filter
					fileChooser.setTitle("Select a File to Upload");
					FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
		            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
		            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
			        File selectedDirectory = fileChooser.showOpenDialog(appStage);
			        if(selectedDirectory == null)
			        	alert("No Directory selected");
			        else{
			        	System.out.println(selectedDirectory.getName());
			        	System.out.println(selectedDirectory.getAbsolutePath());
			        	Thread thread = new Thread(new UploadRunnable(curflowPane,FileManagerCntrl.this,currentFolder,selectedDirectory.getName(),selectedDirectory));
	    				thread.start();
				            //labelSelectedDirectory.setText(selectedDirectory.getAbsolutePath());
				        }	
				}
			});
		 
		 logoutBackBtn.setPrefWidth(optionSize+50);
		 logoutBackBtn.setAlignment(Pos.CENTER_LEFT);
		 logoutBackBtn.setGraphic(new ImageView(
				 new Image("/img/arrow-left-bold-circle.png")));
		 logoutBackBtn.setGraphicTextGap(10);
		 logoutBackBtn.setOnAction(new EventHandler<ActionEvent>() {
	
			@Override
			public void handle(ActionEvent event) {
				prevImagePane();
			}
		});
		 optionsBox.getChildren().addAll(logoutBackBtn,search,
				refreshBtn,downloadBtn,uploadBtn);
		 this.setGlobalEventHandler(filesPane);
		 this.setGlobalEventHandler(search); 
	
		
		 appStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			 filesPane.setFitToWidth(true);
		 });
		
		 imagePanes = new ArrayList<>();
		 folderPaths = new ArrayList<>();
		 tempPanes = new ArrayList<>();
		 tempPaths = new ArrayList<>();
		
		 filesPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		 filesPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
	     filesPane.setContent(curflowPane);
		 filesPane.setFocusTraversable(false);
		 filesPane.setOnDragOver(event -> {
			if (event.getGestureSource() != filesPane && event.getDragboard().hasString()) {
				/* allow for both copying and moving, whatever user chooses */
				event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			event.consume();
		 });
		currentFolder = mainPath;
		
	    menuOption.getItems().addAll(create,paste);
	    
		this.fetchFiles(null);
		
		filesPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent mouseEvent) {
	        	menuOption.hide();
	        	if(selectedVbs !=null) {
	        		for(VBox previous: selectedVbs) {
	        			previous.setBackground(unfocusBackground);
	        		}
	        		selectedVbs = new ArrayList<>();
	        	}
	    		curPos = prevPos = 0;
	    		firstTime = 1;
	    		
	        	if(mouseEvent.getButton().equals(MouseButton.SECONDARY)){
	        			menuOption.show(filesPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
	        	}
			}
		});
		
		filesPane.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            create.setOnAction(new EventHandler<ActionEvent>() {
    			@Override
    			public void handle(ActionEvent event) {
    				Thread thread = new Thread(new AddFolderRunnable(getIp(),currentFolder,curflowPane,FileManagerCntrl.this,in, out));
    				thread.start();
    			}
			});
            if(copyVbs == null && moveVbs == null) 
            	paste.setDisable(true);
            else
            	paste.setDisable(false);
    		paste.setOnAction(new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent event) {
	    				Thread thread =null ;
	    				if(moveVbs!=null)
	    					thread = new Thread(new InternalFileTransferRunnable("Move",moveVbs,takenFlowPane,takenFolder,FileManagerCntrl.this, curflowPane,currentFolder));
	    				else if(copyVbs!=null)
	    					thread = new Thread(new InternalFileTransferRunnable("Copy",copyVbs,takenFlowPane,takenFolder,FileManagerCntrl.this, curflowPane,currentFolder));
	    				if(thread !=null)
	    					thread.start();
	    				moveVbs = copyVbs = null;
	    			}
				});
	    });
		final KeyCombination keyCombinationShiftC = new KeyCodeCombination(
				KeyCode.C, KeyCombination.CONTROL_DOWN);
		final KeyCombination keyCombinationShiftD = new KeyCodeCombination(
				KeyCode.D ,KeyCombination.CONTROL_DOWN);
		final KeyCombination keyCombinationShiftA = new KeyCodeCombination(
				KeyCode.A, KeyCombination.CONTROL_DOWN);
		filesPane.setOnKeyReleased(new  EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.CONTROL)
					multiSelection = false;
			}
		});
		filesPane.setOnKeyPressed(new  EventHandler<KeyEvent>() {
		
			@Override
			public void handle(KeyEvent event) {
				if (keyCombinationShiftC.match(event)) {
				       System.out.println("CTRL + C Pressed");
				}
				else if (keyCombinationShiftD.match(event)) {
				       System.out.println("CTRL + D Pressed");
				}
				else if (keyCombinationShiftA.match(event)) {
					for(Node vbn: curflowPane.getChildren()) {
						VBox vb = (VBox)vbn;
						vb.setBackground(focusBackground);
            			selectedVbs.add(vb);
					}
				       System.out.println("CTRL + A Pressed");
				}
				if (event.getCode() == KeyCode.CONTROL) 
					multiSelection = true;
				
				else if(event.getCode() == KeyCode.DELETE) {
						if(curflowPane.getChildren().size()>0) {
							System.out.println(selectedVbs.size());
							if(selectedVbs.size()>1) {
								System.out.println("Multiple targets");
								Thread thread = new Thread(new DeleteFileRunnable(true,selectedVbs,null,currentFolder,out, in, FileManagerCntrl.this, curflowPane));
		        				thread.start();
		        				selectedVbs  = new ArrayList<>();
							}
							else if(selectedVbs.size()==1) {
								System.out.println("Single Target");
								VBox current = (VBox)curflowPane.getChildren().get(curPos);
								current.setBackground(unfocusBackground);
		        				String name = currentFolder + current.getAccessibleText();
		        				if(!name.endsWith("jpg") && !name.endsWith("png"))
		        						name+="/";
		        				Thread thread = new Thread(new DeleteFileRunnable(false,selectedVbs,current,name,out, in, FileManagerCntrl.this, curflowPane));
		        				thread.start();
		        				selectedVbs  = new ArrayList<>();
							}
							else {
								System.out.println("wtf?");
							}
						}
				}
				else if(event.getCode() == KeyCode.ENTER) {
					if(firstTime == 0) {
						if(curflowPane.getChildren().size()>0) {
							if(!multiSelection) {
								VBox current = (VBox)curflowPane.getChildren().get(curPos);
								if(current.getAccessibleRoleDescription().equals("folder")) 
									openFolder(current);
								else
									openImage(current);
							}
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
		Platform.runLater(new Runnable() {
        @Override
        public void run() {
            filesPane.requestFocus();
            multiSelection = false;
        }
    });
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public boolean isMultiselectionOn() {
		return multiSelection;
	}
	
	public FlowPane getFlowPane() {
		return curflowPane;
		
	}
	
	public  List<VBox> getSelectedItems(){
		return selectedVbs;
	}
	
	public int getimageSize() {
		return imageSize;
	}
	
	public int getPadding() {
		return padding;
	}
	
	public String getToken() {
		return token;
	}
	
	public void resetImagests() {
		firstTime = 1;
	    curPos = 0;
	    prevPos = 0;
	}
	
	public DataOutputStream getOut() {
		return out;
	}

	public void setOut(DataOutputStream out) {
		this.out = out;
	}
	
	public DataInputStream getIn() {
		return in;
	}
	
	public void setIn(DataInputStream in) {
		this.in = in;
	}
	
	@FXML
	void logout() {
    	try {
    		System.out.println("Terminating connection");
    		String request = token + " terminate_con\0";
	        out.write(request.getBytes());
	        if(client.isConnected()) {
	        	System.out.println("closed connection");
	        	client.close();
	        }
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass()
		    			.getResource("../fxmls/main.fxml"));
		    Parent root;
			root = fxmlLoader.load();
			Main.changeScene(root);
    	} catch (IOException e) {
			e.printStackTrace();
			client = null;
			out = null;
			in = null;
					
		}
    }
	
	public void folderEffects(Node parent,VBox vb) {
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem open = new MenuItem("Open");
		MenuItem move = new MenuItem("Move");
		MenuItem copy = new MenuItem("Copy");
        MenuItem delete = new MenuItem("Delete");
        MenuItem rename = new MenuItem("Rename");
        MenuItem paste = new MenuItem("Paste into Directory");
        
        contextMenu.getItems().addAll(open,copy, paste,move,rename, delete);
        vb.setFocusTraversable(false);
		vb.setBackground(unfocusBackground);
		vb.setAccessibleRoleDescription("folder");
		
		vb.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {

	    		copy.setOnAction(new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent event) {
	    				copyVbs = new ArrayList<>(selectedVbs);
	    				moveVbs = null;
	    				takenFolder = currentFolder;
	    				takenFlowPane = curflowPane;

	    			}
				});
	    		
	    		rename.setOnAction(new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent event) {
	    				if(selectedVbs.size()==1) {
		                    System.out.println("Renaming . ..");
		    				Thread thread = new Thread(new FileRenameRunnable(vb,currentFolder,out, in, FileManagerCntrl.this, curflowPane));
		    				thread.start();
	    				}
	                }
				});
	    		
	    		move.setOnAction(new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent event) {
	    				moveVbs = new ArrayList<>(selectedVbs);
	    				copyVbs = null;
	    				takenFolder  = currentFolder;
	    				takenFlowPane = curflowPane;
		   			}
				});
	    		
	    		if(copyVbs == null && moveVbs == null) 
	    			paste.setDisable(true);
		        else
		         	paste.setDisable(false);
	    		
				paste.setOnAction(new EventHandler<ActionEvent>() {
		 			@Override
		 			public void handle(ActionEvent event) {
		 				pasteOnFolder(vb);
		 			}
				});
				
				delete.setOnAction(new EventHandler<ActionEvent>() {
	    			@Override
	    			public void handle(ActionEvent event) {
	    				if(multiSelection) {
	    					vb.setBackground(unfocusBackground);
	         				Thread thread = new Thread(new DeleteFileRunnable(multiSelection,selectedVbs,vb,currentFolder,out, in, FileManagerCntrl.this, curflowPane));
	         				thread.start();
	    				}
	    				else {
	    					vb.setBackground(unfocusBackground);
	        				String name = currentFolder + vb.getAccessibleText();
	        				Thread thread = new Thread(new DeleteFileRunnable(multiSelection,selectedVbs,vb,name,out, in, FileManagerCntrl.this, curflowPane));
	        				thread.start();	
	    				}
	    			}
				});
				
	        	
	    		contextMenu.getOwnerNode();
	        
	    		contextMenu.show(parent, event.getScreenX(), event.getScreenY());
	        	
	            event.consume();
	        });
		
        vb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	menuOption.hide();
            	mouseEvent.consume();

            	if(!multiSelection) {
            		reset_Selected();
	            	ObservableList<Node> tim = curflowPane.getChildren();
	    			int box = 0;
	    			if (lastVb != null && lastVb != vb) 
        				lastVb.setBackground(unfocusBackground);
	    			for(Node vbc :  tim ) {
	    				vbc = (VBox)vbc;
	    				if(vbc == vb) {
	    					firstTime = 0;
	    			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	    			        previous.setBackground(unfocusBackground);
	    					prevPos = curPos = box;
	    					VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	    			        current.setBackground(focusBackground);
	    			        lastVb = current;
	    			        selectedVbs = new ArrayList<>();
	    			        selectedVbs.add(current);
	    					break;
	    				}
	    				box++;
	    			}
	    			if(mouseEvent.getClickCount() == 2)
            			openFolder(vb);
	    			
            	}
            	else {
            		if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
	            		if (vb.getBackground() == unfocusBackground) {
	            			vb.setBackground(focusBackground);
	            			selectedVbs.add(vb);
	    				}
	    				else {
	    					vb.setBackground(unfocusBackground);
	            			selectedVbs.remove(vb);
	    				}
	        			popText.setText(vb.getAccessibleText());
	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
	                    popOver.show(vb);
	            	
	        		}
            		else {
            			if (vb.getBackground() == unfocusBackground) {
	            			vb.setBackground(focusBackground);
	            			selectedVbs.add(vb);
	    				}
            		}
	    		}
            }

        });
     
        
      
        vb.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            contextMenu.hide();
        });
        //if user clicks elsewhere hide menu
        parent.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
        	if (lastVb != null && parent.isFocused()) lastVb.setBackground(unfocusBackground);
            contextMenu.hide();
        });
        
        vb.setOnDragEntered(event -> {
        	            /* the drag-and-drop gesture entered the target */
        	            /* show to the user that it is an actual gesture target */
        	            if (event.getGestureSource() != vb && event.getDragboard().hasString()) {
        	                vb.setBackground(focusBackground);
        	            }
        	            event.consume();
        	        });
        	        
        vb.setOnDragExited(event -> {
            /* mouse moved away, remove the graphical cues */
            vb.setBackground(unfocusBackground);

            event.consume();
        });
        
        vb.setOnDragDropped(event -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
            	System.out.println("Item was droped on me: "+vb.getAccessibleText());
                //do what you want
            	pasteOnFolder(vb);
                success = true;
            }
            /*
             * let the source know whether the string was successfully
             * transferred and used
             */
            System.out.println("Success is: "+success);
            event.setDropCompleted(success);

            event.consume();
        });
        
        vb.setOnDragDetected(event -> {
            /* drag was detected, start drag-and-drop gesture */
            /* allow any transfer mode */
            Dragboard db = vb.startDragAndDrop(TransferMode.MOVE);

            /* put a string on dragboard */
            ClipboardContent content = new ClipboardContent();
            Text imageName = (Text) vb.getChildren().get(1);
            ImageView imageView = (ImageView) vb.getChildren().get(0);
            content.putString(imageName.getText());
            content.putImage(imageView.getImage());
            db.setContent(content);
            moveVbs = new ArrayList<>();
            moveVbs.add(vb);
			copyVbs = null;
			takenFolder  = currentFolder;
			takenFlowPane = curflowPane;
			System.out.println("Folder data was initialized");
            event.consume();
        });
        
        vb.setOnDragDone(event -> {
            /* the drag-and-drop gesture ended */
            /* if the data was successfully moved, clear it */
            if (event.getTransferMode() == TransferMode.MOVE) {
                //do what you want
            	System.out.println("Droped");
            }

            event.consume();
        });
	}

	public void imageEffects(Node parent,VBox vb) {
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem open = new MenuItem("Open");
		MenuItem move = new MenuItem("Move");
		MenuItem copy = new MenuItem("Copy");
        MenuItem delete = new MenuItem("Delete");
        MenuItem rename = new MenuItem("Rename");
        
        contextMenu.getItems().addAll(open,copy, move,rename, delete);
        
        vb.setFocusTraversable(false);
		vb.setBackground(unfocusBackground);
		vb.setAccessibleRoleDescription("picture");

        
        vb.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	menuOption.hide();
            	mouseEvent.consume();

            	if(!multiSelection) {
            		reset_Selected();
	            	ObservableList<Node> tim = curflowPane.getChildren();
	    			int box = 0;
	    			if (lastVb != null && lastVb != vb) 
        				lastVb.setBackground(unfocusBackground);
	    			for(Node vbc :  tim ) {
	    				vbc = (VBox)vbc;
	    				if(vbc == vb) {
	    					firstTime = 0;
	    			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	    			        previous.setBackground(unfocusBackground);
	    					prevPos = curPos = box;
	    					VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	    			        current.setBackground(focusBackground);
	    			        lastVb = current;
	    			        selectedVbs = new ArrayList<>();
	    			        selectedVbs.add(current);
	    					break;
	    				}
	    				box++;
	    			}
	    			if(mouseEvent.getClickCount() == 2)
            			openImage(vb);
	    			popOver = new PopOver(popText);
	    			popText.setText(vb.getAccessibleText());
                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
                    popOver.show(vb);
            	}
            	else {
            		if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
	            		if (vb.getBackground() == unfocusBackground) {
	            			vb.setBackground(focusBackground);
	            			System.out.println("Added to list");
	            			selectedVbs.add(vb);
	    				}
	    				else {
	    					vb.setBackground(unfocusBackground);
	            			selectedVbs.remove(vb);
	    				}
	        		}
            		else {
            			if (vb.getBackground() == unfocusBackground) {
	            			vb.setBackground(focusBackground);
	            			selectedVbs.add(vb);
	    				}
            		}
	    		}
            }
	    });
      
        vb.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	popOver.hide();
            };
        });
        
        vb.setOnDragDetected(event -> {
	            /* drag was detected, start drag-and-drop gesture */
            /* allow any transfer mode */
	            Dragboard db = vb.startDragAndDrop(TransferMode.MOVE);

            /* put a string on dragboard */
	        
            ClipboardContent content = new ClipboardContent();
            Text imageName = (Text) vb.getChildren().get(1);
	            ImageView imageView = (ImageView) vb.getChildren().get(0);
	            content.putString(imageName.getText());
	            content.putImage(imageView.getImage());
	            db.setContent(content);
	            moveVbs = new ArrayList<>();
	            moveVbs.add(vb);
				copyVbs = null;
				takenFolder  = currentFolder;
				takenFlowPane = curflowPane;
	            event.consume();
	        });
	        
        vb.setOnDragDone(event -> {
            /* the drag-and-drop gesture ended */
            /* if the data was successfully moved, clear it */
            if (event.getTransferMode() == TransferMode.MOVE) {
//            	System.out.println("Image was moved");
                //do what you want
            }

            event.consume();
        });
        	       
        vb.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
        	
        	copy.setOnAction(new EventHandler<ActionEvent>() {
    			@Override
    			public void handle(ActionEvent event) {
    				copyVbs = new ArrayList<>(selectedVbs);
    				moveVbs = null;
    				takenFolder  = currentFolder;
    				takenFlowPane = curflowPane;
    				if(multiSelection)
    					setMultiseletion();
    			}
			});
    		
        	move.setOnAction(new EventHandler<ActionEvent>() {
    			@Override
    			public void handle(ActionEvent event) {
    				moveVbs = new ArrayList<>(selectedVbs);
    				copyVbs = null;
    				takenFolder  = currentFolder;
    				takenFlowPane = curflowPane;
    				if(multiSelection)
    					setMultiseletion();
    			}
			});
        	
    	 	rename.setOnAction(new EventHandler<ActionEvent>() {
                 @Override
                 public void handle(ActionEvent event) {
                	 if(selectedVbs.size() == 1) {
                		 System.out.println("Renaming . ..");
                		 Thread thread = new Thread(new FileRenameRunnable(vb,currentFolder,out, in, FileManagerCntrl.this, curflowPane));
                		 thread.start();
                	 }
                 }
             });
     
    	 	delete.setOnAction(new EventHandler<ActionEvent>() {
     			@Override
     			public void handle(ActionEvent event) {
     				if(selectedVbs.size()==1) {
	     				vb.setBackground(unfocusBackground);
	     				String name = currentFolder + vb.getAccessibleText();
	     				Thread thread = new Thread(new DeleteFileRunnable(false,selectedVbs,vb,name,out, in, FileManagerCntrl.this, curflowPane));
	     				thread.start();
     				}
     				else {
     					vb.setBackground(unfocusBackground);
         				Thread thread = new Thread(new DeleteFileRunnable(true,selectedVbs,vb,currentFolder,out, in, FileManagerCntrl.this, curflowPane));
         				thread.start();
     				}
     			}
 			});
             
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
	
	public void openFolder(VBox vb) {
		   String name = vb.getAccessibleText();
           int i = 0;
           boolean answer = false;
           
           //Reset the variables for GUI
	        reset_Selected();
	        lastVb = null;
	        firstTime = 1;
	        curPos = prevPos = 0 ;
		        
           for (String folder : folderPaths) {
           	String folderUrl = currentFolder+name+"/";
           	if (folder.equals(folderUrl)) {
           		tempPanes.add(imagePanes.get(i));
           		tempPaths.add(folderUrl);
           		curflowPane = (FlowPane)imagePanes.get(i);
           		currentFolder = folderUrl;
           		dirPath.setText(currentFolder);
           		logoutBackBtn.setText("Back");
           		fadeEffect(250, imagePanes.get(i));
           		
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
           {
           	Platform.runLater(new Runnable() {
       	        @Override
       	        public void run() {
       	            filesPane.requestFocus();
       	        }
       	    });
           }
	}

	public void openImage(VBox vb) {
		try {
            String imgName = vb.getAccessibleText();
            String url = currentFolder+imgName;
            	if (proc != null) {
                	proc.destroy();
                	proc =  null;
            	}
        	     if(System.getProperty("os.name").equals("Linux")) {
        	    	String port = ""+port_for_pics;
                	String[] command = new String[] {"java", "-jar", "/home/timos/Desktop/GitLab/sourceCode/TransferApp/image_viewer.jar",token,currentFolder, url,client.getInetAddress().getHostAddress(),port,vb.getAccessibleText()};
                	ProcessBuilder pb = new ProcessBuilder(command);
                	pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
					proc = pb.start();
					command = null;
        	     }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}catch (Exception e) {
       	     	System.out.println(e);
			}
	}

	public void fadeEffect(int duration,Node node) {
		FadeTransition ft = new FadeTransition(Duration.millis(duration), node);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.play();
	}

	private void fetchFiles(VBox vbf) {
		FlowPane flowPane = new FlowPane();
		flowPane.setFocusTraversable(false);
		flowPane.setAlignment(Pos.TOP_LEFT);
		flowPane.setPadding(new Insets(totalPadding,totalPadding,totalPadding,totalPadding));
		
		flowPane.managedProperty().bind(flowPane.visibleProperty());

		curflowPane = flowPane; 
		
    	imagePanes.add(flowPane);
    	tempPanes.add(flowPane);
    	multiSelection = false;
    	filesPane.setContent(curflowPane);
    	String folderName =  null;
    	if (vbf == null ) 
    		folderName = "";
    	else {
    		String text = vbf.getAccessibleText();
    		folderName = text+"/";
    	}
    	currentFolder = currentFolder + folderName;
    	dirPath.setText(currentFolder);
    	
    	folderPaths.add(currentFolder);
    	tempPaths.add(currentFolder);
    	
    	if (!currentFolder.equals(mainPath))
			logoutBackBtn.setText("Back");
    	Thread thread = new Thread(new FolderIndexRunnable(init,getIp(),currentFolder, flowPane, FileManagerCntrl.this,in,out));
		thread.start();
    	if(init) {
    		init= false;
    	}
    	System.out.println("I am back");
	}
	
	@FXML
	//handler for back button
	private void prevImagePane() {
		if (!currentFolder.equals(mainPath)) {
			Node prev = null;
			int i = 0;
			try {
				for (Node pane : tempPanes) {
					if (pane == curflowPane) {
						if(curflowPane.getChildren().size()>0) {
							VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	      			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	      			        if(current!=null && previous!=null) {
	      			        	current.setBackground(unfocusBackground);
	      			        	previous.setBackground(unfocusBackground);
	      			        }
						}
      			        curPos = prevPos = 0 ;
      			        firstTime = 1;
						currentFolder = tempPaths.get(i-1); 
						dirPath.setText(currentFolder);
						multiSelection = false;
						tempPaths.remove(i);
						curflowPane = (FlowPane)prev;
						tempPanes.remove(pane);
						pane = null;
						fadeEffect(250, prev);
						filesPane.setContent(prev);
						if (currentFolder.equals(mainPath)) {
							logoutBackBtn.setText("Logout");
						}
						break;
					}
					prev = pane;
					i++;
				}
			}catch(IndexOutOfBoundsException e) {
				System.out.println(e.getCause());
				
			}
			finally {
				Platform.runLater(new Runnable() {
			        @Override
			        public void run() {
			            filesPane.requestFocus();
			        }
			    });
			}
		}
		else {
			logout();
		}
	}
	
	private void search() {
		System.out.println("Select first image that her name conatains search query!");
	}
	
	private void setGlobalEventHandler(Node node) {
		
	    node.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	    	if (node == search) {
	    		 if (ev.getCode() == KeyCode.ENTER) {
	    				search();
	 	        }
			}
        	else if( node == filesPane) {
        	    	try {
        	    		if(selectedVbs!=null) {
        	    			if(ev.getCode() == KeyCode.RIGHT  || ev.getCode() == KeyCode.LEFT ||  ev.getCode() == KeyCode.DOWN || ev.getCode() == KeyCode.UP) {
	        	    			if(selectedVbs.size() >1) 
	        	    				reset_Selected();
        	    			}
        	    		}
	    	    		int filesLine =(int)(curflowPane.getWidth()-2*totalPadding)/(imageSize+2*padding);
	        	    	int picsum = curflowPane.getChildren().size();
	        	        if(firstTime == 1) {
	        		        if(ev.getCode() == KeyCode.RIGHT  || ev.getCode() == KeyCode.LEFT ||  ev.getCode() == KeyCode.DOWN || ev.getCode() == KeyCode.UP) {
	        		        		firstTime=0;
		        			        popOver.hide();
	        		        		VBox  vb = (VBox) curflowPane.getChildren().get(curPos);
	        		        		lastVb = vb;
	        		        		vb.requestFocus();
	        		        		vb.setBackground(focusBackground);
	        		        		if(vb.getAccessibleRoleDescription().equals("picture")) {
		        		        		popOver = new PopOver(popText);
		        		        		popText.setText(vb.getAccessibleText());
		        	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		        	                    popOver.show(vb);
	        		        		}
	        		        	}
	        	        }
	        	        else {
	        		        if(ev.getCode() == KeyCode.RIGHT) {
	        			        popOver.hide();
	        		        	prevPos = curPos;
	        			        if(curPos +1 < picsum) 
	        			        	curPos++;
	        			        else 
	        			        	curPos = 0 ;
	        			        VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	        			        lastVb = current;
	        			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	        			        current.requestFocus();
	        			        current.setBackground(focusBackground);
	        			        previous.setBackground(unfocusBackground);
	        			        System.out.println("About to show");
	        			        if(current.getAccessibleRoleDescription().equals("picture")) {
		        			        popOver = new PopOver(popText);
		        			        popText.setText(current.getAccessibleText());
	        	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
	        	                    popOver.show(current);
	        			        }
	        		        }
	        		        else if(ev.getCode() == KeyCode.LEFT) {
	        		        	prevPos = curPos;
	        		        	 popOver.hide();
	        			        if(curPos == 0) 
	        			        	curPos = picsum -1 ;
	        			        else 
	        			        	curPos --;
	        			        VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	        			        lastVb = current;
	        			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	        			        current.requestFocus();
	        			        current.setBackground(focusBackground);
	        			        previous.setBackground(unfocusBackground);
	        			        if(current.getAccessibleRoleDescription().equals("picture")) {
		        			        popOver = new PopOver(popText);
		        			        popText.setText(current.getAccessibleText());
	        	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
	        	                    popOver.show(current);
	        			        }
	        			    }
	        		        else if(ev.getCode() == KeyCode.DOWN) {
	        		        	prevPos = curPos;
	        			        if(curPos + filesLine <= picsum -1 ) { 
	        			        	 popOver.hide();
	        				        curPos = curPos +filesLine;
	        				        VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	        				        lastVb = current;
	            			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	        				        current.requestFocus();
		        			        current.setBackground(focusBackground);
		        			        previous.setBackground(unfocusBackground);
		        			        if(current.getAccessibleRoleDescription().equals("picture")) {
			        			        popOver = new PopOver(popText);
			        			        popText.setText(current.getAccessibleText());
		        	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		        	                    popOver.show(current);
		        			        }
	        			        }
	        			    }
	        		        else if(ev.getCode() == KeyCode.UP) {
	        		        	prevPos = curPos;
	        		        	 popOver.hide();
	        			        if(curPos - filesLine >=0) {
	        			        	curPos -= filesLine;
	        			        	VBox current =  (VBox) curflowPane.getChildren().get(curPos);
	        			        	lastVb = current;
	            			        VBox previous = (VBox) curflowPane.getChildren().get(prevPos);
	        			        	current.requestFocus();
		        			        current.setBackground(focusBackground);
		        			        previous.setBackground(unfocusBackground);
		        			        if(current.getAccessibleRoleDescription().equals("picture")) {
			        			        popOver = new PopOver(popText);
			        			        popText.setText(current.getAccessibleText());
		        	                	popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		        	                    popOver.show(current);
		        			        }
	        			        }
	        			    }
	        	        }
        	    	}catch(NullPointerException e) {
        	    		System.out.println("please wait");
        	    	}
        	    	catch(NumberFormatException e) {
        	    		System.out.println("FlowPane is not ready yet");
        	    	}
        	}
        	        ev.consume();
        	    });
        	}

	public String getCurrentFolder() {
		return currentFolder;
		}
	
	public void setCurrentFolder(String currentFolder) {
		this.currentFolder = currentFolder;
		}
	
	public static String getIp() {
		return ip;
		}

	public void setIp(String ip) {
		FileManagerCntrl.ip = ip;
	}

	public void alert(String input) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Error Occured");
		alert.setHeaderText(input);

		alert.showAndWait();

	}

	public void pasteOnFolder(VBox vb) {
		int i=0;
		FlowPane tempPane = null;
		String tempPath=currentFolder+ vb.getAccessibleText()+"/";
		for(String tim: folderPaths) {
			if(tim.equals(tempPath)){
				tempPane = (FlowPane)imagePanes.get(i);
			}
			i++;
		}
		Thread thread =null ;
		
		if(moveVbs!=null) 
				thread = new Thread(new InternalFileTransferRunnable("Move",moveVbs,takenFlowPane,takenFolder,FileManagerCntrl.this, tempPane,tempPath));
		else if(copyVbs!=null) 
			thread = new Thread(new InternalFileTransferRunnable("Copy",copyVbs,takenFlowPane,takenFolder,FileManagerCntrl.this, tempPane,tempPath));
		if(thread !=null)
			thread.start();
		moveVbs = copyVbs = null;
//		selectedVbs  = new ArrayList<>();
	}

	public void reset_Selected() {
		if (selectedVbs != null) {
			for (VBox vb : selectedVbs) {
				System.out.println(vb.getAccessibleText());
				vb.setBackground(unfocusBackground);
				//TODO release data
			}
		if(selectedVbs.size()>1) {
			prevPos = curPos=0;
			firstTime = 1;
		}
		selectedVbs = new ArrayList<>();
		}
		
	}

	public void setMultiseletion() {
		if (multiSelection) {
			selectedVbs = new ArrayList<>();
			if(lastVb!=null)
				selectedVbs.add(lastVb);
		}
		else {
			if (selectedVbs != null) {
				for (VBox vb : selectedVbs) {
					vb.setBackground(unfocusBackground);
					//TODO release data
				}
			selectedVbs = null;
			}
			prevPos = curPos=0;
			firstTime = 1;

		}
	}
}

