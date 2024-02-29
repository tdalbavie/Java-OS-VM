import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class FakeFileSystem implements Device
{
    private final RandomAccessFile[] files = new RandomAccessFile[10];

    // Opens new RandomAccessFile for the given filename and stores it.
    @Override
    public int open(String filename)
    {
        // Initializes value for index the RandomAccessFile is inserted in.
        int deviceId = -1;
        if (filename == null || filename.isEmpty())
        {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        for (int i = 0; i < files.length; i++)
        {
            if (files[i] == null)
            {
                try
                {
                    // Opens the file for both reading and writing.
                    files[i] = new RandomAccessFile(filename, "rw");
                    deviceId = i;
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to open file: " + filename, e);
                }
                // Stops after the first empty spot.
                break;
            }
        }
        // Returns the index that is used to access the file.
        return deviceId;
    }

    @Override
    public void close(int index)
    {
        if (index >= 0 && index < files.length && files[index] != null)
        {
            try
            {
                files[index].close();
                // Clears out the internal array entry after closing.
                files[index] = null;
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to close file at index: " + index, e);
            }
        }
    }

    @Override
    public byte[] read(int index, int count)
    {
        if (index >= 0 && index < files.length && files[index] != null)
        {
            try
            {
                byte[] buffer = new byte[count];
                int bytesRead = files[index].read(buffer);
                if (bytesRead < count)
                {
                    // Truncates or copies the buffer to fit the actual number of bytes read if necessary.
                    return Arrays.copyOf(buffer, bytesRead);
                }
                // Returns the full buffer if bytesRead equals count.
                return buffer;
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to read from file at index: " + index, e);
            }
        }
        // Returns an empty array if conditions are not met.
        return new byte[0];
    }

    @Override
    public int write(int index, byte[] data)
    {
        if (index >= 0 && index < files.length && files[index] != null)
        {
            try
            {
                files[index].write(data);
                // Returns the number of bytes written.
                return data.length;
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to write to file at index: " + index, e);
            }
        }
        // Returns 0 if the file is not available for writing.
        return 0;
    }

    @Override
    public void seek(int index, int position)
    {
        if (index >= 0 && index < files.length && files[index] != null)
        {
            try
            {
                files[index].seek(position);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to seek in file at index: " + index, e);
            }
        }
    }
}

