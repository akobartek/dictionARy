package pl.sokolowskibartlomiej.languagesar.model

data class TranslationResult(val data: Translations)

data class Translations(val translations: List<TranslatedText>)

data class TranslatedText(val translatedText: String)
