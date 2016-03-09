package org.gemini.httpengine.annotation;

import org.gemini.httpengine.inject.APIClassInjector;
import org.gemini.httpengine.inject.APIMethodInjector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by geminiwen on 15/5/20.
 */
public class AnnotationProcessor extends AbstractProcessor {

    private static final String SUFFIX = "$$APIINJECTOR";

    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        writeLog("annotations: " + annotations + "\n");
        Map<TypeElement, APIClassInjector> targetClassMap = findAndParseTargets(roundEnv);

        writeLog("class map: " + targetClassMap.entrySet().size());
        writeLog("class map: " + targetClassMap.entrySet().toArray());
        for (Map.Entry<TypeElement, APIClassInjector> entry : targetClassMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            APIClassInjector injector = entry.getValue();
            writeLog("\ntypeElement " + typeElement + "\ninjector, " + injector);

            try {
                String value = injector.brewJava();

                writeLog("getFqcn" + injector.getFqcn());
                JavaFileObject jfo = filer.createSourceFile(injector.getFqcn(), typeElement);
                Writer writer = jfo.openWriter();
                writer.write(value);
                writer.flush();
                writer.close();
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), typeElement);
            }
        }


        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(Path.class.getCanonicalName());
        return supportTypes;
    }

    private Map<TypeElement, APIClassInjector> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, APIClassInjector> targetClassMap = new LinkedHashMap<>();

        //解决PATH的标签
        for (Element element : env.getElementsAnnotatedWith(Path.class)) {
            ExecutableElement executableElement = (ExecutableElement) element;

            //类名（接口名）
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            //方法名
            APIClassInjector injector = getOrCreateTargetClass(targetClassMap, enclosingElement);
            APIMethodInjector methodInjector = new APIMethodInjector(executableElement);
            injector.addMethod(methodInjector);
        }
        return targetClassMap;
    }

    /**
     * 查找是否有缓存的注入类对象
     *
     * @param targetClassMap
     * @param enclosingElement
     * @return
     */
    private APIClassInjector getOrCreateTargetClass(Map<TypeElement, APIClassInjector> targetClassMap, TypeElement enclosingElement) {
        APIClassInjector injector = targetClassMap.get(enclosingElement);
       if (injector == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + SUFFIX;

            TypeMirror elementType = enclosingElement.asType();
            boolean isInterface = isInterface(elementType);

            injector = new APIClassInjector(classPackage, className, targetType, isInterface);
            writeLog("targettype, " + targetType + "\n");
            targetClassMap.put(enclosingElement, injector);
        }
        return injector;
    }

    private boolean isInterface(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }
        return ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.INTERFACE;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * 获取类名
     *
     * @param type
     * @param packageName
     * @return
     */
    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    /**
     * 获取某个类型的包名
     *
     * @param type
     * @return
     */
    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private void writeLog(String str) {
        try {
            FileWriter fw = new FileWriter(new File("/Users/zhaibingjie/process.txt"), true);
            fw.write(str + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
