public class Main
{
    public static void main(String[] args)
    {
        // Starts the Ping process.
        OS.Startup(new PCB(new Ping()));
        // Creates the Pong process.
        OS.createProcess(new PCB(new Pong()));

        /* This was used to demonstrate reading and writing to files and usage of devices.
        // Creates a new Process to write to file.
        OS.Startup(new PCB(new WriteToFile()));
        // Adds a second process to then read from the file created.
        OS.createProcess(new PCB(new ReadFromFile()));
         */

        /* This was used to demonstrate demotion.
        // Start's the OS starting with Real Time Process.
        OS.Startup(new PCB(new RealTimeProcess(), 0));
        // Creates another process for Interactive process.
        OS.createProcess(new PCB(new InteractiveProcess(),1));
        // Creates another process for Background process.
        OS.createProcess(new PCB(new BackgroundProcess(),2));
        // Creates another process for Real Time Process but with sleep, this process will not get demoted.
        OS.createProcess((new PCB((new RealTimeSleepProcess()), 0)));
         */
    }
}
