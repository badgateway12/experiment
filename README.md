Создай папку solution. В ней три папки source, target и tests.
В папке source создай файл Solution.java  с кодом:

```

public class Solution {
    public String method2(String arg) {
        return arg;
    }
}

```

В папке tests создай 1-й файл TestClass.java  с кодом:

```

import org.junit.Test;
import org.junit.Assert;

public class TestClass {
   String arg = "hello";
   @Test
   public void test() {
      Assert.assertEquals(arg, "hello");
   }
}


```

и 2-й файл DynamicTestClass.java  с кодом:


```

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamicTestClass {
    private static Object solution;
    private static Method method;

    @BeforeAll
    static void beforeAllTests() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, MalformedURLException {
        File file = new File("/Users/ibutakova/solution/target/");
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
        Class<?>  cls = cl.loadClass("Solution");
        Constructor<?> constructor = cls.getConstructor();
        solution = constructor.newInstance();
        method = solution.getClass().getDeclaredMethod("method2", String.class);
        method.setAccessible(true);
    }

    @ParameterizedTest
    @CsvSource({ "Hello, Hello", "Yes, Yes", "No, No"})
    void testMethod(String in, String out) throws InvocationTargetException, IllegalAccessException {
        assertEquals(out, method.invoke(solution, in));
    }
}

```

В папку tests скачай и положи .jar:
 - junit-4.12.jar
 - hamcrest-core-1.3.jar
 - junit-jupiter-api-5.4.0-M1.jar
 - apiguardian-api-1.0.0.jar
 - junit-jupiter-params-5.4.0-M1.jar

Исправь везде path.

Под мак команда "java -cp .:/tests:/tests/junit-4.12.jar:/tests/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestClass"};

Под винду возможно так "java -cp .;/tests:/tests/junit-4.12.jar;/tests/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestClass"};
