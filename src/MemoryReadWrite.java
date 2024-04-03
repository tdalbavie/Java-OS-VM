public class MemoryReadWrite extends UserlandProcess
{
    @Override
    public void main()
    {
        int allocatedAddress = OS.AllocateMemory(1024); // Allocate 1 page
        if (allocatedAddress != -1)
        {
            byte testValue = 123;
            WriteMemory(allocatedAddress, testValue); // Write a value
            byte readValue = ReadMemory(allocatedAddress); // Read the value back

            if (testValue == readValue)
            {
                System.out.println("MemoryReadWriteTest: Success");
            }
            else
            {
                System.out.println("MemoryReadWriteTest: Failure");
            }

            OS.FreeMemory(allocatedAddress, 1024); // Cleanup
        }
        else
        {
            System.out.println("MemoryReadWriteTest: Allocation Failed");
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
