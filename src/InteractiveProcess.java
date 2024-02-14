public class InteractiveProcess extends UserlandProcess
{
    @Override
    public void main()
    {
        while(true)
        {
            System.out.println("Interactive");
            cooperate(); // Checks each time process needs to yield to next process.
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
