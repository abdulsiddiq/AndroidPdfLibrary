package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.text.TextUtils;
import android.util.Log;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.PageClaimer;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.DBResourse;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.document.AdaptiveDoc;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks.Downloader;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks.DownloaderImpl;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

public class ProgressiveDownloadManagerImpl implements ProgressiveDownloadManager,DownloadCallbacks
{
    private PageClaimer mPageClaimer;
    private Downloader mDownloader;
    private AdaptiveDoc mAdaptiveDoc;

    private DownloadCallbacks _downloadListeners;

    private boolean mDownloadInProgress = false;


    public ProgressiveDownloadManagerImpl( PageClaimer pageClaimer, AdaptiveDoc adaptiveDoc ){
        mPageClaimer = pageClaimer;
        mDownloader = new DownloaderImpl(this);
        mAdaptiveDoc = adaptiveDoc;
    }

    @Override
    public void addToDownloads( PDFDownloadInfo... pdfDownloadInfos)
    {
        DBResourse.insertPdf(pdfDownloadInfos);
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
    public void addCallBackListener( DownloadCallbacks callback )
    {
        _downloadListeners = callback;
    }

    @Override
    public void onDownloadComplete(PDFDownloadInfo info )
    {
        if(_downloadListeners != null) _downloadListeners.onDownloadComplete(info);
        mPageClaimer.claimPage(info.pdfId);
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
        if(info != null)
        {
            startDownload();
        }
        else
        {
            PDFResourceLoader.stopProgressiveService();
            Log.d("donwload","stop service called");
        }
    }


}
