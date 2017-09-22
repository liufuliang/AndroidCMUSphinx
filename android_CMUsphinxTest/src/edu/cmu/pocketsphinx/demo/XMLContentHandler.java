package edu.cmu.pocketsphinx.demo;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//SAX类：DefaultHandler，它实现了ContentHandler接口。在实现的时候，只需要继承该类，重载相应的方法即可。
public class XMLContentHandler extends DefaultHandler {

            private sphinx_model_config sphinx_model_config_1 = null;
            private List<xml_test_data> datas = null;
            private xml_test_data current_xml_test_data;
            private String tagName = null;//当前解析的元素标签

             public sphinx_model_config get_sphinx_model_config() {
                        return this.sphinx_model_config_1;
            }
 
            //接收文档开始的通知。当遇到文档的开头的时候，调用这个方法，可以在其中做一些预处理的工作。
            @Override
            public void startDocument() throws SAXException {
            	sphinx_model_config_1 = new sphinx_model_config();
            	datas = new ArrayList<xml_test_data>();
            	sphinx_model_config_1.set_xml_test_datas(datas);
            	
            }

             //接收元素开始的通知。当读到一个开始标签的时候，会触发这个方法。其中namespaceURI表示元素的命名空间；
            //localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集合
            @Override
            public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {


            			if(localName.equals("test_data")){
                        		current_xml_test_data = new xml_test_data();
                        }
                        

                        this.tagName = localName;
            }
 
            //接收字符数据的通知。该方法用来处理在XML文件中读到的内容，第一个参数用于存放文件的内容，
            //后面两个参数是读到的字符串在这个数组中的起始位置和长度，使用new String(ch,start,length)就可以获取内容。
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

             //接收文档的结尾的通知。在遇到结束标签的时候，调用这个方法。其中，uri表示元素的命名空间；
            //localName表示元素的本地名称（不带前缀）；name表示元素的限定名（带前缀）
            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {

                        if(localName.equals("test_data")){
                        	datas.add(current_xml_test_data);
                        	current_xml_test_data = null;
                        }

                        this.tagName = null;
            }
}