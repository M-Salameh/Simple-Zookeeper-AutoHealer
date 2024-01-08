import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class WorkerApplication
{
    public static void main(String[] args)
    {
        Worker worker = new Worker();
        try {
            worker.connectToZookeeper();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try
        {
            worker.work();
        }
        catch (Exception e) {
           return;
        }
    }
}
