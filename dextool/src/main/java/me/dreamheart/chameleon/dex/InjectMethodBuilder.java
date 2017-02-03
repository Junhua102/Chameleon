package me.dreamheart.chameleon.dex;

import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jf.dexlib2.immutable.instruction.ImmutableInstructionFactory;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.util.ArrayList;

/**
 * Created by ljh102 on 2017/1/30.
 * 创建需要注入的方法
 */
public class InjectMethodBuilder {

    static private MethodReference getHookAttachBaseContextMethodRef () {
        return new ImmutableMethodReference("Lme/dreamheart/chameleon/Hook;", "attachBaseContext", Lists.newArrayList("Ljava/lang/Object;", "Ljava/lang/Object;"), "Ljava/lang/Object;");
    }

    static private MethodReference getActivityAttachBaseContextMethodRef () {
        return new ImmutableMethodReference("Landroid/app/Activity;", "attachBaseContext", Lists.newArrayList("Landroid/content/Context;"), "V");
    }

    static private TypeReference getContextTypeReference () {
        return new ImmutableTypeReference("Landroid/content/Context;");
    }

    static public Method buildAttachBaseContextMethod (String className) {
        ArrayList<ImmutableInstruction> instructions = Lists.newArrayList(
                ImmutableInstructionFactory.INSTANCE.makeInstruction35c(Opcode.INVOKE_STATIC, 2, 1, 2, 0, 0, 0, getHookAttachBaseContextMethodRef()),
                ImmutableInstructionFactory.INSTANCE.makeInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0),
                ImmutableInstructionFactory.INSTANCE.makeInstruction21c(Opcode.CHECK_CAST, 0, getContextTypeReference()),
                ImmutableInstructionFactory.INSTANCE.makeInstruction35c(Opcode.INVOKE_SUPER, 2, 1, 0, 0, 0, 0, getActivityAttachBaseContextMethodRef()),
                ImmutableInstructionFactory.INSTANCE.makeInstruction10x(Opcode.RETURN_VOID)
        );
        ImmutableMethodImplementation methodImpl = new ImmutableMethodImplementation(3, instructions, null, null);

        ArrayList<ImmutableMethodParameter> methodParameters = Lists.newArrayList(new ImmutableMethodParameter("Landroid/content/Context;", null, "newBase"));
        return new ImmutableMethod(className, "attachBaseContext", methodParameters, "V", AccessFlags.PROTECTED.getValue(), null,
                methodImpl);
    }
}
