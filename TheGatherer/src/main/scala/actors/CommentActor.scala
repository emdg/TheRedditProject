package com.emdg.thegatherer.actors
import akka.actor.Actor
import com.emdg.thegatherer.domain._
import com.emdg.thegatherer.db.DB



class CommentActor extends Actor{

	var currentLink:Link = null
	val commentDatabase = DB.couch.database("comment")
	var lastID = ""
	var tmpLastID = ""
	def receive = {


		case CommentBunch(comments) =>
			comments.foreach((x) => {
				commentDatabase.saveDoc(x)
			})
			lastID = comments.last._id


			if (comments.isEmpty || tmpLastID == lastID){
				sender ! Done(currentLink)
			}
			else {
				tmpLastID = lastID
				sender ! Request("http://www.reddit.com/r/%s/comments/%s/%s.json?limit=1000&after=%s".format(currentLink.subreddit, currentLink._id, currentLink.title.replace(" ", "_"), lastID))
			}

		case x: WorkRequest =>
			currentLink = x.model match {
				case l: Link =>
					l
			}
			//sender ! Done(currentLink)
			sender ! Request("http://www.reddit.com/r/%s/comments/%s/%s.json?limit=1000".format(currentLink.subreddit, currentLink._id, currentLink.title.replace(" ", "_")))
	}
}