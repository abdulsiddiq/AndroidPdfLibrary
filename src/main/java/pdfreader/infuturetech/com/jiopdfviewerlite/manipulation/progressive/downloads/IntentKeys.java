package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef ({IntentKeys.DOWNLOAD_LINK,IntentKeys.SAVE_FILE_TO,IntentKeys.TOTAL_PAGE,IntentKeys.PASS})
@Retention(RetentionPolicy.SOURCE)
public @interface IntentKeys
{
    String DOWNLOAD_LINK = "dl";
    String DOWNLOAD_BASE_LINK = "dbl";
    String TOTAL_PAGE = "tp";
    String SAVE_FILE_TO = "sft";
    String ITEM_ID = "iid";
    String PASS = "pass";
}
