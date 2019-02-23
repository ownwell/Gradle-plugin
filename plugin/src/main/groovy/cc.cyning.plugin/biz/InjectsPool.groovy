package cc.cyning.plugin.biz

import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.MethodInfo
import javassist.bytecode.annotation.Annotation
import org.gradle.api.Project
import javassist.ClassPool
import javassist.CtClass

import java.lang.reflect.Method

/**
 * 修改bytecode
 */
public class InjectsPool {

    //初始化类池
    private final static ClassPool pool = ClassPool.getDefault();

    public static boolean shouldInject(String entryName) {
        return entryName.endsWith(".class") &&
                !entryName.contains(File.separator + "R\$") &&
                !entryName.endsWith(File.separator + "R.class") &&
                !entryName.endsWith(File.separator + "BuildConfig.class")
    }

    public static void inject(String path, Project project) {

        //将当前路径加入类池,不然找不到这个类
        println("path  -- " + path)
        pool.appendClassPath(path)

        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())

        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle");

        File dir = new File(path);
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (shouldInject(filePath)) {
                    println("filePath = " + filePath)


                    String className = (filePath - (path + "/")).replace(".class", "")
                            .replace("/", ".")
                    println(className)

                    CtClass mActivityClass = pool.getCtClass("android.app.Activity")
                    CtClass ctClass = pool.getCtClass(className)
//                    println( mActivityClass)


                    println("类名 = " + ctClass)
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()

                    CtMethod[] mCtMethods = ctClass.getDeclaredMethods();

                    mCtMethods.each { CtMethod mMethod ->
                        println("方法名 = " + mMethod.getName())
                        println("返回类型 = " + mMethod.getReturnType().getName())
//                        println("参数类型 = " + mMethod.getParameterTypes())


                        mMethod.addLocalVariable("startMs", CtClass.longType);

                        mMethod.insertBefore("startMs = System.currentTimeMillis();");
                        String body = "{" +
                                "final long endMs = System.currentTimeMillis();" +

                                "System.out.println(\"Executed in ms: [" +className + ","+mMethod.getName() +
                                "] ---> \" + (endMs-startMs));}"


                        println(body)
                        mMethod.insertAfter(body)
                        MethodInfo methodInfo = mMethod.getMethodInfo();

                        AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo
                                .getAttribute(AnnotationsAttribute.visibleTag);
                        System.out.println('attribute = ' + attribute)
                        if (attribute != null) {
                            Annotation[] anns= attribute.getAnnotations()
                            for(Annotation ann:anns) {
                                System.out.println(ann.getTypeName());
                                System.out.println(ann.getMemberValue('id'));
                                System.out.println(ann.getMemberValue('msg'));
                            }
                        }
//                        //获取注解属性 
//                        Object[] all = mMethod.getAnnotations();
//                        if (all) {
//                            Cy a = (Cy)all[0];
//                            int id = a.id()
//                            String name = a.msg()
//                            System.out.println("id: " + id + ", name: " + name);
//                        }




//                             ;  
//                             //获取注解 
//                             System.out.println(annotation); 
//                             //获取注解的值 
//                             String text =((StringMemberValue) annotation.getMemberValue("unitName")).getValue() ;        
//                             System.out.println("注解名称===" + text); 
//                                


                    }




                    ctClass.writeFile(path)
                    ctClass.detach()//释放

                }
            }
        }

    }

}
