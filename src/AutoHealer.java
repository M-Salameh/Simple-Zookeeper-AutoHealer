import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.*;

public class AutoHealer implements Watcher
{
        private static final String ZOOKEEPER_ADDRESS = "192.168.184.10:2181";
        private static final int SESSION_TIMEOUT = 3000;

        // Parent Znode where each worker stores an ephemeral child to indicate it is alive
        private static final String AUTOHEALER_ZNODES_PATH = "/workers";

        // Path to the worker jar
        private final String pathToProgram;

        // The number of worker instances we need to maintain at all times
        private final int numberOfWorkers;
        private ZooKeeper zooKeeper;


        public AutoHealer(int numberOfWorkers, String pathToProgram) {
            this.numberOfWorkers = numberOfWorkers;
            this.pathToProgram = pathToProgram;
        }

        public void startWatchingWorkers() throws KeeperException, InterruptedException {
            if (zooKeeper.exists(AUTOHEALER_ZNODES_PATH, this) == null) {
                zooKeeper.create(AUTOHEALER_ZNODES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            launchWorkersIfNecessary();
        }

        public void connectToZookeeper() throws IOException {
            this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        }

        public void run() throws InterruptedException {
            synchronized (zooKeeper) {
                zooKeeper.wait();
            }
        }

        public void close() throws InterruptedException {
            zooKeeper.close();
        }

        @Override
        public void process(WatchedEvent event) {
            switch (event.getType())
            {
                case None: {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        System.out.println("Successfully connected to Zookeeper");
                    } else {
                        synchronized (zooKeeper) {
                            System.out.println("Disconnected from Zookeeper event");
                            zooKeeper.notifyAll();
                        }
                    }
                    break;
                }


                case NodeChildrenChanged:{
                    if (event.getPath().equals(AUTOHEALER_ZNODES_PATH))
                    {
                        try
                        {
                            launchWorkersIfNecessary();
                        }
                        catch (KeeperException | InterruptedException e) {
                            System.out.println("Failed to launch workers: " + e.getMessage());
                        }
                    }
                    break;
                }

            }
        }


        private void launchWorkersIfNecessary() throws KeeperException, InterruptedException
        {
            List<String> workerZnodes = zooKeeper.getChildren(AUTOHEALER_ZNODES_PATH, this);
            int existingWorkersNumber = workerZnodes.size();
            if (existingWorkersNumber > 0)
            {
                System.out.println("Last Child Znode Now is : " + workerZnodes.get(existingWorkersNumber -1));
            }
            /*while (existingWorkersNumber.get() < numberOfWorkers )
            {
                Thread thread = new Thread(()->
                {
                    try {
                        startNewWorker();
                        existingWorkersNumber.getAndIncrement();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.start();
            }*/
            while (existingWorkersNumber < numberOfWorkers )
            {
                try
                {
                    System.out.println("Increasing Workers !!");
                    startNewWorker();
                    existingWorkersNumber++;
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                }
            }
            while (existingWorkersNumber > numberOfWorkers)
            {
                String worker = workerZnodes.get(0);
                String workerPath = AUTOHEALER_ZNODES_PATH + "/" + worker;
                System.out.println("delete node: " + AUTOHEALER_ZNODES_PATH + "/" + worker);
                zooKeeper.delete(workerPath, -1);
                existingWorkersNumber--;
                workerZnodes.remove(0);
            }
        }

        /**
         * Helper method to start a single worker
         * @throws IOException
         */
        private void startNewWorker() throws IOException
        {
            File file = new File(pathToProgram);
            String command = "java -Dorg.slf4j.simpleLogger.defaultLogLevel=off -jar " + file.getName();
            System.out.println(String.format("Launching worker instance : %s ", command));
            Runtime.getRuntime().exec(command, null, file.getParentFile());
        }
    }
