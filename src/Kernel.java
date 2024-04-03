import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device
{
    private final Thread thread;
    private final Semaphore semaphore;
    private final Scheduler scheduler;
    private final Clock clock;
    private VFS vfs;
    private final HashMap<Integer, PCB> processMap;
    // This queue will hold on to messages if the receiving process has not yet been created.
    private final HashMap<Integer, KernelMessage> messageQueue;
    // Memory divided into 1024 1KB pages.
    private static final boolean[] memoryPages = new boolean[1024];

    // Kernel constructor.
    public Kernel()
    {
        this.semaphore = new Semaphore(0);
        this.thread = new Thread(this);
        this.scheduler = new Scheduler();
        this.clock = Clock.systemUTC();
        this.thread.start();
        this.vfs = new VFS();
        this.processMap = new HashMap<>();
        this.messageQueue = new HashMap<>();
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
        PCB newProcess = (PCB) OS.getFunctionParameters().getFirst();
        int PID = scheduler.createProcess(newProcess);
        OS.setReturnValue(PID);
        // Adds the new process to the map of processes.
        addProcessToMap(newProcess);
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
        // Ensures input includes a unique identifier for each device.
        PCB currentProcess = scheduler.getCurrentProcess();

        // Makes sure there is a current process.
        if (currentProcess == null)
        {
            return -1;
        }

        int deviceId = vfs.open(input);

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
            vfs.close(deviceId);
            currentProcess.getDeviceIds()[index] = -1;
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

    public int GetPid()
    {
        return scheduler.getPid();
    }

    public int GetPidByName(String name)
    {
        return scheduler.getPidByName(name);
    }

    // Adds process to map when new process is created.
    public void addProcessToMap(PCB pcb)
    {
        processMap.put(pcb.getPid(), pcb);
    }

    // Removes process from map if it gets deleted.
    public void removeProcessFromMap(int pid)
    {
        processMap.remove(pid);
    }

    // Gets a process using a pid number.
    public PCB getProcessByPid(int pid)
    {
        return processMap.get(pid);
    }

    // Sends a message to a process.
    public void sendMessage(KernelMessage km)
    {
        // Makes a deep copy of the message.
        KernelMessage copyMessage = new KernelMessage(km);

        // Gets the target process by searching for its Pid.
        PCB targetProcess = getProcessByPid(km.getTargetPid());

        // Makes sure target exists.
        if (targetProcess != null)
        {
            targetProcess.enqueueMessage(copyMessage);
            // When the target is found, puts it back into rotation.
            scheduler.removeFromWaiting(targetProcess.getPid());
        }
        // Will put the message in a queue for when the targetProcess has not been created yet.
        else
        {
            messageQueue.put(copyMessage.getTargetPid(), copyMessage);
        }
    }

    // Makes a process wait until a message is sent to it.
    public KernelMessage waitForMessage()
    {
        PCB currentPCB = scheduler.getCurrentProcess();
        if (currentPCB == null)
            return null;

        // In case there is a message queued when the process has not been created yet.
        if (!messageQueue.isEmpty())
        {
            if (messageQueue.containsKey(currentPCB.getPid()))
            {
                // Returns the message held by the kernel once it is created and ready to receive.
                return messageQueue.remove(currentPCB.getPid());
            }
        }

        // Gets and removes the message that is being sent to the process (If there is one).
        KernelMessage message = currentPCB.dequeueMessage();
        if (message != null)
        {
            return message;
        }
        // If no message is already waiting for it, it will wait for a message.
        else
        {
            // Adds process to the waiting queue and stops it.
            scheduler.addToWaiting();

            // Waits for a message to be received.
            synchronized (currentPCB)
            {
                while ((message = currentPCB.dequeueMessage()) == null);

            }
            return message;
        }
    }

    public int AllocateMemory(int size)
    {
        // Size must be a multiple of 1024
        if (size % 1024 != 0)
            return -1;

        int numPages = size / 1024;
        for (int i = 0; i <= memoryPages.length - numPages; i++)
        {
            // Checks for a contiguous block of free pages.
            boolean suitable = true;
            for (int j = 0; j < numPages; j++)
            {
                if (memoryPages[i + j])
                {
                    suitable = false;
                    // When this block is not suitable, move to the next.
                    break;
                }
            }

            // If a suitable block is found, mark it as used and return the start address.
            if (suitable)
            {
                for (int j = 0; j < numPages; j++)
                {
                    // Marks the page as used.
                    memoryPages[i + j] = true;
                }
                // Returns the virtual start address.
                return i * 1024;
            }
        }
        // If no suitable block found.
        return -1;
    }

    public boolean FreeMemory(int pointer, int size)
    {
        // Pointer and size must align with page boundaries.
        if (pointer % 1024 != 0 || size % 1024 != 0)
            return false;

        int startPage = pointer / 1024;
        int numPages = size / 1024;

        // Verifies the request doesn't exceed memory bounds.
        if (startPage + numPages > memoryPages.length) return false;

        // Marks the specified pages as free.
        for (int i = startPage; i < startPage + numPages; i++)
        {
            memoryPages[i] = false;
        }

        return true;
    }
}
