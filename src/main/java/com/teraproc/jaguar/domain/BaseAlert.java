package com.teraproc.jaguar.domain;

public abstract class BaseAlert {
  private long policyId;
  private int successiveIntervals;

  public BaseAlert() {
  }

  public long getPolicyId() {
    return policyId;
  }

  public void setPolicyId(long policyId) {
    this.policyId = policyId;
  }

  public int getSuccessiveIntervals() {
    return successiveIntervals;
  }

  public void setSuccessiveIntervals(int successiveIntervals) {
    this.successiveIntervals = successiveIntervals;
  }
}
