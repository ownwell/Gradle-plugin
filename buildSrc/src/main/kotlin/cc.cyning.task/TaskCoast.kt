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

    override fun apply(p0: Project) {
        var map = HashMap<String, TaskInfo>()
        p0.gradle.addListener(object : TaskExecutionListener {
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


        p0.gradle.addBuildListener(object : BuildListener {
            override fun buildFinished(result: BuildResult) {
                println("----------------------耗时-----------")
                map.forEach { key, value ->
                    if (value.time >= 100L) {
                        println("|  $key  ${value.time}")
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