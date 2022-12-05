package com.jdcloud.logs.sample;

import com.jdcloud.logs.api.util.StringUtils;
import com.jdcloud.logs.sample.cases.Sample;

/**
 * 启动类
 *
 * @author liubai
 * @date 2022/11/29
 */
public class Application {

    public static void main(String[] args) throws InterruptedException {
        String className = System.getProperty("sampleClass");
        if (StringUtils.isBlank(className)) {
            className = "PerformanceSample";
        }
        String classFullName = "com.jdcloud.logs.sample.cases." + className;
        newInstance(classFullName).execute();
    }

    private static Sample newInstance(String className) {
        try {
            return (Sample) Class.forName(className).getConstructor().newInstance();
        } catch (Throwable e) {
            return null;
        }
    }
}
