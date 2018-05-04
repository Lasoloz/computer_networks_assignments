/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.client.ui;

import com.gitlab.lasoloz.irc.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame implements ActionListener {
    private static Logger LOGGER = LoggerFactory.getLogger(LoginFrame.class);

    private JTextField usernameField;
    private JTextField hostAddressField;
    private JTextField hostPortField;

    public LoginFrame() {
        JLabel usernameLabel = new JLabel("Username:");
        JLabel hostAddressLabel = new JLabel("Server address:");
        JLabel hostPortLabel = new JLabel("Server port:");

        usernameField = new JTextField("username");
        hostAddressField = new JTextField("127.0.0.1");
        hostPortField = new JTextField("9010");

        JButton loginButton = new JButton("Log in");
        loginButton.addActionListener(this);

        this.getRootPane().setDefaultButton(loginButton);

        JPanel basePanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;

        basePanel.add(usernameLabel, c);
        c.gridy = 1;
        basePanel.add(hostAddressLabel, c);
        c.gridy = 2;
        basePanel.add(hostPortLabel, c);

        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        basePanel.add(usernameField, c);
        c.gridy = 1;
        basePanel.add(hostAddressField, c);
        c.gridy = 2;
        basePanel.add(hostPortField, c);

        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.insets = new Insets(10, 0, 0, 0);
        basePanel.add(loginButton, c);

        add(basePanel);

        setTitle("Login - " + ClientFrame.APP_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(500, 500, 400, 150);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Client client = new Client();
        String username = usernameField.getText();
        String hostName = hostAddressField.getText();
        String hostPort = hostPortField.getText();

        if (username.equals("")) {
            LOGGER.info("Username is empty!");
            return;
        }

        try {
            int portValue = Integer.parseInt(hostPort);

            if (client.connect(hostName, portValue, username)) {
                // New client frame...
                ClientFrame clientFrame = new ClientFrame(client);
                clientFrame.setVisible(true);
                clientFrame.runThread();
                dispose();
            } else {
                LOGGER.error("Failed to connect to server!");
                client.close();
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("Failed to parse port number!");
        }
    }
}
