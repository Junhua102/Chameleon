package me.dreamheart.chameleon.dex;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Created by Junhua Lv on 2017/2/1.
 * 对APK中的dex进行重构
 */

public class ApkRefactor {

    static public class RefactorItem {
        private String srcClassName;
        private String desClassName;
        String srcClassType;
        String desClassType;
        String srcSubClassTypePrefix;
        String desSubClassTypePrefix;
        String srcPackageClassTypePrefix;
        int srcPackageClassTypePrefixLen;
//        String desPackageClassTypePrefix;
        boolean isActivity;

        public RefactorItem(String orgClassName, String newClassName) {
            this(orgClassName, newClassName, true);
        }

        public RefactorItem(String orgClassName, String newClassName, boolean isActivity) {
            srcClassName = orgClassName.replace(".", "/");
            desClassName = newClassName.replace(".", "/");
            srcClassType = "L" + srcClassName + ";";
            desClassType = "L" + desClassName + ";";
            srcSubClassTypePrefix = "L" + srcClassName + "$";
            desSubClassTypePrefix = "L" + desClassName + "$";
            srcPackageClassTypePrefix = "L" + srcClassName.substring(0, srcClassName.lastIndexOf('/') + 1);
            srcPackageClassTypePrefixLen= srcPackageClassTypePrefix.length();
//            desPackageClassTypePrefix = "L" + desClassName.substring(0, desClassName.lastIndexOf('/') + 1);
            this.isActivity = isActivity;
        }

        /**
         * 是否与被改名的类在同一包名下
         * @param type
         * @return
         */
        public boolean isSamePackage(String type){
            return type.indexOf(srcPackageClassTypePrefix) == 0 && type.substring(srcPackageClassTypePrefixLen).indexOf('/') < 0;
        }
    }

    static public class ApkInfo {
        String packageName;

        public ApkInfo(String packageName) {
            this.packageName = packageName.replace(".", "/");
        }
    }

