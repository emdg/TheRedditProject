package com.emdg.thegatherer.actors
import com.emdg.thegatherer.domain._


import akka.actor.{Actor, Props}

class ScraperActor extends Actor {

	val requestActor = context.actorOf(Props(classOf[RequestActor], self))
	val subredditActor = context.actorOf(Props[SubredditActor])
	val linkActor = context.actorOf(Props[LinkActor])
	val commentActor = context.actorOf(Props[CommentActor])
	def receive = {
		case request: Request =>
			requestActor ! request


		case subreddits: SubRedditBunch =>
			if (!subreddits.models.isEmpty)
			subredditActor ! subreddits

		case links: LinkBunch =>
			if (!links.models.isEmpty)
			linkActor ! links
		case comments: CommentBunch => {
			if (!comments.models.isEmpty)
			commentActor ! comments
		}

		case x @ Done(model) =>
			model match {
				case subreddit: SubReddit =>
					subredditActor ! x
				case link : Link =>
					linkActor ! x

			}

		case x @ WorkRequest(model) =>
			model match {
				case subreddit: SubReddit =>
					requestActor ! x
					linkActor ! x
				case link: Link =>
					requestActor ! x
					commentActor ! x
				case _ =>
					requestActor ! x
			}

		case null => 


		case x: Any => 
			println(x)
			println("uncaught message from Scraper")
	}



	self ! Request("http://www.reddit.com/reddits.json?after=t5_2ti4h&limit=15")
}