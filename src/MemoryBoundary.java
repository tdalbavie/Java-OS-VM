public class MemoryBoundary extends UserlandProcess
{
    @Override
    public void main()
    {
        // Attempts to fill memory to its boundary.
        int lastAllocation = -1;
        while (true)
        {
            // Allocates 1KB blocks.
            int address = OS.AllocateMemory(1024);
            // Checks each time to see if memory is full and breaks.
            if (address == -1)
            {
                break;
            }
            lastAllocation = address;
        }

        if (lastAllocation != -1)
        {
            // Frees the last allocated block.
            OS.FreeMemory(lastAllocation, 1024);

            // Attempts to reallocate the freed space.
            int reallocatedAddress = OS.AllocateMemory(1024);

            if (reallocatedAddress != -1)
            {
                System.out.println("MemoryBoundaryTest: Success");
                OS.FreeMemory(reallocatedAddress, 1024);
            }
            else
            {
                System.out.println("MemoryBoundaryTest: Failure");
            }
        }

        else
        {
            System.out.println("MemoryBoundaryTest: Unable to fill memory to boundary.");
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
