import java.util.Arrays;

public class KernelMessage
{
    private final int senderPid;
    private final int targetPid;

    private final int messageType;
    private final byte[] data;

    public KernelMessage(int senderPid, int targetPid, int messageType, byte[] data)
    {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.messageType = messageType;
        this.data = data.clone();
    }

    // Copy constructor for deep copy use.
    public KernelMessage(KernelMessage message)
    {
        this.senderPid = message.senderPid;
        this.targetPid = message.targetPid;
        this.messageType = message.messageType;
        this.data = message.data.clone();
    }

    // ToString for debugging.
    @Override
    public String toString()
    {
        return String.format("From: %d, To: %d, Type: %d, Data: %s",
                senderPid, targetPid, messageType, Arrays.toString(data));
    }

    // Getters
    public int getSenderPid()
    {
        return senderPid;
    }

    public int getTargetPid()
    {
        return targetPid;
    }

    public int getMessageType()
    {
        return messageType;
    }

    public byte[] getData()
    {
        // Returns a copy of the data to ensure encapsulation.
        return data.clone();
    }
}
