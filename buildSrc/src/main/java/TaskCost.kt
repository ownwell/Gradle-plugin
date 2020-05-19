import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class TaskCost : Plugin<Project> {
    override fun apply(project: Project) {
        project.gradle.addListener(object : TaskExecutionListener {
            override fun beforeExecute(task: Task) {}
            override fun afterExecute(task: Task, taskState: TaskState) {}
        })
    }
}