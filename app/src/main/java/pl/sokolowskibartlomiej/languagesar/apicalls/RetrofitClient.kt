package pl.sokolowskibartlomiej.languagesar.apicalls

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.sokolowskibartlomiej.languagesar.apicalls.translate.TranslateApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private const val BASE_URL_TRANSLATE_API =
        "https://translation.googleapis.com/language/translate/"
    private var translateApiClient: Retrofit? = null
    val translateApi: TranslateApi = getTranslateApiClient().create(TranslateApi::class.java)

    private fun getTranslateApiClient(): Retrofit {
        if (translateApiClient === null) {
            translateApiClient = Retrofit.Builder()
                .baseUrl(BASE_URL_TRANSLATE_API)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(getOkHttpClient())
                .build()
        }
        return translateApiClient!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder().build()
                chain.proceed(request)
            }
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
}