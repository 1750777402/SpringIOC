package org.ygz.framework.ioc.context;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ygz.framework.ioc.annoation.Autowired;
import org.ygz.framework.ioc.annoation.Repository;
import org.ygz.framework.ioc.annoation.Service;
import org.ygz.framework.ioc.xml.BeanXMLReaderUtil;

public class ClassPathXmlApplicationContext implements ApplicationContext {
	
	private String configLocation;
	
	// 定义存储到扫描的class对象缓存
	List<Class<?>> classCache = Collections.synchronizedList(new ArrayList<>(256));
	// 定义一个保存实例化好的对象容器
	Map<String,Object> beanConatainer = new ConcurrentHashMap<>(128);
	
	public ClassPathXmlApplicationContext() {
		
	}
	
	public ClassPathXmlApplicationContext(String configLocation) {
		this.configLocation = configLocation;
		// 定义初始化容器的方法
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化spring容器
	 * @throws Exception 
	 */
	private void initialize() throws Exception {
		// 1、获取到包扫描了路径
		String packageString = BeanXMLReaderUtil.getInstance().getScanPackae(configLocation);
		System.out.println(packageString);
		// 2、根据包路径获取到需要容器管理的Class对象（哪些类标注我们@Repository，@Service注解）
		scannerPackage(packageString);
		// 3、把需要管理的对象根据名称装配到定义IOC容器中(实例过程的key有2种可能[1、定义了别名，2、使用默认别名])
		doCreateBean();
		// 4、实现容器中所有实例对象运行中所需要的依赖对象的装配(哪些类中属性标注了@Autowired注解)
		dependBean();
	}

	/**
	 * 依赖装配
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private void dependBean() throws Exception {
		if (beanConatainer.size() == 0) {
			return;
		}
		
		for (Map.Entry<String, Object> entry : beanConatainer.entrySet()) {
			Object instance = entry.getValue();
			// 获取当前类中所有定义字段
			Field[] fields = instance.getClass().getDeclaredFields();
			if(fields!= null && fields.length > 0) {
				for (Field field : fields) {
					// 判断字段上面是否存在@Autowired注解
					if (field.isAnnotationPresent(Autowired.class)) {
						Autowired autowired = field.getAnnotation(Autowired.class);
						if (!"".equals(autowired.value())) {
							String beanName = autowired.value();
							// 获取字段需要装配依赖对象
							Object injectionInstance = getBean(beanName);
							// 把值设置到对应对象中
							field.setAccessible(true);
							field.set(instance, injectionInstance);
						} else {
							String beanName = field.getType().getSimpleName();
							// 获取字段需要装配依赖对象
							Object injectionInstance = getBean(beanName);
							// 把值设置到对应对象中
							field.setAccessible(true);
							field.set(instance, injectionInstance);
						}
					}
				}
			}
			
		}
		
	}

	/**
	 * IOC管理对象的实例化操作
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void doCreateBean() throws Exception {
		if (classCache.size() == 0) {
			return;
		}
		
		for (Class<?> cl : classCache) {
			// 获取实例的别名(默认为类的首字母小写) 
			String beanName = lowerCpacity(cl.getSimpleName());
			
			// 判断是否定义别名
			if (cl.isAnnotationPresent(Repository.class)) {
				Repository repository = cl.getAnnotation(Repository.class);
				if (!"".equals(repository.value())) {
					beanName = repository.value();
				}
			}
			
			if (cl.isAnnotationPresent(Service.class)) {
				Service service = cl.getAnnotation(Service.class);
				if (!"".equals(service.value())) {
					beanName = service.value();
				}
			}
			// 创建当前类的实例
			Object instance = cl.newInstance();
			// 存储到ioc容器中
			beanConatainer.put(beanName, instance);
			
			// 判断当前类是否实现了接口
			Class<?>[] interfaces = cl.getInterfaces();
			
			if (interfaces == null) {
				return;
			}
			
			for (Class<?> inerClass : interfaces) {
				beanConatainer.put(inerClass.getSimpleName(), instance);
			}
		}
		
		System.out.println(beanConatainer);
	}
	
	
	/**
	 * 把一个类的首字母转换为小写
	 * @param className
	 * @return
	 */
	private String lowerCpacity(String className) {
		char[] charArray = className.toCharArray();
		charArray[0] += 32;
		
		return String.valueOf(charArray);
	}

	/**
	 * 根据包路径获取到需要容器管理的Class对象（哪些类标注我们@Repository，@Service注解）
	 * @param packageString
	 */
	private void scannerPackage(String packageString)  {
		URL url = this.getClass().getClassLoader().getResource(packageString.replaceAll("\\.", "/"));
		File file = new File(url.getFile());
		file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File child) {
				if (child.isDirectory()) {
					// 递归获取到当前目录下面所有文件
					scannerPackage(packageString+"."+child.getName());
				} else {
					if (child.getName().endsWith(".class")) {
						String classPath = packageString + "." + child.getName().replaceAll("\\.class", "");
						// 把路径转换为一个类对象
						try {
							Class<?> loadClass = this.getClass().getClassLoader().loadClass(classPath);
							
							if (loadClass.isAnnotationPresent(Repository.class)
									|| loadClass.isAnnotationPresent(Service.class)) {
								classCache.add(loadClass);
							}
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				return false;
			}
		});
	}

	@Override
	public Object getBean(String name) {
		return this.beanConatainer.get(name);
	}

}
