import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class UserlandProcess implements Runnable
{
    // Thread to run the program.
    private final Thread thread;
    // Semaphore for cooperation.
    private final Semaphore semaphore;
    // Timer to stop process temporarily and pass it to something else.
    private Boolean quantumExpired;
    // Simulated TLB: [Virtual Page][Physical Page].
    private static final int[][] TLB = new int[2][2];
    // Holds 1MB physical memory.
    private static final byte[] physicalMemory = new byte[1024 * 1024];


    // Initializes thread, semaphore, and quantum.
    public UserlandProcess()
    {
        this.thread = new Thread(this);
        this.semaphore = new Semaphore(1);
        // Fixes issue of processes not switching.
        try
        {
            semaphore.acquire();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
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
        //System.out.println("Start: " + semaphore.availablePermits());
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
            //System.out.println("Stop: " + semaphore.availablePermits());
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

    public byte ReadMemory(int address)
    {
        int virtualPage = address / 1024;
        int pageOffset = address % 1024;
        int physicalPage = -1;

        // Check TLB for mapping
        for (int i = 0; i < TLB.length; i++)
        {
            if (TLB[i][0] == virtualPage)
            {
                physicalPage = TLB[i][1];
                break;
            }
        }

        // Handles TLB miss.
        if (physicalPage == -1)
        {
            // Call to OS to handle TLB miss and mapping update.
            OS.GetMapping(virtualPage);
            // Retries finding the mapping in TLB after OS updates it.
            for (int i = 0; i < TLB.length; i++)
            {
                if (TLB[i][0] == virtualPage)
                {
                    physicalPage = TLB[i][1];
                    break;
                }
            }
        }

        int physicalAddress = physicalPage * 1024 + pageOffset;
        // Returns value at physical address.
        return physicalMemory[physicalAddress];
    }

    public void WriteMemory(int address, byte value)
    {
        int virtualPage = address / 1024;
        int pageOffset = address % 1024;
        int physicalPage = -1;

        // Check TLB for mapping
        for (int i = 0; i < TLB.length; i++)
        {
            if (TLB[i][0] == virtualPage)
            {
                physicalPage = TLB[i][1];
                break;
            }
        }

        // Handles TLB miss.
        if (physicalPage == -1)
        {
            // Call to OS to handle TLB miss and mapping update.
            OS.GetMapping(virtualPage);
            // Retries finding the mapping in TLB after OS updates it.
            for (int i = 0; i < TLB.length; i++)
            {
                if (TLB[i][0] == virtualPage)
                {
                    physicalPage = TLB[i][1];
                    break;
                }
            }
        }

        int physicalAddress = physicalPage * 1024 + pageOffset;
        // Writes value to physical address.
        physicalMemory[physicalAddress] = value;
    }

    public static void updateTLB(int virtualPage, int physicalPage)
    {
        // Simplified TLB update mechanism. In real systems, this might involve replacement policies.
        TLB[0][0] = virtualPage; // Overwrite the first entry as a simple approach
        TLB[0][1] = physicalPage;
    }

    // Clears the TLB (this is called on process switch).
    public static void clearTLB()
    {
        for (int i = 0; i < TLB.length; i++)
        {
            TLB[i][0] = -1; // Invalidate the entry
            TLB[i][1] = -1; // Invalidate the entry
        }
    }
}
