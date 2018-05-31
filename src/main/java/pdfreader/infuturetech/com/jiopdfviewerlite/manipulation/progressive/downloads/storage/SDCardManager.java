package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.storage;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.File;

import pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util.PDFResourceLoader;

/**
 * Created by pushanpuri on 14/11/16.
 */

public class SDCardManager
{
    private String __externalAssetsPath;

    SDCardManager(String externalAssetsPath, String folderName)
    {
        __externalAssetsPath = externalAssetsPath;

        int apiVersion = Build.VERSION.SDK_INT;
        if (apiVersion >= Build.VERSION_CODES.KITKAT)
        {
            String path = getSDCardPath(folderName);
            if(path != null)
            {
                __externalAssetsPath = path;
            }
        }
    }

    @TargetApi(19)
    private String getSDCardPath(String folderName)
    {
        try
        {
            File f = PDFResourceLoader.getExternalFilesDir(null);
            File[] fs = PDFResourceLoader.getExternalFilesDirs(null);

            String internal = f.getAbsolutePath();
            String external = internal;

            for (int i = 0; i < fs.length; i++)
            {
                File loc = fs[i];
                if (loc != null && !loc.getAbsolutePath().equalsIgnoreCase(internal))
                {
                    external = loc.getAbsolutePath();
                }
            }
            external = checkAndCreateFolders(external, folderName);
            if (external == null)
            {
                external = checkAndCreateFolders(internal, folderName);
            }
            return external;
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    private String checkAndCreateFolders(String path, String folderName)
    {
        try
        {
            File folder = new File(path, folderName);
            if (folder.exists())
            {
                return folder.getAbsolutePath();
            }
            else
            {
                if (folder.mkdir())
                {
                    return folder.getAbsolutePath();
                }
            }
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    private String getPathForAsset(String path)
    {
        try
        {
            File folder = new File(__externalAssetsPath);
            if (!folder.exists())
            {
                if (!folder.mkdir())
                {
                    return null;
                }
            }
            return new File(__externalAssetsPath,path).getAbsolutePath();
        }
        catch (Exception ex)
        {

        }
        return null;
    }

    String getPath(String asset)
    {
        return getPathForAsset(asset);
    }
}
