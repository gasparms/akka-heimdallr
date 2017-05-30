package com.datio.akka.http

import com.datio.akka.http.directives._

/**
  * Collects all Datio directives into one trait for simple importing.
  */
trait Directives extends HeimdallrDirectives

object Directives extends Directives
