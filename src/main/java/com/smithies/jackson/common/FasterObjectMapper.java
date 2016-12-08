package com.smithies.jackson.common;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public interface FasterObjectMapper {

	ObjectMapper threadSafeInstance();

	<T> String marshall(T object);

	<T> String marshallList(T object, Class<?> type);

	<T> void marshall(T object, OutputStream stream);

	<T> void write(T object, File file);

	<T> T unmarshall(String json, Class<T> type);

	<T> T unmarshall(InputStream inputStream, Class<T> type);

	<T> T unmarshall(byte[] bytes, Class<T> type);

	<T> T readValue(String json, TypeReference<T> typeReference);

}
