public class DeviceMapping
{
    public Device device;
    public int deviceId;
    private final String uniqueIdentifier;

    public DeviceMapping(Device device, int deviceId, String uniqueIdentifier)
    {
        this.device = device;
        this.deviceId = deviceId;
        // This will be used strictly for getting deviceID.
        this.uniqueIdentifier = uniqueIdentifier;
    }

    // This will match identifiers.
    public boolean matchesUniqueIdentifier(String identifier)
    {
        return this.uniqueIdentifier.equals(identifier);
    }
}
