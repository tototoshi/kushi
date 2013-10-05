package com.github.tototoshi.kushi

import java.net.{URI, InetSocketAddress}
import com.twitter.util.Future
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http._
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import scala.collection.mutable.{Map => MutableMap}
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

object Kushi {

  private val logger = LoggerFactory.getLogger(Kushi.getClass)

  def main(args: Array[String]) = {

    val service = new Service[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest) = {

        val uri = new URI(request.getUri)
        val host = uri.getHost
        val port = if (uri.getPort == -1) 80 else uri.getPort

        val headers = {
          val _h = MutableMap.empty[String, String]

          request.getHeaders.asScala.foreach {
            entry =>
              val k = entry.getKey
              val v = entry.getValue
              if (k != "Host") {
                _h += (k -> v)
              }
          }

          _h.toMap
        }

        logger.info("-------------------------------------------------------")
        logger.info("URI: " + request.getUri)
        headers.foreach {
          case (k, v) =>
            logger.info("Header: " + k + ": " + v)
        }

        request match {
          case req if req.getMethod.getName == "GET" => {
            new HttpClient(host, port).GET(request.getUri, headers)
          }
          case req if req.getMethod.getName == "POST" => {
            new HttpClient(host, port).POST(request.getUri, headers, request.getContent)
          }
        }
      }
    }

    val port = 8080

    ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(port))
      .name("串")
      .build(service)

    println("Server start up! port: %d".format(port))

  }

}

