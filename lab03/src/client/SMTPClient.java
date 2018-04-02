/*
 * Heim László
 * hlim1626
 * 522
 * lab03
 */
package client;

import smtp.SMTPRequest;

public class SMTPClient {
    public static void main(String[] args) {
        // Default value:
        String hostName = "localhost";
        int portNumber = 25;

        // Override default values if needed:
        if (args.length >= 1) {
            hostName = args[0];
        }

        if (args.length == 2) {
            portNumber = Integer.parseInt(args[1]);
        }

        // Create a request builder:
        SMTPRequest req = new SMTPRequest(System.out);

        // Add contents to request:
        req.setMailFrom("<hlim1626@linux.scs.ubbcluj.ro>")
                .addReceipt("<hlim1626@linux.scs.ubbcluj.ro>")
                .addContentLine("Subject: Test message")
                .addContentLine("")
                .addContentLine("Hello!");

        // Execute request:
        if (req.executeRequest(hostName, portNumber)) {
            System.out.println("Message sent successfully!");
        } else {
            System.out.println("Couldn't send message!");
        }
    }
}
