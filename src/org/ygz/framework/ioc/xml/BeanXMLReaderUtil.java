package org.ygz.framework.ioc.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BeanXMLReaderUtil {
	
	private static BeanXMLReaderUtil reader = new BeanXMLReaderUtil();
	
	private BeanXMLReaderUtil() {
		
	}
	
	/**
	 * 根据xml文件中配置解析获取到包的扫描路径
	 * @param configLocation
	 * @return
	 * @throws Exception
	 */
	public String getScanPackae(String configLocation) throws Exception {
		// 把xml文件转换为输入流
//		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		File f = new File(configLocation);
		InputStream in = new FileInputStream(f);
		document = saxReader.read(in);
		// 获取根元素
		Element rootElement = document.getRootElement();
		// 解析component-scan
		Element element = rootElement.element("component-scan");
		
		return element.attributeValue("base-package");
	}
	
	public static BeanXMLReaderUtil getInstance() {
		return reader;
	}

}
