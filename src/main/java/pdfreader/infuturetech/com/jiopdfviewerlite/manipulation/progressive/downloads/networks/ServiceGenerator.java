package pdfreader.infuturetech.com.jiopdfviewerlite.manipulation.progressive.downloads.networks;

import retrofit2.Retrofit;

public class ServiceGenerator
{

    public static <S> S createGetService(Class<S> serviceClass,String baseUrl)
    {
        //String BASE_URL = "http://magsapi.media.jio.com/jiov2/jiomags-api-v2/v1/";
//        String BASE_URL = ApplicationURL.BASE_URL;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();
        return retrofit.create(serviceClass);
    }
}
