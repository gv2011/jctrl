package com.github.gv2011.jctrl.gui.installerold;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.gv2011.jctrl.gui.panel.FixPanel;
import com.github.gv2011.util.icol.Opt;

public class Main {

  public static void main(final String[] args) {
    EventQueue.invokeLater(()->{
      try {
        new Main();
      } catch (final Exception e) {
        e.printStackTrace();
      }
    });
  }

  private FixPanel form;

  private Main() {
    final JFrame frame = new JFrame("Jctrl Installer");
    frame.setBounds(100, 100, 510, 358);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.getContentPane().setLayout(null);

    final JPanel panel;
    App app;
    {
      final FixPanel fixPanel = new FixPanel();
      panel = fixPanel;
      form = fixPanel;
      final Function<String, Opt<Path>> fileSystem = FormLogic.defaultFileSystem();
      app = new App(FormLogic.defaultFileSystem(), form, new TestJdkSource());
      final FormUpdater updater = new FormUpdater(fileSystem, app, form);
      final Function<String, Opt<String>> systemEnvironment = k->Opt.ofNullable(System.getenv(k));
      new FormInitializer(fileSystem, systemEnvironment).initializeForm(form, app, updater);
    }

    frame.addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(final WindowEvent e) {
        frame.dispose();
        app.close();
      }
    });


    panel.setBounds(0, 0, 500, 330);
    frame.getContentPane().add(panel);
    frame.setVisible(true);
  }

}
