package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.storage;

import android.content.Context;
import android.util.Log;

import java.io.File;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

public class PathManager
{
    public static final String PATH_PREF = "pth";

    private static final String DATA_CACHE_FOLDER = "datacache";
    private static final String THUMBNAILS_FOLDER = "thumbnails";
    private final String ASSETS_FOLDER = ".assets";

    private static final float MB = 1024.0f * 1024.0f;

    private static boolean __DOWNLOAD_EXTERNAL__ = false;

    public static void SHOULD_DOWNLOAD_IN_EXTERNAL_SDCARD_IF_AVAILABLE(Context context, boolean shouldDownloadExternal)
    {
        __DOWNLOAD_EXTERNAL__ = shouldDownloadExternal;
    }

    private String __externalCachePath;
    private String __externalDataCachePath;
    private String __externalAssetsPath;
    private SDCardManager _sdCardManager;
    private Object _lockObject;

    public PathManager()
    {
        init();
        _lockObject = new Object();
        updateExternalStoragePath();
        _sdCardManager = new SDCardManager( __externalAssetsPath,ASSETS_FOLDER);
    }

    private void init()
    {
        __DOWNLOAD_EXTERNAL__ = false;
    }


    void recreateStoragePath()
    {
        synchronized (_lockObject)
        {
            refreshFolder(__externalCachePath);
            refreshFolder(__externalDataCachePath);
            refreshFolder(__externalAssetsPath);
            _sdCardManager = new SDCardManager(__externalAssetsPath, ASSETS_FOLDER);
        }
    }

    private void refreshFolder(String path)
    {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory())
        {
            folder.mkdirs();
        }
    }

    private void updateExternalStoragePath()
    {
        synchronized (_lockObject)
        {
            setCacheDirectory();
            setMediaDownloadDirectory();
        }
    }

    private void setMediaDownloadDirectory()
    {
        try
        {
            File file = PDFResourceLoader.getExternalFilesDir(null);
            String str = file.getAbsolutePath();
            __externalDataCachePath = checkAndCreateFolders(str, DATA_CACHE_FOLDER);
            __externalAssetsPath = checkAndCreateFolders(str, ASSETS_FOLDER);
        }
        catch (Exception ex)
        {
            __externalDataCachePath = createDirectoryOnAlternatePath(DATA_CACHE_FOLDER);
            __externalAssetsPath = createDirectoryOnAlternatePath(ASSETS_FOLDER);
        }
    }

    private void setCacheDirectory()
    {
        try
        {
            File f = PDFResourceLoader.getExternalCacheDir();
            float free = f.getFreeSpace() / MB;
            if (free < 100)
            {
//                Toast.makeText(context, "WARNING: Less than 100 MB space left. Please free some space to continue using app successfully.", Toast.LENGTH_LONG).show();

            }
            __externalCachePath = checkAndCreateFolders(f.getAbsolutePath(), THUMBNAILS_FOLDER);
        }
        catch (Exception ex)
        {
            __externalCachePath = null;

            __externalCachePath = createDirectoryOnAlternatePath(THUMBNAILS_FOLDER);
            //HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
        }
    }

    private String checkAndCreateFolders(String path, String folderName)
    {
        if(path != null)
        {
            File folder = new File(path, folderName);
            if (folder.exists())
            {
                if(folder.isDirectory())
                {
                    return folder.getAbsolutePath();
                }
                else
                {
                    folder.delete();
                }
            }

            if (folder.mkdir())
            {
                return folder.getAbsolutePath();
            }
            else
            {
                Log.d("SDCARD","ERROR: Unable to create folder: "+folderName+" on path " + path + ". Trying on alternate path.");
            }
        }
        return createDirectoryOnAlternatePath(folderName);
    }

    private String createDirectoryOnAlternatePath(String folderName)
    {
        File file = PDFResourceLoader.getFilesDir();
        if(file != null)
        {
            File folder = new File(file.getAbsolutePath(), folderName);
            if (folder.exists())
            {
                if(folder.isDirectory())
                {
                    return folder.getAbsolutePath();
                }
                else
                {
                    folder.delete();
                }
            }

            if (folder.mkdir())
            {
                return folder.getAbsolutePath();
            }
            else
            {
                Log.d("SDCARD", "ERROR: Unable to create folder: "+folderName+" on alternate path. " + folder.getAbsolutePath());
            }
        }
/*
        if(file == null)
        {
            Toast.makeText(_context, "ERROR: SDCard is either not present or is not available for system use. It could be unmounted or read-only.", Toast.LENGTH_LONG).show();
        }
*/
        return "";
    }


/*
    public String getThumbnailsFolder() throws NullPointerException
    {
        String str = null;
        synchronized (_lockObject)
        {
            if (__externalCachePath == null)
            {
                recreateStoragePath();
                if (__externalCachePath == null)
                {
                    Toast.makeText(_context, "ERROR: SDCard is either not present or is not available for system use. It could be unmounted or read-only.", Toast.LENGTH_LONG).show();
                    throw new SDCardNotAvailableException("SD Card not available. SD Card is either not present or is unmounted.");
                }
            }
            str = __externalCachePath + "/";
        }
        return str;
    }
*/

    public String getAssetsFolder() throws NullPointerException
    {
        String str = null;
        synchronized (_lockObject)
        {
            if (__externalAssetsPath == null)
            {
                recreateStoragePath();
                if (__externalAssetsPath == null)
                {
                    throw new SDCardNotAvailableException("SD Card not available. SD Card is either not present or is unmounted.");
                }
            }
            str = __externalAssetsPath + "/";
        }
        return str;
    }

/*
    public String getDataCacheFolder() throws NullPointerException
    {
        String str = null;
        synchronized (_lockObject)
        {
            if (__externalDataCachePath == null)
            {
                recreateStoragePath();
                if (__externalDataCachePath == null)
                {
                    Toast.makeText(_context, "ERROR: SDCard is either not present or is not available for system use. It could be unmounted or read-only.", Toast.LENGTH_LONG).show();
                    throw new SDCardNotAvailableException("SD Card not available. SD Card is either not present or is unmounted.");
                }
            }
            str = __externalDataCachePath + "/";
        }
        return str;
    }
*/

/*
    public File getThumbnailsFile(String fileName) throws NullPointerException
    {
        return new File(getThumbnailsFolder(), fileName);
    }

*/
    /*
    public File getMediaFile(String asset) throws NullPointerException
	{
		return new File(getMediaFolder(),asset);
	}*/

    public String getAssetFilePath(String asset) throws NullPointerException
    {
        File f = new File(getAssetsFolder(), asset);
        return f.getAbsolutePath();
    }

    public String getDownloadLocation(String asset)
    {
        if(__DOWNLOAD_EXTERNAL__)
        {
            String path = _sdCardManager.getPath(asset);
            if(path != null)
            {
                return path;
            }
        }
        return getAssetFilePath(asset);
    }

    public String getDownloadFolder(String asset)
    {
        String str = getDownloadLocation(asset);
        if (str != null)
        {
            File f = new File(str);
            if(f.exists() && !f.isDirectory())
            {
                f.delete();
            }
            f.mkdir();
        }
        return str;
    }
}
