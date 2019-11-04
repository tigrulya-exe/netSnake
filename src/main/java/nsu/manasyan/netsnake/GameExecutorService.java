package nsu.manasyan.netsnake;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameExecutorService {
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
