/*
 * Heim László, hlim1626, 522-es csoport
 */
package com.gitlab.lasoloz.irc.client.ui;

import javax.swing.*;
import java.awt.*;

public class UserListPane extends JPanel {
    public final static String SEND_TO_EVERYONE = "Send to everyone";
    private DefaultListModel<String> listModel;
    private JList<String> userList;

    UserListPane() {
        listModel = new DefaultListModel<>();
        listModel.addElement(SEND_TO_EVERYONE);

        userList = new JList<>(listModel);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setSelectedIndex(0);

        JScrollPane userListScroll = new JScrollPane();
        userListScroll.add(userList);
        userListScroll.setViewportView(userList);

        setLayout(new BorderLayout());
        add(userListScroll, BorderLayout.CENTER);
    }


    public void addUser(String username) {
        if (username.equals(SEND_TO_EVERYONE)) {
            return;
        }

        listModel.addElement(username);
    }

    public void removeUser(String username) {
        if (username.equals(SEND_TO_EVERYONE)) {
            return;
        }

        listModel.removeElement(username);
    }

    public String getSelectedUser() {
        if (userList.getSelectedIndex() == 0) {
            userList.setSelectedIndex(0);
        }

        return listModel.getElementAt(userList.getSelectedIndex());
    }
}
