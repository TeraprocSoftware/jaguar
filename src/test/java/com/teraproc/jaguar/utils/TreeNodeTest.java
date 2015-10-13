package com.teraproc.jaguar.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeNodeTest {
  private TreeNode underTest;

  @Before
  public void setup() {
    String root = "node-1";
    underTest = new TreeNode(root);
  }

  @Test
  public void testOneLevelTree() {
    assertEquals(false, underTest.hasChild());
    assertEquals(0, underTest.getChildren().size());
    assertEquals(null, underTest.getParent());
    assertEquals("node-1", underTest.getData());
  }

  @Test
  public void testTwoLevelsTree() {
    TreeNode child21 = underTest.addChild("node-2-1");
    TreeNode child22 = underTest.addChild("node-2-2");
    assertEquals(true, underTest.hasChild());
    assertEquals(2, underTest.getChildren().size());
    assertEquals(false, child21.hasChild());
    assertEquals(false, child22.hasChild());
    assertEquals(underTest, child21.getParent());
    assertEquals(underTest, child22.getParent());
    assertEquals("node-2-1", child21.getData());
    assertEquals("node-2-2", child22.getData());
  }

  @Test
  public void testThreeLevelsTree() {
    TreeNode child21 = underTest.addChild("node-2-1");
    TreeNode child22 = underTest.addChild("node-2-2");
    TreeNode child31 = new TreeNode("node-3-1");
    TreeNode child32 = new TreeNode("node-3-2");
    TreeNode child33 = new TreeNode("node-3-3");
    TreeNode child34 = new TreeNode("node-3-4");
    child21.addChildNode(child31);
    child21.addChildNode(child32);
    child22.addChildNode(child33);
    child22.addChildNode(child34);
    assertEquals(true, child21.hasChild());
    assertEquals(2, child21.getChildren().size());
    assertEquals(true, child22.hasChild());
    assertEquals(2, child22.getChildren().size());
    assertEquals(false, child31.hasChild());
    assertEquals(false, child32.hasChild());
    assertEquals(false, child33.hasChild());
    assertEquals(false, child34.hasChild());
    assertEquals(underTest, child21.getParent());
    assertEquals(underTest, child22.getParent());
    assertEquals(child21, child31.getParent());
    assertEquals(child21, child32.getParent());
    assertEquals(child22, child33.getParent());
    assertEquals(child22, child34.getParent());
    assertEquals("node-2-1", child21.getData());
    assertEquals("node-2-2", child22.getData());
    assertEquals("node-3-1", child31.getData());
    assertEquals("node-3-2", child32.getData());
    assertEquals("node-3-3", child33.getData());
    assertEquals("node-3-4", child34.getData());
  }
}
