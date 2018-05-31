package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@StringDef ({IntentActions.ACTION_DOWNLOAD,IntentActions.ACTION_RENDER})
@Retention (RetentionPolicy.SOURCE)
public @interface IntentActions
{
    String ACTION_DOWNLOAD = "d";
    String ACTION_RENDER = "r";
}
