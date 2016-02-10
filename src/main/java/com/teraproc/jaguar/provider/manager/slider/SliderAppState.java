package com.teraproc.jaguar.provider.manager.slider;

import com.teraproc.jaguar.provider.manager.AppState;

public class SliderAppState implements AppState {
  public static final int STATE_INCOMPLETE = 0;
  public static final int STATE_SUBMITTED = 1;
  public static final int STATE_CREATED = 2;
  public static final int STATE_LIVE = 3;
  public static final int STATE_STOPPED = 4;
  public static final int STATE_DESTROYED = 5;
  public static final int STATE_ORPHAN = 6;
  private int state;

  public SliderAppState(int state) {
    this.state = state;
  }

  public SliderAppState(String state) {
    if ("INCOMPLETE".equals(state)) {
      this.state = STATE_INCOMPLETE;
    } else if ("SUBMITTED".equals(state)) {
      this.state = STATE_SUBMITTED;
    } else if ("CREATED".equals(state)) {
      this.state = STATE_CREATED;
    } else if ("LIVE".equals(state)) {
      this.state = STATE_LIVE;
    } else if ("STOPPED".equals(state)) {
      this.state = STATE_STOPPED;
    } else if ("DESTROYED".equals(state)) {
      this.state = STATE_DESTROYED;
    } else if ("ORPHAN".equals(state)) {
      this.state = STATE_ORPHAN;
    } else {
      this.state = -1;
    }
  }

  @Override
  public int getState() {
    return state;
  }

  @Override
  public void setState(int state) {
    this.state = state;
  }

  @Override
  public String toString() {
    if (state == STATE_INCOMPLETE) {
      return "INCOMPLETE";
    } else if (state == STATE_SUBMITTED) {
      return "SUBMITTED";
    } else if (state == STATE_CREATED) {
      return "CREATED";
    } else if (state == STATE_LIVE) {
      return "LIVE";
    } else if (state == STATE_STOPPED) {
      return "STOPPED";
    } else if (state == STATE_DESTROYED) {
      return "DESTROYED";
    } else if (state == STATE_ORPHAN) {
      return "ORPHAN";
    } else {
      return "UNKNOWN";
    }
  }

  @Override
  public int[] getLiveState() {
    return new int[] {3};
  }

  @Override
  public boolean isLive() {
    return state == STATE_LIVE ? true : false;
  }
}