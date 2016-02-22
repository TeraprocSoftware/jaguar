package com.teraproc.jaguar.utils;

import java.util.Collection;
import java.util.LinkedList;

public class TreeNode<T> {
  private T data;
  private TreeNode<T> parent;
  private Collection<TreeNode<T>> children;

  public TreeNode(T data) {
    this.data = data;
    this.children = new LinkedList<>();
  }

  public TreeNode<T> addChild(T child) {
    TreeNode<T> childNode = new TreeNode<T>(child);
    childNode.parent = this;
    this.children.add(childNode);
    return childNode;
  }

  public void addChildNode(TreeNode<T> childNode) {
    childNode.parent = this;
    this.children.add(childNode);
  }


  public boolean hasChild() {
    return children.size() != 0;
  }

  public Collection<TreeNode<T>> getChildren() {
    return children;
  }

  public T getData() {
    return data;
  }

  public TreeNode<T> getParent() {
    return parent;
  }
}
