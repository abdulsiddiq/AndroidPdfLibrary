package pdfreader.infuturetech.com.jiopdfviewerlite.pdflite.pdfbox;

/**
 * Created by Krypto on 26-01-2018.
 */

public class LogFactory
{

    public static Log getLog( Class<?> filterClass )
    {
        return new Log(filterClass.getName());
    }


    public static class Log
    {
        String TAG;

        Log(String tag)
        {
            TAG = tag;

        }

        public void error(String message)
        {
            android.util.Log.e(TAG,message);
        }

        public void error(Exception ex1,Exception ex2)
        {
            android.util.Log.e(TAG,"ex 1 = "+ex1.getMessage()+" ex2 ="+ex2.getMessage());
        }

        public void error(String error1,Exception ex2)
        {
            android.util.Log.e(TAG,"ex 1 = "+error1+" ex2 ="+ex2.getMessage());
        }

        public void warn(String error1,Exception ex2)
        {
            android.util.Log.w(TAG,"ex 1 = "+error1+" ex2 ="+ex2.getMessage());
        }

        public void warn(String message)
        {
            android.util.Log.w(TAG,message);
        }

        public void debug(String message)
        {
            android.util.Log.d(TAG,message);
        }

        public void debug(String message,Exception ex)
        {
            android.util.Log.d(TAG,message + " exception = "+ ex.getMessage());
        }

        public void info(String message)
        {
            android.util.Log.i(TAG,message);
        }

        public void verbose(String message)
        {
            android.util.Log.v(TAG,message);
        }

        public static boolean isDebugEnabled()
        {
            return false;
        }

    }

}
