package org.gemini.httpengine.inject;

import org.gemini.httpengine.annotation.GET;
import org.gemini.httpengine.annotation.POST;
import org.gemini.httpengine.annotation.Path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Created by geminiwen on 15/5/21.
 */
public class APIMethodInjector {

    private ExecutableElement executableElement;

    private final TypeMirror returnType;
    private final List<? extends VariableElement> arguments;
    private final String methodName;
    private String httpMethod;
    private String url;

    public APIMethodInjector(ExecutableElement executableElement) {
        String methodName = executableElement.getSimpleName().toString();
        TypeMirror returnType = executableElement.getReturnType();
        List<? extends VariableElement> arguments = executableElement.getParameters();

        this.returnType = returnType;
        writeLog("returnType.toString " + returnType.toString() + ", returnType.getClass " + returnType.getClass() + "\n");
        this.arguments = arguments;
        this.methodName = methodName;

        //真正Path的值
        this.url = executableElement.getAnnotation(Path.class).value();

        if (executableElement.getAnnotation(POST.class) != null) {
            this.httpMethod = "POST";
        } else if (executableElement.getAnnotation(GET.class) != null) {
            this.httpMethod = "GET";
        }
    }

    public String brewJava() throws Exception{
        StringBuilder sb = new StringBuilder("public ");

        //build return type
        TypeKind returnTypeKind = returnType.getKind();
        Map<String, String> parameterNameMap = new LinkedHashMap<>();

        switch (returnTypeKind) {
            case VOID: {
                sb.append("void ");
                break;
            }
            default: {
                sb.append(returnType.toString() + " ");
            }
        }

        sb.append(methodName + "(");
        boolean isFirst = true;

        for (VariableElement variableElement : arguments) {
            DeclaredType type = (DeclaredType)variableElement.asType();
            String typeName = type.asElement().toString();
            String variableName = variableElement.getSimpleName().toString();

            if (!isFirst) {
                sb.append(", ");
            }

            sb.append(typeName);
            sb.append(" " + variableName);

            if (isFirst) {
                isFirst = false;
            }
        }
        sb.append(") {\n");
        sb.append(buildFunctionBody(parameterNameMap));
        sb.append("}\n");

        return sb.toString();
    }

    //方法和变量要分开，因为变量在开头，方法却有很多
    private String buildFunctionBody(Map<String, String> parameters) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("return BxGiftClient.getClient()\n");
        stringBuilder.append(".url(\"" + this.url + "\")\n");
        for (Map.Entry<String, String> parameter: parameters.entrySet()) {
            String name = parameter.getKey();
            String variableName = parameter.getValue();
            stringBuilder.append(".addQueryParameter(\"" + name + "\", " + variableName + ")\n");
        }
        if(this.httpMethod.equals("GET")) {
            stringBuilder.append(".get()\n");
        } else {
            stringBuilder.append(".addInterceptor(new VerifyInterceptor.GzipRequestInterceptor())\n");
            stringBuilder.append(".post()\n");
        }
        stringBuilder.append(".makeCall(new TypeToken<GeneralItem>() {}\n");
        stringBuilder.append(".getType());\n");

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private void writeLog(String str) {
        try {
            FileWriter fw = new FileWriter(new File("/Users/zhaibingjie/methodinject.txt"), true);
            fw.write(str + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
