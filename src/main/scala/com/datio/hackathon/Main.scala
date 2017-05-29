package com.datio.hackathon

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.datio.hackathon.heimdallr.HeimdallrAdapter
import com.datio.heimdallr.api.auth.AuthPipeline
import com.typesafe.config.ConfigFactory


object StartApplication extends App {
  StartApp
}

object StartApp {
  implicit val system: ActorSystem = ActorSystem("Akke-Heimdallr-routing-service")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  implicit val pipeline: AuthPipeline = AuthPipeline.fromConfig(config)

  val server = new HeimdallrRoutingServer()
  val serverUrl = config.getString("http.interface")
  val port = config.getInt("http.port")
  val thresholdValue = config.getInt("http.thresholdValue")
  server.startServer(serverUrl, port)
}