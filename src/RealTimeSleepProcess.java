public class RealTimeSleepProcess extends UserlandProcess
{
    @Override
    public void main()
    {
        while(true)
        {
            System.out.println("Real Time Sleep");
            cooperate(); // Checks each time process needs to yield to next process.
            // Sleeps for 10 seconds after printing once.
            OS.Sleep(10000);
            // Sleeps to make print less frequent and easier to see the process switch.
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
