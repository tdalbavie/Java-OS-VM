public class Main
{
    public static void main(String[] args)
    {
        // Start's the OS starting with Real Time Process.
        OS.Startup(new PCB(new RealTimeProcess(), 0));
        // Creates another process for Interactive process.
        OS.createProcess(new PCB(new InteractiveProcess(),1));
        // Creates another process for Background process.
        OS.createProcess(new PCB(new BackgroundProcess(),2));
        // Creates another process for Real Time Process but with sleep.
        OS.createProcess((new PCB((new RealTimeSleepProcess()), 0)));
    }
}
