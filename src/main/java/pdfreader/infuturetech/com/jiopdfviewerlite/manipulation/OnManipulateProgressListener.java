package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Krypto on 21-02-2018.
 */

public interface OnManipulateProgressListener
{
    int STATUS_COMPLETE = 200;
    int STATUS_ERROR = 300;
    int PROGRESS = 400;

    @IntDef({STATUS_COMPLETE,STATUS_ERROR})
    @Retention( RetentionPolicy.SOURCE)
    @interface PDFProcess
    {

    }

    void onProgress(int progress);

    void onProcessComplete(@PDFProcess int status,int numberOfFilesCreated,int totalNoOfPages);
}
