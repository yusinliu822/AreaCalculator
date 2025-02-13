import java.io.File;
import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class AreaCalculatorMainController implements EventHandler<MouseEvent> {
	@FXML public Button btnOpenFile;
	@FXML public Button btnAddBGColor;
	@FXML public Button btnClearBGColor;
	@FXML public Button btnAnalysis;
	@FXML public Button btnAnalysisAuto;
	@FXML public ImageView ivPreview;
	@FXML public ImageView ivMask;
	@FXML public Image selectedImage;
	@FXML public PixelReader pixelReader;
	@FXML public PixelWriter pixelWriter;
	@FXML public ColorPicker cprBGColor;
	@FXML public Slider sliderR;
	@FXML public Slider sliderG;
	@FXML public Slider sliderB;
	@FXML public Slider sliderRGB;
	@FXML public Label lblOutputIndex;
	@FXML public Label lblOutput;
	@FXML public CheckBox chkBGMask;
	@FXML public GridPane grpList;
	@FXML public Pane pneColor1;
	@FXML public Pane pneColor2;
	@FXML public Pane pneColor3;
	@FXML public Pane pneColor4;
	ArrayList<Color> bgColorList = new ArrayList<Color>();
	
	@FXML
	public void btnOpenFilePressed() {
		//讀取圖片
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.setTitle("開啟圖片");
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JPG, PNG", "*.jpg", "*.png"));
		File selectedFile = chooser.showOpenDialog(Main.currentStage);
		if (selectedFile != null)System.out.println(selectedFile.getAbsolutePath());
		
		//顯示圖片
		String imgUrl;
		try {			
			imgUrl = selectedFile.toURI().toURL().toExternalForm();
			selectedImage = new Image(imgUrl);
			ivPreview.setImage(selectedImage);
			ivMask.setImage(null);
			pixelReader = selectedImage.getPixelReader();
		} catch (Exception e) {}
	}
	
	@FXML
	public void btnAddBGColorPressed() {
		Color currentColor = cprBGColor.getValue();
		bgColorList.add(currentColor);
		if(bgColorList.size()>4) bgColorList.remove(0);
		displayChoosenColor();
	}
	
	@FXML
	public void btnClearBGColorPressed() {
		bgColorList.clear();
		clearChoosenColor();
		ivMask.setImage(null);
	}
	
	@Override
	public void handle(MouseEvent e) {
		if(e.getButton() == MouseButton.SECONDARY) {
			int clickX = (int)e.getSceneX()-(int)ivPreview.getLayoutX();
			int clickY = (int)e.getSceneY()-(int)ivPreview.getLayoutY();
			double picX = selectedImage.getWidth();
			double picY = selectedImage.getHeight();
			double cropX = ivPreview.getFitWidth();
			double cropY = ivPreview.getFitHeight();

			if (picX > picY)
				cropY *= picY / picX;
			else if (picY > picX)
				cropX *= picX /picY;

			if (clickX >= 0 && clickX <= cropX && clickY >= 0 && clickY <= cropY){ // 邊界檢查
				Double mappedX = clickX * picX / cropX;
				Double mappedY = clickY * picY / cropY;
//				System.out.println(String.format("Clicked: (%d, %d), Mapped: (%.2f, %.2f)", clickX, clickY, mappedX, mappedY));
				Color currentColor = pixelReader.getColor(mappedX.intValue(), mappedY.intValue());
				bgColorList.add(currentColor);
				if(bgColorList.size()>4) bgColorList.remove(0);
				displayChoosenColor();
			}
		}
	}
	
	
	@FXML
	public void chkBGMaskAction() {
		ivMask.setVisible(chkBGMask.isSelected());
	}
	
	@FXML
	public void btnAnalysisPressed() {
		System.out.println(bgColorList);
		double isBGColor = 0;
		
		//靈敏度控制
		double minRSemblance = sliderR.getValue();
		double minGSemblance = sliderG.getValue();
		double minBSemblance = sliderB.getValue();
		
		WritableImage maskImage = new WritableImage((int)selectedImage.getWidth(),(int)selectedImage.getHeight());
        pixelWriter = maskImage.getPixelWriter();
		
		for(int hIndex = 0; hIndex < (int)selectedImage.getHeight(); hIndex++) {
			for(int wIndex = 0; wIndex < (int)selectedImage.getWidth(); wIndex++) {
				Color currentColor = pixelReader.getColor(wIndex, hIndex);
				for (Color bgColor : bgColorList) {
					double rsemblance = getColorRSemblance(currentColor, bgColor);
					double gsemblance = getColorGSemblance(currentColor, bgColor);
					double bsemblance = getColorBSemblance(currentColor, bgColor);
					if(rsemblance >= minRSemblance && rsemblance <= 1){
						if(gsemblance >= minGSemblance && gsemblance <= 1) {
							if(bsemblance >= minBSemblance && bsemblance <= 1) {
								isBGColor++;
								pixelWriter.setColor(wIndex, hIndex, Color.HOTPINK);
								break;
							}
						}
					}
				}
			}
		}
		
		ivMask.setImage(maskImage);
		Double output = 1-isBGColor/(selectedImage.getHeight()*selectedImage.getWidth());
		lblOutput.setText(output.toString());
		System.out.println(1-isBGColor/(selectedImage.getHeight()*selectedImage.getWidth()));
	}
	
	@FXML
	public void btnAnalysisAutoPressed() {
		System.out.println(bgColorList);
		double isBGColor = 0;
		Color maskColor = Color.HOTPINK;
		
		//靈敏度控制
		double minSemblance = sliderRGB.getValue();
		
		WritableImage maskImage = new WritableImage((int)selectedImage.getWidth(),(int)selectedImage.getHeight());
        pixelWriter = maskImage.getPixelWriter();
		
		for(int hIndex = 0; hIndex < (int)selectedImage.getHeight(); hIndex++) {
			for(int wIndex = 0; wIndex < (int)selectedImage.getWidth(); wIndex++) {
				Color currentColor = pixelReader.getColor(wIndex, hIndex);
				for (Color bgColor : bgColorList) {
					if(!maskImage.getPixelReader().getColor(wIndex, hIndex).equals(maskColor)) {
						double semblance = getColorSemblance(currentColor, bgColor);
						if(semblance >= minSemblance && semblance <= 1){
							isBGColor++;
							pixelWriter.setColor(wIndex, hIndex, maskColor);
							break;
						}
					}
				}
			}
		}
		
		ivMask.setImage(maskImage);
		Double output = 1-isBGColor/(selectedImage.getHeight() * selectedImage.getWidth());
		lblOutput.setText(output.toString());
		System.out.println(isBGColor + ", " + (selectedImage.getHeight() * selectedImage.getWidth()));
		System.out.println(1-isBGColor/(selectedImage.getHeight() * selectedImage.getWidth()));
	}
	
	//計算RGB相似度
	public static double getColorRSemblance(Color color1, Color color2){
        double rsemblance = 1.0 - Math.abs(color1.getRed() - color2.getRed());
        return rsemblance;
    }
	public static double getColorGSemblance(Color color1, Color color2){
        double gsemblance = 1.0 - Math.abs(color1.getGreen() - color2.getGreen());
        return gsemblance;
    }
	public static double getColorBSemblance(Color color1, Color color2){
        double bsemblance = 1.0 - Math.abs(color1.getBlue() - color2.getBlue());
        return bsemblance;
    }
	
	public static double getColorSemblance(Color color1, Color color2){
		double semblance = (255 - (Math.abs(color1.getRed() - color2.getRed()) * 255 * 0.297
								+ Math.abs(color1.getGreen() - color2.getGreen()) * 255 * 0.593
								+ Math.abs(color1.getBlue() - color2.getBlue()) * 255 * 0.11)) / 255;
		return semblance;
	}
	
	//呈現選到的背景顏色
	public void displayChoosenColor() {
		try {
			pneColor1.setStyle("-fx-background-color: #" + bgColorList.get(0).toString().substring(2, 8));
			pneColor2.setStyle("-fx-background-color: #" + bgColorList.get(1).toString().substring(2, 8));
			pneColor3.setStyle("-fx-background-color: #" + bgColorList.get(2).toString().substring(2, 8));
			pneColor4.setStyle("-fx-background-color: #" + bgColorList.get(3).toString().substring(2, 8));
		}catch (Exception e){};
	}
	//清除選到的背景顏色
	public void clearChoosenColor() {
		try {
			pneColor1.setStyle("-fx-background-color: #" + "00000000");
			pneColor2.setStyle("-fx-background-color: #" + "00000000");
			pneColor3.setStyle("-fx-background-color: #" + "00000000");
			pneColor4.setStyle("-fx-background-color: #" + "00000000");
		}catch (Exception e){};
	}

}
