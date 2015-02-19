package tk.minecraftopia.util;

import java.io.*;
import java.util.concurrent.locks.*;
import java.util.*;
import java.sql.*;
import java.util.concurrent.*;

public class MySQLConnectionPool implements Closeable
{
    private int poolsize;
    private final Vector<JDCConnection> connections;
    private final ConnectionReaper reaper;
    private final String url;
    private final String user;
    private final String password;
    private final Lock lock;
    
    public MySQLConnectionPool(final String url, final String user, final String password, final int poolsize) throws ClassNotFoundException {
        this.poolsize = 10;
        this.lock = new ReentrantLock();
        this.poolsize = poolsize;
        Class.forName("com.mysql.jdbc.Driver");
        this.url = url;
        this.user = user;
        this.password = password;
        this.connections = new Vector<JDCConnection>(this.poolsize);
        (this.reaper = new ConnectionReaper((ConnectionReaper)null)).start();
    }
    
    @Override
    public void close() {
        this.lock.lock();
        final Enumeration<JDCConnection> conns = this.connections.elements();
        while (conns.hasMoreElements()) {
            final JDCConnection conn = conns.nextElement();
            this.connections.remove(conn);
            conn.terminate();
        }
        this.lock.unlock();
    }
    
    public Connection getConnection() throws SQLException {
        this.lock.lock();
        try {
            final Enumeration<JDCConnection> conns = this.connections.elements();
            while (conns.hasMoreElements()) {
                final JDCConnection conn = conns.nextElement();
                if (conn.lease()) {
                    if (conn.isValid()) {
                        return conn;
                    }
                    this.connections.remove(conn);
                    conn.terminate();
                }
            }
            final JDCConnection conn = new JDCConnection(DriverManager.getConnection(this.url, this.user, this.password));
            conn.lease();
            if (!conn.isValid()) {
                conn.terminate();
                throw new SQLException("Failed to validate a brand new connection");
            }
            this.connections.add(conn);
            return conn;
        }
        finally {
            this.lock.unlock();
        }
    }
    
    private void reapConnections() {
        this.lock.lock();
        final long stale = System.currentTimeMillis() - 20000L;
        final Iterator<JDCConnection> itr = this.connections.iterator();
        while (itr.hasNext()) {
            final JDCConnection conn = itr.next();
            if (conn.inUse() && stale > conn.getLastUse() && !conn.isValid()) {
                itr.remove();
            }
        }
        this.lock.unlock();
    }
    
