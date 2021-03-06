package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.inject.Qualifier;
import javax.inject.Scope;
import org.jetbrains.annotations.Nullable;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;

final class Util {
  static <T extends AccessibleObject & Member> void validateVisibility(T target) {
    int modifiers = target.getModifiers();
    if ((modifiers & (PRIVATE | PROTECTED)) != 0) {
      throw new IllegalStateException(target + " must be public or package-protected");
    }
    if ((modifiers & PUBLIC) == 0) {
      target.setAccessible(true);
    }
  }

  static @Nullable Annotation findQualifier(Annotation[] annotations) {
    Annotation qualifier = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
        if (qualifier != null) {
          throw new IllegalStateException("Found multiple qualifier annotations: @"
              + qualifier.annotationType().getName()
              + " and @"
              + annotation.annotationType().getName());
        }
        qualifier = annotation;
      }
    }
    return qualifier;
  }

  static @Nullable Annotation findScope(Annotation[] annotations) {
    Annotation scope = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Scope.class) != null) {
        if (scope != null) {
          throw new IllegalStateException("Found multiple scope annotations: @"
              + scope.annotationType().getName()
              + " and @"
              + annotation.annotationType().getName());
        }
        scope = annotation;
      }
    }
    return scope;
  }

  static boolean hasAnnotation(Annotation[] annotations, Class<?> annotationClass) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(annotationClass)) {
        return true;
      }
    }
    return false;
  }

  static Object tryInvoke(Method method, Object target, Object... arguments) {
    Throwable cause;
    try {
      return method.invoke(target, arguments);
    } catch (IllegalAccessException e) {
      cause = e;
    } catch (InvocationTargetException e) {
      cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
    }
    throw new RuntimeException(
        "Unable to invoke " + method + " on " + target + " with arguments "
            + Arrays.toString(arguments), cause);
  }

  static <T> T tryNewInstance(Constructor<T> constructor, Object... arguments) {
    Throwable cause;
    try {
      return constructor.newInstance(arguments);
    } catch (IllegalAccessException e) {
      cause = e;
    } catch (InstantiationException e) {
      cause = e;
    } catch (InvocationTargetException e) {
      cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
    }
    throw new RuntimeException(
        "Unable to invoke " + constructor + " with arguments " + Arrays.toString(arguments), cause);
  }

  private Util() {}
}
