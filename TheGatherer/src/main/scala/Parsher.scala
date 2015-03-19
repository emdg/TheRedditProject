import scalaj.http._

import play.api.libs.json._
import play.api.libs.functional.syntax._





object Parsher extends App {


	case class Comment(_id: String, body: String, subreddit_id: String, score: Int, parent_id: String)


	implicit val commentReads: Reads[Comment] = (
		(__ \ "id").read[String] and
		(__ \ "body").read[String] and
		(__ \ "subreddit_id").read[String] and
		(__ \ "score").read[Int] and 
		(__ \ "parent_id").read[String]
	)(Comment.apply _)

	val response: HttpResponse[String] = Http("http://www.reddit.com/r/funny/comments/2x10dh/I_guess_that's_my_email_address?.json?limit=1000").asString
	val json = Json.parse(response.body)
	val a = (json \\ "children")

	a.foreach((x) => {
		val a = (x(0) \ "data")

		println(a.asOpt[Comment])
		readLine("")
	})
}


