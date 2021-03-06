package nsu.manasyan.netsnake.models;

import nsu.manasyan.netsnake.Wrappers.FullPoints;
import nsu.manasyan.netsnake.contexts.AnnouncementContext;
import nsu.manasyan.netsnake.contexts.ScoreContext;
import nsu.manasyan.netsnake.proto.SnakesProto;
import nsu.manasyan.netsnake.proto.SnakesProto.NodeRole;
import nsu.manasyan.netsnake.proto.SnakesProto.GameConfig;
import nsu.manasyan.netsnake.proto.SnakesProto.GameState;
import nsu.manasyan.netsnake.proto.SnakesProto.GameMessage.AnnouncementMsg;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientGameModel {

    public long getLastStateSeq() {
        return lastStateSeq;
    }

    public interface GameStateListener{
        void onUpdate(List<ScoreContext> scores);
    }

    public interface AnnouncementListener{
        void onUpdate(Map<AnnouncementMsg, AnnouncementContext> announcements);
    }

    public interface ConfigListener{
        void onUpdate(GameConfig config);
    }

    private int playerId;

    private long lastStateSeq = 0;

    private int masterId = 0;

    private volatile NodeRole playerRole = NodeRole.MASTER;

    private Map<Integer, ScoreContext> scores = new TreeMap<>();

    private Map<AnnouncementMsg, AnnouncementContext> availableGames = new HashMap<>();

    private GameConfig currentConfig;

    private GameState gameState;

    private volatile InetSocketAddress masterAddress;

    private SnakesProto.Direction currentDirection;

    private volatile InetSocketAddress deputyAddress;

    private int deputyId;

    private List<GameStateListener> gameStateListeners = new ArrayList<>();

    // incapsulate it in announcement wrapper
    private volatile List<AnnouncementListener> announcementListeners = new ArrayList<>();

    private String playerName;

    private List<ConfigListener> configListeners = new ArrayList<>();

    public ClientGameModel(){
        playerId = -1;
    }

    public String getPlayerName() {
        return playerName;
    }

    private List<FullPoints> fullPoints = new CopyOnWriteArrayList<>();

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public GameConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(GameConfig currentConfig) {
        this.currentConfig = currentConfig;
        notifyAllConfigListeners();
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        lastStateSeq = gameState.getStateOrder();
        updateScores(gameState.getPlayers().getPlayersList());
        notifyAllGameStateListeners();
    }

    private void updateScores(List<SnakesProto.GamePlayer> playersList) {
        scores.clear();
        playersList.forEach(p -> {
            if (p.getRole() != NodeRole.VIEWER) {
                addScore(p.getId(), p.getName(), p.getScore());
            }
        });
    }

    public List<FullPoints> getFullPoints() {
        return fullPoints;
    }

    public NodeRole getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(NodeRole playerRole) {
        this.playerRole = playerRole;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public InetSocketAddress getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }

    public void registerGameStateListener(GameStateListener listener){
        gameStateListeners.add(listener);
    }

    public void registerAnnouncementListener(AnnouncementListener announcementListener){
        announcementListeners.add(announcementListener);
    }

    public void registerConfigListener(ConfigListener listener){
        configListeners.add(listener);
    }

    public void notifyAllConfigListeners(){
        configListeners.forEach(l -> l.onUpdate(currentConfig));
    }

    public void notifyAllAnnouncementListeners(){
        announcementListeners.forEach(l -> l.onUpdate(availableGames));
    }

    public void notifyAllGameStateListeners(){
        List<ScoreContext> scoresList = new ArrayList<>(scores.values());

        scoresList.sort(Comparator.comparingInt(ScoreContext::getPoints).reversed());
        gameStateListeners.forEach(l -> l.onUpdate(scoresList));
    }

    public SnakesProto.Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(SnakesProto.Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Map<AnnouncementMsg, AnnouncementContext> getAvailableGames() {
        return availableGames;
    }

    public void addAvailableGame(AnnouncementMsg announcementMsg, AnnouncementContext context){
        availableGames.put(announcementMsg, context);
        notifyAllAnnouncementListeners();
    }

    public void addScore(int playerId, String playerName, int newPoints) {
        ScoreContext oldScore = scores.get(playerId);

        if (oldScore == null) {
            scores.put(playerId, new ScoreContext(playerName, newPoints));
        } else {
            oldScore.addPoints(newPoints);
        }
    }

    public int getDeputyId() {
        return deputyId;
    }

    public void setDeputyId(int deputyId) {
        this.deputyId = deputyId;
    }

    public void removeScore(int playerId){
        scores.remove(playerId);
    }

    public void clear(){
        playerId = -1;
        scores.clear();
        configListeners.clear();
        announcementListeners.clear();
    }

    public void clearGameStateListeners(){
        gameStateListeners.clear();
    }

    public InetSocketAddress getDeputyAddress() {
        return deputyAddress;
    }

    public void setDeputyAddress(InetSocketAddress deputyAddress) {
        this.deputyAddress = deputyAddress;
    }

    public int getMasterId() {
        return masterId;
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
    }
}