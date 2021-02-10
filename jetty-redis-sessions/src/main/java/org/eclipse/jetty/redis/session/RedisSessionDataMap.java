//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd., CloudBees, Inc.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
// based on https://github.com/eclipse/jetty.project/tree/e46459e8a8/jetty-memcached/jetty-memcached-sessions
package org.eclipse.jetty.redis.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionDataMap;
import org.eclipse.jetty.util.ClassLoadingObjectInputStream;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.JedisURIHelper;

/**
 * RedisSessionDataMap
 *
 * Uses redis as a cache for SessionData
 */
@ManagedObject
public class RedisSessionDataMap extends AbstractLifeCycle implements SessionDataMap {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "6379";
    protected SessionContext _context; //context associated with this session data map
    protected JedisPool _pool;
    protected int _expirySec = 0;
    protected String _host;
    protected int _port;
    protected int _connectionTimeout = Protocol.DEFAULT_TIMEOUT;
    protected int _soTimeout = Protocol.DEFAULT_TIMEOUT;
    protected String _password;
    protected int _database = Protocol.DEFAULT_DATABASE;
    protected String _clientName;
    protected boolean _ssl;
    protected SSLSocketFactory _sslSocketFactory;
    protected SSLParameters _sslParameters;
    protected HostnameVerifier _hostnameVerifier;
    protected int _maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;
    protected int _minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;
    protected int _maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
    protected String _keyPrefix;
    protected boolean _compression = false;

    /**
     * @param host address of memcache server
     * @param port address of memcache server
     */
    public RedisSessionDataMap(String host, String port) {
        if (host == null || port == null) {
            throw new IllegalArgumentException("Host: " + host + " port: " + port);
        }
        _host = host;
        int v;
        try {
            v = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port: " + port, e);
        }
        if (v <= 0 || v > 65535) {
            throw new IllegalArgumentException("Port: " + v);
        }
        _port = v;
    }

    /**
     * @param sec the expiry to use in seconds
     */
    public void setExpirySec(int sec) {
        _expirySec = sec;
    }

    /**
     * Expiry time for memached entries.
     *
     * @return memcached expiry time in sec
     */
    @ManagedAttribute(value = "redis expiry time in sec", readonly = true)
    public int getExpirySec() {
        return _expirySec;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        this._host = host;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        this._port = port;
    }

    public String getUrl() {
        return (_ssl ? "rediss" : "redis") + "://" + (_password == null ? null : ":" + _password + "@") + _host + ":"
                + _port + (_database != Protocol.DEFAULT_DATABASE ? "/" + _database : "/");
    }

    public void setUrl(String url) {
        URI uri = URI.create(url);
        if (JedisURIHelper.isValid(uri)) {
            setHost(uri.getHost());
            setPort(uri.getPort());
            setPassword(JedisURIHelper.getPassword(uri));
            setDatabase(JedisURIHelper.getDBIndex(uri));
            setSSL(uri.getScheme().equals("rediss"));
        } else {
            throw new IllegalArgumentException("Url: " + url);
        }
    }

    public int getConnectionTimeout() {
        return _connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this._connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return _soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this._soTimeout = soTimeout;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public int getDatabase() {
        return _database;
    }

    public void setDatabase(int database) {
        this._database = database;
    }

    public String getClientName() {
        return _clientName;
    }

    public void setClientName(String clientName) {
        this._clientName = clientName;
    }

    public boolean isSSL() {
        return _ssl;
    }

    public void setSSL(boolean ssl) {
        this._ssl = ssl;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return _sslSocketFactory;
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this._sslSocketFactory = sslSocketFactory;
    }

    public SSLParameters getSSLParameters() {
        return _sslParameters;
    }

    public void setSSLParameters(SSLParameters sslParameters) {
        this._sslParameters = sslParameters;
    }

    public HostnameVerifier getHostnameVerifier() {
        return _hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this._hostnameVerifier = hostnameVerifier;
    }

    public int getMaxIdle() {
        return _maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this._maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return _minIdle;
    }

    public void setMinIdle(int minIdle) {
        this._minIdle = minIdle;
    }

    public int getMaxTotal() {
        return _maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this._maxTotal = maxTotal;
    }

    public String getKeyPrefix() {
        return _keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this._keyPrefix = keyPrefix;
    }

    public boolean isCompression() {
        return _compression;
    }

    public void setCompression(boolean compression) {
        this._compression = compression;
    }

    /**
     * @see SessionDataMap#initialize(SessionContext)
     */
    @Override
    public void initialize(SessionContext context) {
        if (isStarted()) {
            throw new IllegalStateException("Context set after RedisSessionDataMap started");
        }
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(_maxIdle);
        poolConfig.setMinIdle(_minIdle);
        poolConfig.setMaxTotal(_maxTotal);
        _pool = new JedisPool(poolConfig, _host, _port, _connectionTimeout, _soTimeout,
                _password, _database, _clientName, _ssl, _sslSocketFactory, _sslParameters, _hostnameVerifier);
        _context = context;
    }

    /**
     * @see SessionDataMap#load(java.lang.String)
     */
    @Override
    public SessionData load(String id) throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("Not started");
        }
        final AtomicReference<SessionData> reference = new AtomicReference<SessionData>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();

