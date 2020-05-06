package cc.cyning.plugins.click

import cc.cyning.plugins.base.BaseBenTransform
import cc.cyning.plugins.toast.ToastVisitor
import com.android.build.api.transform.Transform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

public class FastClickTransform extends BaseBenTransform {
    public FastClickTransform(Project project) {
        super(project);
    }

    @Override
    public String getName() {
        return 'fast_click';
    }

    @Override
    protected boolean isMatch(File file, File destFile) {
        return  true
    }



    @Override
    protected void weavFile(File file, File destFile) {

        classVisitor(file,destFile)
    }
    private static void classVisitor(File input, File output) {
        try {
            String inputPath = input.getAbsolutePath();
//            println(inputPath)
            FileInputStream is = new FileInputStream(inputPath)

            ClassReader reader = new ClassReader(is)
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            FastClickAdapter cv = new FastClickAdapter(writer)
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
