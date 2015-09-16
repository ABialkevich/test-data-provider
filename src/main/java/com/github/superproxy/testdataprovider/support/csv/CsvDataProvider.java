package com.github.superproxy.testdataprovider.support.csv;

import com.github.superproxy.testdataprovider.DataProvider;
import com.github.superproxy.testdataprovider.MethodContext;
import com.github.superproxy.testdataprovider.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvDataProvider implements DataProvider {

    @Override
    public Class getDataType() {
        return Csv.class;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvDataProvider.class);

    @Override
    public Object[][] getObjects(MethodContext methodContext) {
        Annotation annotation = methodContext.getAnnotation();
        Method method = methodContext.getMethod();
        if (annotation instanceof Csv) {
            String path = ((Csv) annotation).value();
            if (StringUtils.isNoneEmpty(path)) {
                methodContext.setPath(path);
            }

            String encoding = ((Csv) annotation).encoding();
            methodContext.setEncoding(encoding);
        }
        try {
            return getObjects(methodContext, method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object[][] getObjects(MethodContext methodContext, Method method) throws Exception {
        String filePath = methodContext.getPath();
        String encoding = methodContext.getEncoding();
        LOGGER.debug("path:{}", new File(".").getAbsolutePath());
        Scanner scanner;

        if (StringUtils.isEmpty(encoding)) {
            scanner = new Scanner(CsvDataProvider.class.getResourceAsStream(filePath));  // 默认自动识别编码的
        } else {
//            scanner = new Scanner(new FileInputStream(filePath), encoding);
            scanner = new Scanner(CsvDataProvider.class.getResourceAsStream(filePath), encoding);  // 默认自动识别编码的

        }
        // 第一行
        String head = scanner.nextLine();
        LOGGER.debug("head:{}", head);
        String[] paramNames = MethodUtils.getParameterNames(method);
//        int paramCount = head.split(",").length;
        int paramCount = paramNames.length;      // 文件格式要求：参数和方法定义的一样顺序
        Class[] paraTypes = method.getParameterTypes();
        List<Object[]> list = new ArrayList<Object[]>();
        while (scanner.hasNext()) {
            String data = scanner.nextLine();
            String[] array = data.split(",");
            LOGGER.debug("data:{}", data);
            Object[] objects = new Object[paramCount];
            for (int i = 0; i < paramCount; i++) {
                // 类型转换
                objects[i] = convert(paraTypes[i], array[i]);
            }
            list.add(objects);
        }
        return list.toArray(new Object[0][0]);
    }

    private static Object convert(Class paraType, String s) {
        Object object;
        if (paraType.equals(Byte.TYPE) || paraType.equals(Byte.class)) {
            object = Byte.valueOf(s);
        } else if (paraType.equals(Integer.TYPE) || paraType.equals(Integer.class)) {
            object = Integer.valueOf(s);
        } else if (paraType.equals(Short.TYPE) || paraType.equals(Short.class)) {
            object = Short.valueOf(s);
        } else if (paraType.equals(Long.TYPE) || paraType.equals(Long.class)) {
            object = Long.valueOf(s);
        } else if (paraType.equals(Float.TYPE) || paraType.equals(Float.class)) {
            object = Float.valueOf(s);
        } else if (paraType.equals(Double.TYPE) || paraType.equals(Double.class)) {
            object = Double.valueOf(s);
        } else if (paraType.equals(Boolean.TYPE) || paraType.equals(Boolean.class)) {
            object = Boolean.valueOf(s);
        } else if (paraType.equals(Character.TYPE) || paraType.equals(Character.class)) {
            object = s.charAt(0);
        } else if (paraType.equals(String.class)) {
            object = s;
        } else {
            object = s;    //String
        }
        return object;
    }
}
