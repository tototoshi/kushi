package com.github.tototoshi.kushi

import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{RequestBuilder, Request, Http}
import java.net.{URL, InetSocketAddress}
import com.twitter.util.{Future, Duration}
import java.util.concurrent.TimeUnit
import org.jboss.netty.buffer.ChannelBuffer

class HttpClient(host: String, port: Int) {

  lazy val clientWithoutErrorHandling: Service[HttpRequest, HttpResponse] =
    ClientBuilder()
      .codec(Http())
      .hosts(new InetSocketAddress(host, port))
      .keepAlive(value = true)
      .hostConnectionLimit(1)
      .tcpConnectTimeout(Duration(3, TimeUnit.SECONDS)) // TODO should be configurable
      .build()

  def GET(url: String, headers: Map[String, String]): Future[HttpResponse] = {
    clientWithoutErrorHandling(
      Request(
        RequestBuilder()
          .url(new URL(url))
          .addHeaders(headers)
          .buildGet()
      )
    )
  }

  def POST(url: String, headers: Map[String, String], content: ChannelBuffer): Future[HttpResponse] = {
    clientWithoutErrorHandling(
      Request(
        RequestBuilder()
          .url(new URL(url))
          .addHeaders(headers)
          .buildPost(content)
      )
    )
  }

}