        Runnable r = () ->
        {
            try {
                byte[] bytes;
                try (Jedis jedis = _pool.getResource()) {
                    bytes = jedis.get(keyAsBytes(id));
                }
                if (bytes == null || bytes.length < 4) {
                    reference.set(null);
                    return;
                }
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                     InputStream in = _compression ? new InflaterInputStream(bais) : null;
                     ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(_compression ? in : bais);) {
                    long created = ois.readLong();
                    long accessed = ois.readLong();
                    long lastAccessed = ois.readLong();
                    long maxIdle = ois.readLong();
                    SessionData data =
                            new SessionData(id, _context.getCanonicalContextPath(), _context.getVhost(), created,
                                    accessed, lastAccessed, maxIdle);
                    SessionData.deserializeAttributes(data, ois);
                    reference.set(data);
                }
            } catch (Exception e) {
                exception.set(e);
            }
        };

        _context.run(r);
        if (exception.get() != null)
            throw exception.get();

        return reference.get();
    }

    private byte[] keyAsBytes(String id) {
        if (_keyPrefix == null) {
            return id.getBytes(StandardCharsets.UTF_8);
        } else {
            return (_keyPrefix + id).getBytes(StandardCharsets.UTF_8);
        }
    }


    /**
     * @see SessionDataMap#store(java.lang.String, SessionData)
     */
    @Override
    public void store(String id, SessionData data) throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("Not started");
        }
        if (data == null) {
            return;
        }

        final AtomicReference<Exception> exception = new AtomicReference<Exception>();

        Runnable r = () -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (DeflaterOutputStream out = _compression ? new DeflaterOutputStream(baos) : null;
                     ObjectOutputStream oos = new ObjectOutputStream(_compression ? out : baos)) {
                    oos.writeLong(data.getCreated());
                    oos.writeLong(data.getAccessed());
                    oos.writeLong(data.getLastAccessed());
                    oos.writeLong(data.getMaxInactiveMs());
                    SessionData.serializeAttributes(data, oos);
                }
                byte[] bytes = baos.toByteArray();
                try (Jedis jedis = _pool.getResource()) {
                    if (_expirySec > 0) {
                        jedis.setex(keyAsBytes(id), _expirySec, bytes);
                    } else {
                        jedis.set(keyAsBytes(id), bytes);
                    }

                }
            } catch (Exception e) {
                exception.set(e);
            }
        };

        _context.run(r);
        if (exception.get() != null) {
            throw exception.get();
        }
    }


    /**
     * @see SessionDataMap#delete(java.lang.String)
     */
    @Override
    public boolean delete(String id) throws Exception {
        if (!isStarted()) {
            throw new IllegalStateException("Not started");
        }
        try (Jedis jedis = _pool.getResource()) {
            return jedis.del(keyAsBytes(id)) > 0;
        }
    }

    @Override
    protected void doStart() throws Exception {
        if (_context == null)
            throw new IllegalStateException("No SessionContext");

        super.doStart();
    }


    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (_pool != null) {
            _pool.close();
            ;
            _pool = null;
        }
    }

}
