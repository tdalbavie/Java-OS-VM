public class IdleProcess extends UserlandProcess
{
    @Override
    public void main()
    {
        while(true)
        {
            //System.out.println("idle");
            cooperate(); // Checks each time process needs to yield to next process.
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
