import java.util.Arrays;
import java.util.LinkedList;

public class PCB
{
    private final UserlandProcess up;
    private static int nextpid;
    private int pid;
    // Tells the scheduler what priority to initialize process as, as well as move process to appropriate queue after sleep.
    // 0 for real-time, 1 for interactive, 2 for background.
    private int priority;
    // This will hold the clock time for when to wake up again (this may never be used if process doesn't sleep).
    private long timeToWake;
    // This will count how many times a process has timed out, this will be used for demotions.
    private int timeoutCounter;
    private int[] deviceIds = new int[10];
    private String processName;
    // This will hold any messages a process is trying to send to it.
    private LinkedList<KernelMessage> messageQueue;

    // Creates thread, sets pid.
    public PCB(UserlandProcess up)
    {
        this.up = up;
        pid = nextpid++;
        // Sets priority to real-time by default.
        priority = 0;
        timeoutCounter = 0;
        Arrays.fill(deviceIds, -1);
        // Gets process name.
        processName = up.getClass().getSimpleName();
        messageQueue = new LinkedList<>();
    }

    // Second constructor to let priority be set by user.
    public PCB(UserlandProcess up, int priority)
    {
        this(up);
        if(priority >= 0 && priority <= 2)
            this.priority = priority;
        else
            throw new IllegalArgumentException("Error: Priority specified is out of range, valid priorities are 0-2.");
    }

    // Calls UserlandProcess' stop. Loops with Thread.sleep() until ulp.isStopped is true.
    public void stop()
    {
        up.stop();
    }

    // Calls UserlandProcess' isDone().
    public boolean isDone()
    {
        return up.isDone();
    }

    // Calls UserlandProcess' start().
    public void start()
    {
        up.start();
    }

    public int getPid()
    {
        return pid;
    }

    public UserlandProcess getProcess()
    {
        return up;
    }

    public int getPriority()
    {
        return priority;
    }

    // This will be used for demotions.
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public void setTimeToWake(long sleepTime)
    {
        this.timeToWake = sleepTime;
    }

    public long getTimeToWake()
    {
        return timeToWake;
    }

    public int getTimeoutCounter()
    {
        return timeoutCounter;
    }

    public void incrementTimeoutCounter()
    {
        timeoutCounter++;
    }

    public void decrementTimeoutCounter()
    {
        timeoutCounter--;
    }

    public void setTimeoutCounter(int timeoutCounter)
    {
        this.timeoutCounter = timeoutCounter;
    }

    public int[] getDeviceIds()
    {
        return deviceIds;
    }

    public String getName()
    {
        return processName;
    }

    // Adds a message to the end of the queue.
    public void enqueueMessage(KernelMessage message)
    {
        this.messageQueue.addLast(message);
    }

    // Gets and removes the next message from the queue.
    public KernelMessage dequeueMessage()
    {
        return this.messageQueue.poll();
    }

    // Peeks at the next message without removing it (Used for testing).
    public KernelMessage peekMessage()
    {
        return this.messageQueue.peek();
    }
}
