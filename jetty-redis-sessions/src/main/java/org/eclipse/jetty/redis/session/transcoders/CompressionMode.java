// copied from https://github.com/killme2008/xmemcached/tree/90dd456f29/src/main/java/net/rubyeye/xmemcached/transcoders
package org.eclipse.jetty.redis.session.transcoders;

/**
 * Compress mode for compressing data
 *
 * @author apple
 */
public enum CompressionMode {
    /**
     * Gzip mode
     */
    GZIP,
    /**
     * Zip mode
     */
    ZIP
}
