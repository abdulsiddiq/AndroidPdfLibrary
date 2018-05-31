package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Krypto on 14-03-2018.
 */
@Retention( RetentionPolicy.SOURCE)
@IntDef({DownloadStatus.DOWNLOADED,DownloadStatus.DOWNLOADING,DownloadStatus.NOT_DOWNLOADED})
public @interface DownloadStatus
{
    int DOWNLOADED = 2;
    int DOWNLOADING = 1;
    int NOT_DOWNLOADED = 0;
}
