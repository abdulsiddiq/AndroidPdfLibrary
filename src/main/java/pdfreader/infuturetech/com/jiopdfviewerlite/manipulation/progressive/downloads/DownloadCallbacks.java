package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;

public interface DownloadCallbacks
{
    void onDownloadComplete( PDFDownloadInfo info );

    void onDownloadError(PDFDownloadInfo info );
}
