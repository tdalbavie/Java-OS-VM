import java.util.ArrayList;
import java.util.Objects;

public class OS
{
    // Holds a reference to the one and only kernel instance.
    private static Kernel kernel;
    // Holds the kernel function call the OS currently wants.
    private static CallType currentCall;
    // Holds the parameters for an unknown function.
    private static ArrayList<Object> functionParameters;
    // Holds the return value from the function that was run.
    private static Object returnValue;

    // Starts up the OS.
    public static void Startup(UserlandProcess init)
    {
        kernel = new Kernel();

        createProcess(init);
        createProcess(new IdleProcess());
    }

    public static int createProcess(UserlandProcess up)
    {
        functionParameters = new ArrayList<>();
        functionParameters.add(up);
        currentCall = CallType.CreateProcess;

        // Signal to kernel to switch.
        kernel.start();

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

        return (int) returnValue;
    }

    public static void switchProcess()
    {
        currentCall = CallType.SwitchProcess;

        // Signal to kernel to switch.
        kernel.start();

        UserlandProcess currentProcess = kernel.getScheduler().currentProcess;
        if (currentProcess != null)
            currentProcess.stop();
    }

    public static CallType getCurrentCall()
    {
        return currentCall;
    }

    public static ArrayList<Object> getFunctionParameters()
    {
        return functionParameters;
    }
}