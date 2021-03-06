/*
 * Copyright 2007-2107 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.persistence.jdbc.support;

import java.util.List;

import net.ymate.platform.persistence.base.ConnectionException;
import net.ymate.platform.persistence.base.OperatorException;
import net.ymate.platform.persistence.jdbc.ISession;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.operator.IResultSetHandler;
import net.ymate.platform.persistence.support.PageResultSet;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * AbstractEntityRepository
 * </p>
 * <p>
 * 实体存储器接口抽象实现;
 * </p>
 * 
 * @author 刘镇(suninformation@163.com)
 * @version 0.0.0
 *          <table style="border:1px solid gray;">
 *          <tr>
 *          <th width="100px">版本号</th><th width="100px">动作</th><th
 *          width="100px">修改人</th><th width="100px">修改时间</th>
 *          </tr>
 *          <!-- 以 Table 方式书写修改历史 -->
 *          <tr>
 *          <td>0.0.0</td>
 *          <td>创建类</td>
 *          <td>刘镇</td>
 *          <td>2013-7-16下午1:06:46</td>
 *          </tr>
 *          </table>
 */
public abstract class AbstractEntityRepository implements IEntityRepository {

	private String __dsName;

	/**
	 * 构造器
	 */
	public AbstractEntityRepository() {
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#setDataSourceName(java.lang.String)
	 */
	public void setDataSourceName(String dsName) {
		this.__dsName = dsName;
	}

	/**
	 * @return 获取当前数据源名称，若为空则返回默认数据源名称
	 */
	protected String getDataSourceName() {
		return StringUtils.defaultIfEmpty(this.__dsName, JDBC.DATASOURCE_DEFAULT_NAME);
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#load(java.lang.Class, java.lang.Object, java.lang.String[])
	 */
	public <T, PK> T load(Class<T> t, PK id, String... fieldFilter) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.find(t, id, fieldFilter);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#save(java.lang.Object)
	 */
	public <T> T save(T entity) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.insert(entity);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#saveAll(java.util.List)
	 */
	public <T> List<T> saveAll(List<T> entities) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.insertAll(entities);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#update(java.lang.Object, java.lang.String[])
	 */
	public <T> T update(T entity, String... fieldFilter) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.update(entity, fieldFilter);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#updateAll(java.util.List, java.lang.String[])
	 */
	public <T> List<T> updateAll(List<T> entities, String... fieldFilter) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.updateAll(entities, fieldFilter);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#delete(java.lang.Object)
	 */
	public <T> T delete(T entity) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.delete(entity);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#delete(java.lang.Class, java.lang.Object)
	 */
	public <T, PK> boolean delete(Class<T> t, PK pk) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return (_session.delete(t, pk) > 0);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#deleteAll(java.util.List)
	 */
	public <T> List<T> deleteAll(List<T> entities) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.deleteAll(entities);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#deleteAll(java.lang.Class, java.lang.Object[])
	 */
	public <T> int[] deleteAll(Class<T> t, Object[] ids) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(getDataSourceName());
		try {
			return _session.deleteAll(t, ids);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#findAll(java.lang.Class, java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	public <T> List<T> findAll(Class<T> t, String cond, Object[] params, String... fieldFilter) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.findAll(t, cond, fieldFilter, params);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#findAll(java.lang.Class, java.lang.String, java.lang.Object[], int, int, boolean, java.lang.String[])
	 */
	public <T> PageResultSet<T> findAll(Class<T> t, String cond, Object[] params, int pageSize, int currentPage, boolean allowRecordCount, String... fieldFilter)
			throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.findAll(t, cond, fieldFilter, pageSize, currentPage, allowRecordCount, params);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#findAll(java.lang.String, net.ymate.platform.persistence.jdbc.operator.IResultSetHandler, java.lang.Object[])
	 */
	public <T> List<T> findAll(String sql, IResultSetHandler<T> handler, Object[] params) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.findAll(sql, handler, params);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#findAll(java.lang.String, net.ymate.platform.persistence.jdbc.operator.IResultSetHandler, int, int, boolean, java.lang.Object[])
	 */
	public <T> PageResultSet<T> findAll(String sql, IResultSetHandler<T> handler, int pageSize, int page, boolean count, Object[] params) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.findAll(sql, handler, pageSize, page, count, params);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#executeForUpdate(java.lang.String, java.lang.Object[])
	 */
	public int executeForUpdate(String sql, Object[] params) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.executeForUpdate(sql, params);
		} finally {
			_session.close();
		}
	}

	/* (non-Javadoc)
	 * @see net.ymate.platform.persistence.jdbc.support.IEntityRepository#executeForUpdateAll(java.lang.String, java.util.List)
	 */
	public int[] executeForUpdateAll(String sql, List<Object[]> params) throws OperatorException, ConnectionException {
		ISession _session = JDBC.openSession(this.getDataSourceName());
		try {
			return _session.executeForUpdateAll(sql, params);
		} finally {
			_session.close();
		}
	}

}
