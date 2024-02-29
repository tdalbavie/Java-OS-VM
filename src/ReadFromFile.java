public class ReadFromFile extends UserlandProcess
{
    @Override
    public void main()
    {
        System.out.println("Reading from File");

        // Opens the file for reading using VFS and store the returned device ID.
        int fileId = OS.open("file myFile.txt");

        // Ensures fileId is valid before attempting to read.
        if (fileId != -1)
        {
            // Read from the file using the fileId.
            byte[] data = OS.read(fileId, 1024);
            StringBuilder contentBuilder = new StringBuilder();

            // Converts each byte directly to a character and append to the builder.
            for (byte datum : data)
            {
                contentBuilder.append((char) datum);
            }

            String content = contentBuilder.toString();
            System.out.println(content);

            // Closes the file using the fileId.
            OS.close(fileId);
        }
        else
        {
            System.out.println("Failed to open file.");
        }

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
