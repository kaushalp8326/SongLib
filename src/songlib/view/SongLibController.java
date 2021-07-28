package songlib.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import songlib.app.Song;

public class SongLibController {
	
	@FXML TextField txtTitle;
	@FXML TextField txtArtist;
	@FXML TextField txtYear;
	@FXML TextField txtAlbum;
	
	@FXML Button cmdEdit;
	@FXML Button cmdEditCancel;
	@FXML Button cmdEditConfirm;
	
	@FXML TextField txtAddTitle;
	@FXML TextField txtAddArtist;
	@FXML TextField txtAddYear;
	@FXML TextField txtAddAlbum;
	
	@FXML Button cmdAdd;
	@FXML Button cmdAddCancel;
	@FXML Button cmdAddConfirm;
	
	@FXML Button cmdDelete;
	
	@FXML ListView<String> lstSongs;
	
	
	// The lists we work with internally; add/remove from here.
	// ALWAYS make sure both lists are updated at the same time. They work independently.
	private static ObservableList<Song> internalObjectList;
	private static ObservableList<String> internalStringList;
	
	// This list is a wrapper for internalList that keeps elements sorted; widgets use this
	private static ObservableList<String> displayList;
	
	// File paths for application source files
	public final static String FXML_MAIN_PATH = "/songlib/view/main.fxml";
	public final static String FXML_ADD_PATH = "/songlib/view/add.fxml";
	public final static String FXML_EDIT_PATH = "/songlib/view/edit.fxml";
	public final static String SONG_PATH = "songs.txt";
	
	// Reference variables for the main stage
	private Stage mainStage;
	private static int selectedIndex;
	

