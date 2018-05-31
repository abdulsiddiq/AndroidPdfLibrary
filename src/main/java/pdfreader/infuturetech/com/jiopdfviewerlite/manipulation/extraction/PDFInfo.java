package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.extraction;

/**
 * Created by Krypto on 23-02-2018.
 */

public class PDFInfo
{
    private String _path;
    private String _password;

    public PDFInfo(String path,String password)
    {
        _path = path;
        _password = password;
    }

    public String getPass()
    {
        return _password;
    }

    public String getPath()
    {
        return _path;
    }
}
