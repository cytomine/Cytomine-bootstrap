package jsondoc.annotation

import java.lang.annotation.*

@Documented
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiObjectFieldsLight {

    public ApiObjectFieldLight[] params();

}
