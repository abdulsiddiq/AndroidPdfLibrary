package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive;

import android.graphics.Bitmap;

public interface PageClaimer
{
    String popKey();

    void claimPage( String pdfId, Bitmap bitmap);
}
