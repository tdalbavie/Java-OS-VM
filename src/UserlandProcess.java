import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class UserlandProcess implements Runnable
{
    // Thread to run the program.
    private Thread thread;
    // Semaphore for cooperation.
    private Semaphore semaphore;
    // Timer to stop process temporarily and pass it to something else.
    private Boolean quantumExpired;
    private final int pid;

    public UserlandProcess()
    {
        this.thread = new Thread(this);
        this.semaphore = new Semaphore(0);
        this.quantumExpired = false;
    }

    // Sets the boolean indicating that this process' quantum has expired.
    public void requestStop()
    {
        this.quantumExpired = true;
    }

    // Will represent the main of the program.
    public abstract void main();

    // Indicates if the semaphore is 0.
    public boolean isStopped()
    {
        return semaphore.availablePermits() == 0;
    }

    // True when the Java thread is not alive.
    public boolean isDone()
    {
        return !thread.isAlive();
    }

    // Releases (increments) the semaphore, allowing this thread to run.
    public void start()
    {
        semaphore.release();
        if (!thread.isAlive())
        {
            thread.start();
        }
    }

    // Acquires (decrements) the semaphore, stopping this thread from running.
    public void stop()
    {
        try
        {
            semaphore.acquire();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    // Acquires the semaphore, then call main.
    public void run()
    {
        try
        {
            semaphore.acquire();
            main();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    // If the boolean is true, set the boolean to false and call OS.switchProcess().
    public void cooperate()
    {
        if (quantumExpired)
        {
            quantumExpired = false;
            OS.switchProcess();
        }
    }
}
