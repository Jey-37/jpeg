package com.example.jpeg;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;


public class MainController implements Initializable
{
	private final Logger logger = Logger.getLogger(MainController.class.getName());

	@FXML public Spinner<Double> qSpinner;
	@FXML private AnchorPane root;
	@FXML private Label label1, label2;
	@FXML private ImageView imView;
	@FXML private Button openButton, saveButton, pressButton;
	@FXML private HBox hBox;

	@FXML private ProgressBar progressBar;

	FileChooser openFileChooser, saveFileChooser;
	{
		openFileChooser = new FileChooser();
		openFileChooser.setTitle("Open Image File");
		openFileChooser.getExtensionFilters().setAll(
				new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"),
				new FileChooser.ExtensionFilter("JPGG Files", "*.jpgg"));

		saveFileChooser = new FileChooser();
		saveFileChooser.setTitle("Save Image File");
		saveFileChooser.getExtensionFilters().setAll(
				new FileChooser.ExtensionFilter("JPGG Files", "*.jpgg"));
	}

	private byte[] pImage;
	private BufferedImage img;
	private String imgName;
	private File curFileDirectory = new File("C:/");

	@FXML
	private void onOpenButtonClick() {
		openFileChooser.setInitialDirectory(curFileDirectory);
		File file = openFileChooser.showOpenDialog(root.getScene().getWindow());
		if (file == null) 
			return;

		pImage = null;
		img = null;
		String fileName = file.getName();
		imgName = fileName.substring(0, fileName.lastIndexOf('.'));
		String ext = fileName.substring(fileName.lastIndexOf('.'));
		try {
			if (ext.equals(".jpgg")) {
				var fin = new FileInputStream(file.getAbsolutePath());
                byte[] inBytes = fin.readAllBytes();
                fin.close();

				Task<Void> pressingTask = new PressingTask() {
					@Override
					protected Void call() {
						blockButtons();
						imView.setOpacity(0.3);
						progressBar.setVisible(true);
						try {
							JPEG jpeg = new JPEG(this);
							img = jpeg.unpressImg(inBytes);
							imView.setImage(SwingFXUtils.toFXImage(img, null));
						} catch (Exception ex) {
							logger.warning(ex.getMessage());
							ex.printStackTrace();
						}

						curFileDirectory = file.getParentFile();
						Platform.runLater(() -> {
							imView.setFitHeight(Math.min(img.getHeight(), hBox.getHeight()));
							imView.setFitWidth(Math.min(img.getWidth(), hBox.getWidth()));
							label1.setVisible(true);
							label2.setText(inBytes.length+" bytes");
							label2.setVisible(true);
						});

						progressBar.setVisible(false);
						unblockButtons();
						imView.setOpacity(1.0);

						return null;
					}
				};
				progressBar.progressProperty().bind(pressingTask.progressProperty());
				new Thread(pressingTask).start();
			} else {
				String localUrl = file.toURI().toString();
				Image fxImage = new Image(localUrl);
				img = SwingFXUtils.fromFXImage(fxImage, null);

				int imgSize = 0;
      			var pf = fxImage.getPixelReader().getPixelFormat().getType();
				if (pf.equals(PixelFormat.Type.BYTE_RGB))
      				imgSize = img.getWidth()*img.getHeight()*3;
      			else
      				imgSize = img.getWidth()*img.getHeight()*4;
				label1.setVisible(true);
				label2.setText(imgSize+" bytes");

      			imView.setImage(fxImage);
				curFileDirectory = file.getParentFile();
				imView.setFitHeight(Math.min(img.getHeight(), hBox.getHeight()));
				imView.setFitWidth(Math.min(img.getWidth(), hBox.getWidth()));
				label2.setVisible(true);
				pressButton.setDisable(false);
			}
		} catch (Exception ex) {
			logger.warning(ex.getMessage());
		}
	}

	@FXML
	private void onSaveButtonClick() {
		saveFileChooser.setInitialDirectory(curFileDirectory);
		saveFileChooser.setInitialFileName(imgName+".jpgg");
		File file = saveFileChooser.showSaveDialog(root.getScene().getWindow());
		if (file == null) 
			return;

		try {
			var fos = new FileOutputStream(file.getAbsolutePath());
			fos.write(pImage);
        	fos.close();
        	ImageIO.write(img, "jpg", new File(file.getParent()+"\\"+imgName+"_pressed.jpg"));
    	} catch (IOException ex) {
			logger.warning(ex.getMessage());
			ex.printStackTrace();
    	}
	}

	@FXML
	private void onPressButtonClick() {
		Task<Void> pressingTask = new PressingTask() {
			@Override
			protected Void call() {
				blockButtons();
				imView.setOpacity(0.3);
				progressBar.setVisible(true);

				try {
					JPEG jpeg = new JPEG(this);
					pImage = jpeg.pressImg(img, qSpinner.getValue());
					setInitialProgress(0.6);
					img = jpeg.unpressImg(pImage);
				} catch (Exception ex) {
					logger.warning(ex.getMessage());
				}

				imView.setImage(SwingFXUtils.toFXImage(img, null));
				Platform.runLater(() -> label2.setText(pImage.length+" bytes"));

				progressBar.setVisible(false);
				unblockButtons();
				imView.setOpacity(1.0);
				return null;
			}
		};
		progressBar.progressProperty().bind(pressingTask.progressProperty());
		new Thread(pressingTask).start();
	}

	public void blockButtons() {
		pressButton.setDisable(true);
		openButton.setDisable(true);
		saveButton.setDisable(true);
	}

	public void unblockButtons() {
		saveButton.setDisable(false);
		openButton.setDisable(false);
		pressButton.setDisable(false);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		hBox.widthProperty().addListener((observableValue, oldWidth, newWidth) -> {
        		if (imView.getImage() != null) {
        			imView.setFitHeight(Math.min(img.getHeight(), hBox.getHeight()));
      				imView.setFitWidth(Math.min(img.getWidth(), hBox.getWidth()));
      			}
    		});
		hBox.heightProperty().addListener((observableValue, oldHeight, newHeight) -> {
        		if (imView.getImage() != null) {
        			imView.setFitHeight(Math.min(img.getHeight(), hBox.getHeight()));
      				imView.setFitWidth(Math.min(img.getWidth(), hBox.getWidth()));
      			}
    		});

		qSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() == 0)
				return;
			newValue = newValue.replace('.', ',');
			for (int i = 0; i < newValue.length(); i++) {
				if (!Character.isDigit(newValue.charAt(i)) && newValue.charAt(i) != ',') {
					qSpinner.getEditor().setText(oldValue);
					return;
				}
			}
			if (newValue.indexOf(',') != newValue.lastIndexOf(',')) {
				qSpinner.getEditor().setText(oldValue);
				return;
			}

			if (newValue.indexOf(',') >= 0) {
				String dec = newValue.substring(newValue.indexOf(',')+1);
				if (dec.length() > 1) {
					qSpinner.getEditor().setText(oldValue);
					return;
				}
			}
			qSpinner.getEditor().setText(newValue);
		});
		qSpinner.getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				if (qSpinner.getEditor().getText().length() == 0)
					qSpinner.getEditor().setText("0");
			}
		});
	}
}
