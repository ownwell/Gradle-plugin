package cc.cyning.plugins.click;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cc.cyning.plugins.click.DeClickAdapter;

/**
 *
 */
public class FastClickAdapter extends ClassVisitor implements Opcodes {

    public FastClickAdapter(ClassVisitor classVisitor) {
        super(ASM7, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        String nameDesc = name+ descriptor;
        return (mv == null) ? null : new DeClickAdapter(mv, nameDesc);
    }

}
