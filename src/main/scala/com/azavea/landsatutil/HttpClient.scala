package com.azavea.landsatutil

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer

import scala.concurrent.Future

/** Wrapper around HTTP I/O. */
class HttpClient(apiEndpoint: Uri)(implicit val system: ActorSystem = ActorSystem(
  s"${HttpClient.AKKA_COOKIE}${java.util.UUID.randomUUID}")) {

  implicit private val materializer = ActorMaterializer()
  import system.dispatcher

  type SprayJsonReader[R] = Unmarshaller[ResponseEntity, R]
  /** Submit query to configured API endpoint.*/
  def get[T: SprayJsonReader](query: Uri.Query): Future[T] = {
    val req = apiEndpoint.withQuery(query)
    system.log.debug(s"Request: $req")
    Http().singleRequest(HttpRequest(uri = req))
      .flatMap(resp ⇒ Unmarshal(resp.entity).to[T])
  }

  /** Release all resources associated with network communications. */
  def shutdown(): Unit = {
    Http().shutdownAllConnectionPools() onComplete { _ ⇒
      materializer.shutdown()
      // As a hack, using Akka system name to determine if we created it.
      if(system.name.startsWith(HttpClient.AKKA_COOKIE))
        system.terminate()
    }
  }
}

object HttpClient {
  private val AKKA_COOKIE = "00http_query_"
}
