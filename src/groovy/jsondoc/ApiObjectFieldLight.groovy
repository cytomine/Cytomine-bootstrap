package jsondoc

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Target(value = ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiObjectFieldLight {

    /**
     * A drescription of what the field is
     * @return
     */
    public String description();

    /**
     * The format pattern for this field
     * @return
     */
    public String format() default "";

    /**
     * The allowed values for this field
     * @return
     */
    public String[] allowedvalues() default [];

    /**
     * The allowed type for this field.
     * Override the object class if not empty.
     * @return
     */
    public String allowedType() default "";


    /**
     * The allowed name for this field
     * Override the field name if not empty.
     * @return
     */
    public String apiFieldName() default "";

    public boolean useForCreation() default true;

    //only user if userForCreation = true
    public boolean mandatory() default true;


    //only if diff from java standart (bool true, string non empty, number !=0,...)
    public String defaultValue() default "";

    public boolean presentInResponse() default true;


}
