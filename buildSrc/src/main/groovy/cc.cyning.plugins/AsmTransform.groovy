package cc.cyning.plugins

import cc.cyning.plugins.click.FastClickAdapter
import cc.cyning.plugins.toast.ToastVisitor
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import org.objectweb.asm.ClassReader
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager

import org.apache.commons.io.FileUtils

import org.gradle.api.Project
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

public class AsmTransform extends Transform {

    Project project;

    public AsmTransform(Project project) {
        this.project = project;
    }
//    @Override
//    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        println("===============ToastFix plugin start===============")
//        transformInvocation.inputs.forEach { transformInput ->
//            transformInput.directoryInputs.forEach {
//                //获取输出dir
//                        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
//
//                File outputDir = outputProvider.getContentLocation(
//                        it.file.absolutePath,
//                        it.contentTypes,
//                        it.scopes,
//                        Format.DIRECTORY)
//                File inputDir = it.file
//                //获取outputDir下所有的class文件
//                com.android.utils.FileUtils.getAllFiles(inputDir).forEach { inputFile ->
//                    byte[] bytes = inputFile.readBytes()
//                    String string = new String(bytes)
//
////                    println(" ------ > " + string)
//                    //待输出待文件
//                    File outputFile = new  File(outputDir, inputFile.name)
//                    if(!outputFile.exists()) {
//                        outputFile.parentFile.mkdir()
//                    }
//                    if(string.contains("android/widget/Toast")) {
//                        //通过ASM处理class文件
//                        println(inputFile.name)
//                        println(" ------ >  find it" )
//                        classVisitor(inputFile, bytes, outputFile)
//
//                    } else {
//                        try {
//                            FileOutputStream fout = new FileOutputStream(output);
//                            fout.write(array);
//                            fout.close();
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            }
//        }
//    }
    private static void classVisitor(File input, byte[] bytes, File output) {
        try {
            println("classVisitor")
            String inputPath = input.getAbsolutePath();
//            println(inputPath)
            FileInputStream is = new FileInputStream(inputPath)
            ClassReader cr = new ClassReader(is)

            ClassReader reader = new ClassReader(bytes)
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            ToastVisitor cv = new ToastVisitor( writer)
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

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        //当前是否是增量编译
        boolean isIncremental = transformInvocation.isIncremental()
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        //引用型输入，无需输出。
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                File dest = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR
                )
                //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                transformJar(jarInput.getFile(), dest)
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
//                println("== DI = " + directoryInput.file.listFiles().toArrayString())
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY


                )
                //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                //FileUtils.copyDirectory(directoryInput.getFile(), dest)
                transformDir(directoryInput.getFile(), dest)
            }
        }

    }

    private static void transformJar(File input, File dest) {
//        println("=== transformJar ===")
        FileUtils.copyFile(input, dest)
    }

    private static void transformSingleFile(File input, File dest) {
        weave(input.getAbsolutePath(), dest.getAbsolutePath())
    }

    private static void transformDir(File input, File dest) {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        String srcDirPath = input.getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
//        println("=== transform dir = " + srcDirPath + ", " + destDirPath)
        for (File file : input.listFiles()) {
//            print(" === filename :" + file.name + "\n")
            String destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            if (file.isDirectory()) {
                transformDir(file, destFile)
            } else if (file.isFile()) {
                toastUtils(file, destFile)

            }
        }
    }

    private static void toastUtils(File file, File destFile) {
        byte[] bytes = file.readBytes()
        String string = new String(bytes)
        if (string.contains("android/widget/Toast") && !file.name.equals("FixToast.class")) {
            //通过ASM处理class文件
            println(file.name)
            println(" ------ >  find it")


            classVisitor(file, bytes, destFile)
//

        }
        else {
        FileUtils.touch(destFile)
        transformSingleFile(file, destFile)
        }
    }


    private static void weave(String inputPath, String outputPath) {
        try {

            FileInputStream is = new FileInputStream(inputPath)
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
            FastClickAdapter adapter = new FastClickAdapter(cw)
            cr.accept(adapter, 0)
            FileOutputStream fos = new FileOutputStream(outputPath)
            fos.write(cw.toByteArray())
            fos.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }


    @Override
    public String getName() {
        return "ben-asm";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }
}
