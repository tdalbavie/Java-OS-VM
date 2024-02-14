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
        createProcess(init);
        // createProcess(new PCB(new IdleProcess()));

        // Signal to kernel to switch.
        //kernel.start();

        // Stops current process and waits for first initialization.
        //stopAndWait();
    }

    // Initializes a new UserlandProcess for the scheduler to be aware of.
    public static int createProcess(PCB up)
    {
        functionParameters = new ArrayList<>();
        functionParameters.add(up);
        currentCall = CallType.CreateProcess;

        // Signal to kernel to switch.
        kernel.start();

        // Stops current process and waits in the case of the first initialization.
        stopAndWait();

        return (int) returnValue;
    }

    // Switches the currently running UserlandProcess.
    public static void switchProcess()
    {
        functionParameters = new ArrayList<>();
        currentCall = CallType.SwitchProcess;

        // Signal to kernel to switch.
        kernel.start();

        // Stops current process, doesn't wait as processes are already initialized.
        stopAndWait();
    }

    // Helper method to stop current process and wait in the case of initialization.
    private static void stopAndWait()
    {
        // Checks for a currently running process and stops it.
        if (kernel.getScheduler().currentProcess != null)
            kernel.getScheduler().currentProcess.stop();

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
}