package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.glidebitmappool.GlideBitmapFactory;
import com.glidebitmappool.GlideBitmapPool;

import pdfreader.infuturetech.com.jiopdfviewerlite.R;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.PDFPageLoadCallbacks;

/**
 * Created by Krypto on 23-02-2018.
 */

public class PDFPageView extends RelativeLayout
{

    private int _pageIndex;
    private AppCompatImageView _pdfPage;
    private PDFPageLoadCallbacks pdfPageLoadCallbacks;
    private int pageWidth,pageHeight;

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public void setPdfPageLoadCallbacks(PDFPageLoadCallbacks pdfPageLoadCallbacks) {
        this.pdfPageLoadCallbacks = pdfPageLoadCallbacks;
    }

    public PDFPageView(Context context,boolean canScroll)
    {
        super(context);
        init(context,canScroll);
    }

    public PDFPageView( Context context, AttributeSet attrs )
    {
        super(context, attrs);
        init(context,false);
    }


    private void init(Context context,boolean canScroll)
    {
       inflate(context, canScroll
                       ? R.layout.pdf_page_scrollable
                       : R.layout.pdfpage
               ,this);
       _pdfPage = findViewById(R.id.page);
    }

    public PDFPageView setPageIndex(int pageIndex)
    {
        _pageIndex = pageIndex;
        return this;
    }

    public void setScaleType( ImageView.ScaleType scaleType)
    {
        _pdfPage.setScaleType(scaleType);
    }

    public void setCustomImageView(AppCompatImageView imageView)
    {
        _pdfPage = imageView;
    }

    @Override
    public int hashCode()
    {
        return _pageIndex;
    }

    public int getIndex()
    {
        return _pageIndex;
    }

    public void setBitmap( final Bitmap bm )
    {
        _pdfPage.post(new Runnable()
        {
            @Override
            public void run()
            {

                recycleBitmap();
                _pdfPage.setImageBitmap(bm);
                if(pdfPageLoadCallbacks != null) pdfPageLoadCallbacks.loadSuccessful();
            }
        });
    }
    public void setBitmap( Resources resources, @DrawableRes int resId)
    {
        setBitmap(GlideBitmapFactory.decodeResource(resources,resId));
    }

    public void recycleBitmap()
    {
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) _pdfPage
                .getDrawable());

        Log.d("recycle","bitmap = "+(bitmapDrawable!=null));
        if(bitmapDrawable != null)
        {
//            bitmapDrawable.getBitmap().recycle();
            GlideBitmapPool.putBitmap(bitmapDrawable.getBitmap());
        }
    }

}