	/*
	 * Getter for the song list. Only used when writing to file at shutdown.
	 */
	public ObservableList<Song> getSongList(){
		return internalObjectList;
	}
	
	
	/*
	 * Fetch a Song object based on a ListView entry.
	 */
	public Song getSongFromString(String str) {
		if(str == null) {
			return null;
		}
		String[] songInfo = str.split(" -- ");
		Song target = new Song(songInfo[0], songInfo[1]);
		int i = 0;
		while(i < internalObjectList.size()) {
			if(internalObjectList.get(i).equals(target)) {
				break;
			}
			i++;
		}
		return internalObjectList.get(i);
	}

	
	/*
	 * Update the data song data text fields when the user selects a new song.
	 * If the list is empty, show nothing.
	 */
	private void showSongData(Stage mainStage) {
		if(internalStringList.size() == 0) {
			txtTitle.setText("");
			txtArtist.setText("");
			txtYear.setText("");
			txtAlbum.setText("");
			return;
		}
		
		String songStr = lstSongs.getSelectionModel().getSelectedItem();
		Song s = getSongFromString(songStr);
		
		// Display data
		txtTitle.setText(s.name);
		txtArtist.setText(s.artist);
		if(s.year == 0) {
			txtYear.setText("");
		}else {
			txtYear.setText(String.valueOf(s.year));
		}
		txtAlbum.setText(s.album);
	}
	
	
	/*
	 * Shows an error dialog.
	 */
	private void showErrorDialog(String content) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(mainStage);
		alert.setTitle("Song Library");
		alert.setHeaderText("");
		alert.setContentText(content);
		alert.showAndWait();
		return;
	}
	
	
	/*
	 * Wrapper for start() where firstInstance is false. We'll use this one most of the time.
	 */
	public void start(Stage mainStage) {
		start(mainStage, false);
	}
	
	
	/*
	 * Creates a new instance of the GUI controller, either on startup or at a scene change.
	 * If we're starting up, this loads songs from file and prepares them to be displayed.
	 * If it's a scene change, we preserve the data from last scene and skip the file read.
	 */
	public void start(Stage mainStage, boolean firstInstance) {
		
		this.mainStage = mainStage;
		
		if(firstInstance) {
			// Initialize input stream
			Scanner fd;
			try {
				fd = new Scanner(new File(SONG_PATH));
			}catch(FileNotFoundException fnfe) {
				System.out.println("Unexpected error encountered when reading songs from file, aborting.");
				return;
			}
			fd.useDelimiter("[;\n]");
			
			// Read from file
			internalObjectList = FXCollections.observableArrayList();
			internalStringList = FXCollections.observableArrayList();
			displayList = new SortedList<String>(internalStringList, (s1, s2) -> s1.toUpperCase().compareTo(s2.toUpperCase()));
			while(fd.hasNext()) {
				String name = fd.next();
				String artist = fd.next();
				String temp = fd.next();
				int year;
				if(temp.equals("")) {
					year = 0;
				}else {
					year = Integer.parseInt(temp);
				}
				String album = fd.next();
				
				Song s;
				if(year!=0 && album.compareTo("")!=0) {
					s=new Song(name, artist, year, album);
				}else if(year==0 && album.compareTo("")==0) {
					s=new Song(name, artist);
				}else if(album.compareTo("")==0) {
					s=new Song(name, artist, year);
				}else {
					s=new Song(name, artist, album);
				}
				internalObjectList.add(s);
				internalStringList.add(s.toString());
			}
			fd.close();
			selectedIndex = 0;
		}
		
		// Connect lstSongs to a listener
		lstSongs.setItems(displayList);
		lstSongs
			.getSelectionModel()
			.selectedIndexProperty()
			.addListener((obs, oldVal, newVal) -> showSongData(mainStage));
		lstSongs.getSelectionModel().select(selectedIndex);
		
	}
	
	
	/*
	 * Response to clicking cmdEdit, cmdEditCancel, cmdAdd, or cmdAddCancel.
	 * Takes the user to the edit/add pane after clicking one of the command buttons
	 * or to the "main" pane after clicking one of the cancel buttons.
	 */
	public void screenTransition(ActionEvent e) {
		FXMLLoader loader = new FXMLLoader();
		Button cmd = (Button)e.getSource();
		if(cmd.equals(cmdAdd)) {
			loader.setLocation(getClass().getResource(FXML_ADD_PATH));
		}else if(cmd.equals(cmdEdit)) {
			if(internalObjectList.size()==0) {
				showErrorDialog("There are no remaining songs to edit.");
				return;
			}
			loader.setLocation(getClass().getResource(FXML_EDIT_PATH));
		}else {
			loader.setLocation(getClass().getResource(FXML_MAIN_PATH));
		}
		
		AnchorPane root;
		try {
			root = (AnchorPane)loader.load();
		}catch(IOException err) {
			System.out.println(err.getMessage());
			System.out.println("Unexpected error encountered when importing FXML layout, aborting.");
			return;
		}
		selectedIndex = lstSongs.getSelectionModel().getSelectedIndex();
		SongLibController controller = loader.getController();
		controller.start(mainStage);
		Scene mainScene = new Scene(root);
		mainStage.setScene(mainScene);
		mainStage.setTitle("Song Library");
		mainStage.setResizable(false);
		mainStage.show();
	}
	
	
	/*
	 * Adds a song to the song list if the inputs are valid and there are no conflicts with existing songs.
	 */
	public void addSong(ActionEvent e) {
		
		Song toAdd;
		
		String name=txtAddTitle.getText();
		String artist=txtAddArtist.getText();
		if(name.compareTo("")==0 || artist.compareTo("")==0) {
			showErrorDialog("Please enter names for both the song title and artist.");
			return;
		}
		
		String temp=txtAddYear.getText();
		int year;
		if(temp.equals("")) {
			year=0;
		}else {
			try {
				year=Integer.parseInt(temp);
			}catch(NumberFormatException nfe) {
				showErrorDialog("Please enter a valid year.");
				return;
			}
		}
		if(year < 0) {
			showErrorDialog("Please enter a valid year.");
			return;
		}
		
		String album=txtAddAlbum.getText();
		
		if(year!=0 && album.compareTo("")!=0) {
			toAdd=new Song(name, artist, year, album);
		}else if(year==0 && album.compareTo("")==0) {
			toAdd=new Song(name, artist);
		}else if(album.compareTo("")==0) {
			toAdd=new Song(name, artist, year);
		}else {
			toAdd=new Song(name, artist, album);
		}
		
		int i=0;
		boolean duplicate=false;
		while(i<internalObjectList.size()) {
			if(internalObjectList.get(i).equals(toAdd)) {
				//song already present -- do not add
				i=internalObjectList.size();
				duplicate=true;
			}else {
				i++;
			}
		}
		
		if(!duplicate) {
			internalObjectList.add(toAdd);
			internalStringList.add(toAdd.toString());
			lstSongs.getSelectionModel().select(toAdd.toString());
			screenTransition(e);
		}else {
			showErrorDialog("The title-artist combination you entered already exists.");
			return;
		}
	}
	

	/*
	 * Adds a song to the song list if the inputs are valid and there are no conflicts with existing songs.
	 */
	public void editSong(ActionEvent e) {
		
		String songStr = lstSongs.getSelectionModel().getSelectedItem();
		Song target = getSongFromString(songStr);
		
		String name=txtTitle.getText();
		String artist=txtArtist.getText();
		if(name.compareTo("")==0 || artist.compareTo("")==0) {
			showErrorDialog("Please enter names for both the song title and artist.");
			return;
		}
		
		String temp=txtYear.getText();
		int year;
		if(temp.equals("")) {
			year=0;
		}else {
			try {
				year=Integer.parseInt(temp);
			}catch(NumberFormatException nfe) {
				showErrorDialog("Please enter a valid year.");
				return;
			}
		}
		if(year < 0) {
			showErrorDialog("Please enter a valid year.");
			return;
		}
		
		String album=txtAlbum.getText();
		
		// Check that we're not stepping on the toes of an existing song
		// The only time it's ok for a duplicate title-artist combo is if we're comparing the song against itself
		for(Song s: internalObjectList) {
			if(s.name.equalsIgnoreCase(name) && s.artist.equalsIgnoreCase(artist) && s != target) {
				showErrorDialog("The title-artist combination you entered already exists.");
				return;
			}
		}
		
		// The edit is valid. Make the change in both internal lists
		internalStringList.remove(target.toString());
		target.name = name;
		target.artist = artist;
		target.year = year;
		target.album = album;
		internalStringList.add(target.toString());
		
		// Force-select this element
		lstSongs.getSelectionModel().select(target.toString());
		screenTransition(e);
	}
	
	/*
	 * Deletes a song from the song list.
	 */
	public void deleteSong(ActionEvent e) {
		if(internalObjectList.size()==0) {
			showErrorDialog("There are no remaining songs to delete.");
			return;
		}
		
		String songStr = lstSongs.getSelectionModel().getSelectedItem();
		Song target = getSongFromString(songStr);
		
		// Confirmation dialog for the delete. Aborts the delete if the user cancels
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(mainStage);
		alert.setTitle("Song Library");
		alert.setHeaderText("");
		String content = "Are you sure you want to delete this song?" +
						 "\nTitle: " + target.name + 
						 "\nArtist: " + target.artist;
		if(target.year > 0) {
			content += "\nYear: " + target.year;
		}if(target.album != null && !target.album.equals("")) {
			content += "\nAlbum: " + target.album;
		}
		alert.setContentText(content);
		Optional<ButtonType> response = alert.showAndWait();
		if(response.isPresent() && response.get() == ButtonType.CANCEL) {
			return;
		}
		
		// User has confirmed. Before we delete, force-select the next item if it
		// exists. If not, select the previous item.
		int i = lstSongs.getSelectionModel().getSelectedIndex();
		if(internalObjectList.size() > 1) {
			if(i == internalObjectList.size() - 1) {
				lstSongs.getSelectionModel().select(i-1);
			}else if(internalObjectList.size() > 1) {
				lstSongs.getSelectionModel().select(i+1);
			}
		}
		internalObjectList.remove(target);
		internalStringList.remove(target.toString());
		
	}
	

}
