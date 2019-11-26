package nsu.manasyan.netsnake.contexts;

public class ScoreContext {
    private String playerName;

    private int points;

    public ScoreContext(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }
}
