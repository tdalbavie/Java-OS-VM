// Interface implemented in RandomDevice.
public interface Device
{
    int open(String seed);
    void close(int index);
    byte[] read(int index, int count);
    int write(int index, byte[] data);
    void seek(int index, int count);
}
