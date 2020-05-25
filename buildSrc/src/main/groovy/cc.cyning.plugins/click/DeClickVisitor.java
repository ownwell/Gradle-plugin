package cc.cyning.plugins.click;



import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class DeClickVisitor extends MethodVisitor {

    String[] interfaces = null;
    private String nameDesc;
    private String mIdValue = null;

    public DeClickVisitor(MethodVisitor mv, String nameDesc) {
        super(ASM7, mv);
        this.nameDesc = nameDesc;
    }


    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        p("visitAnnotation " + descriptor);

        if (!"Lcc/cyning/libuitls/Cy;".equals(descriptor) ) {
            AnnotationVisitor av =  super.visitAnnotation(descriptor, visible);
            return av;

        }

        return  new AnnotationVisitor(ASM7) {
            @Override
            public void visit(String name, Object value) {
                super.visit(name, value);

                p("visitAnnotation visit " + name + ",vaule =" + value);
                if ("msg".equals(name)) {
                    mIdValue = (String) value;
                }

            }

            @Override
            public void visitEnum(String name, String descriptor, String value) {
                super.visitEnum(name, descriptor, value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                return super.visitAnnotation(name, descriptor);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return super.visitArray(name);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                p("visitAnnotation visit " + "visitEnd =" );

            }
        };

    }




    @Override
    public void visitInsn(int opcode) {
        p("visitInsn " );

        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        p("visitEnd " );

        super.visitEnd();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        p("visitMethodInsn " + owner+"." +name+"("+ descriptor+")");

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        p("visitInsnAnnotation "  + descriptor);

        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        p("visitIntInsn");

    }

    @Override
    public void visitCode() {
        super.visitCode();
        p("visitCode " + nameDesc);
        if (nameDesc.equals("onClick(Landroid/view/View;)V") && "true".equals(mIdValue)) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(new Long(800L));
            mv.visitMethodInsn(
                    INVOKESTATIC, "cc/cyning/fastclick/FastClick", "isFastDoubleClick",
                    "(Landroid/view/View;J)Z", false
            );

            Label label = new Label();
            mv.visitJumpInsn(IFEQ, label);
            mv.visitInsn(RETURN);
            mv.visitLabel(label);

            mIdValue = null;
        }

    }
    private void p(String line) {
        System.out.println("--------->"+ "CCCC: " + line);
    }
}

