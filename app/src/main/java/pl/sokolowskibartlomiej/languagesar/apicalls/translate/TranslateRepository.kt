package pl.sokolowskibartlomiej.languagesar.apicalls.translate

class TranslateRepository(private val api: TranslateApi) {

    suspend fun translateText(text: String, sourceLang: String, targetLang: String): String =
        api.translateText(text, sourceLang, targetLang).data.translations[0].translatedText
}