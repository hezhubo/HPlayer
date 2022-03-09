package com.hezb.clingupnp

import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.xml.sax.XMLReader
import javax.xml.parsers.SAXParserFactory

/**
 * Project Name: HPlayer
 * File Name:    AndroidDIDLParser
 *
 * Description: XML解析器 处理org.xml.sax.SAXNotRecognizedException.
 * 见github issues https://github.com/4thline/cling/issues/249
 *
 * @author  hezhubo
 * @date    2022年03月06日 17:38
 */
class AndroidDIDLParser : DIDLParser() {

    override fun create(): XMLReader {
        return try {
            val factory = SAXParserFactory.newInstance()

            // Configure factory to prevent XXE attacks
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // factory.setXIncludeAware(false);
            // factory.setNamespaceAware(true);
            if (schemaSources != null) {
                factory.schema = createSchema(schemaSources)
            }
            val xmlReader = factory.newSAXParser().xmlReader
            xmlReader.errorHandler = errorHandler
            xmlReader
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }

}