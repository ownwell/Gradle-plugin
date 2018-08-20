package cc.cyning.plugin

import cc.cyning.plugin.extension.Message
import cc.cyning.plugin.tasks.FirstTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

public class FirstPlugin implements Plugin<Project> {

    void apply(Project project) {

        def cyMessage = project.extensions.create("cyMessage", Message, project)


        Task sendMsgTsk = project.task("sendMessage", type:FirstTask ) {

            message = cyMessage.message
            greeter = cyMessage.greeter

        }

    }
}
