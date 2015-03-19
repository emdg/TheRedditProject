package com.emdg.thegatherer.domain
import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Comment(_id: String, 
	body: String, 
	subreddit_id: String, 
	score: Int, 
	author: String,
	parent_id: String,
	link_id: String, 
	_rev: Option[String]) extends Model


case class CommentBunch(models: List[Comment]) extends Collection

object CommentProtocol {
	implicit val commentReads: Reads[Comment] = (
		(__ \ "id").read[String] and
		(__ \ "body").read[String] and
		(__ \ "subreddit_id").read[String] and
		(__ \ "score").read[Int] and 
		(__ \ "author").read[String] and
		(__ \ "parent_id").read[String] and
		(__ \ "link_id").read[String] and
		(__ \ "rev").readNullable[String]
	)(Comment.apply _)
}