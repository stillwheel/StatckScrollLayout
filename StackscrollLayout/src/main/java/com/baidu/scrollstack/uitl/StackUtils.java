package com.baidu.scrollstack.uitl;

/**
 * Created by baidu on 16/3/29.
 */
public class StackUtils {

    public static float getSpringTension(float currentLength, float maxLength){
        return 1.0f - (currentLength / maxLength);
    }
}
