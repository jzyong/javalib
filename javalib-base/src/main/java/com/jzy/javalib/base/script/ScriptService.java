package com.jzy.javalib.base.script;

import com.jzy.javalib.base.util.FileUtil;
import com.jzy.javalib.base.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 脚本加载器<br>
 * 支持多路径加载
 *
 * @author jzy
 */
public  class ScriptService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptService.class);
	/**
	 * jdk版本
	 */
	public static String JDK_VERSION = "14";

	public ScriptService() {
		String dir = System.getProperty("user.dir");
		String path = dir + "-scripts" + File.separator + "src" + File.separator + "main" + File.separator + "java"
				+ File.separator;
		String outpath = dir + File.separator + "target" + File.separator + "scriptsbin" + File.separator;
		String jarsDir = dir + File.separator + "target" + File.separator;
		setSource(path, outpath, jarsDir);
		LOGGER.info("项目路径：{} 脚本路径：{} 输出路径：{} jar路径：{}", dir, path, outpath, jarsDir);
	}

	// // 脚本源文件夹
//    private String sourceDir;
	private List<String> sourceDirs = new ArrayList<>();
	// 输出文件夹
	private String outDir;
	// 附加的jar包地址
	private String jarsDir = "";
	/**
	 * 脚本{"接口名"：{"类名":"类对象"}}
	 */
	Map<String, Map<String, IScript>> tmpScriptInstances = new ConcurrentHashMap<>();
	Map<String, Map<String, IScript>> scriptInstances = new ConcurrentHashMap<>();
	/**
	 * 脚本{"类名":"类对象"}
	 */
	Map<String, IScript> tmpNameScriptMap = new ConcurrentHashMap<>();
	Map<String, IScript> nameScriptMap = new ConcurrentHashMap<>();


	public final void setSource(String source, String out, String jarsDir) {
		if (stringIsNullEmpty(source)) {
			LOGGER.error("指定 输入 输出 目录为空");
			return;
		}
//        this.sourceDir = source;
		this.sourceDirs.add(source);
		this.outDir = out;
		this.jarsDir = jarsDir;
	}

	public String init(Consumer<String> condition) {
		return loadJava(condition);
	}

	/**
	 * 添加额外的源代码编译路径
	 *
	 * @param sourceDir
	 */
	public void addSourceDir(String sourceDir) {
		this.sourceDirs.add(sourceDir);
	}

	/**
	 * 加载scripts目录下面的所有脚本文件
	 *
	 * @param className 加载指定类，null 加载script整个目录
	 * @return
	 */
	public String loadScripts(String className) {
		return loadJava(className, "com" + File.separator + "game" + File.separator + "scripts");
	}

	/**
	 * 加载消息处理器
	 *
	 * @param className 加载指定类，null 加载handler整个目录
	 * @return
	 */
	public String loadHandlers(String className) {
		return loadJava(className, "com" + File.separator + "game" + File.separator + "handler");
	}

	/**
	 * 根据接口类名获取所有实现该接口的脚本
	 *
	 * @param name
	 * @return
	 */
	public Collection<IScript> getScripts(String name) {
		Map<String, IScript> scripts = ScriptService.this.scriptInstances.get(name);
		if (scripts != null) {
			return scripts.values();
		}
		return  Collections.emptyList();
	}

	/**
	 * 根据接口类获取所有实现该接口的脚本
	 *
	 * @param <E>
	 * @param clazz
	 * @return
	 */
	public <E> Collection<E> getScripts(Class<E> clazz) {
		Map<String, IScript> scripts = ScriptService.this.scriptInstances.get(clazz.getName());
		if (scripts != null) {
			return (Collection<E>) scripts.values();
		}
		return Collections.emptyList();
	}

	/**
	 * 根据脚本类名获取脚本
	 *
	 * @param <T>
	 * @param scriptName
	 * @return
	 */
	public <T extends IScript> T getScript(String scriptName) {
		if (this.nameScriptMap.containsKey(scriptName)) {
			return (T) this.nameScriptMap.get(scriptName);
		}
		return null;
	}

	/**
	 * 执行指定名的脚本
	 *
	 * @param <T>
	 * @param scriptName
	 * @param action
	 */
	public <T extends IScript> void consumerScript(String scriptName, Consumer<T> action) {
		T s = getScript(scriptName);
		if (s != null && action != null) {
			try {
				action.accept(s);
			} catch (Exception e) {
				LOGGER.error(String.format("consumerScript IBaseScript: %s) ", scriptName), e);
			}
		} else {
			LOGGER.warn("找不到指定脚本{}", scriptName);
		}
	}

	/**
	 * 执行脚本集合
	 *
	 * @param <T>
	 * @param scriptClass
	 * @param action
	 */
	public <T extends IScript> void consumerScripts(Class<T> scriptClass, Consumer<T> action) {
		Collection<IScript> evts = getScripts(scriptClass.getName());
		if (evts != null && !evts.isEmpty() && action != null) {
			evts.forEach(script -> {
				try {
					action.accept((T) script);
				} catch (Exception e) {
					LOGGER.error(String.format("consumerScripts IBaseScript:%s", scriptClass.getName()), e);
				}
			});
		}
	}

	/**
	 * 执行单个脚本，返回执行结果
	 *
	 * @param <T>
	 * @param scriptName
	 * @param condition
	 * @return
	 */
	public <T extends IScript> boolean predicateScript(String scriptName, Predicate<T> condition) {
		T s = getScript(scriptName);
		if (s != null && condition != null) {
			try {
				if (condition.test(s)) {
					return true;
				}
			} catch (Exception e) {
				LOGGER.error("predicateScript IBaseScript:" + scriptName, e);
			}
		}
		return false;
	}

	/**
	 * 执行脚本集合，当执行结果为true时，中断执行，并返回true。否则统一返回执行false
	 *
	 * @param <T>
	 * @param scriptClass
	 * @param condition
	 * @return
	 */
	public <T extends IScript> boolean predicateScripts(Class<? extends IScript> scriptClass, Predicate<T> condition) {
		Collection<IScript> evts = getScripts(scriptClass.getName());
		if (evts != null && !evts.isEmpty() && condition != null) {
			Iterator<IScript> iterator = evts.iterator();
			while (iterator.hasNext()) {
				try {
					if (condition.test((T) iterator.next())) {
						return true;
					}
				} catch (Exception e) {
					LOGGER.error("predicateScripts IBaseScript:" + scriptClass.getName(), e);
				}
			}
		}
		return false;
	}

	/**
	 * 执行单个脚本，返回特定参数
	 *
	 * @param <T>
	 * @param <R>
	 * @param scriptName
	 * @param function
	 * @return
	 */
	public <T extends IScript, R> R functionScript(String scriptName, Function<T, R> function) {
		T s = getScript(scriptName);
		if (s != null && function != null) {
			try {
				R r = function.apply(s);
				if (r != null) {
					return r;
				}
			} catch (Exception e) {
				LOGGER.error("functionScripts IBaseScript:" + scriptName, e);
			}
		}
		return null;
	}

	public <T extends IScript, R> R functionScripts(Class<? extends IScript> scriptClass, Function<T, R> function) {
		Collection<IScript> evts = getScripts(scriptClass.getName());
		if (evts != null && !evts.isEmpty() && function != null) {
			Iterator<IScript> iterator = evts.iterator();
			while (iterator.hasNext()) {
				try {
					R r = function.apply((T) iterator.next());
					if (r != null) {
						return r;
					}
				} catch (Exception e) {
					LOGGER.error("functionScripts IBaseScript:" + scriptClass.getName(), e);
				}
			}
		}
		return null;
	}

	final boolean stringIsNullEmpty(String str) {
		return str == null || str.length() <= 0 || "".equals(str.trim());
	}

	/**
	 * 编译 java 源文件
	 *
	 * @return
	 */
	String compile() {
		FileUtil.deleteDirectory(this.outDir);
		StringBuffer sb = new StringBuffer();
		LOGGER.info("输出路径：{}", outDir);
		LOGGER.info("JAR路径：{}", jarsDir);
		for (String sourceDir : sourceDirs) {
			List<File> sourceFileList = new ArrayList<>();
			FileUtil.getFiles(sourceDir, sourceFileList, ".java", null);
			LOGGER.info("脚本源：{}     ==> 源文件数：{}", sourceDir, sourceFileList.size());
			sb.append(this.compile(sourceDir, sourceFileList));
		}
		return sb.toString();
	}

	/**
	 * 编译文件
	 *
	 * @param sourceFileList 文件列表
	 * @return
	 */
	String compile(String sourceDir, List<File> sourceFileList) {
		StringBuilder sb = new StringBuilder();
		if (null != sourceFileList) {
			DiagnosticCollector<JavaFileObject> oDiagnosticCollector = new DiagnosticCollector<>();
			// 获取编译器实例
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			// 获取标准文件管理器实例
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(oDiagnosticCollector, null,
					Charset.forName("utf-8"));
			try {
				// 没有java文件，直接返回
				if (sourceFileList.isEmpty()) {
					// log.warn(this.sourceDir + "目录下查找不到任何java文件");
					return sourceDir + "目录下查找不到任何java文件";
				}
				// 创建输出目录，如果不存在的话
				new File(this.outDir).mkdirs();
				// 获取要编译的编译单元
				Iterable<? extends JavaFileObject> compilationUnits = fileManager
						.getJavaFileObjectsFromFiles(sourceFileList);
				/**
				 * 编译选项，在编译java文件时，编译程序会自动的去寻找java文件引用的其他的java源文件或者class。
				 * -sourcepath选项就是定义java源文件的查找目录， -classpath选项就是定义class文件的查找目录。
				 */
				ArrayList<String> options = new ArrayList<>(0);
				options.add("-g");
				options.add("-source");
				options.add(JDK_VERSION);
				// options.add("-Xlint");
				// options.add("unchecked");
				options.add("-encoding");
				options.add("UTF-8");
				options.add("-sourcepath");
				options.add(sourceDir); // 指定文件目录
				options.add("-d");
				options.add(this.outDir); // 指定输出目录

				ArrayList<File> jarsList = new ArrayList<>();
				FileUtil.getFiles(this.jarsDir, jarsList, ".jar", null);
				// 生产环境jar包在根目录的lib文件下
				if (jarsList.isEmpty()) {
					String dir = System.getProperty("user.dir");
					var jarsDir = dir + File.separator;
					LOGGER.warn("jar路径：{} 未找到jar包依赖 新路径：{}查找", this.jarsDir, jarsDir);
					this.jarsDir = jarsDir;
					FileUtil.getFiles(this.jarsDir, jarsList, ".jar", null);
				}

				StringBuilder sbJar = new StringBuilder();
				for (File file : jarsList) {
					sbJar.append(file.getPath()).append(File.pathSeparator);
				}
				String jarString = sbJar.toString();
				LOGGER.trace("jarString:" + jarString);
				if (!stringIsNullEmpty(jarString)) {
					options.add("-classpath");
					options.add(jarString);// 指定附加的jar包
				}

				JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, fileManager, oDiagnosticCollector,
						options, null, compilationUnits);
				// 运行编译任务
				Boolean call = compilationTask.call();
				if (!call) {
					oDiagnosticCollector.getDiagnostics().forEach(f -> {
						sb.append(";");
						JavaFileObject source = f.getSource();
						String name = null;
						if (source != null) {
							name = source.getName();
							sb.append(name);
						}
						sb.append(" line:").append(f.getLineNumber());
						LOGGER.warn("加载脚本错误：{} line: {} --> {}", name, f.getLineNumber(), f.getMessage(Locale.ENGLISH));
					});
				}
			} catch (Exception ex) {
				sb.append(sourceDir).append("错误：").append(ex);
				LOGGER.error("加载脚本错误：", ex);
			} finally {
				try {
					fileManager.close();
				} catch (IOException ex) {
					LOGGER.error("", ex);
				}
			}
		} else {
			LOGGER.warn(sourceDir + "目录下查找不到任何java文件");
		}
		return sb.toString();
	}

	/**
	 * 加载脚本文件
	 *
	 * @param condition
	 * @return
	 */
	public String loadJava(Consumer<String> condition) {
		String compile = this.compile();
		if (compile == null || compile.isEmpty()) {
			List<File> sourceFileList = new ArrayList<>(0);
			FileUtil.getFiles(this.outDir, sourceFileList, ".class", null);
			String[] fileNames = new String[sourceFileList.size()];
			for (int i = 0; i < sourceFileList.size(); i++) {
				fileNames[i] = sourceFileList.get(i).getPath();
			}
			tmpScriptInstances = new ConcurrentHashMap<>();
			tmpNameScriptMap = new ConcurrentHashMap<>();
			loadClass(fileNames);
			if (tmpScriptInstances.size() > 0) {
				scriptInstances.clear();
				scriptInstances = tmpScriptInstances;
			}
			if (tmpNameScriptMap.size() > 0) {
				nameScriptMap.clear();
				nameScriptMap = tmpNameScriptMap;
			}
		} else if (!compile.isEmpty()) {
			if (condition != null) {
				condition.accept(compile);
			}
		}
		return compile;
	}

	/**
	 * 加载脚本文件
	 *
	 * @param source 加载的文件或者目录
	 * @return
	 */
	public String loadJava(String className, String... source) {
		FileUtil.deleteDirectory(this.outDir);
		List<File> sourceFileList = new ArrayList<>();
		StringBuffer stringBuffer = new StringBuffer();
		for (String sourceDir : sourceDirs) {
			FileUtil.getFiles(sourceDir, sourceFileList, ".java", fileAbsolutePath -> {
				if (source == null) {
					return true;
				}
				for (String str : source) {
					if (fileAbsolutePath.contains(str) || str.equals("")) {
						return true;
					}
				}
				return false;
			});
			String result = this.compile(sourceDir, sourceFileList);
			stringBuffer.append(result);
		}

		StringBuilder loadJava = new StringBuilder("加载了脚本：");
		if (stringBuffer.length() < 1) {
			sourceFileList.clear();
			FileUtil.getFiles(this.outDir, sourceFileList, ".class", fileAbsolutePath -> {
				if (source == null) {
					return true;
				}
				for (String str : source) {
					if (fileAbsolutePath.contains(str) || str.equals("")) {
						return true;
					}
				}
				return false;
			});
			String[] fileNames = new String[sourceFileList.size()];
			if (!StringUtil.isEmpty(className)) {
				for (int i = 0; i < sourceFileList.size(); i++) {
					if (sourceFileList.get(i).getPath().contains(className)) {
						fileNames = new String[1];
						fileNames[0] = sourceFileList.get(i).getPath();
						break;
					}
				}
			} else {
				for (int i = 0; i < sourceFileList.size(); i++) {
					fileNames[i] = sourceFileList.get(i).getPath();
				}
			}

			tmpScriptInstances = new ConcurrentHashMap<>();
			tmpNameScriptMap = new ConcurrentHashMap<>();
			loadClass(fileNames);
			if (tmpScriptInstances.size() > 0) {
				for (Iterator<Map.Entry<String, Map<String, IScript>>> iterator = tmpScriptInstances.entrySet()
						.iterator(); iterator.hasNext();) {
					Map.Entry<String, Map<String, IScript>> next = iterator.next();
					String key = next.getKey();
					Map<String, IScript> value = next.getValue();
					if (scriptInstances.containsKey(key)) {
						Map<String, IScript> map = scriptInstances.get(key);
						value.forEach((k, v) -> {
							map.put(k, v);
						});
					} else {
						scriptInstances.put(key, value);
					}
					loadJava.append(key).append(";");
				}
			}
			if (tmpNameScriptMap.size() > 0) {
				tmpNameScriptMap.forEach((k, v) -> {
					nameScriptMap.put(k, v);
				});
			}
		}
		return loadJava.toString();
	}

	/**
	 * 加载脚本文件
	 *
	 * @param names
	 */
	void loadClass(String... names) {
		try {
			ScriptClassLoader loader = new ScriptClassLoader();
			for (String name : names) {
				String tmpName = name.replace(outDir, "").replace(".class", "").replace(File.separatorChar, '.');
				loader.loadClass(tmpName);
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error("", e);
		}
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	class ScriptClassLoader extends ClassLoader {

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class<?> defineClass = null;
			defineClass = super.loadClass(name);
			return defineClass;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Class<?> findClass(String name) {
			// log.warn("加载脚本目录名称：" + (name));
			byte[] classData = getClassData(name);
			Class<?> defineClass = null;
			if (classData != null) {
				try {
					defineClass = defineClass(name, classData, 0, classData.length);
					String nameString = defineClass.getName();
					if (!Modifier.isAbstract(defineClass.getModifiers())
							&& !Modifier.isPrivate(defineClass.getModifiers())
							&& !Modifier.isStatic(defineClass.getModifiers()) && !nameString.contains("$")) {
						Object newInstance = defineClass.newInstance();
						List<Class<?>> interfaces = new ArrayList<>();
						if (IInitScript.class.isAssignableFrom(defineClass)
								|| IScript.class.isAssignableFrom(defineClass)) {
							Class<?> cls = defineClass;
							while (cls != null && !cls.isInterface() && !cls.isPrimitive()) {
								interfaces.addAll(Arrays.asList(cls.getInterfaces()));
								cls = cls.getSuperclass();
							}
							if (newInstance instanceof IInitScript) {
								((IInitScript) newInstance).init();
							}
						}

						// TODO 预留加载其他脚本
//						// handler
//						if (IHandler.class.isAssignableFrom(defineClass)) {
//							Handler handler = defineClass.getAnnotation(Handler.class);
//							if (handler != null) {
//								if (TcpHandler.class.isAssignableFrom(defineClass)) {
//									TcpMessageBean messageBean = new TcpMessageBean(handler.msg(), defineClass,
//											handler.executor());
//									messagebeans.put(handler.mid(), messageBean);
//									LOGGER.info("tcp handler ：[{}]", nameString);
//								} else if (HttpHandler.class.isAssignableFrom(defineClass)) {
//									httpHandlerClazzs.put(handler.path(), (Class<? extends HttpHandler>) (defineClass));
//									LOGGER.info("http handler ：[{}]", nameString);
//								} else if (RpcHandler.class.isAssignableFrom(defineClass)) {
//									rpcHandlerClazzes.put(handler.path(), (Class<? extends RpcHandler>) (defineClass));
//									LOGGER.info("rpc handler ：[{}]", nameString);
//								} else {
//									LOGGER.warn("handler[{}]未继承Handler", defineClass.getSimpleName());
//								}
//
//							} else {
//								LOGGER.warn("handler[{}]未添加注解", defineClass.getSimpleName());
//							}
//						}

						if (newInstance != null && !interfaces.isEmpty()) {
//                            LOGGER.warn("已实例化脚本：" + nameString);
							for (Class<?> aInterface : interfaces) {
								if (IScript.class.isAssignableFrom(aInterface)) {
									if (!tmpScriptInstances.containsKey(aInterface.getName())) {
										tmpScriptInstances.put(aInterface.getName(), new ConcurrentHashMap<>());
									}
									LOGGER.info("脚本 ：[{}]", nameString);
									// [接口名 [类名,类对象]]
									tmpScriptInstances.get(aInterface.getName()).put(defineClass.getSimpleName(),
											(IScript) newInstance);
									// [类名,类对象]
									tmpNameScriptMap.put(defineClass.getSimpleName(), (IScript) newInstance);
								}
							}
						} else {
//                            LOGGER.warn("成功编译脚本但没有加载到管理器：" + nameString);
						}
					} else {
//                        LOGGER.warn("没有加载脚本：" + nameString);
					}
				} catch (Exception ex) {
					LOGGER.error("加载脚本发生错误", ex);
				}
			}
			return defineClass;
		}

		private byte[] getClassData(String className) {
			String path = classNameToPath(className);
			// log.warn("加载脚本路径", path);
			InputStream ins = null;
			try {
				File file = new File(path);
				if (file.exists()) {
					ins = new FileInputStream(path);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int bufferSize = 4096;
					byte[] buffer = new byte[bufferSize];
					int bytesNumRead = 0;
					while ((bytesNumRead = ins.read(buffer)) != -1) {
						baos.write(buffer, 0, bytesNumRead);
					}
					return baos.toByteArray();
				} else {
					LOGGER.warn("自定义脚本文件不存在：" + path);
				}
			} catch (IOException e) {
				LOGGER.error("", e);
			} finally {
				if (ins != null) {
					try {
						ins.close();
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			}
			return null;
		}

		private String classNameToPath(String className) {
			File file = null;
			try {
				String path = outDir + className.replace('.', File.separatorChar) + ".class";
				// log.warn("classNameToPath path:{}", path);
				file = new File(path);
				if (!file.exists()) {
					LOGGER.warn("classNameToPath path:{}不存在", path);
				}
				return file.getPath();
			} catch (Exception e) {
				LOGGER.error(outDir, e);
			}
			return "";
		}
	}

	//TODO 预留接口，在子类实现
//	public TcpMessageBean getMessagebean(int msgId) {
//		return messagebeans.get(msgId);
//	}
//
//	/**
//	 * 获取http 消息处理器
//	 *
//	 * @param path
//	 * @return
//	 */
//	public HttpHandler getHttpHandler(String path) {
//		if (path.contains("?")) {
//			path = path.substring(0, path.indexOf("?"));
//		}
//		Class<? extends HttpHandler> class1 = httpHandlerClazzs.get(path);
//		if (class1 == null) {
//			return null;
//		}
//		try {
//			return class1.newInstance();
//		} catch (Exception e) {
//			LOGGER.error("HTTP消息", e);
//		}
//		return null;
//	}
//
//	public RpcHandler getRpcHandler(String path) {
//		Class<? extends RpcHandler> class1 = rpcHandlerClazzes.get(path);
//		if (class1 == null) {
//			return null;
//		}
//		try {
//			return class1.getDeclaredConstructor().newInstance();
//		} catch (Exception e) {
//			LOGGER.error("RPC消息", e);
//		}
//		return null;
//	}
//
//	/**
//	 * 消息是否注册
//	 *
//	 * @param mid
//	 * @return
//	 */
//	public boolean msgIsRegister(int mid) {
//		return messagebeans.containsKey(mid);
//	}
//
//	/**
//	 * 获取tcp消息id
//	 *
//	 * @return
//	 */
//	public Set<Integer> getTcpMessageIds() {
//		return messagebeans.keySet();
//	}
//
//	/**
//	 * 注册tcp Handler
//	 *
//	 * @param handlerClass
//	 */
//	public void registerTcpHandler(Class<? extends TcpHandler> handlerClass) {
//		try {
//			Handler handler = handlerClass.getAnnotation(Handler.class);
//			if (handler != null) {
//				TcpMessageBean messageBean = new TcpMessageBean(handler.msg(), handlerClass, handler.executor());
//				messagebeans.put(handler.mid(), messageBean);
//				LOGGER.info("加载到tcp handler到容器：{}", handlerClass.getSimpleName());
//			}
//		} catch (Exception e) {
//			LOGGER.error("registerTcpHandler", e);
//		}
//	}
}
