package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import pdfreader.infuturetech.com.jiopdfviewerlite.R;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadCallbacks;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.IntentActions;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.IntentKeys;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.ProgressiveDownloadManager;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.ProgressiveDownloadManagerImpl;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.DBResourse;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.document.AdaptiveDoc;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.document.AdaptiveDocImpl;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.downloadutils.UserPreference;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.view.PDFPageView;

public class ProgressiveExtractor extends Service implements PageClaimer , LifecycleObserver
{

    public static String CONTENT_KEY = "";

    HashMap<String,PDFPageView> mPagesToLoad;
    Stack<String> mStackKeys;
    public static final int NOTIF_ID = 5;

    private IBinder myBinder = new ExtractorBinder();

    private ProgressiveDownloadManager mDownloadManager;

    private AdaptiveDoc mAdaptiveDocument;

    private NotificationManager mNotificationManager;
    private String mChannelId = "22";

    private ExtractorHandler mExtractorHandler;

    /**
     * Check in the priority stack if user has requested any page
     * @return
     * combined key of pdfitem and page number requested by user
     */
    @Override
    public String popKey()
    {
        try
        {
            return mStackKeys.pop();
        }catch (EmptyStackException ex)
        {
            return null;
        }
    }

    /**
     * On complete of downloading the page bitmap is returned from the
     * pdf page processed
     * @param pdfId
     * combined Id of pdf item and the page
     */
    @Override
    public void claimPage( String pdfId)
    {
        PDFPageView pageView = mPagesToLoad.get(pdfId);
        if(pageView != null)
        {
            Bitmap bitmap = mAdaptiveDocument.getPage(pdfId,pageView.getPageWidth(),pageView.getPageHeight());
            pageView.setBitmap(bitmap);
        }
    }

    @Override
    public void onDownloadStart()
    {
        showNotification();
    }

    @Nullable
    @Override
    public IBinder onBind( Intent intent )
    {
        return myBinder;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        handleIntents(intent);
        return START_REDELIVER_INTENT;
    }

    private void handleIntents( Intent intent )
    {
        String action = intent.getAction();
        String itemId = intent.getStringExtra(IntentKeys.ITEM_ID);
        String url = intent.getStringExtra(IntentKeys.DOWNLOAD_LINK);
        String baseDownloadurl = intent.getStringExtra(IntentKeys.DOWNLOAD_BASE_LINK);
        int totalPage = intent.getIntExtra(IntentKeys.TOTAL_PAGE,0);
        String contentKey = intent.getStringExtra(IntentKeys.PASS);
        String saveTo = intent.getStringExtra(IntentKeys.SAVE_FILE_TO);
        UserPreference.addPdfDetails(getApplicationContext(),itemId,baseDownloadurl,url,totalPage,contentKey,saveTo);

        Set<String> pagesAvailable = new HashSet<>();
        String[] pagesArray = new File(saveTo).list();

        if(pagesArray != null)
        {
            Collections.addAll(pagesAvailable, pagesArray);
        }

        if(action.equals(IntentActions.ACTION_DOWNLOAD))
        {
            ArrayList<PDFDownloadInfo> PDFInfos = new ArrayList<>();
            for (int index = 1; index <= totalPage; index++)
            {
                String combinedId = PDFUtil.combineId(itemId,index);
                if(!pagesAvailable.contains(combinedId+".pdf")) PDFInfos.add(new PDFDownloadInfo(combinedId, url));
            }
            PDFDownloadInfo[] arrayOfInfo = PDFInfos.toArray(new PDFDownloadInfo[0]);
            mDownloadManager.addToDownloads(arrayOfInfo);
            if(!mDownloadManager.downloadInProgress()) mDownloadManager.startDownload();
        }

    }

    /**
     * Application should call this method to demand for the page in between the pdf
     * @param itemId
     * the id of the pdf
     * @param pageView
     * the view to render the page
     */
    public void renderPage( String itemId, PDFPageView pageView )
    {
        mExtractorHandler.obtainMessage(100,new RenderMessageWrapper(pageView,itemId)).sendToTarget();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        HandlerThread thread = new HandlerThread("bg");
        thread.start();
        mExtractorHandler = new ExtractorHandler(thread.getLooper());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        DBResourse.init(getApplicationContext());
        mStackKeys = new Stack<>();
        mPagesToLoad = new HashMap<>();
        mAdaptiveDocument = new AdaptiveDocImpl();
        mDownloadManager = new ProgressiveDownloadManagerImpl(this,mAdaptiveDocument);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelName = "progressive_download";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(mChannelId, channelName, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        showNotification();

    }


    public class ExtractorBinder extends Binder
    {
        public ProgressiveExtractor getProgressiveExtractor()
        {
            return ProgressiveExtractor.this;
        }
    }

    public void removePage(String itemId, PDFPageView pageView)
    {
        String combinedId = PDFUtil.combineId(itemId,pageView.getIndex());
        mStackKeys.remove(combinedId);
        mPagesToLoad.remove(combinedId);
    }


    //In a service
    public void showNotification(){
        Intent notificationIntent = getApplication().getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            builder = new Notification.Builder(getApplicationContext(),mChannelId);
        }
        else {
            builder = new Notification.Builder(getApplicationContext());
        }
        Notification notification  = builder.setContentTitle(getString(R.string.app_name))
                .setContentText("Download in Progress")
                .setSmallIcon(R.drawable.placeholder)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        startForeground(NOTIF_ID,notification);
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
    public void onAppGoBackground()
    {
        if(!mDownloadManager.downloadInProgress())
        {
            stopSelf();
        }
    }


    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mNotificationManager.cancel(NOTIF_ID);
    }


    public void cancelDownload(String itemId)
    {
        mDownloadManager.cancelDownload(itemId);
    }

    private class ExtractorHandler extends Handler
    {
        ExtractorHandler( Looper looper )
        {
            super(looper);
        }

        @Override
        public void handleMessage( Message msg )
        {
            super.handleMessage(msg);
            RenderMessageWrapper wrapper = (RenderMessageWrapper) msg.obj;
            renderPage(wrapper.mItemId,wrapper.mPdfPageView);
        }

        private void renderPage( String itemId, PDFPageView pageView )
        {

            String combinedId = PDFUtil.combineId(itemId,pageView.getIndex()+1);
//        Check if available in downloaded pdf
            Bitmap bitmap = mAdaptiveDocument.getPage(combinedId,pageView.getPageWidth(),pageView.getPageHeight());


            if(bitmap != null)
            {
                pageView.setBitmap(bitmap);
                return;
            }

//        else add to download map
            mDownloadManager.addToDownloads(new PDFDownloadInfo(combinedId,UserPreference.getAppendUrl(getApplicationContext(),itemId)));
//        Add as priority
//        Remove if key is already available to rearrange the priority of the item
            mStackKeys.remove(combinedId);
            mStackKeys.push(combinedId);
            mPagesToLoad.put(combinedId,pageView);
            if(!mDownloadManager.downloadInProgress()) mDownloadManager.startDownload();
        }

    }

    private class RenderMessageWrapper
    {
        PDFPageView mPdfPageView;
        String mItemId;

        RenderMessageWrapper(PDFPageView pageView,String itemId)
        {
            mPdfPageView = pageView;
            mItemId = itemId;
        }
    }


    public void addProgressObserver( DownloadCallbacks callbacks )
    {
        mDownloadManager.addCallBackListener(callbacks);
    }

}
