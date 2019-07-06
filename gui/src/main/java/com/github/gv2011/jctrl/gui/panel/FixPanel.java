package com.github.gv2011.jctrl.gui.panel;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.github.gv2011.jctrl.gui.installerold.InstallForm;

public final class FixPanel extends JPanel implements InstallForm {

  private final JTextField jdk;
  private final JTextField jctrl;
  private final JButton jdkSelect;
  private final JCheckBox jdkInstall;
  private final JCheckBox jdkUse;
  private final JTextArea jdkMessage;
  private final JButton jctrlSelect;
  private final JCheckBox jctrlInstall;
  private final JCheckBox jctrlUpdate;
  private final JTextArea jctrlMessage;
  private final JCheckBox jctrlEnable;
  private final JButton commit;

  /**
   * Create the panel.
   */
  public FixPanel() {
    setBorder(null);
    setLayout(null);

    final JPanel panel = new JPanel();
    panel.setBorder(
        new TitledBorder(null, "JDK Installation Directory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    panel.setBounds(12, 12, 476, 125);
    add(panel);
    panel.setLayout(null);

    jdk = new JTextField();
    jdk.setBackground(Color.WHITE);
    jdk.setBounds(12, 30, 370, 20);
    panel.add(jdk);
    jdk.setText("C:\\programs\\java\\jdk-11.0.1");
    jdk.setColumns(60);

    jdkSelect = new JButton("Select");
    jdkSelect.setBounds(394, 30, 70, 20);
    panel.add(jdkSelect);

    jdkInstall = new JCheckBox("Download and install");
    jdkInstall.setBounds(12, 58, 150, 24);
    panel.add(jdkInstall);

    jdkUse = new JCheckBox("Use existing");
    jdkUse.setBounds(196, 58, 109, 24);
    panel.add(jdkUse);

    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBorder(null);
    scrollPane.setBounds(12, 90, 452, 20);
    panel.add(scrollPane);

    jdkMessage = new JTextArea();
    jdkMessage.setBorder(null);
    jdkMessage.setLineWrap(true);
    jdkMessage.setBackground(new Color(238, 238, 238));
    jdkMessage.setForeground(Color.BLACK);
    jdkMessage.setEditable(false);
    scrollPane.setViewportView(jdkMessage);
    jdkMessage.setText("rejgferfoERJFOÖERIJFEOFJIAEROÖJIDOÖACVIAEROQOÖR");

    final JPanel panel_1 = new JPanel();
    panel_1.setLayout(null);
    panel_1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Jctrl Installation Directory",
        TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
    panel_1.setBounds(12, 149, 476, 125);
    add(panel_1);

    jctrl = new JTextField();
    jctrl.setText("C:\\programs\\java\\jdk-11.0.1");
    jctrl.setColumns(60);
    jctrl.setBackground(Color.WHITE);
    jctrl.setBounds(12, 30, 370, 20);
    panel_1.add(jctrl);

    jctrlSelect = new JButton("Select");
    jctrlSelect.setBounds(394, 30, 70, 20);
    panel_1.add(jctrlSelect);

    jctrlInstall = new JCheckBox("Install");
    jctrlInstall.setBounds(12, 58, 70, 24);
    panel_1.add(jctrlInstall);

    jctrlUpdate = new JCheckBox("Update");
    jctrlUpdate.setBounds(86, 58, 109, 24);
    panel_1.add(jctrlUpdate);

    final JScrollPane scrollPane_1 = new JScrollPane();
    scrollPane_1.setBorder(null);
    scrollPane_1.setBounds(12, 90, 452, 20);
    panel_1.add(scrollPane_1);

    jctrlMessage = new JTextArea();
    jctrlMessage.setText("rejgferfoERJFOÖERIJFEOFJIAEROÖJIDOÖACVIAEROQOÖR");
    jctrlMessage.setLineWrap(true);
    jctrlMessage.setForeground(Color.BLACK);
    jctrlMessage.setEditable(false);
    jctrlMessage.setBorder(null);
    jctrlMessage.setBackground(UIManager.getColor("Button.background"));
    scrollPane_1.setViewportView(jctrlMessage);

    jctrlEnable = new JCheckBox("Enable Jctrl");
    jctrlEnable.setBounds(12, 287, 110, 24);
    add(jctrlEnable);

    commit = new JButton("Start");
    commit.setBounds(389, 286, 99, 26);
    add(commit);

  }

  @Override
  public JTextField getJdk() {
    return jdk;
  }

  @Override
  public JButton getJdkSelect() {
    return jdkSelect;
  }

  @Override
  public JCheckBox getJdkInstall() {
    return jdkInstall;
  }

  @Override
  public JCheckBox getJdkUse() {
    return jdkUse;
  }

  @Override
  public JTextArea getJdkMessage() {
    return jdkMessage;
  }

  @Override
  public JTextField getJctrl() {
    return jctrl;
  }

  @Override
  public JButton getJctrlSelect() {
    return jctrlSelect;
  }

  @Override
  public JCheckBox getJctrlInstall() {
    return jctrlInstall;
  }

  @Override
  public JCheckBox getJctrlUpdate() {
    return jctrlUpdate;
  }

  @Override
  public JTextArea getJctrlMessage() {
    return jctrlMessage;
  }

  @Override
  public JCheckBox getJctrlEnable() {
    return jctrlEnable;
  }

  @Override
  public JButton getCommit() {
    return commit;
  }
}
