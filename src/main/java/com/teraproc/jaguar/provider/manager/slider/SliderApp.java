package com.teraproc.jaguar.provider.manager.slider;

import java.util.HashMap;
import java.util.Map;

public class SliderApp {
  private String version;
  private String name;
  private String type;
  private long createTime;
  private long updateTime;
  private int state;
  private Map<String, SliderAppComponent> components;

  public SliderApp() {
    components = new HashMap<>();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public Map<String, SliderAppComponent> getComponents() {
    return components;
  }

  public void setComponents(Map<String, SliderAppComponent> components) {
    this.components = components;
  }
}
