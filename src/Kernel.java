import java.time.Clock;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device
{
    private final Thread thread;
    private final Semaphore semaphore;
    private final Scheduler scheduler;
    private final Clock clock;
    private VFS vfs;

    // Kernel constructor.
    public Kernel()
    {
        this.semaphore = new Semaphore(0);
        this.thread = new Thread(this);
        this.scheduler = new Scheduler();
        this.clock = Clock.systemUTC();
        this.thread.start();
        this.vfs = new VFS();
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

    @Override
    public int open(String input)
    {
        // Ensure input includes a unique identifier for each device
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure there is a current process.
        if (currentProcess == null)
        {
            return -1;
        }

        int deviceId = vfs.open(input); // Open device in VFS

        if (deviceId != -1)
        {
            // Stores the device ID in the PCB.
            for (int i = 0; i < currentProcess.getDeviceIds().length; i++)
            {
                if (currentProcess.getDeviceIds()[i] == -1)
                {
                    currentProcess.getDeviceIds()[i] = deviceId;
                    break;
                }
            }
        }

        return deviceId;
    }

    @Override
    public void close(int index)
    {
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure process exists and index is not out of bounds.
        if (currentProcess == null || index < 0 || index >= currentProcess.getDeviceIds().length)
        {
            return;
        }

        // Gets the device at the specified index.
        int deviceId = currentProcess.getDeviceIds()[index];

        if (deviceId != -1)
        {
            vfs.close(deviceId); // Assuming vfs.close() method exists
            currentProcess.getDeviceIds()[index] = -1; // Mark as closed
        }
    }

    @Override
    public byte[] read(int index, int count)
    {
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure process exists and index is not out of bounds.
        if (currentProcess == null || index < 0 || index >= currentProcess.getDeviceIds().length)
        {
            return new byte[0];
        }

        // Gets the device at the specified index.
        int deviceId = currentProcess.getDeviceIds()[index];

        if (deviceId != -1)
        {
            return vfs.read(deviceId, count);
        }

        // Returns 0 if deviceId is not valid.
        return new byte[0];
    }

    @Override
    public int write(int index, byte[] data)
    {
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure process exists and index is not out of bounds.
        if (currentProcess == null || index < 0 || index >= currentProcess.getDeviceIds().length)
        {
            return 0;
        }

        // Gets the device at the specified index.
        int deviceId = currentProcess.getDeviceIds()[index];

        // Writes to device.
        if (deviceId != -1)
        {
            return vfs.write(deviceId, data);
        }

        // Returns 0 if deviceId is not valid.
        return 0;
    }

    @Override
    public void seek(int index, int count)
    {
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure process exists and index is not out of bounds.
        if (currentProcess == null || index < 0 || index >= currentProcess.getDeviceIds().length)
        {
            return;
        }

        // Gets the device at the specified index.
        int deviceId = currentProcess.getDeviceIds()[index];

        // Seeks for the device.
        if (deviceId != -1)
        {
            vfs.seek(deviceId, count);
        }
    }
}
