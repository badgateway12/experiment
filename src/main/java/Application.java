import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;

public class Application {
    public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, MalformedURLException {
        final DockerClient docker = DefaultDockerClient.fromEnv().build();

        final HostConfig hostConfig = HostConfig
                .builder()
                .appendBinds(HostConfig.Bind.from("/Users/ibutakova/solution/source")
                        .to("/source")
                        .readOnly(true)
                        .build())
                .appendBinds(HostConfig.Bind.from("/Users/ibutakova/solution/target")
                        .to("/target")
                        .readOnly(false)
                        .build())
                .appendBinds(HostConfig.Bind.from("/Users/ibutakova/solution/tests")
                        .to("/tests")
                        .readOnly(false)
                        .build())
                .build();

        final ContainerConfig containerConfig = ContainerConfig
                .builder()
                .image("openjdk:11")
                .hostConfig(hostConfig)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        final ContainerCreation container = docker.createContainer(containerConfig);

        final String containerId = container.id();

        docker.startContainer(containerId);

        String[] compileSourcesCmd = {"sh", "-c", "javac /source/Solution.java -d /target"};
        String[] compileJUnit4TestsCmd = {"sh", "-c", "javac -cp ./tests/junit-4.12.jar ./tests/TestClass.java -d ./tests"};
        String[] compileJUnit5TestsCmd = {"sh", "-c", "javac -cp .:/tests/junit-jupiter-api-5.4.0-M1.jar:/tests/apiguardian-api-1.0.0.jar:/tests/junit-jupiter-params-5.4.0-M1.jar ./tests/DynamicTestClass.java -d ./tests"};
        String[] runJUnit4TestsCmd = {"sh", "-c", "java -cp .:/tests:/tests/junit-4.12.jar:/tests/hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestClass"};
        //String[] runJUnit5TestsCmd = {"sh", "-c", "java -cp .:/tests:/tests/junit-platform-console-standalone-1.4.0-M1.jar DynamicTestClass"};

        for (String[] cmd : List.of(compileSourcesCmd, compileJUnit4TestsCmd, compileJUnit5TestsCmd, runJUnit4TestsCmd)) {
            final ExecCreation execCreation = docker.execCreate(
                    containerId, cmd, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());

            final LogStream output = docker.execStart(execCreation.id());
            final String execOutput = output.readFully();

            if (!execOutput.isEmpty() && !execOutput.contains("OK")) {
                System.out.println("compilation failed");
                System.out.println(execOutput);
            }
            else {
                System.out.println("compilation succeeded");
            }
        }

        cleanUp(docker, containerId);
    }

    private static void cleanUp(DockerClient docker, String id) throws DockerException, InterruptedException {
        docker.stopContainer(id, 1);
        docker.removeContainer(id);
        docker.close();
    }
}
