package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.downloadutils;

import android.content.Context;
import android.content.SharedPreferences;

import pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.IntentKeys;

public class UserPreference
{
    private static final String PREF_NAME = "pn";
    public static void addPdfDetails( Context context,String itemId, String baseDownloadUrl,String appendUrl,int totalPage )
    {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(IntentKeys.DOWNLOAD_BASE_LINK+itemId,baseDownloadUrl);
        editor.putString(IntentKeys.DOWNLOAD_LINK+itemId,appendUrl);
        editor.putInt(IntentKeys.TOTAL_PAGE+itemId,totalPage);
        editor.apply();
    }

    public static String getDownloadUrl(Context context, String itemId)
    {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return preferences.getString(IntentKeys.DOWNLOAD_BASE_LINK+itemId,"");
    }

    public static String getAppendUrl(Context context, String itemId)
    {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return preferences.getString(IntentKeys.DOWNLOAD_LINK+itemId,"");
    }

    public static int getTotalPageCount(Context context, String itemId)
    {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return preferences.getInt(IntentKeys.TOTAL_PAGE+itemId,0);
    }

}
