import java.util.ArrayList;

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

    // Starts up the OS.
    public static void Startup(PCB init)
    {
        kernel = new Kernel();

        // Initialize the first process and IdleProcess.
        createProcess(new PCB(new IdleProcess(), 0));

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
}