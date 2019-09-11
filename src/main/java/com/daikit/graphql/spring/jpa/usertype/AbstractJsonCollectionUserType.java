package com.daikit.graphql.spring.jpa.usertype;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daikit.graphql.spring.jpa.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @param <JAVA_VALUE_DATA_TYPE>
 *            the collection, generic type
 * @author Thibaut Caselli
 */
public abstract class AbstractJsonCollectionUserType<JAVA_VALUE_DATA_TYPE> implements UserType {

	private static final int[] SQL_TYPES = {Types.LONGVARCHAR};

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// ABSTRACT METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	protected abstract TypeReference<?> getTypeReference();

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public boolean equals(final Object x, final Object y) throws HibernateException {
		return (x == null || ((Collection<?>) x).isEmpty()) && (y == null || ((Collection<?>) y).isEmpty())
				|| ObjectUtils.nullSafeEquals(x, y);
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session,
			final Object owner) throws HibernateException, SQLException {
		final String json = rs.getString(names[0]);
		return StringUtils.hasText(json) ? JsonUtils.fromJson(json, getTypeReference()) : createEmptyInstance();
	}

	@Override
	public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
			final SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, SQL_TYPES[0]);
		} else {
			st.setString(index, value instanceof Collection ? JsonUtils.toJson(value) : value.toString());
		}
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		return JsonUtils.copy(value);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(final Object value) throws HibernateException {
		// serialize value for cache
		return JsonUtils.toJson(value);
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
		// deserialize value from cache
		return deepCopy(cached);
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
		// get a new value from the given original object
		return deepCopy(original);
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PRIVATE UTILS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@SuppressWarnings("unchecked")
	private Object createEmptyInstance() {
		try {
			return returnedClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Unable to instantiate collection for type : " + returnedClass().getName());
		}
	}

}
