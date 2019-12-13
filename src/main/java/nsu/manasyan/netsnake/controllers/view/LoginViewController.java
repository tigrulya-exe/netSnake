package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;


public class LoginViewController {
    @FXML
    private TextField nameTextField;

    @FXML
    private Button loginButton;

    public void loginClicked(){
        String name = nameTextField.getText();
        ClientController.getInstance().setPlayerName(name);
        NetSnakeApp.setScene(SceneFactory.SceneType.MENU);
    }
}
