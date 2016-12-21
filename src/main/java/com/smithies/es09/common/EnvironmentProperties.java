package com.smithies.es09.common;

import org.springframework.core.env.Environment;

public abstract class EnvironmentProperties {

  public static String getName(Environment e) {
    return replaceUsernameToken(e, environmentName(e));
  }

  public static String environmentName(Environment e) {
    return e.getProperty("environment", getUsername(e));
  }

  public static String replaceUsernameToken(Environment e, final String environment) {
    return environment.replace("#{username}", getUsername(e));
  }

  public static String getUsername(Environment e) {
    return e.getProperty("user.name", "unknown_user");
  }

}
