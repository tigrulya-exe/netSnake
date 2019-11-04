package nsu.manasyan.netsnake;

import nsu.manasyan.netsnake.out.SnakesProto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameObjectBuilder {
    private static final int DEFAULT_PORT = 18888;

    private static final int DEFAULT_MASTER_ID = 0;

    private static final String DEFAULT_ADDRESS_STR = "127.0.0.1";

    private static final String DEFAULT_NAME = "Steve";

    public static SnakesProto.GamePlayers initNewGamePlayers() {
        SnakesProto.GamePlayer master = initMaster();
        return SnakesProto.GamePlayers
                .newBuilder()
                .setMaster(master)
                .build();
    }

    public static SnakesProto.GamePlayer initMaster() {
        return SnakesProto.GamePlayer
                .newBuilder()
                .setId(DEFAULT_MASTER_ID)
                .setIpAddress(DEFAULT_ADDRESS_STR)
                .setPort(DEFAULT_PORT)
                .setName(DEFAULT_NAME)
                .build();
    }

    public static SnakesProto.GameState initNewGameState(SnakesProto.GameConfig config){
        return SnakesProto.GameState
                .newBuilder()
                .setStateOrder(0)
                .setPlayers(initNewGamePlayers())
                .addAllFoods(initFoods(config))
                .addSnakes(initNewSnake(DEFAULT_MASTER_ID))
                .build();
    }

    public static List<SnakesProto.GameState.Coord> initFoods(SnakesProto.GameConfig config){
        Random random = new Random();
        List<SnakesProto.GameState.Coord> foods = new ArrayList<>();
        int x, y;

        for(int i = 0; i < config.getFoodStatic() + config.getFoodPerPlayer(); ++i){
            x = random.nextInt(config.getWidth());
            y = random.nextInt(config.getHeight());
            foods.add(SnakesProto.GameState.Coord.newBuilder().setX(x).setY(y).build());
        }

        return foods;
    }

    public static SnakesProto.GameState.Snake initNewSnake(int playerId){
        // TODO find free 5x5 to place 2x1 snake (head + tail)
        return null;
    }
}
