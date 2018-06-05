package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.document;

import android.graphics.Bitmap;

public interface AdaptiveDoc
{
    void addPage(String combineId,String pagePath);

    Bitmap getPage( String combineId,int pageWidth,int pageHeight);
}
