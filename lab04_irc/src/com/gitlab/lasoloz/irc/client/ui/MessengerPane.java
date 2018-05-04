/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.client.ui;

import javax.swing.*;
import java.awt.*;

public class MessengerPane extends JPanel {
    private DefaultListModel<String> messageListModel;


    MessengerPane() {
        messageListModel = new DefaultListModel<>();

        JList<String> messageList = new JList<>(messageListModel);
        messageList.setLayoutOrientation(JList.VERTICAL);
        messageList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                super.setSelectionInterval(-1, -1);
            }
        });
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.add(messageList);
        scrollPane.setViewportView(messageList);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }


    public void addMessage(String message) {
        messageListModel.addElement(message);

        if (messageListModel.size() > 100) {
            messageListModel.remove(0);
        }
    }
}
