package com.emdg.thegatherer

import akka.actor.{ActorSystem, Props}
import com.emdg.thegatherer.db.DB
import com.emdg.thegatherer.actors.ScraperActor

object Boot extends App {

	DB.createDatabases()
	val system = ActorSystem("Gatherer")
	val scraperActor = system.actorOf(Props[ScraperActor])
	scraperActor ! "start"
}