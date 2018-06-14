package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation;

import android.os.HandlerThread;
import android.util.Log;

import com.glidebitmappool.GlideBitmapPool;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.extraction.PDFDocumentsHandler;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.extraction.PDFInfo;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.extraction.PageViewProvider;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.view.PDFPageView;

/**
 * Created by Krypto on 22-02-2018.
 */

public class PDFPageExtractor implements PageViewProvider {

    private static final String TAG = "extractor";
    private final ArrayList<PDFPageView> _pageToBeLoadedList;
    private final ReentrantLock _lock = new ReentrantLock();
    private HandlerThread _pageExtractorThread;
    private PDFDocumentsHandler _pageHandler;


    //    private CacheManager _cacheManager;
    public PDFPageExtractor(int splitAt, PDFInfo... path) {
        _pageToBeLoadedList = new ArrayList<>();
        init(splitAt, path);
    }

    private void init(int splitAt, PDFInfo... path) {
        _pageExtractorThread = new HandlerThread("pageExtractor");
        _pageExtractorThread.start();
//        _cacheManager = new CacheManager();
        _pageHandler = new PDFDocumentsHandler(_pageExtractorThread.getLooper(), this)
                .init(splitAt, path);
        GlideBitmapPool.initialize(10 * 1024 * 1024); // 10mb max memory size
    }

    public void loadPage(PDFPageView pdfPageView) {
/*
        Message message = Message.obtain();
        message.obj = pdfPageView;
        _pageHandler.sendMessage(message);
*/

        _lock.lock();

        int index = _pageToBeLoadedList.indexOf(pdfPageView);
        Log.e("Load Page called ", "" + index);
        if (index != -1) {
            _pageToBeLoadedList.remove(index);
        }
        _pageToBeLoadedList.add(0, pdfPageView);

        if (_pageHandler != null) {
            _pageHandler.obtainMessage().sendToTarget();
        }

        _lock.unlock();
    }

    public void removePage(PDFPageView pageView) {
        _lock.lock();
        _pageToBeLoadedList.remove(pageView);
        _lock.unlock();
    }

    public void GC() {
        _lock.lock();
        if (_pageExtractorThread != null) {
            _pageExtractorThread.quit();
            _pageHandler.removeCallbacksAndMessages(null);
            _pageExtractorThread = null;
            _pageHandler = null;
            Log.e("GC called ", "GC called");
            GlideBitmapPool.clearMemory();
            GlideBitmapPool.shutDown();
        }
        _lock.unlock();
    }

    @Override
    public PDFPageView getPage() {
        _lock.lock();
        PDFPageView pageView = null;
        if (_pageToBeLoadedList.size() > 0) {

            pageView = _pageToBeLoadedList.remove(0);
        }
        _lock.unlock();
        return pageView;
    }
}
