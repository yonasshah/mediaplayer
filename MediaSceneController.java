package com.example.music;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;

import java.io.File;

public class MediaSceneController {
    @FXML
    private ImageView settingsButton, closeButton, closeButtonSettings, playButton, rewindButton, forwardButton;
    @FXML
    private AnchorPane paneSettings;
    private String path;
    @FXML
    private Label titleLabel;
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaView;
    @FXML
    private Pane mainPane;
    @FXML
    private Slider volSlider, progressBar;
    private static FadeTransition fadeOut = new FadeTransition();
    private static FadeTransition fadeIn = new FadeTransition();

    //Image pauseB = new Image("pause.png");
    Image pauseB = new Image(getClass().getResource("paused.png").toString());
    Image playB = new Image(getClass().getResource("play.png").toString());
    @FXML
    public void handleButtonAction(MouseEvent event) {
        // If mouse click on close button then closes else
        // If mouse click on settings button then settings pane opens
        if (event.getSource() == closeButton) {
            System.exit(0);
        } else if (event.getSource() == settingsButton) {
            showTransition(paneSettings);
        }
        // If mouse click on close button in settings pane then close settings pane
        if (event.getSource() == closeButtonSettings) {
            hideTransition(paneSettings);
        }
    }

    private void showTransition(AnchorPane anchorPane) {
        fadeIn.setNode(anchorPane);
        fadeIn.setDuration(Duration.millis(1000));
        fadeOut.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        anchorPane.setVisible(true);
        fadeIn.play();
    }

    private void hideTransition(AnchorPane anchorPane) {
        fadeOut.setNode(anchorPane);
        fadeOut.setDuration(Duration.millis(1500));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        anchorPane.setVisible(false);
        fadeOut.play();
    }

    public void chooseFile(ActionEvent event) {
        String title = "";

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        path = file.toURI().toString();


        if (path != null) {
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            title = file.getName();
            String titleFinal = title.substring(0, title.lastIndexOf("."));

            titleLabel.setText("Now playing: " + titleFinal);
            TranslateTransition tt = new TranslateTransition(Duration.seconds(10.0), titleLabel);
            double xCo = titleLabel.getLayoutX();
            tt.setFromX(xCo);
            tt.setToX(mainPane.getBoundsInParent().getWidth() - titleLabel.getWidth());
            tt.setCycleCount(TranslateTransition.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            // Handling time during media played
            // Grabs the current time in the media
            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    progressBar.setValue(newValue.toSeconds());
                }
            });

            progressBar.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    // passes value of progress in media to mediaPlayer
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });
            progressBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    // passes value of progress in media to mediaPlayer
                    mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
                }
            });
            mediaPlayer.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration total = media.getDuration();
                    progressBar.setMax(total.toSeconds());
                }
            });

            // Handles volume slider
            volSlider.setValue(mediaPlayer.getVolume() * 100.0);
            volSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    mediaPlayer.setVolume(volSlider.getValue() / 100.0);
                }
            });
            mediaPlayer.play();
            // Wait for the titleLabel to be added to the scene
            Platform.runLater(() -> {
                Scene scene = titleLabel.getScene();
                if (scene != null) {
                    // Set the toX value based on the current width of mainPane
                    double toX = mainPane.localToScene(mainPane.getBoundsInLocal()).getMaxX() - titleLabel.getWidth();
                    tt.setToX(toX);
                }
            });
        }

    }

    public void playMedia() {
        playButton.setOnMouseClicked(event -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setImage(playB);
            } else {
                mediaPlayer.play();
                mediaPlayer.setRate(1.0);
                playButton.setImage(pauseB);
            }
        });

    }

    public void rewindMedia() {
        // set the initial rewind time
        Duration rewindTime = Duration.millis(1000);

        // create a Timeline to continuously update the media position
        Timeline rewindTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            // subtract the rewind time from the current position and set it as the new position
            Duration currentPosition = mediaPlayer.getCurrentTime();
            Duration newPosition = currentPosition.subtract(rewindTime);
            mediaPlayer.seek(newPosition);
        }));
        rewindTimeline.setCycleCount(Timeline.INDEFINITE); // set the timeline to run indefinitely

        // start the rewind timeline when a button is pressed
        rewindButton.setOnMousePressed(event -> rewindTimeline.play());

        // stop the rewind timeline when the button is released
        rewindButton.setOnMouseReleased(event -> rewindTimeline.pause());
    }

    public void forwardMedia() {
        // set the initial rewind time
        Duration forwardTime = Duration.millis(1000);

        //create a Timeline to continuously update the media position
        Timeline forwardTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            // adds the forward time to the current position and set it as the new position
            Duration currentPosition = mediaPlayer.getCurrentTime();
            Duration newPosition = currentPosition.add(forwardTime);
            mediaPlayer.seek(newPosition);
        }));
        forwardTimeline.setCycleCount(Timeline.INDEFINITE); // set the timeline to run indefinitely

        // start the rewind timeline when a button is pressed
        forwardButton.setOnMousePressed(event -> forwardTimeline.play());

        // stop the rewind timeline when the button is released
        forwardButton.setOnMouseReleased(event -> forwardTimeline.pause());

    }
}

