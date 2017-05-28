package com.datio.hackathon

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.datio.hackathon.routes.HeimdallrRoute

import scala.concurrent.ExecutionContext.Implicits.global

class HeimdallrRoutingServer(implicit val system: ActorSystem,
                             implicit val materializer: ActorMaterializer) extends HeimdallrRoute {

  def startServer(address: String, port: Int) = {
    Http().bindAndHandle(routes, address, port)
  }

}
