package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;

public interface ProgressiveDownloadManager
{
    void addToDownloads( PDFDownloadInfo... infos );

    void startDownload();

    boolean downloadInProgress();

    void addCallBackListener(DownloadCallbacks callbacks);

    void cancelDownload(String itemId);
}