    public static boolean refactoring(String apkFile, ApkInfo apkInfo, List<RefactorItem> refactorItems, String outFile, String tempFileDir) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(apkFile));
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outFile));

            ZipEntry zipEntry;
            byte[] entryData = new byte[512 * 1024];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String zipEntryName = zipEntry.getName();
                zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
                if (zipEntryName.equals("classes.dex")) {
                    ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                    int readSize;
                    while ((readSize = zipInputStream.read(entryData)) > 0) {
                        swapStream.write(entryData, 0, readSize);
                    }
                    refactoring(swapStream.toByteArray(), apkInfo, refactorItems, zipOutputStream, tempFileDir);
                } else {
                    int readSize;
                    while ((readSize = zipInputStream.read(entryData)) > 0) {
                        zipOutputStream.write(entryData, 0, readSize);
                    }
                }
            }

            zipOutputStream.finish();
            zipOutputStream.close();
            zipInputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param dexData
     * @param apkInfo
     * @param refactorItems
     * @param outputStream
     * @param tempFileDir
     * @throws Exception
     */
    private static void refactoring(byte[] dexData, ApkInfo apkInfo, final List<RefactorItem> refactorItems, OutputStream outputStream, String tempFileDir) throws Exception{
        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(Opcodes.forApi(Config.ApiLevel), new ByteArrayInputStream(dexData));

        DexRewriter accessFlagRewriter = new DexRewriter(new RewriterModule() {

            /**
             * 将同包名其他类的Field由default改成public
             * @param rewriters
             * @return
             */
            @Nonnull
            @Override
            public Rewriter<Field> getFieldRewriter(@Nonnull Rewriters rewriters) {
                return new Rewriter<Field>() {
                    @Nonnull
                    @Override
                    public Field rewrite(@Nonnull Field value) {
//                        System.out.print(value.getName() + "\n");
                        for (RefactorItem refactorItem : refactorItems) {
                            if (refactorItem.isSamePackage(value.getDefiningClass()) &&
                                    AccessFlagUtils.isDefault(value.getAccessFlags())) {
                                int accessFlag = AccessFlagUtils.changeToPublic(value.getAccessFlags());
                                return new ImmutableField(value.getDefiningClass(), value.getName(),
                                        value.getType(), accessFlag, value.getInitialValue(), value.getAnnotations());
                            }
                        }
                        return value;
                    }
                };
            }

            /**
             * 将同包名其他类的Method由default改成public
             * @param rewriters
             * @return
             */
            @Nonnull
            @Override
            public Rewriter<Method> getMethodRewriter(@Nonnull Rewriters rewriters) {
                return new Rewriter<Method>() {
                    @Nonnull
                    @Override
                    public Method rewrite(@Nonnull Method value) {
//                        System.out.print(value.getName() + "\n");
                        for (RefactorItem refactorItem : refactorItems) {
                            if (refactorItem.isSamePackage(value.getDefiningClass()) &&
                                    AccessFlagUtils.isDefault(value.getAccessFlags())) {
                                int accessFlag = AccessFlagUtils.changeToPublic(value.getAccessFlags());
                                return new ImmutableMethod(value.getDefiningClass(), value.getName(),
                                        value.getParameters(), value.getReturnType(),accessFlag,
                                        value.getAnnotations(), value.getImplementation());
                            }
                        }
                        return value;
                    }
                };
            }
        });
        DexFile rewriteDexFile = accessFlagRewriter.rewriteDexFile(dexBackedDexFile);

        DexRewriter typeRewriter = new DexRewriter(new RewriterModule() {
            @Nonnull
            @Override
            public Rewriter<String> getTypeRewriter(@Nonnull Rewriters rewriters) {
                return new Rewriter<String>() {
                    @Nonnull
                    @Override
                    public String rewrite(@Nonnull String value) {
//                        System.out.print(value + "\n");

                        for (RefactorItem refactorItem : refactorItems) {
                            if (value.equals(refactorItem.srcClassType)) {
                                return refactorItem.desClassType;
                            } else if (value.indexOf(refactorItem.srcSubClassTypePrefix) == 0) {
                                return value.replace(refactorItem.srcSubClassTypePrefix, refactorItem.desSubClassTypePrefix);
                            }
                        }

                        return value;
                    }
                };
            }
        });
        rewriteDexFile = typeRewriter.rewriteDexFile(rewriteDexFile);

        DexFileWrapper fileWrapper = new DexFileWrapper(rewriteDexFile);
        DexPool dexPool = new DexPool(fileWrapper.getOpcodes());
        for (ClassDef classDef: fileWrapper.getClasses()) {
            for (RefactorItem refactorItem : refactorItems) {

                // 同包名的类，将访问权限由default改成public
                if (refactorItem.isSamePackage(classDef.getType()) &&
                        AccessFlagUtils.isDefault(classDef.getAccessFlags())) {
                    ClassDefWrapper classDefWrapper = new ClassDefWrapper(classDef);
                    classDefWrapper.setAccessFlags(AccessFlagUtils.changeToPublic(classDef.getAccessFlags()));
                    classDef = classDefWrapper;
                }

                if (!refactorItem.isActivity)
                    continue;

                String activityType = refactorItem.desClassType;
                if (classDef.getType().equals(activityType)) {
                    ClassDefWrapper classDefWrapper;
                    if (classDef instanceof ClassDefWrapper) {
                        classDefWrapper = (ClassDefWrapper) classDef;
                    } else {
                        classDefWrapper = new ClassDefWrapper(classDef);
                    }
                    // 向Activity注入attachBaseContext的钩子代码
                    Method attachBaseContextMethod = InjectMethodBuilder.buildAttachBaseContextMethod(activityType);
                    classDefWrapper.addMethod(attachBaseContextMethod);
                    // 向Activity注入onCreate的钩子代码
                    Method onCreateMethod = classDefWrapper.getVirtualMethod("onCreate");
                    Method onCreateMethodInjected = InjectMethodBuilder.buildOnCreateMethod(activityType, onCreateMethod);
                    classDefWrapper.replaceVirtualMethod(onCreateMethodInjected);

                    classDef = classDefWrapper;
                }
            }
            dexPool.internClass(classDef);
        }
        // 先写到临时文件里
        File tempFile = new File(tempFileDir, UUID.randomUUID().toString());
        dexPool.writeTo(new FileDataStore(tempFile));
        // 再从文件里读取出来
        FileInputStream fileInputStream = new FileInputStream(tempFile);
        byte[] fileData = new byte[512 * 1024];
        int readSize;
        while ((readSize = fileInputStream.read(fileData)) > 0) {
            outputStream.write(fileData, 0, readSize);
        }
        fileInputStream.close();
        // 删除临时文件
        tempFile.delete();
    }

    /**
     * 重构过程中，可能会将同包名下的applicaion也重命名了
     * @deprecated
     * @param oldName
     * @param refactorItems
     * @return
     */
    static public String getNewApplicationName (String oldName, List<RefactorItem> refactorItems) {
        return oldName;
    }
}
