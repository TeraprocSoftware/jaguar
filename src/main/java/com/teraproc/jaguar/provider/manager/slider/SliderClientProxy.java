package com.teraproc.jaguar.provider.manager.slider;

import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.log.JaguarLoggerFactory;
import com.teraproc.jaguar.log.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.slider.api.ClusterDescription;
import org.apache.slider.client.SliderClient;
import org.apache.slider.common.params.ActionFlexArgs;
import org.apache.slider.common.params.ActionResizeContainerArgs;
import org.apache.slider.core.exceptions.SliderException;
import org.apache.slider.core.exceptions.UnknownApplicationInstanceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

@Service
public class SliderClientProxy {
  private static final Logger LOGGER =
      JaguarLoggerFactory.getLogger(SliderClientProxy.class);
  private static final String YARN_MEMORY = "yarn.memory";
  private static final String YARN_VCORES = "yarn.vcores";
  private static final String REQUESTED_INSTANCES = "role.requested.instances";
  private static final String ACTUAL_INSTANCES = "role.actual.instances";
  private static String executeUser = "";

  @Value("${hadoop.conf.dir}")
  private String hadoopConfDir;

  @Value("${slider.conf.dir}")
  private String sliderConfDir;

  public SliderApp getSliderApp(final JaguarUser user, final String appName)
      throws YarnException, IOException, InterruptedException {
    setUsertoRunAs(user.getId());
    return invokeSliderClientRunnable(
        new SliderClientContextRunnable<SliderApp>() {
          @Override
          public SliderApp run(SliderClient sliderClient)
              throws YarnException, IOException {
            if (appName != null) {
              ClusterDescription description =
                  sliderClient.getClusterDescription(appName);
              return createSliderAppObject(description);
            }
            return null;
          }
        });
  }

  public void flexApp(
      final JaguarUser user, final String appName,
      final Map<String, Integer> componentsMap)
      throws YarnException, IOException, InterruptedException {
    setUsertoRunAs(user.getId());
    invokeSliderClientRunnable(
        new SliderClientContextRunnable<ApplicationId>() {
          @Override
          public ApplicationId run(SliderClient sliderClient)
              throws YarnException,
              IOException, InterruptedException {
            ActionFlexArgs flexArgs = new ActionFlexArgs();
            flexArgs.parameters.add(appName);
            for (Map.Entry<String, Integer> e : componentsMap.entrySet()) {
              flexArgs.componentDelegate.componentTuples.add(e.getKey());
              flexArgs.componentDelegate.componentTuples.add(
                  e.getValue()
                      .toString());
            }
            sliderClient.actionFlex(appName, flexArgs);
            return sliderClient.applicationId;
          }
        });
    LOGGER.info(
        Logger.NOT_SERVICE_RELATED, "Flexed Slider App [" + appName + "]");
  }

  public void resizeContainer(
      final JaguarUser user, final String appName,
      final String containerId, final int vCores, final int memory)
      throws YarnException, IOException, InterruptedException {
    setUsertoRunAs(user.getId());
    invokeSliderClientRunnable(
        new SliderClientContextRunnable<ApplicationId>() {
          @Override
          public ApplicationId run(SliderClient sliderClient)
              throws YarnException,
              IOException, InterruptedException {
            ActionResizeContainerArgs resizeArgs =
                new ActionResizeContainerArgs();
            resizeArgs.parameters.add(appName);
            resizeArgs.containers.add(containerId);
            resizeArgs.vCores = vCores;
            resizeArgs.memory = memory;
            sliderClient.actionResizeContainer(appName, resizeArgs);
            return sliderClient.applicationId;
          }
        });
    LOGGER.info(
        Logger.NOT_SERVICE_RELATED,
        "Resized Slider App [" + appName + "] container [" + containerId + "]");
  }

  public boolean appExists(final JaguarUser user, final String appName)
      throws IOException, InterruptedException, YarnException {
    setUsertoRunAs(user.getId());
    return invokeSliderClientRunnable(
        new SliderClientContextRunnable<Boolean>() {
          @Override
          public Boolean run(SliderClient sliderClient)
              throws YarnException, IOException {
            if (appName != null) {
              try {
                return sliderClient.actionExists(appName, false)
                    == SliderClient.EXIT_SUCCESS;
              } catch (UnknownApplicationInstanceException e) {
                return Boolean.FALSE;
              }
            }
            return Boolean.FALSE;
          }
        });
  }

  public boolean appComptExists(
      final JaguarUser user, final String appName, final String comptName)
      throws YarnException, IOException, InterruptedException {
    setUsertoRunAs(user.getId());
    return invokeSliderClientRunnable(
        new SliderClientContextRunnable<Boolean>() {
          @Override
          public Boolean run(SliderClient sliderClient)
              throws YarnException, IOException {
            if (appName != null && comptName != null) {
              try {
                if (sliderClient.actionExists(appName, Boolean.FALSE)
                    == SliderClient.EXIT_SUCCESS) {
                  ClusterDescription description =
                      sliderClient.getClusterDescription(appName);
                  if (description.roles.containsKey(comptName)) {
                    return Boolean.TRUE;
                  }
                }
              } catch (Exception e) {
                return Boolean.FALSE;
              }
            }
            return Boolean.FALSE;
          }
        });
  }

