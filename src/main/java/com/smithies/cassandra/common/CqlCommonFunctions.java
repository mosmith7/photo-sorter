package com.smithies.cassandra.common;
import java.util.List;
import java.util.Optional;

import org.springframework.cassandra.core.RowMapper;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.querybuilder.Select;
import com.smithies.jackson.common.FasterObjectMapper;

public class CqlCommonFunctions {

 public static final <T> JsonColumnRowMapper<T> mapperFor(FasterObjectMapper mapper,
     String colName, Class<T> type) {
   return new JsonColumnRowMapper<>(mapper, colName, type);
 }

 public static class JsonColumnRowMapper<T> implements RowMapper<T> {

   private FasterObjectMapper mapper;
   private String colName;
   private Class<T> type;

   public JsonColumnRowMapper(FasterObjectMapper mapper, String colName, Class<T> type) {
     this.mapper = mapper;
     this.colName = colName;
     this.type = type;
   }

   @Override
   public T mapRow(Row row, int rowNum) throws DriverException {
     return mapper.unmarshall(row.getString(colName), type);
   }

 }

 public static class ValueNotFoundException extends RuntimeException {

 }

 public static class TooManyValuesFoundException extends RuntimeException {

 }

 public static <T> T get(CassandraOperations cassandra, final String cql, RowMapper<T> mapper) {
   List<T> query = cassandra.query(cql, mapper);
   if (query.isEmpty()) {
     throw new ValueNotFoundException();
   }
   if (query.size() > 1) {
     throw new TooManyValuesFoundException();
   }
   return query.get(0);
 }

 public static <T> Optional<T> getOptional(CassandraOperations cassandra, final String cql,
     RowMapper<T> mapper) {
   List<T> query = cassandra.query(cql, mapper);
   if (query.isEmpty()) {
     return Optional.empty();
   }
   if (query.size() > 1) {
     throw new TooManyValuesFoundException();
   }
   return Optional.of(query.get(0));
 }

  public static <T> Optional<T> getOptional(CassandraOperations cassandra, RowMapper<T> mapper,
	      final Select statement) {
	    List<T> query = cassandra.query(statement, mapper);
	    if (query.isEmpty()) {
	      return Optional.empty();
	    }
	    if (query.size() > 1) {
	      throw new TooManyValuesFoundException();
	    }
	    return Optional.of(query.get(0));
	  }
 
 public static Optional<Row> getOptionalRow(CassandraOperations cassandra, Select select) {
	    List<Row> rows = cassandra.query(select).all();
	    if (rows.isEmpty()) {
	      return Optional.empty();
	    }
	    if (rows.size() > 1) {
	      throw new CqlCommonFunctions.TooManyValuesFoundException();
	    }
	    return Optional.of(rows.get(0));
	  }

}