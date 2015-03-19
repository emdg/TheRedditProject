package com.emdg.thegatherer.db
import akka.actor.ActorSystem
import akka.util._
import scala.concurrent.duration._
import gnieh.sohva.async._

object DB {
	implicit val system = ActorSystem()

	implicit val timeout = Timeout(20.seconds)

	val couch = new CouchClient





	def createDatabases() = {
		DB.couch.database("subreddit").create
		DB.couch.database("link").create
		DB.couch.database("comment").create

	}

}