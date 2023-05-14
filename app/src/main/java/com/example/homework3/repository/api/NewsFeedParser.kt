package com.example.homework3.repository.api

import android.util.Xml
import com.example.homework3.model.NewsItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// We don't use namespaces.
private val ns: String? = null

class NewsFeedParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<NewsItem> {
        inputStream.use { inputStream1 ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream1, null)
            parser.nextTag()
            return readRss(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRss(parser: XmlPullParser): List<NewsItem> {
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        var entries: List<NewsItem> = ArrayList()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag.
            if (parser.name == "channel") {
                entries = readChannel(parser)
            } else {
                skip(parser)
            }
        }
        return entries
    }


    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readChannel(parser: XmlPullParser): List<NewsItem> {
        val entries = mutableListOf<NewsItem>()

        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "item") {
                entries.add(readItem(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }


    companion object {
        const val GUID = "guid"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val MEDIA_CONTENT = "media:content"
        const val DC_CREATOR = "dc:creator"
        const val PUB_DATE = "pubDate"
        const val LINK = "link"
        const val CATEGORY = "category"
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser): NewsItem {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var id = ""
        var title = ""
        var description = ""
        var imageUrl = ""
        var author = ""
        var publicationDate = Date()
        var fullArticleLink = ""
        val keywords = mutableListOf<String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                GUID -> id = readTag(parser, GUID)
                TITLE -> title = readTag(parser, TITLE)
                DESCRIPTION -> description = readTag(parser, DESCRIPTION)
                MEDIA_CONTENT -> {
                    val imageUrl1 = readImageUrl(parser)
                    if (imageUrl1 != null) imageUrl = imageUrl1
                }
                DC_CREATOR -> author = readTag(parser, DC_CREATOR)
                PUB_DATE -> publicationDate = convertStringToDate(readTag(parser, PUB_DATE))
                LINK -> fullArticleLink = readTag(parser, LINK)
                CATEGORY -> {
                    keywords.add(readTag(parser, CATEGORY))
                }
                else -> skip(parser)
            }
        }
        return NewsItem(
            id,
            title,
            description,
            imageUrl,
            author,
            publicationDate,
            fullArticleLink,
            keywords
        )
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImageUrl(parser: XmlPullParser): String? {
        var url = ""
        var mediumAttributeValue = ""
        var keyword: String? = null

        parser.require(XmlPullParser.START_TAG, ns, MEDIA_CONTENT)


        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i) == "medium") {
                mediumAttributeValue = parser.getAttributeValue(i)
            }

            if (parser.getAttributeName(i) == "url") {
                url = parser.getAttributeValue(i)
            }
        }

        if (!mediumAttributeValue.contains("image")) return null

        // The correct image tag contains a "media:keywords" tag with the value "headline"
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "media:keywords" -> keyword = readTag(parser, "media:keywords")
                else -> skip(parser)
            }
        }

        if (keyword?.contains("headline") == true) {
            return url
        }

        // Changed this to return url instead of null because it is the next best match
        return url
    }

    private fun convertStringToDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)

        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: ParseException) {
            // Parse this date format instead (some RSS feeds use this format)
            val replace = dateString.replace("Z", "+0000")

            dateFormat.parse(replace) ?: Date()
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val ret = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return ret
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}