package edu.cmu.pocketsphinx.demo;

public class xml_test_data {
	//子目录名和测试文件名
	private String file_name = null;

	//测试结果
	private String result = null;
	
	public void set_file_name(String f)
	{
		this.file_name = f;
	}
	
	public String get_file_name()
	{
		return this.file_name;
	}
	
	public void set_result(String r)
	{
		this.result = r;
	}
	
	public String get_result()
	{
		return this.result;
	}
}
