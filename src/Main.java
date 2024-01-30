public class Main
{
    public static void main(String[] args)
    {
        // Start's the OS starting with HelloWorld.
        OS.Startup(new HelloWorld());
        System.out.println("test");
        // Creates another process for GoodbyeWorld.
        OS.createProcess(new GoodbyeWorld());
    }
}
