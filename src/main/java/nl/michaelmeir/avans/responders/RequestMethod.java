package nl.michaelmeir.avans.responders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//RequestMethod is used to specify the request method that the method has to be called with
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMethod {
    String value();
}

//Value contains a value name and value type that specific values in a request body should contain
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Value {
    public String name();
    public Class type();
}

//Required contains a Value array to read from what a method requires to be called from a request method
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Required {
    public Value[] value();
}