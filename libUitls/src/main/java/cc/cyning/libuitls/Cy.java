package cc.cyning.libuitls;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Cy {
    public int id() default -1;

    public String msg() default "Hi";
}
