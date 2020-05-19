package cc.cyning.plugins

import cc.cyning.plugins.base.BaseBenTransform
import cc.cyning.plugins.click.FastClickTransform
import cc.cyning.plugins.toast.ToastFixTransform
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class FixToastPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        registerTransform(android)
    }

    def static registerTransform(BaseExtension android) {
        BaseBenTransform transform = new ToastFixTransform()
        android.registerTransform(transform)
    }
}
