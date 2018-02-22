[description]
Redis cache for SessionData

[tags]
session

[depends]
session-store

[files]
maven://redis.clients/jedis/2.9.0|lib/jedis/jedis-2.9.0.jar

[lib]
lib/jetty-redis-sessions-${jetty.version}.jar
lib/jedis/*.jar

[license]
Based on Jetty Memcached Sessions hosted in the Jetty project and released under dual license:
Eclipse Public License - Version 1.0 / Apache License - Version 2.0

Modifications to the code inspired by Jetty Memcached Sessions are licensed under the same terms as the original.

Some code is copied from Xmemcached.
Xmemcached is an open source project hosted on Github and released under the Apache 2.0 license.
https://github.com/killme2008/xmemcached
http://www.apache.org/licenses/LICENSE-2.0.html

Jedis is an open source project hosted on Github and released under the MIT license.
https://github.com/xetorthio/jedis/
Copyright (c) 2010 Jonathan Leibiusky

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

[xml]
etc/sessions/session-data-cache/jedis.xml
