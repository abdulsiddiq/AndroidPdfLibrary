package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class PDFDownloadPageInfo
{
    @PrimaryKey (autoGenerate = true)
    public int pageDownloadId;

    @ColumnInfo (name = "grp_id")
    String pdfId;

    @ColumnInfo (name = "page_no")
    int pageNumber;


    PDFDownloadPageInfo(String pdfId,int pageNumber)
    {
        this.pdfId = pdfId;
        this.pageNumber = pageNumber;
    }

}
