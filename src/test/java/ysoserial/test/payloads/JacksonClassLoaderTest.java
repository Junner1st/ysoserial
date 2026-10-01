package ysoserial.test.payloads;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class JacksonClassLoaderTest {

    @Test
    public void createsJackson2PayloadFromIsolatedClassLoader() throws Exception {
        assertPayloadCanBeCreated("Jackson2", "javax.management.BadAttributeValueExpException");
    }

    @Test
    public void createsJackson1PayloadFromIsolatedClassLoader() throws Exception {
        assertPayloadCanBeCreated("Jackson1", "java.util.HashMap");
    }

    private void assertPayloadCanBeCreated(String payloadName, String expectedPayloadClass) throws Exception {
        String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
        List<URL> urls = new ArrayList<URL>(classPathEntries.length);
        for (String classPathEntry : classPathEntries) {
            urls.add(new File(classPathEntry).toURI().toURL());
        }

        URLClassLoader payloadClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
        try {
            Class<?> payloadUtils = Class.forName("ysoserial.payloads.ObjectPayload$Utils", true, payloadClassLoader);
            Method makePayloadObject = payloadUtils.getMethod("makePayloadObject", String.class, String.class);

            Object payload = makePayloadObject.invoke(null, payloadName, "true");

            assertEquals(expectedPayloadClass, payload.getClass().getName());
            Class<?> baseJsonNode = Class.forName("com.fasterxml.jackson.databind.node.BaseJsonNode", false, payloadClassLoader);
            assertSame(payloadClassLoader, baseJsonNode.getClassLoader());
        } finally {
            payloadClassLoader.close();
        }
    }
}
