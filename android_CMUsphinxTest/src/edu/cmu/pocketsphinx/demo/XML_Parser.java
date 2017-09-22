package edu.cmu.pocketsphinx.demo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XML_Parser {
	public static sphinx_model_config readXML(InputStream inStream) {
		   try {
		            //����������
		            SAXParserFactory spf = SAXParserFactory.newInstance();
		            SAXParser saxParser = spf.newSAXParser();
		 
		            //���ý�������������ԣ�true��ʾ���������ռ�����
		            //saxParser.setProperty("http://xml.org/sax/features/namespaces",true);
		            XMLContentHandler handler = new XMLContentHandler();
		            saxParser.parse(inStream, handler);
		            inStream.close();

		            return handler.get_sphinx_model_config();
		   } catch (Exception e) {
		            e.printStackTrace();
		   }

		  return null;
		}

		 
		
}
