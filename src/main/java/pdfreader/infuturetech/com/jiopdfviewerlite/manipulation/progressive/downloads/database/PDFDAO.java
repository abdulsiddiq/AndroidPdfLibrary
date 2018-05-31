package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.List;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadStatus;

/**
 * Created by Krypto on 12-03-2018.
 */
@Dao
public abstract class PDFDAO
{
    @Query("Select * from PDFDownloadInfo group by grp_id")
    public abstract List<PDFDownloadInfo> getAllGroup();

    @Query("Select grp_id from PDFDownloadInfo where status LIKE "+ DownloadStatus.NOT_DOWNLOADED+" group by grp_id LIMIT 1")
    public abstract int getGroupToDownload();

    @Nullable
    @Query("Select * from PDFDownloadInfo where status <> "+ DownloadStatus.DOWNLOADED+" and grp_id LIKE :groupId LIMIT 1")
    public abstract PDFDownloadInfo getItemToDownload( String groupId );

   @Nullable
    @Query("Select * from PDFDownloadInfo where status <> "+ DownloadStatus.DOWNLOADED+" LIMIT 1")
    public abstract PDFDownloadInfo getItemToDownload();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertPdf( PDFDownloadInfo... item);

    @Delete
    public abstract void delete( PDFDownloadInfo item );

    @Query("Delete from PDFDownloadInfo where grp_id Like :groupId")
    public abstract void deleteGroup( int groupId );

    @Query("Delete From PDFDownloadInfo")
    public abstract void deleteAll();

    @Query("UPDATE PDFDownloadInfo set status = :status where grp_id LIKE :downloadId")
    public abstract void updateStatus( String downloadId, @DownloadStatus int status );

    @Query("Update PDFDownloadInfo set size = :size where grp_id LIKE :downloadId")
    public abstract void setSize( float size, int downloadId );

    @Query("Select grp_id from PDFDownloadInfo where status LIKE "+ DownloadStatus.NOT_DOWNLOADED+" AND grp_id LIKE :groupId LIMIT 1")
    public abstract long isItemAvailable( int groupId );
}
