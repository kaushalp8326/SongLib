package songlib.app;

import java.io.FileWriter;
import java.io.IOException;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import songlib.view.*;

/*
 * Application main class
 */
public class SongLib extends Application{
	
	SongLibController controller;
	
	@Override
	public void start(Stage mainStage) {
		
		// get FXML
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(SongLibController.FXML_MAIN_PATH));
		AnchorPane root;
		try {
			root = (AnchorPane)loader.load();
		}catch(IOException e) {
			System.out.println("Unexpected error encountered when importing FXML layout, aborting.");
			return;
		}
		
		// get controller
		controller = loader.getController();
		controller.start(mainStage, true);
		
		// set the scene
		Scene mainScene = new Scene(root);
		mainStage.setScene(mainScene);
		mainStage.setTitle("Song Library");
		mainStage.setResizable(false);
		mainStage.show();
		
	}
	
	/*
	 * Writes song list updates to file on application shutdown.
	 */
	@Override
	public void stop() {
		ObservableList<Song> songs = controller.getSongList();
		FileWriter fd;
		try {
			fd = new FileWriter(SongLibController.SONG_PATH);
			fd.write("");
			for(Song s: songs) {
				String year = s.year == 0 ? "" : String.valueOf(s.year);
				String line = s.name + ";" + s.artist + ";" + year + ";" + s.album + "\n";
				fd.append(line);
			}
			fd.close();
		}catch(IOException e) {
			System.out.println("Song list save file is corrupted or missing, unable to save changes.");
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
