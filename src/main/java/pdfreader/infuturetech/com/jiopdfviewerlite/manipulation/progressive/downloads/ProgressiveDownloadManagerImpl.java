package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

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

    private boolean mDownloadInProgress = false;

    private DownloadHandler mHandler;

    public ProgressiveDownloadManagerImpl( PageClaimer pageClaimer, AdaptiveDoc adaptiveDoc ){
        mPageClaimer = pageClaimer;
        mDownloader = new DownloaderImpl(this);
        mAdaptiveDoc = adaptiveDoc;
        HandlerThread downloadThread = new HandlerThread("downloadThread");
        downloadThread.start();
        mHandler = new DownloadHandler(downloadThread.getLooper());
    }

    @Override
    public void addToDownloads( String combinedId, String appendUrl, int totalPages )
    {
        PDFDownloadInfo[] PDFInfos = new PDFDownloadInfo[totalPages];
        for (int index = 1; index <= PDFInfos.length; index++)
        {
            PDFInfos[index - 1] = new PDFDownloadInfo(combinedId, appendUrl);
        }
        mHandler.obtainMessage(DownloadHandler.ADD, PDFInfos).sendToTarget();
    }

    @Override
    public void startDownload()
    {
        mHandler.obtainMessage(DownloadHandler.DOWNLAOD).sendToTarget();
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
        }
    }


    private class DownloadHandler extends android.os.Handler
    {
        final static int ADD = 100;
        final static int DOWNLAOD = 200;

        DownloadHandler( Looper looper )
        {
            super(looper);
        }

        @Override
        public void handleMessage( Message msg )
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case ADD:
                    DBResourse.insertPdf((PDFDownloadInfo[]) msg.obj);
                    break;
                case DOWNLAOD:
                    mDownloadInProgress = true;
                    PDFDownloadInfo item = getPriorityPdf();
                    if(item == null) item = DBResourse.itemToDownload();
                    mDownloader.download(item);
                    break;
            }
        }
    }

}
