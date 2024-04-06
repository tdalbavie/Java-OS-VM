public class VirtualMemoryReadWriteCont extends UserlandProcess
{
    @Override
    public void main()
    {
        int i = 0;
        while(i != 100)
        {
            // Allocates 1 page.
            int allocatedSize = 1024;
            int startAddress = OS.AllocateMemory(getPCB(), allocatedSize);

            if (startAddress == -1)
            {
                System.out.println("Memory allocation failed.");
                return;
            }

            // Writes a test value to the beginning of the allocated memory.
            byte testValue = 30;
            WriteMemory(startAddress, testValue);

            // Reads the value back.
            byte readValue = ReadMemory(startAddress);

            // Verifies that the value read matches the value written.
            if (readValue == testValue)
            {
                System.out.println("VirtualReadWriteProcessCont: Read/Write test passed.");
            }
            else
            {
                System.out.println("VirtualReadWriteProcessCont: Read/Write test failed.");
            }

            // Cleans up by freeing the allocated memory.
            OS.FreeMemory(getPCB(), startAddress, allocatedSize);

            cooperate();

            // Sleeps to make print less frequent and easier to see the process switch.
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }

            i++;
        }
        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
