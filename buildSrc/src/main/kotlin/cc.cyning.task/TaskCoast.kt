package cc.cyning.task

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class TaskCoast : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("taskExInfo", Cost::class.java)


        var map = HashMap<String, TaskInfo>()
        project.gradle.addListener(object : TaskExecutionListener {
            var startTime: Long = 0

            init {

            }

            override fun beforeExecute(task: Task) {
                startTime = System.currentTimeMillis() as Long
            }

            override fun afterExecute(task: Task, taskState: TaskState) {

                val cost = System.currentTimeMillis() as Long - startTime
                val name = task.path
                var taskItem = TaskInfo(name, cost)
                map.put(name, taskItem)
//                println("$name ----------> $cost")

            }
        })


        project.gradle.addBuildListener(object : BuildListener {
            override fun buildFinished(result: BuildResult) {
                println()
                println()
                println("----------------------耗时-----------")
                var taskExInfo = project.extensions.getByType(Cost::class.java)
                map.forEach { key, value ->
                    if (taskExInfo != null) {
                        var dealLine: Int = taskExInfo.duration
                        var enable = taskExInfo.enable
                        if (enable && value.time >= dealLine) {
                            println("|  $key  ${value.time}")
                        }
                    }


                }
                println("----------------------耗时end-----------")

            }

            override fun settingsEvaluated(p0: Settings) {
            }

            override fun projectsLoaded(p0: Gradle) {
            }

            override fun buildStarted(p0: Gradle) {
            }

            override fun projectsEvaluated(p0: Gradle) {
            }
        })
    }
}

data class TaskInfo(val path: String, val time: Long)

open class Cost {
    var duration = 0
    var enable = false
    fun duration(duration: Int) {
        this.duration = duration
    }
}