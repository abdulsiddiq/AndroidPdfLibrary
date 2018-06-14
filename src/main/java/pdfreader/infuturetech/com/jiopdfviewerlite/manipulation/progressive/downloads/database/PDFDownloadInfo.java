package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadStatus;

/**
 * Created by Krypto on 12-03-2018.
 */

@Entity
public class PDFDownloadInfo
{
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "grp_id")
    public String pdfId;

    @ColumnInfo(name = "downloadurl")
    public String downloadUrl;

    @ColumnInfo(name = "status")
    @DownloadStatus
    public int downloadStatus = DownloadStatus.NOT_DOWNLOADED;

    @ColumnInfo(name = "size")
    public long fileSize = 0;


    public PDFDownloadInfo( String pdfId, String downloadUrl)
    {
        this.pdfId = pdfId;
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl()
    {
        return this.downloadUrl;
    }

    /**
     * This will be a combination of item id and the page number
     * To get item id or page number use the API
     * {@link pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil#getItemId(String)}
     * or
     * {@link pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil#getIndexFrom(String)}
     */
    public String getGroupId()
    {
        return this.pdfId;
    }


    public void setDownloadStatus(@DownloadStatus int downloadStatus)
    {
        this.downloadStatus = downloadStatus;
    }

    public int getDownloadStatus()
    {
        return this.downloadStatus;
    }

    public long getFileSize()
    {
        return this.fileSize;
    }

    public void setPath( String filePath )
    {
        this.downloadUrl = filePath;
    }
}
