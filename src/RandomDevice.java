import java.util.Random;
public class RandomDevice implements Device
{
    private final Random[] devices = new Random[10];

    // Creates a new random device and puts it in the devices array.
    @Override
    public int open(String seed)
    {
        int deviceId = -1;
        for (int i = 0; i < devices.length; i++)
        {
            if (devices[i] == null)
            {
                if (seed != null && !seed.isEmpty())
                {
                    try
                    {
                        devices[i] = new Random(Integer.parseInt(seed));
                        deviceId = i;
                    }
                    catch (NumberFormatException e)
                    {
                        // Fallback in case seed is not a valid integer.
                        devices[i] = new Random();
                        deviceId = i;
                    }
                }
                else
                {
                    devices[i] = new Random();
                    deviceId = i;
                }
                // Stops after filling the first empty spot.
                break;
            }
        }
        return deviceId;
    }

    // Deletes a device entry.
    @Override
    public void close(int index)
    {
        if (index >= 0 && index < devices.length)
        {
            devices[index] = null;
        }
    }

    // Fills/creates an array with random values.
    @Override
    public byte[] read(int index, int count)
    {
        if (index >= 0 && index < devices.length && devices[index] != null)
        {
            // Allocates a byte array to hold the bytes of all integers.
            byte[] bytes = new byte[count * 4];

            for (int i = 0; i < count; i++)
            {
                int value = devices[index].nextInt();
                // Converts each integer to 4 bytes and store in the byte array.
                bytes[4 * i] = (byte) (value >> 24);
                bytes[4 * i + 1] = (byte) (value >> 16);
                bytes[4 * i + 2] = (byte) (value >> 8);
                bytes[4 * i + 3] = (byte) (value);
            }
            return bytes;
        }
        // Returns an empty array if index is out of bounds or device is null.
        return new byte[0];
    }


    // Returns 0 since it doesn't make sense to have here.
    @Override
    public int write(int index, byte[] data)
    {
        return 0;
    }

    @Override
    public void seek(int index, int count)
    {
        // Reads random bytes but do not return them.
        if (index >= 0 && index < devices.length && devices[index] != null)
        {
            for (int i = 0; i < count; i++)
            {
                devices[index].nextInt();
            }
        }
    }
}
