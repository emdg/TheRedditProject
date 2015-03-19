package com.emdg.thegatherer.actors
import akka.actor.Actor
import com.emdg.thegatherer.domain._
import com.emdg.thegatherer.db.DB



class SubredditActor extends Actor{


	val subredditDatabase = DB.couch.database("subreddit")
	var lastID = ""
	var currentSubReddits:List[SubReddit] = List()


	def receive = {

		case SubRedditBunch(subreddits) =>
			subreddits.foreach((x) => {
				subredditDatabase.saveDoc(x)
			})

			currentSubReddits = subreddits
			lastID = subreddits.last._id
			sender ! WorkRequest(currentSubReddits.head)
	
		case x: Done =>
			currentSubReddits = currentSubReddits.filter((y: SubReddit) => y._id != x.model._id)
			if (currentSubReddits.isEmpty){
				sender ! Request("http://www.reddit.com/reddits.json?limit=100&after=%s".format("t5_"+lastID))
			}
			else {
				sender ! WorkRequest(currentSubReddits.head)
			}
		case _ => 
			println("uncaught message from SubReddit")

	}
}