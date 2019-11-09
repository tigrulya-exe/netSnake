package nsu.manasyan.netsnake.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GameExecutorService {
    private GameExecutorService(){}

    private static class SingletonHelper{
        private static final ExecutorService executorService = Executors.newCachedThreadPool();
    }

    public static ExecutorService getExecutorService() {
        return SingletonHelper.executorService;
    }
}
