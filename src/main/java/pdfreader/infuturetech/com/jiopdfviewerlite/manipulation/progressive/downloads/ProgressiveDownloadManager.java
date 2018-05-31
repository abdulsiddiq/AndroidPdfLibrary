package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

public interface ProgressiveDownloadManager
{
    void addToDownloads(String itemId,String baseUrl,int totalPages);

    void startDownload();

    boolean downloadInProgress();
}
