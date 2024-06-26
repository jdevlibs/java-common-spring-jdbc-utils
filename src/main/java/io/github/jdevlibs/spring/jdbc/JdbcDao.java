/*  ---------------------------------------------------------------------------
 *  * Copyright 2020-2021 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring.jdbc;

import io.github.jdevlibs.spring.Transformers;
import io.github.jdevlibs.spring.jdbc.criteria.*;
import io.github.jdevlibs.utils.JdbcUtils;
import io.github.jdevlibs.utils.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public abstract class JdbcDao implements InitializingBean {
    private static final String PL_SQL_VOID = "'{' call {0} '}'";
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /* ++++++++++++++++++++++++++ Initial and Validate +++++++++++++++++++++++ */
    @Override
    public void afterPropertiesSet() throws IllegalArgumentException {
        validateJdbcTemplate();
    }

    protected abstract void autowiredJdbcTemplate(JdbcTemplate jdbcTemplate);

    protected abstract void setPagingOption(StringBuilder sql, Parameter params, Criteria paging);

    public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        if (jdbcTemplate != null) {
            jdbcTemplate.setResultsMapCaseInsensitive(true);
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        }
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    private void validateJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate is required");
        }
    }

    /*++++++++++++++++++ SQL -> List Java Bean ++++++++++++++++++ */
    /**
     * Query and auto-convert to the collection of the target class.
     * @param sql The sql statement
     * @param clazz The result target class
     * @return Collection of result target class
     * @param <T> Generic result class
     */
    public <T> List<T> queryToList(String sql, Class<T> clazz) {
        return queryToList(sql, new IndexParameter(0), clazz);
    }

    /**
     * Query and auto-convert to the collection of the target class.
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param clazz The result target class
     * @return Collection of result target class
     * @param <T> Generic result class
     */
    public <T> List<T> queryToList(String sql, Parameter params, Class<T> clazz) {
        logStatement(sql, params, clazz);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), Transformers.toBean(clazz));
        } else {
            return getJdbcTemplate().query(sql, Transformers.toBean(clazz), toArrays(params));
        }
    }

    /**
     * Query to the collection with RowMapper
     * @param sql The sql statement
     * @param mapper RowMapping implement
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> List<T> queryToList(String sql, RowMapper<T> mapper) {
        return queryToList(sql, new IndexParameter(0), mapper);
    }

    /**
     * Query to the collection with RowMapper
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @param mapper RowMapping implement
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> List<T> queryToList(String sql, Parameter params, RowMapper<T> mapper) {
        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), mapper);
        } else {
            return getJdbcTemplate().query(sql, mapper, toArrays(params));
        }
    }

    /*++++++++++++++++++ SQL -> Java Bean ++++++++++++++++++ */

    /**
     * Query and auto-convert to target class (Bean model)
     * @param sql The sql statement
     * @param clazz The result target class
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> T queryToBean(String sql, Class<T> clazz) {
        return queryToBean(sql, new IndexParameter(0), clazz);
    }

    /**
     * Query and auto-convert to target class (Bean model)
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param clazz The result target class
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> T queryToBean(String sql, Parameter params, Class<T> clazz) {
        try {
            logStatement(sql, params, clazz);

            if (params instanceof NameParameter) {
                return getNamedParameterJdbcTemplate().queryForObject(sql, params.toSqlParameter(),
                        Transformers.toBean(clazz));
            } else {
                return getJdbcTemplate().queryForObject(sql, Transformers.toBean(clazz), toArrays(params));
            }
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /*++++++++++++++++++ SQL -> Object ++++++++++++++++++ */
    /**
     * Query and auto-convert to object (Int, Number, String, etc.)
     * @param sql The sql statement
     * @param type The result class type
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> T queryToObject(String sql, Class<T> type) {
        return queryToObject(sql, new IndexParameter(0), type);
    }

    /**
     * Query and auto-convert to object (Int, Number, String, etc.)
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param type The result class type
     * @return result target class
     * @param <T> Generic result class
     */
    public <T> T queryToObject(String sql, Parameter params, Class<T> type) {
        try {
            logStatement(sql, params, type);

            if (params instanceof NameParameter) {
                return getNamedParameterJdbcTemplate().queryForObject(sql, params.toSqlParameter(), type);
            } else {
                return getJdbcTemplate().queryForObject(sql, type, toArrays(params));
            }
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /**
     * Query mapping with RowMapper
     * @param sql The sql statement
     * @param mapper RowMapping implement
     * @return result object
     * @param <T> Generic result class
     */
    public <T> T queryForObject(String sql, RowMapper<T> mapper) {
        return queryForObject(sql, new IndexParameter(0), mapper);
    }

    /**
     * Query mapping with RowMapper
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @param mapper RowMapping implement
     * @return result object
     * @param <T> Generic result class
     */
    public <T> T queryForObject(String sql, Parameter params, RowMapper<T> mapper) {

        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params.toSqlParameter(), mapper);
        } else {
            return getJdbcTemplate().queryForObject(sql, mapper, toArrays(params));
        }
    }

    /*++++++++++++++++++ SQL -> Custom Extractor ++++++++++++++++++ */
    /**
     * Query and auto-convert to ResultSetExtractor operation
     * @param sql The sql statement
     * @param rse The ResultSetExtractor operation
     * @return result target class by ResultSetExtractor
     * @param <T> Generic result class
     */
    public <T> T query(String sql, ResultSetExtractor<T> rse) {
        return query(sql, new IndexParameter(0), rse);
    }

    /**
     * Query and auto-convert to ResultSetExtractor operation
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param rse The ResultSetExtractor operation
     * @return result target class by ResultSetExtractor
     * @param <T> Generic result class
     */
    public <T> T query(String sql, Parameter params, ResultSetExtractor<T> rse) {

        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), rse);
        } else {
            return getJdbcTemplate().query(sql, rse, toArrays(params));
        }
    }

    /*++++++++++++++++++ Paging ++++++++++++++++++ */
    /**
     * Query and auto-convert to a Paging result
     * @param sql The sql statement
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging data
     * @param <T> Generic result class
     */
    public <T> Paging<T> queryWithPaging(String sql, Criteria criteria, Class<T> clazz) {
        return queryWithPaging(sql, new IndexParameter(), criteria, clazz);
    }

    /**
     * Query and auto-convert to a Paging result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging data
     * @param <T> Generic result class
     */
    public <T> Paging<T> queryWithPaging(String sql, Parameter params, Criteria criteria, Class<T> clazz) {
        Paging<T> paging = new Paging<>();
        if (!criteria.isSkipRowCount() || criteria.getTotalElement() == null) {
            Long count = countForPaging(sql, params);
            paging.setTotalElements(count);
            criteria.setTotalElement(count);
        } else {
            paging.setTotalElements(criteria.getTotalElement());
        }

        List<T> items = queryToPaging(sql, params, criteria, clazz);
        paging.setItems(items);
        paging.setCriteria(criteria);
        paging.calculateTotalPage();

        return paging;
    }

    /**
     * Query and auto-convert to paging collection result
     * @param sql The sql statement
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging collection data
     * @param <T> Generic result class
     */
    public <T> List<T> queryAsPagingResult(String sql, Criteria criteria, Class<T> clazz) {
        return queryToPaging(sql, new IndexParameter(), criteria, clazz);
    }

    /**
     * Query and auto-convert to paging collection result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging collection data
     * @param <T> Generic result class
     */
    public <T> List<T> queryToPaging(String sql, Parameter params, Criteria criteria, Class<T> clazz) {

        StringBuilder pageSql = new StringBuilder();
        pageSql.append("SELECT * FROM (").append(sql);
        pageSql.append(" ) TB");
        if (!criteria.isEmptySort()) {
            setOrderByOption(pageSql, criteria);
        }
        setPagingOption(pageSql, params, criteria);

        return queryToList(pageSql.toString(), params, clazz);
    }

    /**
     * Query counts a paging collection result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return result total record data
     */
    public Long countForPaging(String sql, Parameter params) {
        String countSql = "SELECT COUNT(*) AS TOTAL FROM (" + sql + ") TB";
        Number value = queryToNumber(countSql, params);
        if (Validators.isNull(value)) {
            return 0L;
        }

        return value.longValue();
    }

    /**
     * Setting for MS SQL Server paging style
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param paging Sql paging criteria
     */
    public void setMSSqlPaging(StringBuilder sql, Parameter params, Criteria paging) {
        if (params instanceof NameParameter) {
            NameParameter name = (NameParameter) params;
            sql.append(" OFFSET :P_ROW_START ROWS FETCH NEXT :P_ROW_TOTAL ROWS ONLY");
            name.add("P_ROW_START", paging.getMsSqlOffset());
            name.add("P_ROW_TOTAL", paging.getSize());
        } else {
            IndexParameter inx = (IndexParameter) params;
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            inx.add(paging.getMsSqlOffset());
            inx.add(paging.getSize());
        }
    }

    /**
     * Setting for Oracle Server paging style
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param paging Sql paging criteria
     */
    public void setOraclePaging(StringBuilder sql, Parameter params, Criteria paging) {
        String normalSql = sql.toString();
        sql.setLength(0);
        sql.append("SELECT T.* FROM (");
        sql.append("SELECT ROWNUM AS PAGE_ROW_NUM, T.* FROM (");
        sql.append(normalSql);
        sql.append(") T");
        sql.append(") T");
        if (params instanceof NameParameter) {
            NameParameter name = (NameParameter) params;
            sql.append(" WHERE T.PAGE_ROW_NUM <= :P_PAGE_ROW_NUM");
            name.add("P_PAGE_ROW_NUM", paging.getOracleRowEnd());
        } else {
            IndexParameter inx = (IndexParameter) params;
            sql.append(" WHERE T.P_PAGE_ROW_NUM <= ?");
            inx.add(paging.getOracleRowEnd());
        }
    }

    /**
     * Set order by a column or property
     * @param sql The sql statement
     * @param paging Sql paging criteria
     */
    public void setOrderByOption(StringBuilder sql, Criteria paging) {
        if (paging.isEmptySort()) {
            return;
        }

        boolean first = true;
        for (Map.Entry<String, String> map : paging.getSorts().entrySet()) {
            if (first) {
                sql.append(" ORDER BY ").append(map.getKey())
                        .append(" ").append(map.getValue());
                first = false;
            } else {
                sql.append(", ").append(map.getKey())
                        .append(" ").append(map.getValue());
            }
        }
    }

    /*++++++++++++++++++ SQL -> Number ++++++++++++++++++ */
    /**
     * Query and auto-convert to Number
     * @param sql The sql statement
     * @return result number class
     */
    public Number queryToNumber(String sql) {
        return queryToObject(sql, new IndexParameter(0), Number.class);
    }

    /**
     * Query and auto-convert to Number
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return result number class
     */
    public Number queryToNumber(String sql, Parameter params) {
        return queryToObject(sql, params, Number.class);
    }

    /*++++++++++++++++++ Procedure ++++++++++++++++++ */
    public void executeProcedure(final ProcedureCriteria criteria) throws SQLException {
        if (Validators.isNull(criteria) || Validators.isEmpty(criteria.getName())) {
            throw new SQLException("Invalid procedure name for dynamic call..");
        }

        try (Connection conn = getConnection()) {
            CallableStatement callSt = null;
            try {
                String sql = generateProcedureName(criteria);
                callSt = conn.prepareCall(sql);

                int inx = 1;
                for (ProcedureParam param : criteria.getParams()) {
                    if (Validators.isNull(param) || Validators.isNull(param.getValue())) {
                        callSt.setObject(inx, null);
                    }
                    callSt.setObject(inx, param.getValue(), param.getSqlType().getValue());
                    inx++;
                }

                logger.debug("Call PL/SQL Statement : {}", sql);
                logger.debug("Call PL/SQL Parameter : {}", criteria.getParams());

                callSt.execute();

            } finally {
                close(callSt);
            }
        }
    }

    /**
     * Close resource
     * @param conn The database connection
     * @param stmt java.sql.Statement
     * @param rs java.sql.ResultSet
     */
    public void close(Connection conn, Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
        close(conn);
    }

    /**
     * Close resource
     * @param stmt java.sql.Statement
     * @param rs java.sql.ResultSet
     */
    public void close(Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
    }

    /**
     * Close auto closeable resource
     * @param obj The Closeable resource
     */
    public void close(AutoCloseable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Get active database connection
     * @return The database connection (null when cannot get)
     * @throws SQLException When error
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get active DataSource
     * @return The active DataSource (null when cannot get)
     */
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    /*++++++++++++++++++ Execute update/insert/delete ++++++++++++++++++ */
    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @return Total row of executing.
     */
    public int execute(String sql) {
        return execute(sql, new IndexParameter(0));
    }

    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return Total row of executing.
     */
    public int execute(String sql, Parameter params) {
        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().update(sql, params.toSqlParameter());
        } else {
            return getJdbcTemplate().update(sql, toArrays(params));
        }
    }

    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @param params SQL Statement parameter
     * @return Total row of executing.
     */
    public int execute(String sql, Object ... params) {
        if (Validators.isEmpty(params)) {
            return getJdbcTemplate().update(sql);
        } else {
            return getJdbcTemplate().update(sql, params);
        }
    }

    /**
     * Concat SQL like contain value (computer to '%computer%')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeContain(String value) {
        return JdbcUtils.sqlFullLike(value);
    }

    /**
     * Concat SQL like contain value (computer to '%computer')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeStart(String value) {
        return JdbcUtils.sqlStartLike(value);
    }

    /**
     * Concat SQL like contain value (computer to 'computer%')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeEnd(String value) {
        return JdbcUtils.sqlEndLike(value);
    }

    private Object[] toArrays(Parameter params) {
        if (Validators.isNull(params)) {
            return new Object[] {};
        }
        return params.toArrayParameter();
    }

    public boolean isOracle() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isOracle(conn);
        } catch (SQLException ex) {
            logger.error("isOracle : {}", ex.getMessage());
        }
        return false;
    }

    public boolean isMySql() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isMySql(conn);
        } catch (SQLException ex) {
            logger.error("isMySql : {}", ex.getMessage());
        }

        return false;
    }

    public boolean isMSSql() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isMsSql(conn);
        } catch (SQLException ex) {
            logger.error("isMSSql : {}", ex.getMessage());
        }
        return false;
    }

    private String generateProcedureName(final ProcedureCriteria criteria) {
        if (Validators.isEmpty(criteria.getParams())) {
            return MessageFormat.format(PL_SQL_VOID, criteria.getName() + "()");
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append(criteria.getName());
        sb.append("(");
        for (int i = 0, size = criteria.getParams().size(); i < size; i++) {
            if (i == 0) {
                sb.append("?");
            } else {
                sb.append(", ?");
            }
        }
        sb.append(")");
        return MessageFormat.format(PL_SQL_VOID, sb.toString());
    }

    private void logStatement(String sql, Parameter params) {
        logStatement(sql, params, null);
    }

    private void logStatement(String sql, Parameter params, Class<?> clazz) {
        logger.debug("SQL Statement :\n {}", sql);
        if (params != null) {
            if (params instanceof NameParameter) {
                logger.debug("NameParameter : {}", params.toMapParameter());
            } else {
                logger.debug("Parameter : {}", params.toArrayParameter());
            }
        }

        if (clazz != null) {
            logger.debug("Result Target class : {}", clazz.getName());
        }
    }
}