    private class ConnectionReaper extends Thread
    {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(30000L);
                }
                catch (InterruptedException ex) {}
                MySQLConnectionPool.this.reapConnections();
            }
        }
    }
    
    public class JDCConnection implements Connection
    {
        private final Connection conn;
        private boolean inuse;
        private long timestamp;
        private int networkTimeout;
        private String schema;
        
        JDCConnection(final Connection conn) {
            this.conn = conn;
            this.inuse = false;
            this.timestamp = 0L;
            this.networkTimeout = 30;
            this.schema = "default";
        }
        
        @Override
        public void clearWarnings() throws SQLException {
            this.conn.clearWarnings();
        }
        
        @Override
        public void close() {
            this.inuse = false;
            try {
                if (!this.conn.getAutoCommit()) {
                    this.conn.setAutoCommit(true);
                }
            }
            catch (SQLException ex) {
                MySQLConnectionPool.this.connections.remove(this.conn);
                this.terminate();
            }
        }
        
        @Override
        public void commit() throws SQLException {
            this.conn.commit();
        }
        
        @Override
        public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
            return this.conn.createArrayOf(typeName, elements);
        }
        
        @Override
        public Blob createBlob() throws SQLException {
            return this.conn.createBlob();
        }
        
        @Override
        public Clob createClob() throws SQLException {
            return this.conn.createClob();
        }
        
        @Override
        public NClob createNClob() throws SQLException {
            return this.conn.createNClob();
        }
        
        @Override
        public SQLXML createSQLXML() throws SQLException {
            return this.conn.createSQLXML();
        }
        
        @Override
        public Statement createStatement() throws SQLException {
            return this.conn.createStatement();
        }
        
        @Override
        public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
            return this.conn.createStatement(resultSetType, resultSetConcurrency);
        }
        
        @Override
        public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
            return this.conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
            return this.conn.createStruct(typeName, attributes);
        }
        
        @Override
        public boolean getAutoCommit() throws SQLException {
            return this.conn.getAutoCommit();
        }
        
        @Override
        public String getCatalog() throws SQLException {
            return this.conn.getCatalog();
        }
        
        @Override
        public Properties getClientInfo() throws SQLException {
            return this.conn.getClientInfo();
        }
        
        @Override
        public String getClientInfo(final String name) throws SQLException {
            return this.conn.getClientInfo(name);
        }
        
        @Override
        public int getHoldability() throws SQLException {
            return this.conn.getHoldability();
        }
        
        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return this.conn.getMetaData();
        }
        
        @Override
        public int getTransactionIsolation() throws SQLException {
            return this.conn.getTransactionIsolation();
        }
        
        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return this.conn.getTypeMap();
        }
        
        @Override
        public SQLWarning getWarnings() throws SQLException {
            return this.conn.getWarnings();
        }
        
        @Override
        public boolean isClosed() throws SQLException {
            return this.conn.isClosed();
        }
        
        @Override
        public boolean isReadOnly() throws SQLException {
            return this.conn.isReadOnly();
        }
        
        @Override
        public boolean isValid(final int timeout) throws SQLException {
            return this.conn.isValid(timeout);
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            return this.conn.isWrapperFor(iface);
        }
        
        @Override
        public String nativeSQL(final String sql) throws SQLException {
            return this.conn.nativeSQL(sql);
        }
        
        @Override
        public CallableStatement prepareCall(final String sql) throws SQLException {
            return this.conn.prepareCall(sql);
        }
        
        @Override
        public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
            return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
            return this.conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql) throws SQLException {
            return this.conn.prepareStatement(sql);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
            return this.conn.prepareStatement(sql, autoGeneratedKeys);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
            return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
            return this.conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
            return this.conn.prepareStatement(sql, columnIndexes);
        }
        
        @Override
        public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
            return this.conn.prepareStatement(sql, columnNames);
        }
        
        @Override
        public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
            this.conn.releaseSavepoint(savepoint);
        }
        
        @Override
        public void rollback() throws SQLException {
            this.conn.rollback();
        }
        
        @Override
        public void rollback(final Savepoint savepoint) throws SQLException {
            this.conn.rollback(savepoint);
        }
        
        @Override
        public void setAutoCommit(final boolean autoCommit) throws SQLException {
            this.conn.setAutoCommit(autoCommit);
        }
        
        @Override
        public void setCatalog(final String catalog) throws SQLException {
            this.conn.setCatalog(catalog);
        }
        
        @Override
        public void setClientInfo(final Properties properties) throws SQLClientInfoException {
            this.conn.setClientInfo(properties);
        }
        
        @Override
        public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
            this.conn.setClientInfo(name, value);
        }
        
        @Override
        public void setHoldability(final int holdability) throws SQLException {
            this.conn.setHoldability(holdability);
        }
        
        @Override
        public void setReadOnly(final boolean readOnly) throws SQLException {
            this.conn.setReadOnly(readOnly);
        }
        
        @Override
        public Savepoint setSavepoint() throws SQLException {
            return this.conn.setSavepoint();
        }
        
        @Override
        public Savepoint setSavepoint(final String name) throws SQLException {
            return this.conn.setSavepoint(name);
        }
        
        @Override
        public void setTransactionIsolation(final int level) throws SQLException {
            this.conn.setTransactionIsolation(level);
        }
        
        @Override
        public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
            this.conn.setTypeMap(map);
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            return this.conn.unwrap(iface);
        }
        
        @Override
        public int getNetworkTimeout() throws SQLException {
            return this.networkTimeout;
        }
        
        @Override
        public void setNetworkTimeout(final Executor exec, final int timeout) throws SQLException {
            this.networkTimeout = timeout;
        }
        
        @Override
        public void abort(final Executor exec) throws SQLException {
        }
        
        @Override
        public String getSchema() throws SQLException {
            return this.schema;
        }
        
        @Override
        public void setSchema(final String str) throws SQLException {
            this.schema = str;
        }
        
        long getLastUse() {
            return this.timestamp;
        }
        
        boolean inUse() {
            return this.inuse;
        }
        
        boolean isValid() {
            try {
                return this.conn.isValid(1);
            }
            catch (SQLException ex) {
                return false;
            }
        }
        
        synchronized boolean lease() {
            if (this.inuse) {
                return false;
            }
            this.inuse = true;
            this.timestamp = System.currentTimeMillis();
            return true;
        }
        
        void terminate() {
            try {
                this.conn.close();
            }
            catch (SQLException ex) {}
        }
    }
}
