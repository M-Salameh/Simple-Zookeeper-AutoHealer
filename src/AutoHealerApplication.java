import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class AutoHealerApplication
{
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException
    {
        if (args.length != 2) {
            System.out.println("Expecting parameters <number of workers> <path to worker jar file>");
            System.exit(1);
        }
        int numberOfWorkers = Integer.parseInt(args[0]);
        String pathToWorkerProgram = args[1];
        AutoHealer autohealer = new AutoHealer(numberOfWorkers, pathToWorkerProgram);
        //AutoHealer autohealer = new AutoHealer(10, ".\\Worker.jar");
        autohealer.connectToZookeeper();
        autohealer.startWatchingWorkers();
        autohealer.run();
        autohealer.close();
    }
}
