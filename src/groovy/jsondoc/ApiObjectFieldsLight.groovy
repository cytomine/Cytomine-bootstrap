package jsondoc

import org.jsondoc.core.annotation.ApiObjectField
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

import java.lang.annotation.*

@Documented
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiObjectFieldsLight {

    public ApiObjectFieldLight[] params();

}
