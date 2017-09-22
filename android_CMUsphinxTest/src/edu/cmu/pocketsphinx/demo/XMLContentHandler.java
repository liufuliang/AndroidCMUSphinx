package edu.cmu.pocketsphinx.demo;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//SAX�ࣺDefaultHandler����ʵ����ContentHandler�ӿڡ���ʵ�ֵ�ʱ��ֻ��Ҫ�̳и��࣬������Ӧ�ķ������ɡ�
public class XMLContentHandler extends DefaultHandler {

            private sphinx_model_config sphinx_model_config_1 = null;
            private List<xml_test_data> datas = null;
            private xml_test_data current_xml_test_data;
            private String tagName = null;//��ǰ������Ԫ�ر�ǩ

             public sphinx_model_config get_sphinx_model_config() {
                        return this.sphinx_model_config_1;
            }
 
            //�����ĵ���ʼ��֪ͨ���������ĵ��Ŀ�ͷ��ʱ�򣬵������������������������һЩԤ����Ĺ�����
            @Override
            public void startDocument() throws SAXException {
            	sphinx_model_config_1 = new sphinx_model_config();
            	datas = new ArrayList<xml_test_data>();
            	sphinx_model_config_1.set_xml_test_datas(datas);
            	
            }

             //����Ԫ�ؿ�ʼ��֪ͨ��������һ����ʼ��ǩ��ʱ�򣬻ᴥ���������������namespaceURI��ʾԪ�ص������ռ䣻
            //localName��ʾԪ�صı������ƣ�����ǰ׺����qName��ʾԪ�ص��޶�������ǰ׺����atts ��ʾԪ�ص����Լ���
            @Override
            public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {


            			if(localName.equals("test_data")){
                        		current_xml_test_data = new xml_test_data();
                        }
                        

                        this.tagName = localName;
            }
 
            //�����ַ����ݵ�֪ͨ���÷�������������XML�ļ��ж��������ݣ���һ���������ڴ���ļ������ݣ�
            //�������������Ƕ������ַ�������������е���ʼλ�úͳ��ȣ�ʹ��new String(ch,start,length)�Ϳ��Ի�ȡ���ݡ�
            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {

                        if(tagName != null){
                                    String data = new String(ch, start, length);
                                    
                                    if(tagName.equals("test_data_folder"))
                                    {
                                                this.sphinx_model_config_1.set_test_data_folder(data);
                                                
                                    }
                                    else if(tagName.equals("test_data_count"))
                                    {
                                    			int c = Integer.valueOf(data).intValue();
                                                this.sphinx_model_config_1.set_test_data_count(c);
                                    }
                                    else if(tagName.equals("file_name"))
                                    {
                                    	current_xml_test_data.set_file_name(data);
                                    	
                                    }
                                    else if(tagName.equals("result"))
                                    {
                                    	current_xml_test_data.set_result(data);
                                    	
                                    }
                        }
            }

             //�����ĵ��Ľ�β��֪ͨ��������������ǩ��ʱ�򣬵���������������У�uri��ʾԪ�ص������ռ䣻
            //localName��ʾԪ�صı������ƣ�����ǰ׺����name��ʾԪ�ص��޶�������ǰ׺��
            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {

                        if(localName.equals("test_data")){
                        	datas.add(current_xml_test_data);
                        	current_xml_test_data = null;
                        }

                        this.tagName = null;
            }
}