package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.text.TextUtils;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.PageClaimer;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.DBResourse;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.document.AdaptiveDoc;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks.Downloader;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks.DownloaderImpl;

public class ProgressiveDownloadManagerImpl implements ProgressiveDownloadManager,DownloadCallbacks
{
    private PageClaimer mPageClaimer;
    private Downloader mDownloader;
    private AdaptiveDoc mAdaptiveDoc;

    private boolean mDownloadInProgress = false;

    public ProgressiveDownloadManagerImpl( PageClaimer pageClaimer, AdaptiveDoc adaptiveDoc ){
        mPageClaimer = pageClaimer;
        mDownloader = new DownloaderImpl(this);
        mAdaptiveDoc = adaptiveDoc;
    }

    @Override
    public void addToDownloads( String combinedId, String appendUrl, int totalPages )
    {
        PDFDownloadInfo[] PDFInfos = new PDFDownloadInfo[totalPages];
        for (int index = 1; index <= PDFInfos.length; index++)
        {
            PDFInfos[index-1]  = new PDFDownloadInfo(combinedId,appendUrl);
        }
        DBResourse.insertPdf(PDFInfos);
    }

    @Override
    public void startDownload()
    {
        mDownloadInProgress = true;
        PDFDownloadInfo item = getPriorityPdf();
        if(item == null) item = DBResourse.itemToDownload();
        mDownloader.download(item);
    }

    @Override
    public boolean downloadInProgress()
    {
        return mDownloadInProgress;
    }

    @Override
    public void onDownloadComplete(PDFDownloadInfo info )
    {
//        mAdaptiveDoc.addPage(info.pdfId,info.downloadUrl);
        mPageClaimer.claimPage(info.pdfId,mAdaptiveDoc.getPage(info.pdfId));
        startDownload();
    }

    private PDFDownloadInfo getPriorityPdf()
    {
        PDFDownloadInfo item = null;
        String combinedId = mPageClaimer.popKey();
        if(!TextUtils.isEmpty(combinedId))
        {
            item = DBResourse.itemToDownload(combinedId);
            return item == null
                    ? getPriorityPdf()
                    : item;
        }
        return item;
    }

    @Override
    public void onDownloadError(PDFDownloadInfo info )
    {
        mDownloadInProgress = false;
        if(info != null) startDownload();
    }

}
