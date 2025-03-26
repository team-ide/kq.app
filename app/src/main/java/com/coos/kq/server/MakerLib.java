package com.coos.kq.server;


import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * MakerLib
 * <a href="https://github.com/java-native-access/jna/blob/5.17.0/lib/native/">jna lib</a>
 */
public interface MakerLib extends Library {

    // 原始C接口
    Pointer Execute(String input);

    // 内存释放接口
    void FreeString(Pointer ptr);

}
