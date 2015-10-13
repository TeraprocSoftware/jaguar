package com.teraproc.jaguar.domain;

import com.teraproc.jaguar.utils.TreeNode;

public class GroupAlert extends BaseAlert {
  private TreeNode<Condition> root;
  private int latestSuccessiveIntervals;

  public GroupAlert() {
  }

  public TreeNode<Condition> getRoot() {
    return root;
  }

  public void setRoot(TreeNode<Condition> root) {
    this.root = root;
  }

  public int getLatestSuccessiveIntervals() {
    return latestSuccessiveIntervals;
  }

  public void setLatestSuccessiveIntervals(int count) {
    latestSuccessiveIntervals = count;
  }

  public void addLastestSuccessiveIntervals(int count) {
    latestSuccessiveIntervals = latestSuccessiveIntervals + count;
  }
}
