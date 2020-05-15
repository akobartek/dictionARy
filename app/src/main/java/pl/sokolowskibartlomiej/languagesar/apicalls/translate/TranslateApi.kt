package pl.sokolowskibartlomiej.languagesar.apicalls.translate

import pl.sokolowskibartlomiej.languagesar.BuildConfig
import pl.sokolowskibartlomiej.languagesar.model.TranslationResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TranslateApi {

    @FormUrlEncoded
    @POST("v2?key=${BuildConfig.TRANSLATE_API_KEY}")
    suspend fun translateText(
        @Field("q") textToTranslate: String,
        @Field("source") sourceLang: String,
        @Field("target") targetLang: String
    ): TranslationResult
}