/*
 * Heim László
 * hlim1626
 * 522
 * lab03
 */
package smtp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class SMTPRequest {
    private String mailFrom;
    private ArrayList<String> receipts;
    private ArrayList<String> contentLines;

    private PrintStream err;

    public SMTPRequest(PrintStream err) {
        receipts = new ArrayList<>();
        contentLines = new ArrayList<>();
        this.err = err;
    }

    public SMTPRequest reset() {
        mailFrom = "Undefined";
        receipts.clear();
        contentLines.clear();
        return this;
    }

    public SMTPRequest setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
        return this;
    }

    public SMTPRequest addReceipt(String receipt) {
        receipts.add(receipt);
        return this;
    }

    public SMTPRequest addContentLine(String line) {
        contentLines.add(line);
        return this;
    }


    public boolean executeRequest(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()
            ));
            DataOutputStream out = new DataOutputStream(
                    socket.getOutputStream()
            );

            String input;

            // Server's first message:
            input = in.readLine();
            System.out.println(input);

            err.println("First message");

            // Hello:
            out.writeBytes("HELO localhost\r\n");
            input = in.readLine();
            System.out.println("S: " + input);

            err.println("Hello sent");

            // From:
            out.writeBytes("MAIL FROM:<" + mailFrom + ">\r\n");
            input = in.readLine();
            System.out.println("S: " + input);

            err.println("Mail from sent");

            // Receipts:
            for (String receipt : receipts) {
                out.writeBytes("RCPT TO:<" + receipt + ">\r\n");
                input = in.readLine();
                System.out.println("S: " + input);
            }

            err.println("Receipts sent");

            // Message lines:
            out.writeBytes("DATA\r\n");
            input = in.readLine();
            System.out.println("S: " + input);

            for (String line : contentLines) {
                out.writeBytes(line + "\r\n");
            }
            // End data:
            out.writeBytes("\r\n.\r\n");
            input = in.readLine();
            System.out.println("S: " + input);

            err.println("Data sent");

            // Quit:
            out.writeBytes("QUIT\r\n");
            input = in.readLine();
            System.out.println("S: " + input);

            err.println("Quit sent");

            return true;
        } catch (IOException ex) {
            err.println("IOException thrown: " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            err.println("Exception thrown: " + ex.getMessage());
            return false;
        }
    }
}
