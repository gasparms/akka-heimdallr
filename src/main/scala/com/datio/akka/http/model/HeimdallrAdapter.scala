package com.datio.akka.http.model

import java.security.cert.X509Certificate
import java.util
import java.util.Optional

import akka.http.javadsl.model
import akka.http.scaladsl.model.headers.{RawHeader, `Tls-Session-Info`}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import com.datio.heimdallr.api.common.{HttpMethod, HttpRequestAdapter}

import scala.collection.JavaConverters._

/**
  * Companion object for [[HeimdallrAdapter]]
  */
object HeimdallrAdapter {

  /**
    * Creates a [[HeimdallrAdapter]] from an akka-http request.
    *
    * @param request an akka-http request.
    * @param body the body of the request (is received in another parameter instead
    *             of inside the request because akka-http obtains request body as a
    *             stream future that have to be resolved).
    * @return the adapted request inside a [[HeimdallrAdapter]].
    */
  def apply(request: HttpRequest)(implicit body: String): HeimdallrAdapter =
    new HeimdallrAdapter(request)

  /**
    * Converts between akka-http model of the request Method ([[akka.http.scaladsl.model.HttpMethod]])
    * and the Heimdallr representation ([[HttpMethod]]).
    *
    * @param httpMethod akka-http method to convert.
    * @return converted method in Heimdallr representation.
    */
  implicit def httpMethodToHeimdallr(httpMethod: akka.http.scaladsl.model.HttpMethod): HttpMethod =
    httpMethod match {
      case HttpMethods.GET => HttpMethod.GET
      case HttpMethods.PUT => HttpMethod.PUT
      case HttpMethods.POST => HttpMethod.POST
      case HttpMethods.DELETE => HttpMethod.DELETE
      case HttpMethods.HEAD => HttpMethod.HEAD
      case HttpMethods.CONNECT => HttpMethod.CONNECT
      case HttpMethods.OPTIONS => HttpMethod.OPTIONS
      case HttpMethods.TRACE => HttpMethod.TRACE
    }

  /**
    * Converts from akka-http model of a request ([[HttpRequest]]) to a Heimdallr-complain
    * [[HttpRequestAdapter]].
    *
    * @param httpRequest an akka-http request.
    * @param body the body of the request (is received in another parameter instead
    *             of inside the request because akka-http obtains request body as a
    *             stream future that have to be resolved).
    * @return the converted request inside a [[HeimdallrAdapter]].
    */
  implicit def httpRequestToHeimdallr(httpRequest: HttpRequest)
                                     (implicit body: String): HeimdallrAdapter =
    HeimdallrAdapter(httpRequest)

  /**
    * Converts from a key-value map of headers (header name - header value) to a list
    * of akka-http headers.
    *
    * @param headers headers to convert.
    * @return akka-http representation of the given headers.
    */
  def headersToListHttpHeader(headers: util.Map[String, String]): List[RawHeader] =
    headers.asScala.toList.map(kv => RawHeader(kv._1, kv._2))
}

/**
  * Adapts akka-http model of a request ([[HttpRequest]]) to a Heimdallr-complain
  * [[HttpRequestAdapter]].
  *
  * @param request an akka-http request.
  * @param body the body of the request (is received in another parameter instead
  *             of inside the request because akka-http obtains request body as a
  *             stream future that have to be resolved).
  */
class HeimdallrAdapter(request: HttpRequest)(implicit body: String) extends HttpRequestAdapter {

  import HeimdallrAdapter._

  override def getBody: String = body

  // CAN'T BE OBTAINED
  override def getOrigin: String = null

  override def getPeerCertificate: X509Certificate =
    request.header[`Tls-Session-Info`]
      .flatMap(sslSession => Option(sslSession.getSession().getPeerCertificates))
      .filter(_.isInstanceOf[Array[X509Certificate]])
      .flatMap(certs => if (certs.length > 0) Some(certs(0).asInstanceOf[X509Certificate]) else None)
      .orNull

  override def getPath: String =
    request.getUri().getPathString

  override def getTarget: String =
    s"${request.getUri().getHost}:${request.getUri().getPort}"

  override def getMethod: com.datio.heimdallr.api.common.HttpMethod =
    request.method

  override def getHeader(s: String): String = {
    val option: Optional[model.HttpHeader] = request.getHeader(s)
    if (option.isPresent) option.get().value() else null
  }
}
