package com.github.gv2011.jctrl.installer;

import static com.github.gv2011.util.icol.ICollections.listOf;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import com.github.gv2011.util.XStream;
import com.github.gv2011.util.icol.Opt;

public class FormInitializer extends FormLogic{

  private final Function<String, Opt<String>> systemEnvironment;

  FormInitializer(final Function<String, Opt<Path>> fileSystem, final Function<String,Opt<String>> systemEnvironment){
    super(fileSystem);
    this.systemEnvironment = systemEnvironment;
  }

  void initializeForm(final InstallForm form, final App app, final FormUpdater formUpdater){
    final Path jdk =
      systemEnvironment.apply("JAVA_HOME")
      .flatMap(this::tryGetDirPath)
      .orElseGet(this::defaultInstallDir)
    ;
    form.getJdk().setText(jdk.toString());

    form.getJctrl().setText(tryFindJctrl().orElseGet(()->defaultInstallDir().resolve("jctrl")).toString());

    form.getJctrlEnable().setSelected(true);

    final FormUpdater updater = new FormUpdater(fileSystem, app, form);
    updater.updateForm();

    form.getJdk().getDocument().addDocumentListener((TextChangedListener)()->updater.updateForm());
    form.getJctrl().getDocument().addDocumentListener((TextChangedListener)()->updater.updateForm());

    final ChangeListener l = e->updater.updateForm();
    listOf(
      form.getJdkInstall(), form.getJdkUse(),
      form.getJctrlEnable(), form.getJctrlInstall(), form.getJctrlUpdate()
    ).forEach(cb->cb.addChangeListener(l))
    ;

    form.getJdkSelect().addActionListener(e->openDirSelector(e, form.getJdk()));
    form.getJctrlSelect().addActionListener(e->openDirSelector(e, form.getJctrl()));

    form.getCommit().addActionListener(a->{
      app.doAction(()->updater.updateForm());
      updater.updateForm();
    });
  }

  private Opt<Path> tryFindJctrl(){
    return
      XStream.of("ProgramW6432", "ProgramFiles")
      .flatOpt(systemEnvironment)
      .flatOpt(this::tryGetDirPath)
      .flatOpt(d->{
        final Path dir = d.resolve("jctrl");
        return Files.isDirectory(dir) ? (Files.isReadable(dir) ? Opt.of(dir) : Opt.empty()) : Opt.empty();
      })
      .tryFindFirst()
    ;
  }

  private Path defaultInstallDir(){
    return
      XStream.of("ProgramW6432", "ProgramFiles")
      .flatOpt(k->Opt.ofNullable(System.getenv(k)))
      .flatOpt(this::tryGetDirPath)
      .findFirst()
      .orElseGet(()->tryGetDirPath(".").get())
    ;
  }

  void openDirSelector(final ActionEvent e, final JTextField field){
    final JFileChooser jfc = new JFileChooser(field.getText());
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    final int returnValue = jfc.showOpenDialog(null);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
      try {
        final Path selectedFile = jfc.getSelectedFile().toPath().toRealPath();
        field.setText(selectedFile.toString());
      } catch (final IOException e1) {
        e1.printStackTrace();
      }
    }
  }
}
