import java.util.concurrent.Semaphore;

public class Kernel implements Runnable
{
    private Thread thread;
    private Semaphore semaphore;
    private Scheduler scheduler;

    // Kernel constructor.
    public Kernel()
    {
        this.semaphore = new Semaphore(0);
        this.thread = new Thread(this);
        this.scheduler = new Scheduler();
        this.thread.start();
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // Checks if the kernel should be running.
                semaphore.acquire();

                switch (OS.getCurrentCall())
                {
                    case CreateProcess:
                        scheduler.CreateProcess((UserlandProcess) OS.getFunctionParameters().getFirst());
                        break;
                    case SwitchProcess:
                        scheduler.SwitchProcess();
                        break;
                }

                // Switch to next process
                scheduler.SwitchProcess();
                if (scheduler.currentProcess != null)
                    scheduler.currentProcess.run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void start()
    {
        semaphore.release();
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }
}
