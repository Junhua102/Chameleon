package me.dreamheart.chameleon.dex;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ljh102 on 2017/1/28.
 * DexBackedDexFile封装
 */
public class DexFileWrapper implements DexFile {

    private DexFile dexFile;
    private ClassDefSet classes;

    public DexFileWrapper(DexFile dexFile) {
        this.dexFile = dexFile;

        ClassDefSet set = new ClassDefSet();
        Set<? extends ClassDef> classDefs = dexFile.getClasses();
        for (ClassDef classDef : classDefs) {
            set.addClassDef(classDef);
        }
        classes = set;
    }

    @Nonnull
    @Override
    public Set<? extends ClassDef> getClasses() {
        return classes;
    }

    @Nonnull
    @Override
    public Opcodes getOpcodes() {
        return dexFile.getOpcodes();
    }
}
