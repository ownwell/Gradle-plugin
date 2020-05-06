package cc.cyning.plugins.toast;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FunToastVistor extends MethodVisitor {
    public static final String  TOAST_CLASS = "android/widget/Toast";
    public static final String   FIX_TOAST_CLASS = "cc/cyning/ben/FixToast";
    public FunToastVistor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitMethodInsn(int opcode, String own, String name, String descriptor, boolean isInterface) {
        String desc = descriptor;

        System.out.println("--------- start----------" + desc);

        String owner = own;

        try {
//            Class<?> clazz = Class.forName(owner.replace("/", "."));
//            Class<?> clazz2 = Class.forName(TOAST_CLASS.replace("/", "."));
//            System.out.println(clazz2.isAssignableFrom(clazz));


            //覆盖方法调用的过程
            if (opcode == Opcodes.INVOKESTATIC && TOAST_CLASS.equals(owner) ) {

                desc = "(Landroid/content/Context;Ljava/lang/CharSequence;I)L"+FIX_TOAST_CLASS+";";
                //方法描述符替换为影子方法
                owner = FIX_TOAST_CLASS;
                System.out.println("dfdfdfdfd");
            }
            if(name.equals( "show")) {
                owner = FIX_TOAST_CLASS;
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("end -------------------");


        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }
}
