package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DownloadAPIClass
{
    // option 2: using a dynamic URL
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync( @Url String fileUrl);
}