  /**
   * Creates a new {@link SliderClient} initialized with appropriate
   * configuration and started. This slider client can be used to invoke
   * individual API.
   * <p/>
   * When work with this client is done,
   * {@link #destroySliderClient(SliderClient)} must be called.
   *
   * @return created {@link SliderClient}
   * @see #destroySliderClient(SliderClient)
   */
  protected SliderClient createSliderClient() {
    SliderClient client = new SliderClient() {
      @Override
      public void init(Configuration conf) {
        super.init(conf);
        try {
          initHadoopBinding();
        } catch (SliderException e) {
          throw new RuntimeException(
              "Unable to automatically init Hadoop binding", e);
        } catch (Exception e) {
          throw new RuntimeException(
              "Unable to automatically init Hadoop binding", e);
        }
      }
    };
    try {
      Configuration sliderConf =
          client.bindArgs(new Configuration(), new String[]{"help"});
      client.init(sliderConf);
      client.start();
    } catch (Exception e) {
      LOGGER.error(
          Logger.NOT_SERVICE_RELATED, "Unable to create SliderClientProxy", e);
      throw new RuntimeException(e.getMessage(), e);
    } catch (Throwable e) {
      LOGGER.error(
          Logger.NOT_SERVICE_RELATED, "Unable to create SliderClientProxy", e);
      throw new RuntimeException(e.getMessage(), e);
    }
    return client;
  }

  protected void destroySliderClient(SliderClient sliderClient) {
    sliderClient.stop();
    sliderClient = null;
  }

  private SliderApp createSliderAppObject(ClusterDescription description) {
    if (description == null) {
      return null;
    }

    SliderApp app = new SliderApp();
    app.setVersion(description.version);
    app.setName(description.name);
    app.setType(description.type);
    app.setState(description.state);
    app.setCreateTime(description.createTime);
    app.setUpdateTime(description.updateTime);

    // set components
    for (Map.Entry<String, Map<String, String>> e : description.roles
        .entrySet()) {
      SliderAppComponent component = new SliderAppComponent();
      component.setComponentName(e.getKey());
      app.getComponents().put(component.getComponentName(), component);
      Map<String, String> componentsObj = e.getValue();
      for (Map.Entry<String, String> comEntry : componentsObj.entrySet()) {
        if (YARN_MEMORY.equals(comEntry.getKey())) {
          component.setMemory(Integer.parseInt(comEntry.getValue()));
        } else if (YARN_VCORES.equals(comEntry.getKey())) {
          component.setVcores(Integer.parseInt(comEntry.getValue()));
        } else if (REQUESTED_INSTANCES.equals(comEntry.getKey())) {
          component
              .setRequestedInstances(Integer.parseInt(comEntry.getValue()));
        } else if (ACTUAL_INSTANCES.equals(comEntry.getKey())) {
          component.setActualInstances(Integer.parseInt(comEntry.getValue()));
        }
      }
    }

    // set containers
    Object status = description.status.get("live");
    for (Map.Entry<String, Object> e : ((Map<String, Object>) status)
        .entrySet()) {
      String cmptName = e.getKey();
      Map<String, Map<String, Object>> cmptContainers =
          (Map<String, Map<String, Object>>) e.getValue();
      for (Map.Entry<String, Map<String, Object>> e1 : cmptContainers
          .entrySet()) {
        String containerId = e1.getKey();
        Map<ResourceType, Integer> resource = new HashMap<>();
        resource.put(
            ResourceType.CPU, (Integer) (e1.getValue().get("vCores")));
        resource.put(
            ResourceType.MEMORY, (Integer) (e1.getValue().get("memory")));
        app.getComponents().get(cmptName).getContainers()
            .put(containerId, resource);
      }
    }
    return app;
  }

  private String getUserToRunAs() {
    return executeUser;
  }

  private void setUsertoRunAs(String user) {
    executeUser = user;
  }

  private <T> T invokeSliderClientRunnable(
      final SliderClientContextRunnable<T> runnable)
      throws IOException, InterruptedException, YarnException {
    ClassLoader currentClassLoader =
        Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      //UserGroupInformation.setConfiguration(getSliderClientConfiguration());
      UserGroupInformation sliderUser;
      String loggedInUser = getUserToRunAs();
      sliderUser = UserGroupInformation.getBestUGI(null, loggedInUser);

      try {
        T value = sliderUser.doAs(
            new PrivilegedExceptionAction<T>() {
              @Override
              public T run() throws Exception {
                // Dynamic add slider and hadoop conf path
                ClassLoader currentClassLoader =
                    Thread.currentThread().getContextClassLoader();
                URLClassLoader urlClassLoader =
                    (URLClassLoader) currentClassLoader;
                File hadoop = new File(hadoopConfDir);
                URL hadoopUrl = hadoop.toURI().toURL();
                File slider = new File(sliderConfDir);
                URL sliderUrl = slider.toURI().toURL();
                try {
                  Class urlClass = URLClassLoader.class;
                  Method method = urlClass
                      .getDeclaredMethod("addURL", new Class[]{URL.class});
                  method.setAccessible(true);
                  method.invoke(urlClassLoader, new Object[]{hadoopUrl});
                  method.invoke(urlClassLoader, new Object[]{sliderUrl});
                } catch (Exception e) {
                  LOGGER.error(
                      Logger.NOT_SERVICE_RELATED,
                      "Failed to add slider class path");
                }

                Thread.currentThread()
                    .setContextClassLoader(currentClassLoader);
                final SliderClient sliderClient = createSliderClient();
                try {
                  return runnable.run(sliderClient);
                } finally {
                  destroySliderClient(sliderClient);
                }
              }
            });
        return value;
      } catch (UndeclaredThrowableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof YarnException) {
          YarnException ye = (YarnException) cause;
          throw ye;
        }
        throw e;
      }
    } finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }
  }

  private interface SliderClientContextRunnable<T> {
    T run(SliderClient sliderClient)
        throws YarnException, IOException, InterruptedException;
  }
}
