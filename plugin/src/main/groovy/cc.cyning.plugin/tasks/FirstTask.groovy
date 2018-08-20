package cc.cyning.plugin.tasks;

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class FirstTask extends DefaultTask {
    final Property<String> message = project.objects.property(String)
    final Property<String> greeter = project.objects.property(String)



    // @TaskAction 表示该Task要执行的动作,即在调用该Task时，hello()方法将被执行
    @TaskAction
    def hello(){
        println   message.get() + " from " + greeter.get()
    }

}
