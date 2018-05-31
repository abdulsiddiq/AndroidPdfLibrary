package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;

public interface Downloader
{
    void download( PDFDownloadInfo item );
}
