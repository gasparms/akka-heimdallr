package com.datio.hackathon.routes

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.datio.hackathon.heimdallr.HeimdallrHttpRequest._
import com.datio.heimdallr.api.auth.AuthPipeline
import com.datio.heimdallr.api.common.User
import com.datio.heimdallr.api.exceptions.{UnauthenticatedException, UnauthorizedException}

import scala.util.{Failure, Success, Try}
import collection.JavaConverters._

trait HeimdallrRoute {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val pipeline: AuthPipeline

  val logger = Logging(system, getClass)

  def heimdallr(pipeline: AuthPipeline): Directive1[User] =
    extractRequest flatMap  {
      request => Try(pipeline.doPipeline(request, null)) match {
        case Success(user) => provide(user)

        case Failure(e:UnauthenticatedException) =>
          complete(StatusCodes.Unauthorized, e.getHeaders.asScala.toList
            .map(kv => RawHeader(kv._1, kv._2)), e.getReason)

        case Failure(e:UnauthorizedException) =>
          complete(StatusCodes.Forbidden, e.getHeaders.asScala.toList
            .map(kv => RawHeader(kv._1, kv._2)), e.getReason)

        case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
      }
    }

  val TIME_ELAPSED = 10000
  implicit def myExceptionHandler =
    ExceptionHandler {
      case e: ArithmeticException =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.InternalServerError, entity = s"The requested resource is not found\n"))
        }
    }

  val routes: Route = {
    heimdallr(pipeline) { user =>
      get {
        path("host") {
          complete {
            HttpResponse(StatusCodes.OK, entity = s"Hey, hostname is ${InetAddress.getLocalHost}\n")
          }
        } ~
          path("test") {
            complete(StatusCodes.OK, s"$user")
          }
      }
    }
  }

}
