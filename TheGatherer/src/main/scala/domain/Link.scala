package com.emdg.thegatherer.domain

import play.api.libs.json._
import play.api.libs.functional.syntax._




case class Link(
		_id: String, 
		title: String, 
		subreddit: String, 
		author: String, 
		num_comments: Int,
		score: Int,
		created: Int,
		_rev: Option[String]
	) extends Model


case class LinkBunch(models: List[Link]) extends Collection




object LinkProtocol{

	implicit val LinkReads: Reads[Link] = (
			(__ \ "data" \ "id").read[String] and
			(__ \ "data" \ "title").read[String] and
			(__ \ "data" \ "subreddit").read[String] and
			(__ \ "data" \ "author").read[String] and
			(__ \ "data" \ "num_comments").read[Int] and
			(__ \ "data" \ "score").read[Int] and
			(__ \ "data" \ "created").read[Int] and
			(__ \ "data" \ "rev").readNullable[String] 
		)(Link.apply _)

}