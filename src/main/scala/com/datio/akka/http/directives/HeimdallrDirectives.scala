package com.datio.akka.http.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.datio.akka.http.model.HeimdallrAdapter._
import com.datio.heimdallr.api.auth.AuthPipeline
import com.datio.heimdallr.api.common.User
import com.datio.heimdallr.api.exceptions.{UnauthenticatedException, UnauthorizedException}

import scala.util.{Failure, Success, Try}

/**
  * Contains all the directives related to the security capabilities
  * using Heimdallr library.
  *
  * @groupname heimdallr
  */
trait HeimdallrDirectives {
  /**
    * Performs the authentication/authorization process for the incoming
    * request using Heimdallr [[AuthPipeline]]. In case that the request
    * is rejected, the appropiate response is sent back to the client.
    *
    * @param pipeline the [[AuthPipeline]] to process for each request.
    * @return the authenticated [[User]] in case the request is accepted.
    * @group heimdallr
    */
  def heimdallr(pipeline: AuthPipeline): Directive1[User] =
    extractRequest flatMap {
      request =>
        entity(as[String]) flatMap {
          implicit body =>
            Try(pipeline.doPipeline(request, null)) match {
              case Success(user) => provide(user)

              case Failure(e: UnauthenticatedException) =>
                complete(StatusCodes.Unauthorized, headersToListHttpHeader(e.getHeaders), e.getReason)

              case Failure(e: UnauthorizedException) =>
                complete(StatusCodes.Forbidden, headersToListHttpHeader(e.getHeaders), e.getReason)

              case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
            }
        }
    }

}
