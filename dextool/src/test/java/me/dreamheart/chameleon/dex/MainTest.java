package me.dreamheart.chameleon.dex;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by Junhua Lv on 2017/2/1.
 */
public class MainTest {

    @Test
    public void testMain() throws Exception {
        long startTime = System.currentTimeMillis();
        apkRefactoring();
        long consume = System.currentTimeMillis() - startTime;
        System.out.print("Consume: " + (consume / 1000f) + "s");
    }

    private static void apkRefactoring () throws Exception {
        List<ApkRefactor.RefactorItem> refactorItems = new ArrayList<>();
        refactorItems.add(new ApkRefactor.RefactorItem("me.dreamheart.demo.appforload.MainActivity", "me.dreamheart.demo.PluginActivity0"));
        refactorItems.add(new ApkRefactor.RefactorItem("me.dreamheart.demo.appforload.Main2Activity", "me.dreamheart.demo.PluginActivity1"));
        ApkRefactor.ApkInfo apkInfo = new ApkRefactor.ApkInfo("me.dreamheart.demo.appforload");
        ApkRefactor.refactoring("testData/appforload.apk", apkInfo, refactorItems, "build/appforload.apk", "build");
    }

    private static void dexRefactoring () throws Exception{
        DexBackedDexFile backedDexFile = DexFileFactory.loadDexFile(new File("testData/classes.dex"), Opcodes.forApi(Config.ApiLevel));
        System.out.print("DexBackedDexFile loaded\n");

        final Map<String, String> classRenameMap = new HashMap<>();
        classRenameMap.put("Lme/dreamheart/demo/appforload/MainActivity;", "Lme/dreamheart/demo/PluginActivity0;");
        classRenameMap.put("Lme/dreamheart/demo/appforload/Main2Activity;", "Lme/dreamheart/demo/PluginActivity1;");
        final Map<String, String> subClassRenameMap = new HashMap<>();
        subClassRenameMap.put("Lme/dreamheart/demo/appforload/MainActivity$", "Lme/dreamheart/demo/PluginActivity0$");
        subClassRenameMap.put("Lme/dreamheart/demo/appforload/Main2Activity$", "Lme/dreamheart/demo/PluginActivity1$");

        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
            @Nonnull
            @Override
            public Rewriter<String> getTypeRewriter(@Nonnull Rewriters rewriters) {
                return new Rewriter<String>() {
                    @Nonnull
                    @Override
                    public String rewrite(@Nonnull String value) {
//                        System.out.print(value + "\n");
                        for (Map.Entry<String, String> entry : classRenameMap.entrySet()) {
                            if (value.equals(entry.getKey())) {
                                return entry.getValue();
                            }
                        }

                        for (Map.Entry<String, String> subClassEntry : subClassRenameMap.entrySet()) {
                            if (value.indexOf(subClassEntry.getKey()) == 0) {
                                return value.replace(subClassEntry.getKey(), subClassEntry.getValue());
                            }
                        }

                        return value;
                    }
                };
            }
        });
        DexFile rewriteDexFile = rewriter.rewriteDexFile(backedDexFile);

        DexFileWrapper fileWrapper = new DexFileWrapper(rewriteDexFile);
        DexPool dexPool = new DexPool(fileWrapper.getOpcodes());
        for (ClassDef classDef: fileWrapper.getClasses()) {
            for (String activityType : classRenameMap.values()) {
                if (classDef.getType().equals(activityType)) {
                    Method attachBaseContextMethod = InjectMethodBuilder.buildAttachBaseContextMethod(activityType);
                    ClassDefWrapper classDefWrapper = new ClassDefWrapper(classDef);
                    classDefWrapper.addMethod(attachBaseContextMethod);
                    classDef = classDefWrapper;
                }
            }
            dexPool.internClass(classDef);
        }
        dexPool.writeTo(new FileDataStore(new File("build/classesNew.dex")));
    }
}