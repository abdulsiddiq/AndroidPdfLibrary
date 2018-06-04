package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.ProgressiveExtractor;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.downloadutils.UserPreference;
import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.utils.PDFUtil;

public class PDFResourceLoader
{
	/**
	 * The Context of the main app
	 */
	private static Context CONTEXT = null;
	
	/**
	 * The AssetManager used to load the resources
	 */
    private static AssetManager ASSET_MANAGER = null;
    
    /**
     * Whether an uninitialized warning has already been given
     */
    private static boolean hasWarned = false;

    /**
     * Initializes the loader
     * 
     * @param context the context of the main app
     */
    public static void init(Context context){
        if(CONTEXT == null) {
            CONTEXT = context.getApplicationContext();
            ASSET_MANAGER = CONTEXT.getAssets();
        }
    }
    
    /**
     * Checks whether the loader has been initialized
     * 
     * @return whether the loader has been initialized or not
     */
    public static boolean isReady() {
    	if(ASSET_MANAGER == null && !hasWarned) {
    		Log.w("PdfBoxAndroid", "Call PDFResourceLoader.init() first to decrease resource load time");
    		hasWarned = true;
    	}
    	return ASSET_MANAGER != null;
    }

    /**
     * Loads a resource file located in the assets folder
     * 
     * @param path the path to the resource
     * @return the resource as an InputStream
     * @throws IOException if the resource cannot be found
     */
    public static InputStream getStream(String path) throws IOException {
        return ASSET_MANAGER.open(path);
    }

    public static FileOutputStream getOutputStream( String filename) throws IOException {
        return CONTEXT.openFileOutput(filename,Context.MODE_PRIVATE);
    }

    public static File getFileStreamPath( String filename){
        return CONTEXT.getFileStreamPath(filename);
    }

    public static FileInputStream getInputStream( String filename) throws IOException {
        return CONTEXT.openFileInput(filename);
    }

    public static void deleteTempFIle( String filename) throws IOException {
        CONTEXT.deleteFile(filename);
    }

    public static int getCurrentDPI() {
        return CONTEXT.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * Get the external app cache directory.
     *
     * @return The external cache dir
     */
    public static String getCacheDir() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                || ! PDFUtil.isExternalStorageRemovable()
                ? CONTEXT.getExternalCacheDir().getPath()
                : CONTEXT.getCacheDir().getPath();
    }

    public static File getFilesDir()
    {
        return CONTEXT.getFilesDir();
    }

    public static File getExternalFilesDir( String path)
    {
        return CONTEXT.getExternalFilesDir(path);
    }

    public static File[] getExternalFilesDirs( String type)
    {
        return CONTEXT.getExternalFilesDirs(type);
    }

    public static File getExternalCacheDir()
    {
        return CONTEXT.getExternalCacheDir();
    }

    public static String getAppendUrl( String itemId )
    {
        return UserPreference.getAppendUrl(CONTEXT,itemId);
    }

    public static String getBaseUrl( String itemId)
    {
        return UserPreference.getDownloadUrl(CONTEXT,itemId);
    }

    public static void stopProgressiveService()
    {
        CONTEXT.stopService(new Intent(CONTEXT,ProgressiveExtractor.class));
    }
}
