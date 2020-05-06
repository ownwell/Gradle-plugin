package cc.cyning.plugins.toast

import cc.cyning.plugins.base.BaseBenTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

public class ToastFixTransform extends BaseBenTransform {
    public ToastFixTransform(Project project) {
        super(project);
    }

    @Override
    public String getName() {
        return 'fix_toast';
    }

    @Override
    protected boolean isMatch(File file, File destFile) {
        byte[] bytes = file.readBytes();
        String string = new String(bytes)
        return string.contains("android/widget/Toast") && !file.name.equals("FixToast.class")
    }



    @Override
    protected void weavFile(File file, File destFile) {

        classVisitor(file,destFile)
    }
    private static void classVisitor(File input, File output) {
        try {
            println("classVisitor")
            String inputPath = input.getAbsolutePath();
//            println(inputPath)
            FileInputStream is = new FileInputStream(inputPath)

            ClassReader reader = new ClassReader(is)
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            ToastVisitor cv = new ToastVisitor(writer)
            reader.accept(cv, ClassReader.EXPAND_FRAMES)
            writer.visitEnd()
            byte[] array = writer.toByteArray()
            FileOutputStream fout = new FileOutputStream(output);
            fout.write(array);
            fout.close();
            print("end --- classVisitor ")
        } catch (Exception e) {
            print(e)
        }
    }

}
