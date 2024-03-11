public class Ping extends UserlandProcess
{
    @Override
    public void main()
    {
        // Must be hard coded as pong does not get created in time to get the Pid through name search.
        int pongPid = 2;
        int myPid = OS.GetPid();
        int messageCount = 0;
        System.out.println("I am PING, pong = " + pongPid);

        while (true)
        {
            // Sends a message to Pong.
            KernelMessage messageToSend = new KernelMessage(myPid, pongPid, messageCount, "PING".getBytes());
            OS.sendMessage(messageToSend);

            // Waits for a response from Pong.
            KernelMessage receivedMessage = OS.waitForMessage();
            // Gets the message from Pong to print.
            String receivedData = new String(receivedMessage.getData());
            System.out.printf("  %s: from: %d to: %d what: %d\n", receivedData, receivedMessage.getSenderPid(), receivedMessage.getTargetPid(), receivedMessage.getMessageType());

            // Increments the messageCount to show the next iteration.
            messageCount++;

            cooperate();
        }
    }
}
