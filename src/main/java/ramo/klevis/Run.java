package ramo.klevis;

import ramo.klevis.ui.UI;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klevis.ramo on 11/14/2017.
 */
public class Run {
    public static void main(String[] args) throws Exception {
        setHadoopHomeEnvironmentVariable();
        SwingUtilities.invokeAndWait(() -> {
            try {
                new UI();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void setHadoopHomeEnvironmentVariable() throws Exception {
        HashMap<String, String> hadoopEnvSetUp = new HashMap<>();
        hadoopEnvSetUp.put("HADOOP_HOME", new File("winutils-master/hadoop-2.8.1").getAbsolutePath());
        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
        theEnvironmentField.setAccessible(true);
        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
        env.clear();
        env.putAll(hadoopEnvSetUp);
        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
        theCaseInsensitiveEnvironmentField.setAccessible(true);
        Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
        cienv.clear();
        cienv.putAll(hadoopEnvSetUp);
    }
}
