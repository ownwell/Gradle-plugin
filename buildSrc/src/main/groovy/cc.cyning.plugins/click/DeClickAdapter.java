package cc.cyning.plugins.click;



import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class DeClickAdapter extends MethodVisitor {

    String[] interfaces = null;
    private String nameDesc;

    public DeClickAdapter(MethodVisitor mv, String nameDesc) {
        super(ASM7, mv);
        this.nameDesc = nameDesc;
    }


    @Override
    public void visitCode() {
        super.visitCode();
        if (nameDesc.equals("onClick(Landroid/view/View;)V")) {
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

            System.out.println("DeClickAdapter end \n");

        }
    }
}

