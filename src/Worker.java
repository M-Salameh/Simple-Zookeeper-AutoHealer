
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class Worker
{
    private static final String ZOOKEEPER_ADDRESS = "192.168.184.10:2181";
    private static final int SESSION_TIMEOUT = 3000;

    // Parent Znode where each worker stores an ephemeral child to indicate it is alive
    private static final String AUTOHEALER_ZNODES_PATH = "/workers";

    private static final float CHANCE_TO_FAIL = 0.001F;

    private final Random random = new Random();
    private ZooKeeper zooKeeper;

    private String myName = "";

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, event -> {
        });
    }

    public void work() throws KeeperException, InterruptedException {
        addChildZnode();
        while (true)
        {
            System.out.println(myName + " is Working...");
            LockSupport.parkNanos(1000L);
            if (random.nextFloat() < CHANCE_TO_FAIL) {
                System.out.println(myName + " encountered Critical error happened");
                throw new RuntimeException(myName + " : Oops");
            }
        }
    }

    private void addChildZnode() throws KeeperException, InterruptedException {
        myName= zooKeeper.create(AUTOHEALER_ZNODES_PATH + "/worker_",
                new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }
}

