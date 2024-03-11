public class Pong extends UserlandProcess
{
    @Override
    public void main()
    {
        int pingPid = OS.GetPidByName("Ping");
        int myPid = OS.GetPid();
        System.out.println("I am PONG, ping = " + pingPid);

        while (true)
        {
            // Waits for a message from Ping.
            KernelMessage receivedMessage = OS.waitForMessage();
            // Gets the data from Ping to print.
            String receivedData = new String(receivedMessage.getData());
            System.out.printf("  %s: from: %d to: %d what: %d\n", receivedData, receivedMessage.getSenderPid(), receivedMessage.getTargetPid(), receivedMessage.getMessageType());

            // Gets the current "what" to display.
            int messageCount = receivedMessage.getMessageType();

            // Sends a response back to Ping.
            KernelMessage messageToSend = new KernelMessage(myPid, pingPid, messageCount, "PONG".getBytes());
            OS.sendMessage(messageToSend);

            cooperate();
        }
    }
}
