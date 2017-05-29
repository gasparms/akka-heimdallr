package com.datio.hackathon.heimdallr

import java.security.cert.X509Certificate

import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpRequest}
import akka.http.scaladsl.model.headers.{RawHeader, `Tls-Session-Info`}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.datio.heimdallr.api.common.{HttpMethod, HttpRequestAdapter}

import scala.concurrent.Await
import scala.concurrent.duration._
import collection.JavaConverters._

object HeimdallrHttpRequest {

  /**
    *
    * @param httpMethod
    * @return
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

  implicit def httpRequestToHeimdallr(httpRequest: HttpRequest)
                                     (implicit materializer: ActorMaterializer): HeimdallrHttpRequest =
    new HeimdallrHttpRequest(httpRequest)

  implicit def mapHeadersToSeqHttpHeader(headers: java.util.Map[String, String]): Seq[HttpHeader] =
    headers.asScala.toList.map(kv => RawHeader(kv._1, kv._2))
}

/**
  * Created by rveral on 29/05/17.
  */
class HeimdallrHttpRequest(request: HttpRequest)
                          (implicit materializer: ActorMaterializer) extends HttpRequestAdapter {
  import HeimdallrHttpRequest._

  implicit val timeout: FiniteDuration = 300.millis

  override def getOrigin: String = "UNDEFINED"

  override def getBody: String =
    Await.result(Unmarshal(request.entity).to[String], timeout)

  override def getPeerCertificate: X509Certificate =
    request.header[`Tls-Session-Info`]
      .flatMap(sslSession => Option(sslSession.getSession().getPeerCertificates))
      .filter(_.isInstanceOf[Array[X509Certificate]])
      .flatMap(certs => if (certs.length > 0) Some(certs(0).asInstanceOf[X509Certificate]) else None)
      .orNull

  override def getMethod: HttpMethod = request.method

  override def getHeader(header: String): String = {
    val requestHeader = request.getHeader(header)
    if (requestHeader.isPresent) requestHeader.get().value()
    else null
  }

  override def getTarget: String =
    s"${request.getUri().getHost.address()}:${request.getUri().getPort}"

  override def getPath: String = request.getUri().getPathString
}
