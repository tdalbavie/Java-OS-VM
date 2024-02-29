import java.util.Arrays;

public class WriteToFile extends UserlandProcess
{
    @Override
    public void main()
    {
        System.out.println("Writing to File");
        int randomDeviceId = OS.open("random");
        int fileDeviceId = OS.open("file myFile.txt");

        // Ensures devices were properly opened.
        if(randomDeviceId == -1 || fileDeviceId == -1)
        {
            System.out.println("Failed to open devices.");
            return;
        }

        // Generate random data from RandomDevice (10 int = 40 byte).
        byte[] randomData = OS.read(randomDeviceId, 5);

        // Converts each byte to a string representation and write it to the file.
        for (int i = 0; i < randomData.length; i++)
        {
            byte b = randomData[i];
            String data;
            if (i < randomData.length - 1)
            {
                // Appends the new line character for all but the last line.
                data = b + "\n";
            }
            else
            {
                // Does not add the last newline character to the file.
                data = String.valueOf(b);
            }
            OS.write(fileDeviceId, data.getBytes());
        }


        System.out.println("Random Data Written: " + Arrays.toString(randomData));

        // Close the devices using their respective ID.
        OS.close(randomDeviceId);
        OS.close(fileDeviceId);

        // Brief pause to ensure Userland Thread synchronization.
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        cooperate();

        // Alternative to ending process since that is not implemented.
        OS.Sleep(99999999);
    }
}
