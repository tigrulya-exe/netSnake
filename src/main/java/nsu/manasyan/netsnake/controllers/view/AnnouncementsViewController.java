package nsu.manasyan.netsnake.controllers.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.controllers.ClientController;
import nsu.manasyan.netsnake.gui.NetSnakeApp;
import nsu.manasyan.netsnake.gui.SceneFactory;
import nsu.manasyan.netsnake.proto.SnakesProto.GameMessage.AnnouncementMsg;

import java.util.Map;

public class AnnouncementsViewController {

    private ClientController clientController = ClientController.getInstance();

    @FXML
    private GridPane announcementsGrid;

    @FXML
    private Button backButton;

    public void backClicked(){
        NetSnakeApp.setScene(SceneFactory.SceneType.MENU);
    }

    public void initialize() {
        clientController.registerAnnouncementListener(this::onAnnouncementsUpdate);

//        for(int i = 0; i < 12    ; ++i) {
//            Button joinButton = new Button("Join");
//            int tmp = i;
//            joinButton.setOnAction(ae -> {
//                System.out.println("COCO JUMBO " + tmp);
//            });
//
//            Label sizeLabel = new Label(
//                    "[" + 30 + " x " + 40 + "]"
//            );
//            Label addressLabel = new Label(
//                    "localhost:8080"
//            );
//
//            addLabel(sizeLabel, 0, i);
//            addLabel(addressLabel, 1, i);
//            announcementsGrid.add(joinButton, 2, i);
//        }
    }

    private void onAnnouncementsUpdate(Map<AnnouncementMsg, AnnouncementContext> announcements){
        announcementsGrid.getChildren().clear();
        int i = 0;

        for (var entry : announcements.entrySet()) {
            updateGrid(entry.getKey(), entry.getValue(), i++);
        }
    }

    private void updateGrid(AnnouncementMsg announcement, AnnouncementContext context, int rank){
        Button joinButton = new Button("Join");
        joinButton.setOnAction(ae -> {
            clientController.joinGame(context.getMasterAddress(), false, announcement.getConfig());
            NetSnakeApp.setScene(SceneFactory.SceneType.GAME);
        });

        Label sizeLabel = new Label(
                "[" + announcement.getConfig().getWidth() + " x " + announcement.getConfig().getHeight() + "]"
        );
        Label addressLabel = new Label(
                context.getMasterAddress().toString()
        );

        addLabel(sizeLabel, 0, rank);
        addLabel(addressLabel, 1, rank);
        announcementsGrid.add(joinButton, 2, rank);
    }

    private void addLabel(Label label, int x, int y){
        label.getStyleClass().add("score-label");
        announcementsGrid.add(label, x, y);
    }
}
