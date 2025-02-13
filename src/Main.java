import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public static Stage currentStage;
	public static Scene mainScene;
	
	@Override
    public void start(Stage primaryStage) throws Exception{
		currentStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("AreaCalculatorMain.fxml"));
        mainScene = new Scene(root);
        currentStage.setTitle("Area Calculator");
        currentStage.setScene(mainScene);
        currentStage.show();
    }
	
	
    public static void main(String[] args) {
        launch(args);
    }
    
}
