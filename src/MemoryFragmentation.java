public class MemoryFragmentation extends UserlandProcess
{
    @Override
    public void main()
    {
        // Allocates multiple small blocks of memory.
        int[] addresses = new int[5];
        for (int i = 0; i < addresses.length; i++)
        {
            // Allocates 1KB blocks.
            addresses[i] = OS.AllocateMemory(1024);
        }

        // Frees some blocks to create fragmentation.
        // Frees the second block.
        OS.FreeMemory(addresses[1], 1024);
        // Frees the fourth block.
        OS.FreeMemory(addresses[3], 1024);

        // Attempts to allocate a larger block that requires fragmentation handling.
        // Allocates a 2KB block.
        int largeBlockAddress = OS.AllocateMemory(2048);

        if (largeBlockAddress != -1)
        {
            System.out.println("MemoryFragmentationTest: Success");
            // Cleanup.
            OS.FreeMemory(largeBlockAddress, 2048);
        }
        else
        {
            System.out.println("MemoryFragmentationTest: Failure");
        }

        // Cleans up remaining allocations.
        for (int address : addresses)
        {
            if (address != -1)
            {
                OS.FreeMemory(address, 1024);
            }
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
