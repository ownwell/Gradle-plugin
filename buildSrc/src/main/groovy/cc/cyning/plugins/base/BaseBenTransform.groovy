package cc.cyning.plugins.base

import cc.cyning.plugins.toast.ToastVisitor
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import org.apache.commons.io.IOUtils
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

public abstract class BaseBenTransform extends Transform {

    Project project;

    public BaseBenTransform(Project project) {
        this.project = project;
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

    private  void transformJar(File input, File dest) {
        FileUtils.copyFile(input, dest)
    }

    private  void transformSingleFile(File input, File dest) {
        FileUtils.copyFile(input,dest);
    }

    private  void transformDir(File input, File dest) {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        String srcDirPath = input.getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        for (File file : input.listFiles()) {
            String destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            if (file.isDirectory()) {
                transformDir(file, destFile)
            } else if (file.isFile()) {
                handleFile(file, destFile)

            }
        }
    }

    private  void handleFile(File file, File destFile) {

        if (isMatch(file, destFile)) {
            //通过ASM处理class文件
            println(file.name)
            println(" ------ >  find it")

            weavFile(file, destFile)

        } else {
            FileUtils.touch(destFile)
            transformSingleFile(file, destFile)
        }
    }

    protected boolean isMatch(File file, File destFile) {
        byte[] bytes = file.readBytes()
        String string = new String(bytes)
        return string.contains("android/widget/Toast") && !file.name.equals("FixToast.class")
    }

    protected abstract void weavFile(File file, File destFile) ;




    public abstract String getName() ;

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
