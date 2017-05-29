package com.datio.hackathon.heimdallr

import java.security.cert.X509Certificate
import java.util.Optional

import akka.http.javadsl.model.HttpHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.Materializer
import akka.util.ByteString
import com.datio.heimdallr.api.common.HttpRequestAdapter

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.http.scaladsl.model.headers.`Tls-Session-Info`


class HeimdallrAdapter(request: HttpRequest)(implicit materializer: Materializer) extends HttpRequestAdapter {

  override def getBody: String = {
    val future = request.entity.dataBytes.runFold(ByteString())(_ ++ _)
    val result = Await.result(future, Duration(4, "seconds"))
    result.decodeString("UTF-8")
  }

  override def getOrigin: String = null

  override def getPeerCertificate: X509Certificate =
    request.header[`Tls-Session-Info`]
      .flatMap(sslSession => Option(sslSession.getSession().getPeerCertificates))
      .filter(_.isInstanceOf[Array[X509Certificate]])
      .flatMap(certs => if (certs.length > 0) Some(certs(0).asInstanceOf[X509Certificate]) else None)
      .orNull

  override def getPath: String = request.getUri().getPathString

  override def getTarget: String = s"${request.getUri().getHost}:${request.getUri().getPort}"

  override def getMethod: com.datio.heimdallr.api.common.HttpMethod = {
    val method: akka.http.scaladsl.model.HttpMethod = request.method

    method match {
      case HttpMethods.GET => com.datio.heimdallr.api.common.HttpMethod.GET
      case HttpMethods.POST => com.datio.heimdallr.api.common.HttpMethod.POST
      case _ => com.datio.heimdallr.api.common.HttpMethod.GET
    }
  }

  override def getHeader(s: String): String = {
    val option: Optional[HttpHeader] = request.getHeader(s)
    if (option.isPresent) option.get().value() else null
  }
}
