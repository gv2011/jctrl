package com.github.gv2011.jctrl.installer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public interface InstallForm {
  JTextField getJdk();

  JButton getJdkSelect();

  JCheckBox getJdkInstall();

  JCheckBox getJdkUse();

  JTextArea getJdkMessage();

  JTextField getJctrl();

  JButton getJctrlSelect();

  JCheckBox getJctrlInstall();

  JCheckBox getJctrlUpdate();

  JTextArea getJctrlMessage();

  JCheckBox getJctrlEnable();

  JButton getCommit();

}
