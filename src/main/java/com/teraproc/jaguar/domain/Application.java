package com.teraproc.jaguar.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

@Entity
@NamedQueries({
    @NamedQuery(name = "Application.findAllByUser",
        query = "SELECT c FROM Application c WHERE c.user.id= :id"),
    @NamedQuery(name = "Application.find",
        query = "SELECT c FROM Application c WHERE c.id= :id"),
    @NamedQuery(name = "Application.findAll",
        query = "SELECT c FROM Application c"),
    @NamedQuery(name = "Application.findAllByState",
        query = "SELECT c FROM Application c WHERE c.enabled= :enabled") })

public class Application {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "service_generator")
  @SequenceGenerator(name = "service_generator",
      sequenceName = "sequence_table")
  private long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private JaguarUser user;

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider")
  private Provider provider;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "state")
  private String state;

  public Application() {
  }

  public Application(JaguarUser user) {
    this.user = user;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public JaguarUser getUser() {
    return user;
  }

  public void setUser(JaguarUser user) {
    this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}