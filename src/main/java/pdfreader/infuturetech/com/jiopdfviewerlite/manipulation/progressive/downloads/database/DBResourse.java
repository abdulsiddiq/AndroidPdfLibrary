package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database;

import android.arch.persistence.room.Room;
import android.content.Context;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadStatus;

/**
 * Created by Krypto on 12-03-2018.
 */

public class DBResourse
{
    private static DownloadDatabase _downloadDB;

    private DBResourse(){}

    public static void init( Context context )
    {
        _downloadDB = Room.databaseBuilder(context,
                DownloadDatabase.class, "pdf_db")
                .allowMainThreadQueries()
                .build();
    }


    public static DownloadDatabase getDB()
    {
        return _downloadDB;
    }

    public static void insertPdf( PDFDownloadInfo... item)
    {
        _downloadDB.downloadDAO().insertPdf(item);
    }

    public static void removeAll()
    {
        _downloadDB.downloadDAO().deleteAll();
    }

    public static void removeGroup(int groupId)
    {
        _downloadDB.downloadDAO().deleteGroup(groupId);
    }

    public static void updateDownloadStatus( PDFDownloadInfo item, @DownloadStatus int status)
    {
        _downloadDB.downloadDAO().updateStatus(item.getGroupId(),status);
    }

    public static PDFDownloadInfo itemToDownload( String groupId)
    {
        return _downloadDB.downloadDAO().getItemToDownload(groupId);
    }

    public static PDFDownloadInfo itemToDownload()
    {
        return _downloadDB.downloadDAO().getItemToDownload();
    }

    public static void updateFileSize(float fileSize, int downloadId)
    {
        _downloadDB.downloadDAO().setSize(fileSize,downloadId);
    }

    public static boolean isItemAvailable( int groupId )
    {
        return _downloadDB.downloadDAO().isItemAvailable(groupId) != 0;
    }
}
