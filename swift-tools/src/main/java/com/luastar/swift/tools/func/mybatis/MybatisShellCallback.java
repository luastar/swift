package com.luastar.swift.tools.func.mybatis;

import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 重写生成输出路径方法
 */
public class MybatisShellCallback extends DefaultShellCallback {


    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite the overwrite
     */
    public MybatisShellCallback(boolean overwrite) {
        super(overwrite);
    }

    /**
     * 输出目录不使用包路径
     *
     * @param targetProject
     * @param targetPackage
     * @return
     * @throws ShellException
     */
    @Override
    public File getDirectory(String targetProject, String targetPackage) throws ShellException {
        File directory = new File(targetProject);
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", directory.getAbsolutePath()));
            }
        }
        return directory;
    }

}
