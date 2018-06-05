package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadCallbacks;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.DownloadStatus;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.DBResourse;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.database.PDFDownloadInfo;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.pdmodel.PDDocument;
import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloaderImpl implements Downloader
{

    private String TAG = "downlaoderImp";

    private DownloadCallbacks _downloadCallbacks;

    public DownloaderImpl( DownloadCallbacks callbacks )
    {
        _downloadCallbacks = callbacks;
    }

    @Override
    public void download(final PDFDownloadInfo item )
    {
        if(item != null)
        {
            DBResourse.updateDownloadStatus(item, DownloadStatus.DOWNLOADING);
            int itemId = PDFUtil.getItemId(item.pdfId);
            int pageNumber = PDFUtil.getIndexFrom(item.pdfId);
            String baseUrl = PDFResourceLoader.getBaseUrl(String.valueOf(itemId));
            DownloadAPIClass downloadAPIClass = ServiceGenerator.createGetService(DownloadAPIClass.class,baseUrl);

            Call<ResponseBody> call = downloadAPIClass.downloadFileWithDynamicUrlSync(item.downloadUrl+pageNumber+".pdf");
            Log.d(TAG, "hit for page "+item.downloadUrl+pageNumber);

            call.enqueue(new Callback<ResponseBody>()
            {
                @Override
                public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response )
                {
                    if (response.isSuccessful())
                    {
                        Log.d(TAG, "server contacted and has file");

                        boolean writtenToDisk = writeResponseBodyToDisk(response.body(), item);
                        if (writtenToDisk)
                        {
//                            DBResourse.updateDownloadStatus(item, DownloadStatus.DOWNLOADED);
                            DBResourse.removeGroup(item.pdfId);
                            _downloadCallbacks.onDownloadComplete(item);
                        } else
                        {
                            _downloadCallbacks.onDownloadError(item);
                        }

                        Log.d(TAG, "file download was a success? " + writtenToDisk);
                    } else
                    {
                        Log.d(TAG, "server contact failed");
                        _downloadCallbacks.onDownloadError(item);
                    }
                }

                @Override
                public void onFailure( Call<ResponseBody> call, Throwable t )
                {
                    Log.e(TAG, "error");
                    _downloadCallbacks.onDownloadError(item);
                }
            });
        }
        else
        {
            _downloadCallbacks.onDownloadError(item);
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body,PDFDownloadInfo pdfDownloadInfo) {
        try {
            // todo change the file location/name according to your needs
//            Create Download Folder
            File futureStudioIconFile = new File(PDFUtil.getDownloadFolder(PDFUtil.getItemId(pdfDownloadInfo.pdfId)));

            if(!futureStudioIconFile.exists())
            {
                futureStudioIconFile.mkdirs();
            }

            File pdfPageFile = new File(futureStudioIconFile,pdfDownloadInfo.pdfId+".pdf");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
//                outputStream = new FileOutputStream(pdfPageFile);

                FileOutputStream fileOutputStream = PDFResourceLoader.getOutputStream("unencrypted.pdf");

                PDDocument document = PDDocument.load(inputStream,PDFResourceLoader.getPassword(pdfDownloadInfo.pdfId));
                document.setAllSecurityToBeRemoved(true);

                document.save(fileOutputStream);
                document.close();

                fileOutputStream.flush();
                fileOutputStream.close();
                FileInputStream fileInputStream = PDFResourceLoader.getInputStream("unencrypted.pdf");
                PDFUtil.fileEncryption(fileInputStream,pdfPageFile,PDFResourceLoader.getPassword(pdfDownloadInfo.pdfId));

                pdfDownloadInfo.downloadUrl = pdfPageFile.getAbsolutePath();

/*
                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
*/

//                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
