package edu.cmu.pocketsphinx.demo;

import java.util.List;

public class sphinx_model_config {
	//测试数据的根目录
	private String test_data_folder = null;
	
	//测试数据的个数
	private int test_data_count = 0;
	
	//测试数据
	private List<xml_test_data> xml_test_datas = null;
	
	public void set_test_data_folder(String f)
	{
		this.test_data_folder = f;
	}
	
	public String get_test_data_folder()
	{
		return this.test_data_folder;
	}
	
	public void set_test_data_count(int c)
	{
		this.test_data_count = c;
	}
	
	public int get_test_data_count()
	{
		return this.test_data_count;
	}
	
	public void set_xml_test_datas(List<xml_test_data> datas)
	{
		this.xml_test_datas = datas;
	}
	
	public List<xml_test_data> get_xml_test_datas()
	{
		return this.xml_test_datas;
	}
	

}
