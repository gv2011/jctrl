package com.github.gv2011.jctrl.gui.installerold;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.nio.file.Path;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.download.DownloadTask;
import com.github.gv2011.util.download.DownloadTask.StatusInfo;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.lock.Lock;

public class App extends FormLogic implements AutoCloseableNt{

  private final InstallForm form;

  private final Lock downloadLock = Lock.create();

  private boolean downloading;
  private final DownloadTask.Factory downloaderFactory = DownloadTask.factory();
  private Opt<DownloadTask> downloader = Opt.empty();
  private Opt<Path> installJdkTo = Opt.empty();

  private final JdkSource jdkSource;

  private Runnable downloadFinishedCallback = ()->{};

  public App(final Function<String, Opt<Path>> fileSystem, final InstallForm form, final JdkSource jdkSource) {
    super(fileSystem);
    this.form = form;
    this.jdkSource = jdkSource;
  }

  private void startDownload(){
    downloader = Opt.of(downloaderFactory.createUnzipTask(
      jdkSource.url(),
      jdkSource.size(),
      jdkSource.hash(),
      jdkSource.rootFolderName(),
      this::statusUpdate,
      this::downloadFinished,
      installJdkTo.get()
    ));
    downloader.get().setThrottle(1000);
    downloader.get().start();
  }

  public boolean isDownloading() {
    return downloadLock.get(()->downloading);
  }

  public void doAction(final Runnable finished) {
    downloadLock.run(()->{
      if(!downloading && form.getJdkInstall().isSelected()){
        downloading = true;
        installJdkTo = Opt.of(tryGetDirPath(form.getJdk().getText()).get());
        downloadFinishedCallback = finished;
        startDownload();
      }
      else{
        if(downloading){
          final boolean cancelled = downloader.get().cancel();
          if(cancelled){
            downloader.get().close();
            downloader = Opt.empty();
            downloading = false;
          }
        }
      }
    });
  }

  private void downloadFinished(final StatusInfo status){
    downloadLock.run(()->{
      downloading = false;
    });
    SwingUtilities.invokeLater(()->{
      form.getJdkMessage().setText(status.message());
      downloadFinishedCallback.run();
      downloadFinishedCallback = ()->{};
    });
  }

  private void statusUpdate(final StatusInfo status){
    call(()->SwingUtilities.invokeAndWait(()->{
      downloadLock.run(()->{
        if(downloading) form.getJdkMessage().setText(status.message());
      });
    }));
  }

  @Override
  public void close() {
    downloadLock.run(()->{
      downloader.ifPresent(DownloadTask::close);
    });
  }

}
