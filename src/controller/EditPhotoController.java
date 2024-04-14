package controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Photo;
import model.Tag;
import util.Helper;
import util.PhotoListCell;
import model.Album;
import model.Tag;
import model.User;


/**
 * The EditPhotoController class edits a specific photo in application
 * It allows the user to edit photo captions, add or delete tags,
 * and log out or return to the album display.
 * 
 * @author Isham Khan and Senuri Rupasinghe
 */
public class EditPhotoController {
	@FXML
    Button backButton, logoutButton, addTagButton, deleteTagButton, editCaptionButton, editExistingTag;
    @FXML
    ImageView imageView;
    @FXML
    Label photoNameText, captionText, dateTakenText;
    @FXML
    TextField tagTypeField, tagValueField, captionField;
    @FXML
    ListView tagsList;
    private Photo photo;
    private Album album;
    private ArrayList<Tag> tags;
    private User user;
    private ArrayList<User> users;
    private int indexOfPhoto;


     /**
     * Starts the photo display view with data of the selected photo information.
     * It sets up the UI elements with the photo's details, tags, and initializes navigation functionality.
     *
     * @param photo  The photo to be displayed.
     * @param album  The album that contains the photo.
     * @param users  The list of all users.
     * @param user   The current user viewing the photo.
     */
    public void Start(Photo photo, Album album, ArrayList<User> users, User user) {
        this.user = user;
        this.users = users;
        this.photo = photo; 
        this.album = album;
        this.tags = photo.getTags();
        tagsList.setItems(FXCollections.observableArrayList(tags));
        this.photoNameText.setText(photo.getName());
        this.captionText.setText(photo.getCaption());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format((photo.getDate()).getTime());
        this.dateTakenText.setText(dateString);
        this.indexOfPhoto = (album.getPhotos()).indexOf(photo);
        this.imageView.setImage(photo.getImage());
    }
    
    /**
     * Handles the editing of a photo's caption. If the caption field is empty, a confirmation dialog is shown.
     * The photo's caption is updated and saved persistently.
     *
     * @param event The action event triggered by pressing the edit caption button.
     */
    
