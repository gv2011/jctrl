package com.github.gv2011.jctrl.gui.installerold;

import static com.github.gv2011.util.icol.ICollections.listOf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.icol.Opt;

public class FormUpdater extends FormLogic{

  private final App app;
  private final InstallForm form;

  FormUpdater(final Function<String, Opt<Path>> fileSystem, final App app, final InstallForm form) {
    super(fileSystem);
    this.app = app;
    this.form = form;
  }

  void updateForm(){
    if(app.isDownloading()) setDownloadState();
    else{
      form.getJdk().setEnabled(true);
      form.getJdkSelect().setEnabled(true);
      form.getJctrl().setEnabled(true);
      form.getJctrlSelect().setEnabled(true);
      form.getCommit().setText("Start");
      final Opt<Path> jdk = tryGetDirPath(form.getJdk().getText());
      final boolean validJdk = jdk.map(d->isValidJdk(d)).orElse(false);

      if(!validJdk){
        form.getJdkUse().setSelected(false);
        form.getJdkUse().setEnabled(false);
      }

      final boolean installEnabled =
        !form.getJdkUse().isSelected() &&
        isPresentAndEmpty(jdk) &&
        !validJdk
      ;
      if(!installEnabled){
        form.getJdkInstall().setSelected(false);
        form.getJdkInstall().setEnabled(false);
      }
      else {
        form.getJdkInstall().setEnabled(true);
        if(form.getJdkUse().isSelected()) form.getJdkInstall().setSelected(false);
      }

      if(form.getJdkInstall().isSelected()){
        form.getJdkUse().setSelected(false);
        form.getJdkUse().setEnabled(false);
      }
      else{
        form.getJdkUse().setEnabled(validJdk);
        form.getJdkUse().setSelected(validJdk);
      }

      final boolean jdkConfigured = form.getJdkInstall().isSelected() || form.getJdkUse().isSelected();

      final Opt<Path> jctrl = tryGetDirPath(form.getJctrl().getText());
      final boolean validJctrl = jctrl.map(d->isValidJctrl(d)).orElse(false);

      if(!jdkConfigured){
        form.getJctrlInstall().setSelected(false);
        form.getJctrlInstall().setEnabled(false);
        form.getJctrlUpdate().setSelected(false);
        form.getJctrlUpdate().setEnabled(false);
      }
      else{
        if(!jctrl.isPresent()) form.getJctrlInstall().setSelected(false);

        if(validJctrl){
          form.getJctrlInstall().setSelected(false);
          form.getJctrlInstall().setEnabled(false);
          form.getJctrlUpdate().setEnabled(true);
        }
        else{
          form.getJctrlUpdate().setSelected(false);
          form.getJctrlUpdate().setEnabled(false);
          form.getJctrlInstall().setEnabled(jctrl.isPresent());
        }
      }

      form.getJctrlEnable().setEnabled(form.getJctrlInstall().isSelected() || form.getJctrlUpdate().isSelected());

      form.getCommit().setEnabled(
          form.getJdkInstall().isSelected() ||
          form.getJctrlInstall().isSelected() ||
          form.getJctrlUpdate().isSelected()
      );
    }
  }

  private boolean isPresentAndEmpty(final Opt<Path> jdk) {
    return jdk.map(FileUtils::isEmpty).orElse(false);
  }

  private void setDownloadState() {
    listOf(
      form.getJdk(), form.getJdkSelect(),
      form.getJdkInstall(), form.getJdkUse(),
      form.getJctrl(), form.getJctrlSelect(),
      form.getJctrlEnable(), form.getJctrlInstall(), form.getJctrlUpdate(),
      form.getJctrlEnable()
    ).forEach(cb->cb.setEnabled(false));
    form.getCommit().setText("Cancel");
  }

  private boolean isValidJdk(final Path dir){
    return Files.isRegularFile(dir.resolve("bin/java.exe"));
  }

  private boolean isValidJctrl(final Path dir){
    return dir.getFileName().toString().equals("jctrl") && FileUtils.list(dir).findAny().isPresent();
  }
}
