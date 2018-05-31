package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Krypto on 12-03-2018.
 */

@Database(entities = {PDFDownloadInfo.class}, version =  1, exportSchema = false)
public abstract class DownloadDatabase extends RoomDatabase
{
    public abstract PDFDAO downloadDAO();
}
