package com.teraproc.jaguar.provider.manager;

public interface AppState {

  int getState();

  void setState(int state);

  String toString();

  int[] getLiveState();

  boolean isLive();
}
