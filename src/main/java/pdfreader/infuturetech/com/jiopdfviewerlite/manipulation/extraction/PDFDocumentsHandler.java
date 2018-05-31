package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.extraction;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.glidebitmappool.GlideBitmapPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.view.PDFPageView;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

/**
 * Created by Krypto on 22-02-2018.
 */

public class PDFDocumentsHandler extends Handler
{
    private final static String TAG = "PDFDocumentHandler";

    private final int MIN_INDEX = 0;
    private final int MAX_INDEX = 1;


    private PdfRenderer _currentDocument;

    private PDFInfo[] _pdfsPath;
    private int _currentPDFIndex;
    private int _splitAt,
            _splitEquivalentIndexDifference,
            _totalPageCount = 0;

    private ParcelFileDescriptor parcelFileDescriptor = null;

    private PageViewProvider _pageProvider;

    private int[][] _pageRange;

    public PDFDocumentsHandler( Looper looper, PageViewProvider provider)
    {
        super(looper);
        _pageProvider = provider;
    }

    public PDFDocumentsHandler init( int splitAt,PDFInfo... path )
    {
        _pdfsPath = new PDFInfo[path.length];
        _pdfsPath = path;
        _currentPDFIndex = 0;
        _splitAt = splitAt;

        _totalPageCount = _pdfsPath.length * _splitAt;

        calculateMinAndMax(path.length);
        loadPDFToCurrentDoc();
        return this;
    }

    /**
     * Calculate the range of page number for every pdf in
     * int[pdfIndex][rangeOfPdf at given pdfIndex]
     * @param numberOfPdfs
     */
    private void calculateMinAndMax(int numberOfPdfs)
    {
        _pageRange = new int[numberOfPdfs][2];

        _pageRange[0][MAX_INDEX] = _splitAt;
        _pageRange[0][MIN_INDEX] = 0;

        for (int index = 1;index < numberOfPdfs;index++)
        {
            _pageRange[index][MAX_INDEX] = _pageRange[index-1][MAX_INDEX] + _splitAt;
            _pageRange[index][MIN_INDEX] = _pageRange[index-1][MIN_INDEX] + _splitAt;
        }
    }

    @Override
    public void handleMessage( Message msg )
    {
        super.handleMessage(msg);
        setPage();
    }

    public void setPage()
    {
        PDFPageView pageToSet = _pageProvider.getPage();
        if (pageToSet != null)
        {
            int pageIndex = pageToSet.getIndex();

            if (pageIndex >= _totalPageCount)
            {
                return;
            }

            pageIndex = loadPdfIfNecessary(pageIndex);

            if (_currentDocument != null)
            {

                PdfRenderer.Page page = _currentDocument.openPage(pageIndex);
                Bitmap bm = GlideBitmapPool.getDirtyBitmap(Math.round(pageToSet.getPageWidth()), Math.round(pageToSet.getPageHeight()), Bitmap.Config.ARGB_8888);
                page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                page.close();
                pageToSet.setBitmap(bm);
            } else
            {
                Log.e(TAG, "current doc is null");
            }
        }
    }

    private boolean isValidIndex( int pageIndex )
    {
        return pageIndex >= 0 &&
                pageIndex < _pageRange[_currentPDFIndex][MAX_INDEX] + _currentDocument.getPageCount();
    }

    /**
     * Checks if the page Index requested falls under the range of the
     * currently loaded pdf containing pages. If not the respective pdf file is
     * loaded.
     *
     * @param pageIndex
     * @throws IOException
     */
    private int loadPdfIfNecessary( int pageIndex )
    {
        if (pageIndex < 0)
        {
            Log.e("pdfViewer", "Index cannot be less than zero");
            return 0;
        }

        int maxrange = _pageRange[_currentPDFIndex][MAX_INDEX];
        int minRange = _pageRange[_currentPDFIndex][MIN_INDEX];

//        check if the index is within the range of the loaded pdf
        if (pageIndex >= minRange && pageIndex < maxrange)
        {
            return getEquivalentPageIndex(pageIndex);
        }

        if (pageIndex >= maxrange)
        {
            increasePdfIndex(pageIndex);
        }

        if (pageIndex < minRange)
        {
            decreasePdfIndex(pageIndex);
        }

        loadPDFToCurrentDoc();
        return getEquivalentPageIndex(pageIndex);
    }


    private void increasePdfIndex( int pageIndex)
    {
        for (int index = _currentPDFIndex; index < _pageRange.length;index++)
        {
            if(pageIndex < _pageRange[index][MAX_INDEX])
            {
                _currentPDFIndex = index;
                return;
            }
        }
        _currentPDFIndex = _pdfsPath.length - 1;
    }

    private void decreasePdfIndex( int pageIndex)
    {
        for (int index = _currentPDFIndex; index >= 0; index--)
        {
            if(pageIndex >= _pageRange[index][MIN_INDEX])
            {
                _currentPDFIndex = index;
                return;
            }
        }
        _currentPDFIndex = 0;
    }

    private void loadPDFToCurrentDoc()
    {
        try
        {
            if (_currentPDFIndex < _pdfsPath.length)
            {
//                    Find the path of PDF for the given page Index
                PDFInfo pdfInfo = _pdfsPath[_currentPDFIndex];
//                _splitEquivalentIndexDifference = _currentPDFIndex * _splitAt;
                _splitEquivalentIndexDifference = _pageRange[_currentPDFIndex][MIN_INDEX];

//           Decrypt the pdf and store in secure place
                final FileOutputStream outputStream = PDFResourceLoader.getOutputStream("tempPdf.pdf");
                PDFUtil.fileDecryption(pdfInfo.getPass(), new File(pdfInfo.getPath()), outputStream);

//           Load the decrypted pdf into renderer
                FileInputStream fileInputStream = PDFResourceLoader.getInputStream("tempPdf.pdf");

                if (parcelFileDescriptor != null)
                {
                    parcelFileDescriptor.close();
                }
                parcelFileDescriptor = ParcelFileDescriptor.dup(fileInputStream.getFD());

                _currentDocument = new PdfRenderer(parcelFileDescriptor);

//            parcelFileDescriptor.close();
                outputStream.flush();
                outputStream.close();
                fileInputStream.close();
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }


    private int getEquivalentPageIndex( int pageIndex )
    {
        pageIndex = _splitEquivalentIndexDifference - pageIndex;
        return Math.abs(pageIndex);
    }

}
