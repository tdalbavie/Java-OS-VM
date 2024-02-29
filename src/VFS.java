public class VFS implements Device
{
    private final DeviceMapping[] mappings = new DeviceMapping[10];

    @Override
    public int open(String input)
    {
        String[] parts = input.split(" ", 2);
        String deviceType = parts[0];
        String deviceArgs = null;

        // Checks if there are arguments, useful for devices that do no require arguments like RandomDevice.
        if (parts.length > 1)
        {
            deviceArgs = parts[1];
        }

        Device device;
        int deviceId = findNextDeviceId();

        // Checks what device to open and creates it.
        switch (deviceType.toLowerCase())
        {
            case "random":
                device = new RandomDevice();
                break;
            case "file":
                // Ensures a file name is given.
                if (deviceArgs == null)
                    throw new IllegalArgumentException("Filename cannot be null or empty for file device");

                device = new FakeFileSystem();
                break;
            default:
                throw new IllegalArgumentException("Unknown device type: " + deviceType);
        }

        // Opens the device with the given argument/file name.
        int deviceIndexId = device.open(deviceArgs);

        // Throws if it gets an invalid ID.
        if (deviceIndexId == -1)
            throw new IllegalArgumentException("Invalid device ID given to VFS from device type: " + deviceType);

        // Adds the device and ID to the array.
        mappings[deviceId] = new DeviceMapping(device, deviceIndexId, input);

        // Returns the deviceId which corresponds to the index in VFS.
        return deviceId;
    }

    // Helper method for open to find next available index.
    private int findNextDeviceId()
    {
        for (int i = 0; i < mappings.length; i++)
        {
            if (mappings[i] == null)
            {
                return i;
            }
        }
        // Throws exception in case array is full.
        throw new IllegalStateException("No available device IDs");
    }

    // Closes device then removes it from mapping.
    @Override
    public void close(int index)
    {
        if (index >= 0 && index < mappings.length && mappings[index] != null)
        {
            mappings[index].device.close(mappings[index].deviceId);
            mappings[index] = null;
        }
    }

    // Calls the read method of whichever device is being called.
    @Override
    public byte[] read(int index, int count)
    {
        if (index >= 0 && index < mappings.length && mappings[index] != null)
        {
            return mappings[index].device.read(mappings[index].deviceId, count);
        }
        return new byte[0]; // Return an empty array if the mapping does not exist
    }

    // Calls the write method of whichever device is being called.
    @Override
    public int write(int index, byte[] data)
    {
        if (index >= 0 && index < mappings.length && mappings[index] != null)
        {
            return mappings[index].device.write(mappings[index].deviceId, data);
        }
        return 0; // Return 0 if the mapping does not exist
    }

    // Calls the seek method of whichever device is being called.
    @Override
    public void seek(int index, int count)
    {
        if (index >= 0 && index < mappings.length && mappings[index] != null)
        {
            mappings[index].device.seek(mappings[index].deviceId, count);
        }
    }

    // A method to get unique identifier of device for use in PCB.
    public int getDeviceIdByUniqueIdentifier(String uniqueIdentifier)
    {
        for (int i = 0; i < mappings.length; i++)
        {
            if (mappings[i] != null && mappings[i].matchesUniqueIdentifier(uniqueIdentifier))
            {
                // Returns the index of the device for use as the ID for simplicity.
                return i;
            }
        }
        // Returns -1 if no mapping was found.
        return -1;
    }
}