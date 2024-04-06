import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class OS
{
    // Holds a reference to the one and only kernel instance.
    private static Kernel kernel;
    // Holds the kernel function call the OS currently wants.
    private static CallType currentCall = CallType.NoProcess;
    // Holds the parameters for an unknown function.
    private static ArrayList<Object> functionParameters;
    // Holds the return value from the function that was run.
    private static Object returnValue;
    // Tracks the swap file device ID.
    private static int swapFileDeviceId = -1;
    // Tracks the next page index in the swap file.
    private static int nextPageIndex = 0;
    // Variable used for page size.
    private static final int PAGE_SIZE = 1024;

    // Starts up the OS.
    public static void Startup(PCB init)
    {
        kernel = new Kernel();

        // Initialize the first process and IdleProcess.
        createProcess(new PCB(new IdleProcess(), 0));

        // Opens the swap file and stores its device ID for later use.
        swapFileDeviceId = kernel.open("file swapfile.txt");
        if (swapFileDeviceId == -1)
        {
            throw new RuntimeException("Failed to open swap file.");
        }

        createProcess(init);
    }

    // Initializes a new UserlandProcess for the scheduler to be aware of.
    public static int createProcess(PCB up)
    {
        functionParameters = new ArrayList<>();
        functionParameters.add(up);
        currentCall = CallType.CreateProcess;

        // Gets the current process that needs to be stopped in case kernel gets ahead.
        PCB currentProcess = kernel.getScheduler().currentProcess;

        // Signal to kernel to switch.
        kernel.start();

        // Stops current process and waits in the case of the first initialization.
        stopAndWait(currentProcess);

        return (int) returnValue;
    }

    // Switches the currently running UserlandProcess.
    public static void switchProcess()
    {
        functionParameters = new ArrayList<>();
        currentCall = CallType.SwitchProcess;

        // Gets the current process that needs to be stopped in case kernel gets ahead.
        PCB currentProcess = kernel.getScheduler().currentProcess;

        // Signal to kernel to switch.
        kernel.start();

        // Stops current process, doesn't wait as processes are already initialized.
        stopAndWait(currentProcess);
        // Clears the TLB when process is switched.
        UserlandProcess.clearTLB();
    }

    // Helper method to stop current process and wait in the case of initialization.
    private static void stopAndWait(PCB processToStop)
    {
        // Checks for a currently running process and stops it.
        if (processToStop != null)
        {
            processToStop.stop();

            // Increments the counter.
            processToStop.incrementTimeoutCounter();

            // If current process reached maximum timeouts of 5, it gets demoted.
            if (processToStop.getTimeoutCounter() == 5)
            {
                int priority = processToStop.getPriority();
                // Demotes process next time it gets put back into list as long as it is not already a background process.
                if (priority < 2)
                {
                    /*
                    // Print statements to show which process level is getting demoted
                    if (priority == 0)
                        System.out.println("Demoting realtime to interactive");
                    else if (priority == 1)
                        System.out.println("Demoting interactive to background");
                    */
                    processToStop.setPriority(priority + 1);
                }

                // Sets counter back to 0
                processToStop.setTimeoutCounter(0);
            }
        }

        // In case no process is running (mainly for init).
        while (kernel.getScheduler().currentProcess == null)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static CallType getCurrentCall()
    {
        return currentCall;
    }

    public static ArrayList<Object> getFunctionParameters()
    {
        return functionParameters;
    }

    // Sets returnValue to the PID value from scheduler (this is temporary until a more permanent solution is built later).
    public static void setReturnValue(int PID)
    {
        returnValue = PID;
    }

    // Calls sleep in kernel to put process to sleep.
    public static void Sleep(long milliseconds)
    {
        kernel.Sleep(milliseconds);
    }

    public static int open(String input)
    {
        return kernel.open(input);
    }

    public static void close(int index)
    {
        kernel.close(index);
    }

    public static byte[] read(int index, int count)
    {
        return kernel.read(index, count);
    }

    public static int write(int index, byte[] data)
    {
        return kernel.write(index, data);
    }

    public static void seek(int index, int count)
    {
        kernel.seek(index, count);
    }

    // Gets the currently running process' Pid.
    public static int GetPid()
    {
        return kernel.GetPid();
    }

    // Searches for an existing process using its name.
    public static int GetPidByName(String name)
    {
        return kernel.GetPidByName(name);
    }

    // Sends a message to a process through the kernel.
    public static void sendMessage(KernelMessage km)
    {
        kernel.sendMessage(km);
    }

    // Makes a process wait until a message is sent to it through the kernel.
    public static KernelMessage waitForMessage()
    {
        return kernel.waitForMessage();
    }

    public static void GetMapping(int virtualPageNumber)
    {
        PCB pcb = kernel.getScheduler().getCurrentProcess();
        VirtualToPhysicalMapping mapping = pcb.getPageTable()[virtualPageNumber];

        if (mapping.physicalPageNumber == -1)
        {
            int newPhysicalPage = findFreePhysicalPage();
            if (newPhysicalPage == -1)
            {
                newPhysicalPage = evictPageAndSwap();
            }

            if (mapping.diskPageNumber != -1)
            {
                readPageFromSwapFile(virtualPageNumber, mapping.diskPageNumber, newPhysicalPage);
            }
            else
            {
                // Initializes physical memory with zeros.
                byte[] emptyData = new byte[PAGE_SIZE];
                Arrays.fill(emptyData, (byte) 0);
                UserlandProcess.writePhysicalMemory(newPhysicalPage, emptyData);
            }

            mapping.physicalPageNumber = newPhysicalPage;
            UserlandProcess.updateTLB(virtualPageNumber, newPhysicalPage);
        }
        else
        {
            UserlandProcess.updateTLB(virtualPageNumber, mapping.physicalPageNumber);
        }
    }

    private static int evictPageAndSwap()
    {
        PCB victimProcess = kernel.getScheduler().getRandomProcess();
        while (victimProcess != null)
        {
            for (VirtualToPhysicalMapping victimMapping : victimProcess.getPageTable())
            {
                if (victimMapping != null && victimMapping.physicalPageNumber != -1)
                {
                    // Looks for a page to evict.
                    if (victimMapping.diskPageNumber == -1)
                    {
                        victimMapping.diskPageNumber = nextPageIndex++;
                    }
                    // Writes victim page to disk.
                    byte[] pageData = UserlandProcess.readPhysicalMemory(victimMapping.physicalPageNumber);
                    writePageToSwapFile(victimMapping.diskPageNumber, pageData);

                    // Updates mapping for the evicted page.
                    int freedPhysicalPage = victimMapping.physicalPageNumber;
                    victimMapping.physicalPageNumber = -1;
                    return freedPhysicalPage;
                }
            }
            // Tries another process if no physical page was found in the current one.
            victimProcess = kernel.getScheduler().getRandomProcess();
        }
        // Should not happen if physical memory management is correct.
        return -1;
    }

    public static void writePageToSwapFile(int diskPageNumber, byte[] data)
    {
        int offset = diskPageNumber * PAGE_SIZE;
        OS.seek(swapFileDeviceId, offset);
        OS.write(swapFileDeviceId, data);
    }


    private static int findFreePhysicalPage()
    {
        for (int i = 0; i < Kernel.getMemoryPages().length; i++)
        {
            // Checks if a free page is found.
            if (!Kernel.getMemoryPages()[i])
            {
                // Marks page as used.
                Kernel.getMemoryPages()[i] = true;
                return i;
            }
        }
        // When no pages are free.
        return -1;
    }

    // Reads a page from the swap file into a physical page in memory.
    public static void readPageFromSwapFile(int virtualPageNumber, int diskPageNumber, int physicalPageNumber) {
        byte[] data;
        // Calculates offset in the swap file.
        int offset = diskPageNumber * PAGE_SIZE;

        // Performs seek and read operations on the swap file.
        OS.seek(swapFileDeviceId, offset);
        // Returns the byte array read from the swap file.
        data = OS.read(swapFileDeviceId, PAGE_SIZE);

        // Writes the data to physical memory.
        UserlandProcess.writePhysicalMemory(physicalPageNumber, data);

        // Updates the PCB's page table with the new mapping.
        PCB currentPCB = kernel.getScheduler().getCurrentProcess();
        if (currentPCB != null)
        {
            // Uses the virtualPageNumber directly as it's known in the context of handling the page fault.
            currentPCB.updatePageTable(virtualPageNumber, physicalPageNumber, -1);
        }
    }

    // Calls Kernel AllocateMemory.
    public static int AllocateMemory(int size)
    {
        return kernel.AllocateMemory(size);
    }

    public static int AllocateMemory(PCB pcb, int size)
    {
        return kernel.AllocateMemory(pcb, size);
    }

   // Calls Kernel FreeMemory.
    public static boolean FreeMemory(int pointer, int size)
    {
        return kernel.FreeMemory(pointer, size);
    }

    public static boolean FreeMemory(PCB pcb, int virtualStartAddress, int size)
    {
        return kernel.FreeMemory(pcb, virtualStartAddress, size);
    }
}
