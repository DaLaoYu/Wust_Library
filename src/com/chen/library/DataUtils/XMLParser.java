package com.chen.library.DataUtils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLParser {
//Ϊ��ȡ�ķ���xml�ַ������ý��������õ���factoryģʽ
	public static void   setXMLParser(String xMLString) {
		//�½�һ��ContentHandler����
		MyContentHandler myContentHandler = new MyContentHandler();
		//��ȡ��������
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			//�ɹ��������ȡXMLReader����
			XMLReader reader = factory.newSAXParser().getXMLReader();
			//Ϊreader�������ݴ�����
			reader.setContentHandler(myContentHandler);
			try {
				//��ʼ����
				reader.parse(new InputSource(new StringReader(xMLString)));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	
		
	}

}
