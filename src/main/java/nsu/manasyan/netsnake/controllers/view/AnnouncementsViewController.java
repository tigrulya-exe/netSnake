package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.proto.SnakesProto.GameMessage.AnnouncementMsg;

import java.util.List;

public class AnnouncementsViewController {

    private ClientController clientController = ClientController.getInstance();

    @FXML
    private Button backButton;

    public void backClicked(){
        NetSnakeApp.setScene(SceneFactory.SceneType.MENU);
    }

    public void initialize() {
        clientController.registerAnnouncementListener(this::onAnnouncementsUpdate);
    }

    private void onAnnouncementsUpdate(List<AnnouncementMsg> announcements){

    }
}
