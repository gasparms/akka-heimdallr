package com.datio.hackathon.routes

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer

trait HeimdallrRoute {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
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
        complete {
          HttpResponse(StatusCodes.OK, entity = s"Hey, hostname is ${InetAddress.getLocalHost}\n")
        }
      }
    }
  }

}
