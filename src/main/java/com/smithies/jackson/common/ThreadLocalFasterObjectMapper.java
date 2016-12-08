package com.smithies.jackson.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

@Service
public class ThreadLocalFasterObjectMapper implements FasterObjectMapper {

	  private final ThreadLocal<ObjectMapper> objectMapper = new ThreadLocal<>();
	  private boolean noNulls;
	  public ThreadLocalFasterObjectMapper() {
	    this(false);
	  }
	  public ThreadLocalFasterObjectMapper(boolean noNulls) {
	    this.noNulls = noNulls;
	  }
	  @Override
	  public ObjectMapper threadSafeInstance() {
	    if (objectMapper.get() == null) {
	      ObjectMapper instance = new ObjectMapper();
	      instance.registerModule(new GuavaModule());
	      instance.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	      if (noNulls) {
	        instance.setSerializationInclusion(Include.NON_NULL);
	      }
	      objectMapper.set(instance);
	    }
	    return objectMapper.get();
	  }
	  @Override
	  public <T> String marshall(final T object) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper.writeValueAsString(object);
	    } catch (JsonProcessingException e) {
	      final String message =
	          "Failed to marshal " + object.getClass() + " with error " + e.getMessage();
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
	  @Override
	  public <T> String marshallList(final T object, Class<?> type) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper
	          .writerWithType(objectMapper.getTypeFactory().constructCollectionType(List.class, type))
	          .writeValueAsString(object);
	    } catch (JsonProcessingException e) {
	      final String message =
	          "Failed to marshal " + object.getClass() + " with error " + e.getMessage();
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
	  @Override
	  public <T> void marshall(final T object, final OutputStream stream) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      objectMapper.writeValue(stream, object);
	    } catch (Exception e) {
	      final String message =
	          "Failed to marshal " + object.getClass() + " with error " + e.getMessage() + " to stream";
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
	  @Override
	  public <T> void write(T object, File file) {
//	    LOG.debug("Writing '{}'", file);
	    try (FileOutputStream stream = new FileOutputStream(file)) {
	      marshall(object, stream);
	    } catch (Exception e) {
//	      LOG.error("Problem marshalling to file '{}'", file, e);
	      throw new RuntimeException("Problem marshalling to file", e);
	    }
	  }
	  @Override
	  public <T> T unmarshall(final String json, final Class<T> type) {
	    if (json == null) {
	      return null;
	    }
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper.readValue(json, type);
	    } catch (final Exception e) {
//	      LOG.error("Failed to unmarshal [{}] with error [{}] for json [{}] ", type, e.getMessage(),
//	          json);
	      throw new RuntimeException("Failed to unmarshall " + type + " with error " + e.getMessage(),
	          e);
	    }
	  }
	  @Override
	  public <T> T unmarshall(InputStream inputStream, Class<T> type) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper.readValue(inputStream, type);
	    } catch (final Exception e) {
	      final String message = "Failed to unmarshal " + type + " with error " + e.getMessage();
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
	  @Override
	  public <T> T unmarshall(byte[] bytes, Class<T> type) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper.readValue(bytes, type);
	    } catch (final Exception e) {
	      final String message = "Failed to unmarshal " + type + " with error " + e.getMessage();
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
	  @Override
	  public <T> T readValue(String json, TypeReference<T> typeReference) {
	    final ObjectMapper objectMapper = threadSafeInstance();
	    try {
	      return objectMapper.readValue(json, typeReference);
	    } catch (final Exception e) {
	      final String message =
	          "Failed to unmarshal " + typeReference + " with error " + e.getMessage();
//	      LOG.error(message);
	      throw new RuntimeException(message, e);
	    }
	  }
//	  @Override
//	  public <T> T read(File file, Class<T> type) {
//	    try {
//	      RwFileUtils.checkFileExists(file);
//	      RwFileUtils.checkFileIsReadable(file);
//	      return unmarshall(new FileInputStream(file), type);
//	    } catch (FileNotFoundException e) {
////	      LOG.error("File does not exist '{}'", file);
//	      throw new RuntimeException("File does not exist: " + file);
//	    }
//	  }
//	  @Override
//	  public void enableNice() {
//	    ObjectMapper threadSafeInstance = threadSafeInstance();
//	    threadSafeInstance.enable(SerializationFeature.INDENT_OUTPUT);
//	    threadSafeInstance.setSerializationInclusion(Include.NON_NULL);
//	  }
}
