public class UnauthorizedMemoryAccess extends UserlandProcess
{
    @Override
    public void main()
    {
        // Creates an address that's unlikely to be allocated.
        int unallocatedAddress = 1024 * 1024;
        try
        {
            // Attempts to read from address.
            byte value = ReadMemory(unallocatedAddress);
            // Attempt to write from address.
            WriteMemory(unallocatedAddress, value);

            System.out.println("UnauthorizedMemoryAccessTest: Failure (No exception thrown)");
        }
        catch (Exception e)
        {
            System.out.println("UnauthorizedMemoryAccessTest: Success (Exception caught)");
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}