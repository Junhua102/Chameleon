package me.dreamheart.chameleon.dex;

import org.jf.dexlib2.dexbacked.util.FixedSizeSet;
import org.jf.dexlib2.iface.ClassDef;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ljh102 on 2017/1/28.
 */
public class ClassDefSet extends FixedSizeSet<ClassDef> {

    private List<ClassDef> list = new ArrayList<>();

    public void addClassDef (ClassDef classDef) {
        list.add(classDef);
    }

    public void replaceClassDef (ClassDef oldClassDef, ClassDef newClassDef) {
        list.remove(oldClassDef);
        list.add(newClassDef);
    }

    @Nonnull
    @Override
    public ClassDef readItem(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}
