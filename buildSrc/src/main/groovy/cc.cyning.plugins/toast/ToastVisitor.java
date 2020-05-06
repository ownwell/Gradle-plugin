package cc.cyning.plugins.toast;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cc.cyning.plugins.toast.FunToastVistor;

public class ToastVisitor  extends ClassVisitor implements Opcodes {
    public ToastVisitor( ClassVisitor classVisitor) {
        super(ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
       MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        System.out.println("visitMethod");
        return new FunToastVistor(Opcodes.ASM7, mv);
    }
}
