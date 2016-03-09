package org.gemini.httpengine.inject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by geminiwen on 15/5/21.
 */
public class APIClassInjector {

    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final boolean isInterface;
    private final Set<APIMethodInjector> methods;

    public APIClassInjector(String classPackage, String className, String targetClass, boolean isInterface) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.isInterface = isInterface;
        this.methods = new LinkedHashSet<>();
    }

    public void addMethod(APIMethodInjector e) {
        methods.add(e);
    }

    public String getFqcn() {
        return classPackage + "." + className;
    }

    public String brewJava() throws Exception{
        StringBuilder builder = new StringBuilder("package " + this.classPackage + ";\n");
        // 通用
        builder.append("import com.baixing.kongbase.bxnetwork.BxGiftClient;\n");
        builder.append("import com.baixing.network.Call;\n");//返回类型
        builder.append("import com.google.gson.reflect.TypeToken;\n");

        builder.append("import com.baixing.kongbase.data.*;\n");//每个api的data不同
        builder.append("import com.baixing.kongbase.data.Gift;\n");//有时需要
        builder.append("import com.baixing.kongbase.bxnetwork.interceptors.VerifyInterceptor;\n");//post时需要

        String action = this.isInterface ? "implements" : "extends";

        builder.append("public class " + this.className + " " + action + " " + this.targetClass + " {\n");
        System.out.println("method number: " + methods.size());

        Logger log = Logger.getLogger("lavasoft");
        log.setLevel(Level.INFO);
        log.info("method number: " + methods.size());
        writeLog("method number: " + methods.size());

        for (APIMethodInjector methodInjector : methods) {
            builder.append(methodInjector.brewJava());
        }
        builder.append("}\n");
        return builder.toString();
    }

    private void writeLog(String str) {
        try {
            FileWriter fw = new FileWriter(new File("/Users/zhaibingjie/classinject.txt"), true);
            fw.write(str + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
