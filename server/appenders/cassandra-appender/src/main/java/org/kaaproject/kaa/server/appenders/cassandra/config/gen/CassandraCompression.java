/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.kaaproject.kaa.server.appenders.cassandra.config.gen;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public enum CassandraCompression {
  NONE, SNAPPY, LZ4;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"CassandraCompression\",\"namespace\":\"org.kaaproject.kaa.server.appenders.cassandra.config.gen\",\"symbols\":[\"NONE\",\"SNAPPY\",\"LZ4\"]}");

  public static org.apache.avro.Schema getClassSchema() {
    return SCHEMA$;
  }
}