    public void handleEditCaption(ActionEvent event) {
        String caption = captionField.getText();
        if (caption.isEmpty()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Edit Caption Confirmation");
            alert.setHeaderText("Clear Caption");
            alert.setContentText("Are you sure you want to clear the caption?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> res = alert.showAndWait();
            if(res.get().equals(ButtonType.YES)) {
                captionField.clear();
            } else {
                return;
            }
        }
        photo.setCaption(caption);
        captionField.clear();
        this.captionText.setText(photo.getCaption());
        Helper.writeUsersToDisk(users);
    }

   	/**
     * Handles the action of the back button which returns the user to the album display.
     * This method loads the AlbumDisplay view and passes along the necessary data.
     *
     * @param event The action event triggered by pressing the back button.
     */
    public void handleBackButton(ActionEvent event) {
        // handle back button -- context of returning to album...
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AlbumDisplay.fxml"));
            Parent root = loader.load();
            PhotoManagerController controller = loader.<PhotoManagerController>getController();
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            controller.Start(user, album, users);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action of moving between photos in an album.
     * This method changes photo selected to next or previous photo in album, redirects to photoView.
     *
     * @param event The action event triggered by pressing the next or previous button.
     * @param index Respective index of next or previous photo.
     */
    public void changePhoto(ActionEvent event, int index) {
        Photo photoSelected = album.getPhotos().get(index);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PhotoView.fxml"));
            Parent root = loader.load();
            PhotoController controller = loader.<PhotoController>getController();
            Scene scene = new Scene(root);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            controller.Start(photoSelected, album, users, user);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the editing of a photo's tag. If the tag field is empty, a confirmation dialog is shown.
     * The photo's tag is updated and saved persistently.
     *
     * @param event The action event triggered by pressing the edit tag button.
     */
    
    public void handleEditTag(ActionEvent event) {
        Tag tagSelected = (Tag) tagsList.getSelectionModel().getSelectedItem();
        for (Tag t : tags) {
            if (t.equals(tagSelected)) {
                tagSelected = t;
            }
        }
        if (tagSelected != null) {
            tags.remove(tagSelected);
            tagTypeField.setText(tagSelected.getName());
            tagValueField.setText(tagSelected.getValue());
        } else {
            Alert alert0 = new Alert(AlertType.ERROR);
            alert0.setTitle("Delete Tag Error");
            alert0.setHeaderText("No Tag Selected");
            alert0.setContentText("Please select a tag to delete from list below.");
            alert0.showAndWait();
            return;
        }
        Helper.writeUsersToDisk(users);
        tagsList.setItems(FXCollections.observableArrayList(tags));
        tagsList.refresh();
    }

        /**
     * Handles adding a new tag to the photo. Checks for duplicate tags and updates the view.
     * New tags are written to disk to update the persistent state.
     *
     * @param event The action event triggered by pressing the add tag button.
     */
    public void handleAddTagButton(ActionEvent event) {
        String tagName = tagTypeField.getText().trim();
        String tagValue = tagValueField.getText().trim();
        if(tagName.equals("") || tagValue.equals("")) {
            Alert alert0 = new Alert(AlertType.ERROR);
            alert0.setTitle("Edit Photo Error");
            alert0.setHeaderText("Tag Input");
            alert0.setContentText("Please verify that both tag typ and tag value fields are filled in.");
            alert0.showAndWait();
            tagTypeField.clear();
            tagValueField.clear();
            Start(photo, album, users, user);
            return;
        }
        Tag tagToAdd = new Tag(tagName, tagValue);
        for (Tag tag : tags) {
            if ((tag.getValue().toUpperCase()).equals(tagToAdd.getValue().toUpperCase()) || (tag.getName().toUpperCase().equals("LOCATION") && tagToAdd.getName().toUpperCase().equals("LOCATION"))) {
                Alert alert0 = new Alert(AlertType.ERROR);
                alert0.setTitle("Edit Photo Error");
                alert0.setHeaderText("Duplicate Tag");
                alert0.setContentText("Tag already exists.");
                alert0.showAndWait();
                tagTypeField.clear();
                tagValueField.clear();
                Start(photo, album, users, user);
                return;
            }
        }
        tags.add(tagToAdd);
        Helper.writeUsersToDisk(users);
        tagsList.setItems(FXCollections.observableArrayList(tags));
        tagsList.refresh();
        tagTypeField.clear();
        tagValueField.clear();
    }

     /**
     * Handles the deletion of a selected tag from the photo. If no tag is selected, an error alert is shown.
     * Updates are written to disk to update the persistent state.
     *
     * @param event The action event triggered by pressing the delete tag button.
     */
    public void handleDeleteTagButton(ActionEvent event) {
        Tag tagToRemove = (Tag) tagsList.getSelectionModel().getSelectedItem();
        if (tagToRemove != null) {
            tags.remove(tagToRemove);
        } else {
            Alert alert0 = new Alert(AlertType.ERROR);
            alert0.setTitle("Delete Tag Error");
            alert0.setHeaderText("No Tag Selected");
            alert0.setContentText("Please select a tag to delete from list below.");
            alert0.showAndWait();
            return;
        }
        Helper.writeUsersToDisk(users);
        tagsList.setItems(FXCollections.observableArrayList(tags));
        tagsList.refresh();
    }

    /**
     * Handles the logout action which returns the user to the login screen.
     * Current user state is saved before logging out.
     *
     * @param event The action event triggered by pressing the logout button.
     */
    public void handleLogoutButton(ActionEvent event) {
        // handle logout
        try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginScreen.fxml"));
			Parent parent = loader.load();
			LoginController controller = loader.<LoginController>getController();
			Scene scene = new Scene(parent);
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			controller.start(stage);
			stage.setScene(scene);
			stage.show();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
    }
}
