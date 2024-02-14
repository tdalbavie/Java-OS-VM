import java.time.Clock;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable
{
    private final Thread thread;
    private final Semaphore semaphore;
    private final Scheduler scheduler;
    private final Clock clock;

    // Kernel constructor.
    public Kernel()
    {
        this.semaphore = new Semaphore(0);
        this.thread = new Thread(this);
        this.scheduler = new Scheduler();
        this.clock = Clock.systemUTC();
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
                if (OS.getCurrentCall() != CallType.NoProcess)
                {
                    // Checks what call OS made when starting to either switch or create process.
                    switch (OS.getCurrentCall())
                    {
                        case CreateProcess:
                            createProcess();
                            break;
                        case SwitchProcess:
                            // Iterates over sleeping processes to check if any need to be brought back.
                            int i = 0;
                            while (i < scheduler.getSleepingProcesses().size())
                            {
                                PCB process = scheduler.getSleepingProcesses().get(i);
                                long currentTime = clock.instant().toEpochMilli();

                                // Checks if it is time to wake up process.
                                if (currentTime >= process.getTimeToWake())
                                {
                                    // Checks what priority the process was and adds it back in.
                                    if (process.getPriority() == 0)
                                    {
                                        scheduler.getSleepingProcesses().remove(process);
                                        scheduler.getRealTimeProcesses().addLast(process);
                                    }
                                    else if (process.getPriority() == 1)
                                    {
                                        scheduler.getSleepingProcesses().remove(process);
                                        scheduler.getInteractiveProcesses().addLast(process);
                                    }
                                    else if (process.getPriority() == 2)
                                    {
                                        scheduler.getSleepingProcesses().remove(process);
                                        scheduler.getBackgroundProcesses().addLast(process);
                                    }
                                    // Break out of loop.
                                    break;
                                }
                                // Increments counter to check next process.
                                else
                                {
                                    i++;
                                }
                            }
                            switchProcess();
                            break;
                        default:
                            break;
                    }
                }

                // Starts the next process.
                if (scheduler.currentProcess != null)
                {
                    scheduler.currentProcess.start();
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Passes the next process to scheduler to add it to rotation.
    private void createProcess()
    {
        int PID = scheduler.createProcess((PCB) OS.getFunctionParameters().getFirst());
        OS.setReturnValue(PID);
    }

    // Tells scheduler to switch to the next process in line.
    private void switchProcess()
    {
        scheduler.switchProcess();
    }

    // Starts up kernel run block again.
    public void start()
    {
        semaphore.release();
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void Sleep(long milliseconds)
    {
        scheduler.Sleep(milliseconds);
    }
}
