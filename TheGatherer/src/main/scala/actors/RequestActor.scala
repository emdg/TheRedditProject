package com.emdg.thegatherer.actors
import com.emdg.thegatherer.domain._
import scalaj.http._
import akka.actor.{Actor, ActorRef}
import play.api.libs.json._
import scala.collection.mutable.ListBuffer

class RequestActor(scraper: ActorRef) extends Actor {
	var urls = List()
	var currentTask = WorkRequest(null)
	var attempts = 2
	var first = true
	def receive = {
		case request: Request =>
			try {
				println(request)
				//Thread.sleep(2000)
				val response: HttpResponse[String] = Http(request.url).asString
				val data = parseResponse(response)
				if (checkIfDeadLock(data) && !first){
					println(currentTask.model)
					scraper ! Done(currentTask.model)
				}
				else {
					data.foreach({
						scraper ! _
					})
				}
				attempts = 2
			}
			catch {
				case e: Exception =>
					attempts -= 1
					println(e)
					println("sleeping for 5 sec before trying again")
					Thread.sleep(1000)
					println(attempts)
					if (attempts == 0){
						attempts = 2
						println(currentTask.model)
						scraper ! Done(currentTask.model)
					}
					else {
						self ! request
					}
			}
		case x: WorkRequest =>
			first = false
			currentTask = x
		case x: Any => 
			println("uncaught from request")
	}




	def checkIfDeadLock(data: List[Collection]): Boolean = {

		currentTask.model match {

			case x:Link =>
				if (data(2).models.isEmpty && data(1).models.length == 1) 
					return true 
				else if (data(1).models.isEmpty && data(2).models.isEmpty)
					return true
				else 
					return false	
			case _ =>
				false
		}
	}


	def parseResponse(response: HttpResponse[String]):List[Collection] = {
		import com.emdg.thegatherer.domain.SubRedditProtocol._
		import com.emdg.thegatherer.domain.LinkProtocol._
		import com.emdg.thegatherer.domain.CommentProtocol._
		val subreddits = ListBuffer[SubReddit]()
		val links = ListBuffer[Link]()
		val comments = ListBuffer[Comment]()

		val json = Json.parse(response.body)

	
		(json \\ "children") match {
			case elements @ (Seq(_)) =>
				elements.foreach((x: JsValue) => {

					x match {
						case JsArray(elements) =>
							elements.foreach((data) => {
								data \ "kind" match {
								case (JsString("t5")) =>
									subreddits += data.as[SubReddit]

								case (JsString("t3")) =>
									links += data.as[Link]
								case (JsString("t1")) =>
									comments += data.as[Comment]


								case y: Any =>
									println(y)
									println("data-type not implemented yet")
								}
							})
					}
				})
	

			case lb: ListBuffer[JsValue] =>
				lb.toList.foreach((x: JsValue) => {
					val data = x(0)
					data \ "kind" match {
						case (JsString("t1")) =>
							comments += (data \ "data").as[Comment]

						case JsString("more") =>
						
						case _:JsUndefined =>

						case _:Any =>
					}
				})
		}
		List(SubRedditBunch(subreddits.toList), LinkBunch(links.toList), CommentBunch(comments.toList))
	}

}
