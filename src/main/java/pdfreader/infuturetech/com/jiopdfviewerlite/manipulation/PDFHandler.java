package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntRange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.multipdf.Splitter;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.PDDocument;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

/**
 * Created by Krypto on 21-02-2018.
 */

public final class PDFHandler extends Handler
{
    private OnManipulateProgressListener _progressListener;

    private PDFDocWorker _worker;
    private HandlerThread _workerThread;

    public PDFHandler(Looper looper)
    {
        super(looper);
    }

    public PDFHandler prepareForDroidNative( String originalPdfPath,String userPassword) throws IOException
    {
        _workerThread = new HandlerThread("pdfworker");
        _workerThread.start();
        _worker = new PDFDocWorker(_workerThread.getLooper(),this);
        Bundle bundle = new Bundle();
        bundle.putString(PDFDocWorker.PASSWORD,userPassword);
        bundle.putString(PDFDocWorker.FILE_PATH,originalPdfPath);
        _worker.obtainMessage(PDFDocWorker.INIT,bundle).sendToTarget();
        return this;
    }

    public void splitAndSave(String outputFilePath,@IntRange(from = 1) int splitPageAt,String password)
    {
        Bundle bundle = new Bundle();
        bundle.putString(PDFDocWorker.PASSWORD,password);
        bundle.putString(PDFDocWorker.FILE_PATH,outputFilePath);
        bundle.putInt(PDFDocWorker.SPLIT_AT,splitPageAt);
        _worker.obtainMessage(PDFDocWorker.SPLIT,bundle).sendToTarget();
    }

    public PDFHandler setProgressListener(OnManipulateProgressListener listener)
    {
        _progressListener = listener;
        return this;
    }

    @Override
    public void handleMessage( Message msg )
    {
        super.handleMessage(msg);
        switch (msg.what)
        {
            case OnManipulateProgressListener.PROGRESS:
                _progressListener.onProgress(msg.arg1);
                break;
            case OnManipulateProgressListener.STATUS_ERROR:
                _progressListener.onProcessComplete(OnManipulateProgressListener.STATUS_ERROR,0,0);
                _workerThread.quit();
                break;
            case OnManipulateProgressListener.STATUS_COMPLETE:
                _progressListener.onProcessComplete(OnManipulateProgressListener.STATUS_COMPLETE,msg.arg1,msg.arg2);
                _workerThread.quit();
                break;
        }
    }


    private class PDFDocWorker extends Handler
    {
        private Handler _mainHandler;
        private PDDocument _doc;

        static final String PASSWORD = "ps";
        static final String FILE_PATH = "fp";
        static final String SPLIT_AT = "sa";

        static final int INIT = 1;
        static final int SPLIT = 2;

        public PDFDocWorker( Looper looper, Handler handler)
        {
            super(looper);
            _mainHandler = handler;
        }

        @Override
        public void handleMessage( Message msg )
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case INIT:
                    init(msg);
                    break;
                case SPLIT:
                    Bundle bundle  = (Bundle) msg.obj;
                    splitAndSave(bundle.getString(FILE_PATH),bundle.getInt(SPLIT_AT,1),bundle.getString(PASSWORD));
                    break;
            }
        }

        private void sendProgress(int progress)
        {
            _mainHandler.obtainMessage(OnManipulateProgressListener.PROGRESS,progress,0).sendToTarget();
        }


        private void init(Message msg)
        {
            try
            {
                Bundle bundle  = (Bundle) msg.obj;
                File orignalPdf = new File(bundle.getString(FILE_PATH));
                String userPassword = bundle.getString(PASSWORD);
                _doc = PDDocument.load(orignalPdf, userPassword);
                _doc.setAllSecurityToBeRemoved(true);

            }catch (IOException ex)
            {
                _mainHandler.obtainMessage(OnManipulateProgressListener.STATUS_ERROR,ex.getMessage()).sendToTarget();
            }
        }

        private void splitAndSave(String outputFilePath,@IntRange(from = 1) int splitPageAt,String password)
        {
            try
            {
                if(_doc != null) {
                    Splitter splitter = new Splitter();
                    splitter.setSplitAtPage(splitPageAt);

                    List<PDDocument> splittedDocs = splitter.split(_doc, new OnManipulateProgressListener() {
                        @Override
                        public void onProgress(int progress) {
                        sendProgress(progress);
                        }

                        @Override
                        public void onProcessComplete(int status, int numberOfFilesCreated, int totalPages) {

                        }
                    });

                    //Creating an iterator
                    Iterator<PDDocument> iterator = splittedDocs.listIterator();

                    int totalFiles = splittedDocs.size();
                    //Saving each page as an individual document
                    int numberOfFilesCreated = 1;
                    FileOutputStream outputStream = null;
                    String TEMP_FILE_NAME = "sep.pdf";
                    while (iterator.hasNext()) {
                        outputStream = PDFResourceLoader.getOutputStream(TEMP_FILE_NAME);
                        PDDocument pd = iterator.next();
                        pd.save(outputStream);
                        outputStream.flush();
                        outputStream.close();
                        pd.close();
                        PDFUtil.fileEncryption(PDFResourceLoader.getInputStream(TEMP_FILE_NAME), new File(outputFilePath + numberOfFilesCreated++ + ".pdf"), password);
                        sendProgress((int) (98+(numberOfFilesCreated-1 / (float) totalFiles) * 2));
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    _doc.close();
                    PDFResourceLoader.deleteTempFIle(TEMP_FILE_NAME);
//                _progressListener.onProcessComplete(OnManipulateProgressListener.STATUS_COMPLETE,numberOfFilesCreated,_doc.getNumberOfPages());
                    _mainHandler.obtainMessage(OnManipulateProgressListener.STATUS_COMPLETE, numberOfFilesCreated, _doc.getNumberOfPages()).sendToTarget();

                }
            }catch (IOException ex)
            {
                _mainHandler.obtainMessage(OnManipulateProgressListener.STATUS_ERROR,ex.getMessage()).sendToTarget();
            }
        }

    }
}
