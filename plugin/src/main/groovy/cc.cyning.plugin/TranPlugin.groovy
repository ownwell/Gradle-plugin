package cc.cyning.plugin

import com.android.build.gradle.AppExtension

import cc.cyning.plugin.transforms.ClassTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

public class TranPlugin implements Plugin<Project> {
    void apply(Project project) {
        System.out.println("------------------开始----------------------");
        //AppExtension就是build.gradle中android{...}这一块
        def android = project.extensions.getByType(AppExtension)

        //注册一个Transform
        def classTransform = new ClassTransform(project);
        android.registerTransform(classTransform);

        System.out.println("------------------结束了吗----------------------");
    }

}