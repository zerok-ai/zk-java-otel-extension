package ai.zerok.javaagent.utils;

import java.util.Collection;
import java.util.Enumeration;

public class CollectionUtils {

    public static boolean isKeyPresentIn(Collection<String> collection, String key){
        if(collection != null){
            return collection.contains(key);
        }

        return false;
    }

    public static boolean isKeyPresentIn(Enumeration<String> enumeration, String key){
        boolean isKeyPresent = false;
        while(enumeration.hasMoreElements()){
            String currentKey = enumeration.nextElement();
            if(currentKey.equals(key)){
                isKeyPresent = true;
            }
        }

        return isKeyPresent;
    }

}
