package com.daikit.graphql.spring.jpa.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Utility class for handling custom json
 *
 * @author Thibaut Caselli
 */
public class JsonUtils {

	private static ObjectMapper MAPPER;
	private static ObjectWriter WRITER_PRETTY;
	private static ObjectWriter WRITER_INLINE;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		WRITER_PRETTY = MAPPER.writer(new DefaultPrettyPrinter());
		WRITER_INLINE = MAPPER.writer();
	}

	/**
	 * Wrapper to {@link #toJson(Object, boolean)} with prettyPrint to false (so
	 * output JSON will be inline)
	 *
	 * @param value
	 *            the object value to be converted
	 * @return the converted value as JSON String
	 */
	public static final String toJson(final Object value) {
		return toJson(value, false);
	}

	/**
	 * Convert an object to JSON
	 *
	 * @param value
	 *            the object value to be converted
	 * @param prettyPrint
	 *            whether the resulting JSON must be pretty printed
	 * @return the converted value as JSON String
	 */
	public static final String toJson(final Object value, final boolean prettyPrint) {
		try {
			return value == null
					? null
					: prettyPrint ? WRITER_PRETTY.writeValueAsString(value) : WRITER_INLINE.writeValueAsString(value);
		} catch (final JsonProcessingException e) {
			throw new IllegalArgumentException("An error happened while converting object to json : " + e.getMessage(),
					e);
		}
	}

	/**
	 * Convert a JSON String to a Java Object typed with given clazz
	 *
	 * @param jsonString
	 *            the JSON String
	 * @param clazz
	 *            he object class
	 * @return the converted object
	 */
	public static final <T> T fromJson(final String jsonString, final Class<T> clazz) {
		try {
			return jsonString == null ? null : MAPPER.readValue(jsonString, clazz);
		} catch (final IOException e) {
			throw new IllegalArgumentException(
					"An error happened while converting object from json : " + e.getMessage(), e);
		}
	}

	/**
	 * Convert a JSON String to a Java Object typed with type related to given
	 * typeReference
	 *
	 * @param jsonString
	 *            the JSON String
	 * @param typeReference
	 *            the type reference
	 * @return the converted object
	 */
	public static final <T> T fromJson(final String jsonString, final TypeReference<T> typeReference) {
		try {
			return jsonString == null ? null : MAPPER.<T>readValue(jsonString, typeReference);
		} catch (final IOException e) {
			throw new IllegalArgumentException(
					"An error happened while converting object from json : " + e.getMessage(), e);
		}
	}

	/**
	 * Copy given object using its JSON representation
	 *
	 * @param object
	 *            the object to be copied
	 * @return the copied object
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T copy(final T object) {
		return object == null ? null : (T) fromJson(JsonUtils.toJson(object), object.getClass());
	}

}
