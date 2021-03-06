package com.datio.hackathon

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.datio.heimdallr.api.auth.AuthPipeline
import com.typesafe.config.ConfigFactory


object StartApplication extends App {
  StartApp
}

object StartApp {
  implicit val system: ActorSystem = ActorSystem("Akke-Heimdallr-routing-service")
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val pipeline = AuthPipeline.fromConfig(system.settings.config.getConfig("heimdallr"))

  val server = new HeimdallrRoutingServer()
  val config = ConfigFactory.load()
  val serverUrl = config.getString("http.interface")
  val port = config.getInt("http.port")
  val thresholdValue = config.getInt("http.thresholdValue")
  server.startServer(serverUrl, port)
}