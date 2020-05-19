package cc.cyning.plugins.click;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

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
        return (mv == null) ? null : new DeClickVisitor(mv, nameDesc);
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        System.out.println("name" + name );

    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
        System.out.println("内部类" + innerName );
        System.out.println("name" + name );
        System.out.println("outerName" + outerName );
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        cv.visitField(Opcodes.ACC_PUBLIC, "age", Type.getDescriptor(int.class), null, null);

    }
}
