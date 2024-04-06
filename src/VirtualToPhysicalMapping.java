public class VirtualToPhysicalMapping
{
    public int physicalPageNumber;
    public int diskPageNumber;

    public VirtualToPhysicalMapping()
    {
        // Indicates the page is not loaded in physical memory.
        this.physicalPageNumber = -1;
        // Indicates the page is not stored on disk.
        this.diskPageNumber = -1;
    }
}
