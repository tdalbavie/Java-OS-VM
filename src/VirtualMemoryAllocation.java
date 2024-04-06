public class VirtualMemoryAllocation extends UserlandProcess
{
    @Override
    public void main()
    {
        // Allocates 100 pages.
        int size = 100 * 1024;
        int startAddress = OS.AllocateMemory(getPCB(), size);

        if (startAddress == -1)
        {
            System.out.println("Process: Failed to allocate memory.");
            return;
        }

        // Attempting to write at the last byte of the allocated memory.
        int testAddress = startAddress + size - 1;
        byte testValue = 123;
        WriteMemory(testAddress, testValue);

        // Reading the value back.
        byte readValue = ReadMemory(testAddress);

        // Verifying that the value read matches the value written.
        if (readValue == testValue)
        {
            System.out.println("VirtualAllocationProcess: Successfully wrote and read at the boundary of allocated memory.");
        }
        else
        {
            System.out.println("VirtualAllocationProcess: Failed to write and read at the boundary of allocated memory.");
        }

        // Freeing the allocated memory.
        OS.FreeMemory(getPCB(), startAddress, size);

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
