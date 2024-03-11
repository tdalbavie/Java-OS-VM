import java.time.Clock;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler
{
    // Holds a list of processes known to the scheduler.
    private final LinkedList<PCB> realTimeProcesses;
    private final LinkedList<PCB> interactiveProcesses;
    private final LinkedList<PCB> backgroundProcesses;
    private final LinkedList<PCB> sleepingProcesses;
    private final LinkedList<PCB> waitingForMessage;
    private final Timer timer;
    // Used to get the current time for sleep method.
    private final Clock clock;
    private static Random rand;
    private boolean goingToSleep;
    private boolean goingToWait;
    // Holds the currently running process.
    public PCB currentProcess;
    final Object lock = new Object();

    // Runs a timer for each process switch interrupt.
    public Scheduler()
    {
        // Initializes each new process.
        realTimeProcesses = new LinkedList<>();
        interactiveProcesses = new LinkedList<>();
        backgroundProcesses = new LinkedList<>();
        sleepingProcesses = new LinkedList<>();
        waitingForMessage = new LinkedList<>();
        goingToSleep = false;
        goingToWait = false;
        // Initializes the randomizer.
        rand = new Random();
        // Initializes the clock.
        clock = Clock.systemUTC();

        timer = new Timer();

        // Runs an interrupt on a timer for each switch in process that needs to happen.
        TimerTask interruption = new TimerTask()
        {
            @Override
            public void run()
            {
                if (currentProcess != null)
                {
                    // Sets quantum to true to tell the current process to yield and switch.
                    currentProcess.getProcess().requestStop();
                }
            };
        };
        timer.scheduleAtFixedRate(interruption, 0, 250);
    }

    // Initializes each new UserlandProcess that needs to be scheduled.
    public int createProcess(PCB up)
    {
        // Adds the new process to the specified priority list (default is realtime).
        if (up.getPriority() == 0)
        {
            realTimeProcesses.add(up);
        }
        else if (up.getPriority() == 1)
        {
            interactiveProcesses.add(up);
        }
        else if (up.getPriority() == 2)
        {
            backgroundProcesses.add(up);
        }

        // Checks if a process is running, if not, starts the new process.
        if (currentProcess == null)
        {
            switchProcess();
        }

        return up.getPid();
    }

    // Switches to the next process to run cooperatively.
    public void switchProcess()
    {
        // In case of one of the lists being empty it will try again.
        while(true)
        {
            int prioritySelector = rand.nextInt(10) + 1;

            // This gets a process from realtime, has 6/10 chance of getting called.
            if (prioritySelector <= 6)
            {
                if (!realTimeProcesses.isEmpty())
                {
                    // Moves the process to the end of the list if it is not done.
                    if (currentProcess != null && !currentProcess.isDone() && !goingToSleep && !goingToWait)
                    {
                        // Puts current process to the back of the list it came from.
                        if (currentProcess.getPriority() == 0)
                        {
                            realTimeProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 1)
                        {
                            interactiveProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 2)
                        {
                            backgroundProcesses.addLast(currentProcess);
                        }
                        goingToSleep = false;
                    }

                    // Removes the next process in line to run and sets it to run next.
                    currentProcess = realTimeProcesses.removeFirst();

                    if (goingToSleep)
                        goingToSleep = false;

                    if (goingToWait)
                        goingToWait = false;

                    // Exits the loop.
                    break;
                }
            }
            // This gets a process from interactive, has 3/10 chance of getting called.
            else if (prioritySelector <= 9)
            {
                //System.out.println("inter");
                //System.out.println(goingToSleep);
                if (!interactiveProcesses.isEmpty())
                {
                    //System.out.println("inter not empty");
                    // Moves the process to the end of the list if it is not done.
                    if (currentProcess != null && !currentProcess.isDone() && !goingToSleep && !goingToWait)
                    {
                        // Puts current process to the back of the list it came from.
                        if (currentProcess.getPriority() == 0)
                        {
                            realTimeProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 1)
                        {
                            interactiveProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 2)
                        {
                            backgroundProcesses.addLast(currentProcess);
                        }
                    }

                    // Removes the next process in line to run and sets it to run next.
                    currentProcess = interactiveProcesses.removeFirst();

                    if (goingToSleep)
                        goingToSleep = false;

                    if (goingToWait)
                        goingToWait = false;

                    // Exits the loop.
                    break;
                }
            }
            // This gets a process from background, has 1/10 chance of getting called.
            else
            {
                //System.out.println("back");
                //System.out.println(goingToSleep);
                if (!backgroundProcesses.isEmpty())
                {
                    //System.out.println("back not empty");
                    // Moves the process to the end of the list if it is not done.
                    if (currentProcess != null && !currentProcess.isDone() && !goingToSleep && !goingToWait)
                    {
                        // Puts current process to the back of the list it came from.
                        if (currentProcess.getPriority() == 0)
                        {
                            realTimeProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 1)
                        {
                            interactiveProcesses.addLast(currentProcess);
                        }
                        else if (currentProcess.getPriority() == 2)
                        {
                            backgroundProcesses.addLast(currentProcess);
                        }
                    }

                    // Removes the next process in line to run and sets it to run next.
                    currentProcess = backgroundProcesses.removeFirst();

                    if (goingToSleep)
                        goingToSleep = false;

                    if (goingToWait)
                        goingToWait = false;

                    // Exits the loop.
                    break;
                }
            }

            // In case no processes are available it will exit the loop, so it does not get stuck here.
            if (realTimeProcesses.isEmpty() && interactiveProcesses.isEmpty() && backgroundProcesses.isEmpty())
                break;
        }
    }

    public void Sleep(long milliseconds)
    {
        // Decrements timeout counter when using sleep to prevent demotion.
        if (currentProcess.getTimeoutCounter() > 0)
        {
            currentProcess.decrementTimeoutCounter();
        }
        // Gets the time to tell the scheduler how long current process should sleep for.
        long sleepTime = clock.instant().toEpochMilli() + milliseconds;
        currentProcess.setTimeToWake(sleepTime);
        // Moves the current process into a sleeping list.
        sleepingProcesses.add(currentProcess);
        // Sets flag to true so the next switch does not move current process to the back of the queue.
        goingToSleep = true;
        currentProcess.getProcess().requestStop();
        currentProcess.getProcess().cooperate();
    }

    public LinkedList<PCB> getRealTimeProcesses()
    {
        return realTimeProcesses;
    }

    public LinkedList<PCB> getInteractiveProcesses()
    {
        return interactiveProcesses;
    }

    public LinkedList<PCB> getBackgroundProcesses()
    {
        return backgroundProcesses;
    }

    public LinkedList<PCB> getSleepingProcesses()
    {
        return sleepingProcesses;
    }

    public PCB getCurrentProcess()
    {
        return currentProcess;
    }

    // Gets the Pid of the currently running program.
    public int getPid()
    {
        return currentProcess.getPid();
    }

    // Searches existing processes by name and return a pid.
    public int getPidByName(String name)
    {
        // Checks all processes in the real time queue.
        for (PCB pcb : realTimeProcesses)
        {
            if (pcb.getName().equals(name))
            {
                return pcb.getPid();
            }
        }
        // Checks all processes in the interactive queue.
        for (PCB pcb : interactiveProcesses)
        {
            if (pcb.getName().equals(name))
            {
                return pcb.getPid();
            }
        }
        // Checks all processes in the background queue.
        for (PCB pcb : backgroundProcesses)
        {
            if (pcb.getName().equals(name))
            {
                return pcb.getPid();
            }
        }
        for (PCB pcb : sleepingProcesses)
        {
            if (pcb.getName().equals(name))
            {
                return pcb.getPid();
            }
        }

        // Returns -1 if no process with the given name is found.
        return -1;
    }

    // Removes the process from wait queue and back into the main rotation.
    public void removeFromWaiting(int pid) 
    {
        int i = 0;
        while (i < waitingForMessage.size())
        {
            PCB process = waitingForMessage.get(i);

            // Checks if message target is present in queue and puts it back in rotation.
            if (process.getPid() == pid)
            {
                // Checks what priority the process was and adds it back in.
                if (process.getPriority() == 0)
                {
                    waitingForMessage.remove(process);
                    getRealTimeProcesses().addLast(process);
                }
                else if (process.getPriority() == 1)
                {
                    waitingForMessage.remove(process);
                    getInteractiveProcesses().addLast(process);
                }
                else if (process.getPriority() == 2)
                {
                    waitingForMessage.remove(process);
                    getBackgroundProcesses().addLast(process);
                }
                break;
            }
            // Increments counter to check next process.
            else
            {
                i++;
            }
        }
    }

    // Adds a process to the waiting queue.
    public void addToWaiting()
    {
        // Decrements timeout counter when using wait to prevent demotion (Since this is similar to sleep).
        if (currentProcess.getTimeoutCounter() > 0)
        {
            currentProcess.decrementTimeoutCounter();
        }

        // Moves the current process into a sleeping list.
        sleepingProcesses.add(currentProcess);
        // Sets flag to true so the next switch does not move current process to the back of the queue.
        goingToWait = true;

        // Stops the process and switches it.
        currentProcess.getProcess().requestStop();
        currentProcess.getProcess().cooperate();
    }
}
