package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive;

public interface PageClaimer
{
    String popKey();

    void claimPage( String pdfId);

    void onDownloadStart();
}
