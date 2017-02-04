package me.dreamheart.chameleon.dex;

import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by ljh102 on 2017/1/29.
 */
public class ClassDefWrapper implements ClassDef {

    private ClassDef classDef;
    private boolean changeAccessFlags = false;
    private int accessFlags;
    private List<Method> virtualMethods = new ArrayList<>();

    public ClassDefWrapper(ClassDef classDef) {
        this.classDef = classDef;
        for (Method method : classDef.getVirtualMethods()) {
            virtualMethods.add(method);
        }
    }

    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
        changeAccessFlags = true;
    }

    @Nonnull
    @Override
    public String getType() {
        return classDef.getType();
    }

    @Override
    public int compareTo(@Nonnull CharSequence o) {
        return classDef.compareTo(o);
    }

    @Override
    public int getAccessFlags() {
        if (changeAccessFlags)
            return accessFlags;

        return classDef.getAccessFlags();
    }

    @Nullable
    @Override
    public String getSuperclass() {
        return classDef.getSuperclass();
    }

    @Nonnull
    @Override
    public List<String> getInterfaces() {
        return classDef.getInterfaces();
    }

    @Nullable
    @Override
    public String getSourceFile() {
        return classDef.getSourceFile();
    }

    @Nonnull
    @Override
    public Set<? extends Annotation> getAnnotations() {
        return classDef.getAnnotations();
    }

    @Nonnull
    @Override
    public Iterable<? extends Field> getStaticFields() {
        return classDef.getStaticFields();
    }

    @Nonnull
    @Override
    public Iterable<? extends Field> getInstanceFields() {
        return classDef.getInstanceFields();
    }

    @Nonnull
    @Override
    public Iterable<? extends Field> getFields() {
        return classDef.getFields();
    }

    @Nonnull
    @Override
    public Iterable<? extends Method> getDirectMethods() {
        return classDef.getDirectMethods();
    }

    @Nonnull
    @Override
    public Iterable<? extends Method> getVirtualMethods() {
        return virtualMethods;
    }

    @Nonnull
    @Override
    public Iterable<? extends Method> getMethods() {
        return classDef.getMethods();
    }

    @Override
    public int length() {
        return classDef.length();
    }

    @Override
    public char charAt(int index) {
        return classDef.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return classDef.subSequence(start, end);
    }

    public void addMethod(Method method) {
        virtualMethods.add(method);
    }

    public void replaceVirtualMethod(Method method){
        for (int i = 0; i < virtualMethods.size(); i++) {
            if (virtualMethods.get(i).getName().equals(method.getName())) {
                virtualMethods.set(i, method);
                return;
            }
        }
        // 没找到则直接添加
        addMethod(method);
    }

    public Method getVirtualMethod(String name){
        for (Method method : virtualMethods) {
            if (method.getName().equals(name))
                return method;
        }
        return null;
    }
}
