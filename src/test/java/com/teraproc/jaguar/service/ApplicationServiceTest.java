package com.teraproc.jaguar.service;

import com.teraproc.jaguar.TestUtils;
import com.teraproc.jaguar.domain.Application;
import com.teraproc.jaguar.domain.JaguarUser;
import com.teraproc.jaguar.domain.Provider;
import com.teraproc.jaguar.provider.manager.ApplicationManager;
import com.teraproc.jaguar.provider.manager.slider.SliderAppState;
import com.teraproc.jaguar.provider.manager.slider.SliderApplicationManager;
import com.teraproc.jaguar.repository.ApplicationRepository;
import com.teraproc.jaguar.repository.UserRepository;
import com.teraproc.jaguar.rest.converter.ApplicationConverter;
import com.teraproc.jaguar.rest.json.ApplicationJson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

public class ApplicationServiceTest {
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private Map<Provider, ApplicationManager> applicationManagers =
      new HashMap<>();
  @Mock
  private ApplicationConverter applicationConverter;
  @InjectMocks
  private ApplicationService underTest = new ApplicationService();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateApplication() throws Exception {
    when(applicationManagers.get(any(Provider.class)))
        .thenReturn(Mockito.mock(SliderApplicationManager.class));
    when(applicationManagers.get(any(Provider.class)).getApplicationState(
            any(JaguarUser.class), anyString())).thenReturn(Mockito.mock(
            SliderAppState.class));
    when(applicationConverter.convert(any(ApplicationJson.class)))
        .thenReturn(TestUtils.getApplication());
    when(userRepository.findOne(TestUtils.DUMMY_USER)).thenReturn(
        Mockito.mock(JaguarUser.class));
    when(userRepository.findOne("unknown")).thenReturn(null);

    ApplicationJson json = TestUtils.getApplicationJson();
    underTest.create(new JaguarUser(TestUtils.DUMMY_USER), json);
    verify(applicationRepository, times(1)).save(any(Application.class));
    verify(userRepository, times(0)).save(any(JaguarUser.class));

    underTest.create(new JaguarUser("unknown"), json);
    verify(applicationRepository, times(2)).save(any(Application.class));
    verify(userRepository, times(1)).save(any(JaguarUser.class));
  }

  @Test
  public void testUpdateApplication() throws Exception {
    when(applicationRepository.findOne(anyLong())).thenReturn(
        TestUtils.getApplication());
    when(
        applicationConverter.update(
            any(Application.class), any(ApplicationJson.class)))
        .thenReturn(Mockito.mock(Application.class));
    ApplicationJson json = TestUtils.getApplicationJson();
    underTest.update(new JaguarUser(TestUtils.DUMMY_USER), 1L, json);
    verify(applicationRepository, times(1)).save(any(Application.class));
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateNotFoundApplication() {
    when(applicationRepository.findOne(anyLong())).thenReturn(null);
    ApplicationJson json = TestUtils.getApplicationJson();
    underTest.update(new JaguarUser(TestUtils.DUMMY_USER), 1L, json);
    verify(applicationRepository, times(0)).save(any(Application.class));
  }

  @Test
  public void testDeleteApplication() throws Exception {
    when(applicationRepository.findOne(anyLong())).thenReturn(
        TestUtils.getApplication());
    underTest.remove(new JaguarUser(TestUtils.DUMMY_USER), 1L);
    verify(applicationRepository, times(1)).delete(any(Application.class));
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteNotFoundApplication() {
    when(applicationRepository.findOne(anyLong())).thenReturn(null);
    underTest.remove(new JaguarUser(TestUtils.DUMMY_USER), 1L);
    verify(applicationRepository, times(0)).delete(any(Application.class));
  }
}
