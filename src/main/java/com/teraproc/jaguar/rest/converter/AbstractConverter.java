package com.teraproc.jaguar.rest.converter;

import com.teraproc.jaguar.rest.json.Json;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConverter<J extends Json, E>
    implements Converter<J, E> {

  @Override
  public E convert(J source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public J convert(E source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<E> convertAllFromJson(List<J> jsonList) {
    List<E> entityList = new ArrayList<>(jsonList.size());
    for (J json : jsonList) {
      entityList.add(convert(json));
    }
    return entityList;
  }

  @Override
  public List<J> convertAllToJson(List<E> entityList) {
    List<J> jsonList = new ArrayList<>(entityList.size());
    for (E entity : entityList) {
      jsonList.add(convert(entity));
    }
    return jsonList;
  }
}
