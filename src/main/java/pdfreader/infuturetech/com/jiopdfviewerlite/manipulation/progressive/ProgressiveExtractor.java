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
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import pdfreader.infuturetech.com.jiopdfviewerlite.R;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.IntentActions;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.IntentKeys;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.ProgressiveDownloadManager;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.ProgressiveDownloadManagerImpl;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.DBResourse;
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
    public static final int NOTIF_ID = 500;

    private IBinder myBinder = new ExtractorBinder();

    private ProgressiveDownloadManager mDownloadManager;

    private AdaptiveDoc mAdaptiveDocument;

    private NotificationManager mNotificationManager;
    private String mChannelId = "22";

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
     * @param bitmap
     * the bitmap of the respective pdf page
     */
    @Override
    public void claimPage( String pdfId, Bitmap bitmap )
    {
        PDFPageView pageView = mPagesToLoad.get(pdfId);
        if(pageView != null)
        {
            pageView.setBitmap(bitmap);
        }
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
        if(IntentActions.ACTION_RENDER.equals(action))
        {
            String itemId = intent.getStringExtra(IntentKeys.ITEM_ID);
            String url = intent.getStringExtra(IntentKeys.DOWNLOAD_LINK);
            String baseDownloadurl = intent.getStringExtra(IntentKeys.DOWNLOAD_BASE_LINK);
            int totalPage = intent.getIntExtra(IntentKeys.TOTAL_PAGE,0);
            UserPreference.addPdfDetails(getApplicationContext(),itemId,baseDownloadurl,url,totalPage);
        }
        else if (IntentActions.ACTION_DOWNLOAD.equals(action))
        {
            String itemId = intent.getStringExtra(IntentKeys.ITEM_ID);
            String url = intent.getStringExtra(IntentKeys.DOWNLOAD_LINK);
            int totalPage = intent.getIntExtra(IntentKeys.TOTAL_PAGE,0);
            mDownloadManager.addToDownloads(itemId,url,totalPage);
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
        String combinedId = PDFUtil.combineId(itemId,pageView.getIndex()+1);
        Log.d("renderpdf","combined id = "+combinedId);
//        Check if available in downloaded pdf
        Bitmap bitmap = mAdaptiveDocument.getPage(combinedId);


        if(bitmap != null)
        {
            pageView.setBitmap(bitmap);
            return;
        }

//        else add to download map
        mDownloadManager.addToDownloads(combinedId,UserPreference.getAppendUrl(getApplicationContext(),itemId),
                UserPreference.getTotalPageCount(getApplicationContext(),itemId));

//        Add as priority
//        Remove if key is already available to rearrange the priority of the item
        mStackKeys.remove(combinedId);
        mStackKeys.push(combinedId);
        mPagesToLoad.put(combinedId,pageView);
        if(!mDownloadManager.downloadInProgress()) mDownloadManager.startDownload();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
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

    @Override
    public void unbindService( ServiceConnection conn )
    {
        super.unbindService(conn);
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
        mStackKeys.remove(pageView);
        mPagesToLoad.remove(PDFUtil.combineId(itemId,pageView.getIndex()));
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
                .setContentText("Extractor Service")
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
    public void onDestroy()
    {
        super.onDestroy();
        mNotificationManager.cancel(NOTIF_ID);
    }

}
