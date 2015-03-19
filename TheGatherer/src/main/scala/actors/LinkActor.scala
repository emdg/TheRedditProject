package com.emdg.thegatherer.actors
import akka.actor.Actor
import com.emdg.thegatherer.domain._
import com.emdg.thegatherer.db.DB



class LinkActor extends Actor{

	var currentSubreddit:SubReddit = null
	val linkDatabase = DB.couch.database("link")
	var workingLinks = List[Link]()
	var lastID = ""
	var first = true
	var tmpLastID = ""

	def receive = {
		case LinkBunch(links) =>
			links.foreach((x) => {
				linkDatabase.saveDoc(x)
			})
			workingLinks = links
			lastID = links.last._id


			if (currentSubreddit != null ){
				sender ! WorkRequest(workingLinks.head)
			}




		case x: WorkRequest =>

			currentSubreddit = x.model match {
				case sub: SubReddit =>
					sub
			}
			//sender ! Done(currentSubreddit)
			sender ! Request("http://www.reddit.com/r/%s.json?limit=100".format(currentSubreddit.name))



		case x: Done =>
			workingLinks = workingLinks.filter(_ != x.model)
			if (workingLinks.isEmpty){
				if (tmpLastID == lastID){
					sender ! Done(currentSubreddit)
				}
				else {
					sender ! Request("http://www.reddit.com/r/%s.json?limit=100&after=%s".format(currentSubreddit.name,"t3_"+lastID))
					tmpLastID = lastID
				}
			}
			else {
				sender ! WorkRequest(workingLinks.head)
			}


		case _ => 
			println("uncaught message from Link")

	}
}