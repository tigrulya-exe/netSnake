package nsu.manasyan.netsnake.util;

import nsu.manasyan.netsnake.models.Field;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState.Coord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnakePartManipulator {

    private Field field;

    private SnakeManipulator snakeManipulator;

    private interface WallPassHandler{
        void handle(int x, int y, Coord point, boolean isVertical);
    }

    private Map<Field.WallPass, WallPassHandler> manipulators = new HashMap<>();

    private SnakePartManipulator(){
        initManipulators();
    }

    private static class SingletonHelper{

        private static final SnakePartManipulator manipulator = new SnakePartManipulator();
    }
    public static SnakePartManipulator getInstance() {
        return SnakePartManipulator.SingletonHelper.manipulator;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Field.WallPass checkIfPassedThroughWall(int x, int y, Field field){
        Field.WallPass pass = field.checkIfPassedThroughWallX(x);
        if(pass != Field.WallPass.NOT_PASSED)
            return pass;
        return field.checkIfPassedThroughWallY(y);
    }

    public void useSnakeCoords(List<SnakesProto.GameState.Coord> points, SnakeManipulator manipulator) {
        int x = points.get(0).getX();
        int y = points.get(0).getY();
        boolean isVertical;
        snakeManipulator = manipulator;

        Field.WallPass wallPass;

//        Field field = MasterController.getInstance().getField();

        for (int i = 1; i < points.size(); ++i) {
            var point = points.get(i);
            isVertical = point.getX() == 0;
            wallPass = checkIfPassedThroughWall(x + point.getX() , y + point.getY() , field);

            x = field.wrapX(x + point.getX());
            y = field.wrapY(y + point.getY());

            manipulators.get(wallPass).handle(x, y, point, isVertical);
        }
    }

    private void leftPass(int x, int y, Coord point, boolean isVertical){
        horizontalPass(x, field.wrapX(x- point.getX()), y);
    }

    private void rightPass(int x, int y, Coord point, boolean isVertical){
        horizontalPass(field.wrapX(x- point.getX()), x, y);
    }

    private void upPass(int x, int y, Coord point, boolean isVertical){
        verticalPass(field.wrapY(y - point.getY()), y, x);
    }

    private void downPass(int x, int y, Coord point, boolean isVertical){
        verticalPass(y, field.wrapY(y - point.getY()), x);
    }

    private void noPass(int x, int y, Coord point, boolean isVertical){
        if (isVertical)
            snakeManipulator.manipulate(field.wrapY(y - point.getY()), y, x, true);
        else
            snakeManipulator.manipulate(field.wrapX(x- point.getX()), x, y, false);
    }

    private void horizontalPass(int fromZeroTo, int fromToWidth, int y){
        snakeManipulator.manipulate(fromToWidth, field.getWidth() - 1, y, false);
        snakeManipulator.manipulate(0, fromZeroTo, y, false);
    }

    private void verticalPass(int fromZeroTo, int fromToHeight, int x){
        snakeManipulator.manipulate( fromToHeight, field.getHeight() - 1, x, true);
        snakeManipulator.manipulate(0, fromZeroTo, x, true);
    }

    private void initManipulators() {
        manipulators.put(Field.WallPass.UP, this::upPass);
        manipulators.put(Field.WallPass.DOWN, this::downPass);
        manipulators.put(Field.WallPass.LEFT, this::leftPass);
        manipulators.put(Field.WallPass.RIGHT, this::rightPass);
        manipulators.put(Field.WallPass.NOT_PASSED, this::noPass);
    }
}