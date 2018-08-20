package cc.cyning.plugin.extension

import org.gradle.api.Project
import org.gradle.api.provider.Property;

public class Message {
    final Property<String> message;

    final Property<String> greeter;


    Message(Project project) {
        message = project.objects.property(String)
        greeter = project.objects.property(String)

        message.set('Hello default message' )
    }
}
