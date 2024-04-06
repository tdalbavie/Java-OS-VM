public class Main
{
    public static void main(String[] args)
    {
        /* Note: these processes are one time runs, we have not implemented a way to kill processes.
        * for the time being I will "kill" processes with a very long sleep. The read and write test
        * will be done with two processes that loop 100 times each to show multiple processes reading
        * and writing with different values and still passing the test. */
        // Creates a new process to read and write from memory.
        OS.Startup(setPCB(new VirtualMemoryReadWrite()));
        OS.createProcess(setPCB(new VirtualMemoryReadWriteCont()));
        // Adds 20 new processes to test over allocation of memory, these processes will be run once.
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));
        OS.createProcess(setPCB(new VirtualMemoryAllocation()));




        /*
        // This was used to demonstrate paging.
        * Note: these processes are one time runs, we have not implemented a way to kill processes.
        * for the time being I will "kill" processes with a very long sleep.*
        // Creates a new process to read and write from memory.
        OS.Startup(new PCB(new MemoryReadWrite()));
        // Adds a new process to extend memory.
        OS.createProcess(new PCB(new MemoryExtension()));
        // Adds a new process to access memory that shouldn't be able to be accessed.
        OS.createProcess(new PCB(new UnauthorizedMemoryAccess()));
        // Adds a new process to test memory fragmentation.
        OS.createProcess(new PCB(new MemoryFragmentation()));
        // Adds a new process to test memory boundaries
        OS.createProcess(new PCB(new MemoryBoundary()));
        */

        /*
        // This was used to demonstrate messaging between processes.
        // Starts the Ping process.
        OS.Startup(new PCB(new Ping()));
        // Creates the Pong process.
        OS.createProcess(new PCB(new Pong()));
        */

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

    private static PCB setPCB(UserlandProcess up)
    {
        PCB pcb = new PCB(up);
        // Sets the PCB in UserlandProcess for use by the UserlandProcess.
        up.setPCB(pcb);

        return pcb;
    }
}

// Add to github and note that this program uses multithreading.