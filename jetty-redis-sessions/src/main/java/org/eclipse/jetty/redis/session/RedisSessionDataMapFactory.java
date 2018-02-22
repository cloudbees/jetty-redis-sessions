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

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.jetty.server.session.SessionDataMap;
import org.eclipse.jetty.server.session.SessionDataMapFactory;
import redis.clients.jedis.Protocol;
import redis.clients.util.JedisURIHelper;

/**
 * RedisSessionDataMapFactory
 */
public class RedisSessionDataMapFactory implements SessionDataMapFactory {
    protected int _expiry;
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
    protected int _maxIdle;
    protected int _minIdle;
    protected int _maxTotal;
    protected String _keyPrefix;

    public int getExpirySec() {
        return _expiry;
    }


    /**
     * @param expiry time in secs that memcached item remains valid
     */
    public void setExpirySec(int expiry) {
        _expiry = expiry;
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


    /**
     * @see org.eclipse.jetty.server.session.SessionDataMapFactory#getSessionDataMap()
     */
    @Override
    public SessionDataMap getSessionDataMap() {
        RedisSessionDataMap m = new RedisSessionDataMap(_host, Integer.toString(_port));
        m.setExpirySec(_expiry);
        m.setConnectionTimeout(_connectionTimeout);
        m.setSoTimeout(_soTimeout);
        m.setPassword(_password);
        m.setDatabase(_database);
        m.setClientName(_clientName);
        m.setSSL(_ssl);
        m.setSSLSocketFactory(_sslSocketFactory);
        m.setSSLParameters(_sslParameters);
        m.setHostnameVerifier(_hostnameVerifier);
        m.setMaxIdle(_maxIdle);
        m.setMinIdle(_minIdle);
        m.setMaxTotal(_maxTotal);
        m.setKeyPrefix(_keyPrefix);
        return m;
    }

}
