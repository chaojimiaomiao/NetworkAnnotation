package org.gemini.httpengine.annotation;

/**
 * Created by geminiwen on 15/5/21.
 */
public class InjectFactory {

    public static <T> T inject(Class<T> type) {
        String name = type.getCanonicalName() + "$$APIINJECTOR";
        T obj = null;
        try {
            obj = (T)Class.forName(name).newInstance();
        } catch (ClassNotFoundException e) {

        } catch (IllegalAccessException e) {

        } catch (InstantiationException e) {

        }
        return obj;
    }
}
