package pl.mobilization.konfeocheckin

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by marekdef on 27.12.17.
 */


class JsoupConverterFactory : Converter.Factory() {

    object CONVERTER : Converter<ResponseBody, Document> {
        override fun convert(responseBody: ResponseBody): Document = Jsoup.parse(responseBody.string());
    }

    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        if(type.equals(Document::class.java))
            return CONVERTER
        return null
    }
}