public class MemoryExtension extends UserlandProcess
{
    @Override
    public void main()
    {
        // Attempts to allocate more memory than a single page.
        int startAddress = OS.AllocateMemory(2048);

        if (startAddress != -1)
        {
            System.out.println("MemoryExtensionTest: Success");
            OS.FreeMemory(startAddress, 2048);
        }
        else
        {
            System.out.println("MemoryExtensionTest: Failure");
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}