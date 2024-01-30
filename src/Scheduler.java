import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler
{
    // Holds a list of processes known to the scheduler.
    private LinkedList<UserlandProcess> processes;
    private Timer timer;
    // Holds the currently running process;
    public UserlandProcess currentProcess;

    public Scheduler()
    {
        processes = new LinkedList<>();
        timer = new Timer();

        TimerTask interupt = new TimerTask()
        {
            @Override
            public void run()
            {
                if (currentProcess != null)
                {
                    currentProcess.requestStop();
                }
            };
        };
        timer.scheduleAtFixedRate(interupt, 0, 250);
    }

    public int CreateProcess(UserlandProcess up)
    {
        // Adds the new process to the list.
        processes.add(up);
        // Checks if a process is running, if not, starts the new process.
        if (currentProcess == null)
        {
            SwitchProcess();
        }

        return up.getPID();
    }

    public void SwitchProcess()
    {
        // Moves the process to the end of the list if it is not done.
        if (currentProcess != null && !currentProcess.isDone())
        {
            processes.addLast((currentProcess));
        }

        if (!processes.isEmpty())
        {
            // Gets the next process.
            currentProcess = processes.removeFirst();
            // Starts/resumes the next process.
            currentProcess.start();
        }
        else
        {
            // When there is no process to run.
            currentProcess = null;
        }
    }
}
