package runnables;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;

import controllers.Controller;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SmbRunnable implements Runnable{

	private final Background focusBackground = new Background( new BackgroundFill(  Color.rgb(65, 107, 175), CornerRadii.EMPTY, Insets.EMPTY ) );
    private final Background unfocusBackground = new Background( new BackgroundFill( Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY ) );
	private String currentFolder;
	private FlowPane flowPane;
	private Controller controller;
	private NtlmPasswordAuthentication auth;
	
	public SmbRunnable(String currentFolder, FlowPane flowPane, Controller controller) {
		this.currentFolder = currentFolder;
		this.flowPane = flowPane;
		this.controller = controller;
		auth = controller.getAuthtentication();
	}
	
	@Override
	public void run() {
		SmbFile[] files = null;
		try {
			if (!currentFolder.endsWith("/"))
				currentFolder += "/";
			files = new SmbFile(currentFolder,auth).listFiles();
		} catch (SmbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (SmbFile file: files) {
			VBox vb = new VBox();
			vb.setPadding(new Insets(5,5,5,5));
            vb.setFocusTraversable(true);
            vb.setAlignment(Pos.CENTER);
            vb.setStyle("-fx-cursor: hand ;");
//            System.out.println(file.getPath());
            try {
				if (file.isDirectory()) {
					Image img = new Image(controller.getClass().getResource("../img/folder.png").toExternalForm(),40.0,40.0,false,false);
				    ImageView imv = new ImageView(img);
				    Text desc = new Text(60,10,file.getName().replace("/", ""));
				    desc.setWrappingWidth(60);
				    vb.getChildren().addAll(imv,desc);
				    controller.folderEffects(flowPane,vb);
				    controller.fadeEffect(500, vb);
				    Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							flowPane.getChildren().add(0,vb);
						}
					});
					
				}
				else if(file.getName().endsWith("jpg") || file.getName().endsWith("png")) {
				    Image img = new Image(controller.getClass().getResource("../img/jpg-icon.png").toExternalForm());
				    ImageView imv = new ImageView(img);
				    String filename = file.getName();
				    if (filename.length() > 10) {
				    	filename = filename.substring(0, 10);
				    	filename += "...";
				    }
				    Text desc = new Text(60,10,filename);
				    vb.setAccessibleText(file.getName());
				    desc.setWrappingWidth(60);
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
					
				}
			} catch (SmbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
