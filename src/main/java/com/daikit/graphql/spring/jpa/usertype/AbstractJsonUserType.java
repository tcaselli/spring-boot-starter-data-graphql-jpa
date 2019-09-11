package com.daikit.graphql.spring.jpa.usertype;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import com.daikit.generics.utils.GenericsUtils;
import com.daikit.graphql.spring.jpa.utils.JsonUtils;

/**
 * Abstract super class for all
 *
 * @author Thibaut Caselli
 * @param <JAVA_DATA_TYPE>
 *            the type of the wrapped data
 */
public abstract class AbstractJsonUserType<JAVA_DATA_TYPE> implements UserType {

	private static Logger logger = LoggerFactory.getLogger(AbstractJsonUserType.class);

	private static final int[] SQL_TYPES = {Types.LONGVARCHAR};

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PROTECTED METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@SuppressWarnings("unchecked")
	protected Class<JAVA_DATA_TYPE> getReturnedType() {
		return (Class<JAVA_DATA_TYPE>) GenericsUtils.getTypeArguments(getClass(), AbstractJsonUserType.class).get(0);
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// METHODS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass() {
		return getReturnedType();
	}

	@Override
	public boolean equals(final Object x, final Object y) throws HibernateException {
		return ObjectUtils.nullSafeEquals(x, y);
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session,
			final Object owner) throws HibernateException, SQLException {
		Object ret = null;
		final String json = rs.getString(names[0]);
		try {
			ret = JsonUtils.fromJson(json, getReturnedType());
		} catch (final Exception e) {
			logger.error("Error during conversion [{}]. IGNORED", e.getMessage());
		}
		// Create empty instance if stored value is null
		if (ret == null) {
			ret = createEmptyInstance();
		}
		return ret;
	}

	@Override
	public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
			final SharedSessionContractImplementor session) throws HibernateException, SQLException {
		boolean setNull = true;
		if (value != null) {
			final String valueJson = JsonUtils.toJson(value);
			// Do not store empty object
			if (!"{}".equals(valueJson)) {
				st.setString(index, valueJson);
				setNull = false;
			}
		}
		if (setNull) {
			st.setNull(index, SQL_TYPES[0]);
		}
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		// Copy the value. Used for storing state from database to further know
		// if property value was modified.
		// This would not be necessary if this type would implement
		// DirtyCheckableUserType , to see if we can improve performance here
		// LogUtil.debug(AbstractJsonUserType.class, "COPY JSON {} [{}]",
		// getReturnedType().getSimpleName(), copy);
		return JsonUtils.copy(value);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
		// deserialize value from cache
		return deepCopy(cached);
	}

	@Override
	public Serializable disassemble(final Object value) throws HibernateException {
		// serialize value for cache
		return JsonUtils.toJson(value);
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
		// get a new value from the given original object
		return deepCopy(original);
	}

	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	// PRIVATE UTILS
	// *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

	private Object createEmptyInstance() {
		try {
			return Modifier.isAbstract(returnedClass().getModifiers())
					? null
					: returnedClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Unable to instantiate type : " + returnedClass().getName());
		}
	}
}
