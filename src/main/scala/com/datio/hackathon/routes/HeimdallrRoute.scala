package com.datio.hackathon.routes

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.datio.hackathon.heimdallr.HeimdallrAdapter
import com.datio.heimdallr.api.auth.AuthPipeline
import com.datio.heimdallr.api.common.User
import com.datio.heimdallr.api.exceptions.{UnauthenticatedException, UnauthorizedException}

import scala.util.{Failure, Success, Try}

trait HeimdallrRoute {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val pipeline: AuthPipeline
  val logger = Logging(system, getClass)

  val TIME_ELAPSED = 10000

  implicit def myExceptionHandler =
    ExceptionHandler {
      case e: ArithmeticException =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.InternalServerError, entity = s"The requested resource is not found\n"))
        }
    }

  val routes: Route = {
    get {
      path("host") {
        extractRequest { (request: HttpRequest) =>
          authentication(request) match {
            case Left((status, msg)) =>
              complete(HttpResponse(status, entity = msg))
            case Right(user) => complete(s"Hey, hostname is ${InetAddress.getLocalHost}\n")
          }
        }
      }
    } ~
      post {
        path("host") {
          extractRequest { (request: HttpRequest) =>
              authentication(request) match {
                case Left((status, msg)) =>
                  complete(HttpResponse(status, entity = msg))
                case Right(user) => complete(s"Hey, hostname is ${InetAddress.getLocalHost}\n")
              }
          }
        }
      }
  }

  def authentication(request: HttpRequest): Either[(StatusCodes.ClientError, String), User] = {

    val haRequest = new HeimdallrAdapter(request)

    Try(pipeline.doPipeline(haRequest, null)) match {
      case Success(user) => Right(user)
      case Failure(ex) => ex match {
        case e: UnauthorizedException =>
          Left((StatusCodes.Forbidden, s"I'm not authorized to say hello :(- ${e.getMessage()}\n"))
        case e: UnauthenticatedException =>
          Left((StatusCodes.Unauthorized, s"User not authenticated- ${e.getMessage()}\n"))
        case e: Exception =>
          Left((StatusCodes.BadRequest, s"Internal Error- ${e.getMessage()}\n"))
      }
    }
  }

}
