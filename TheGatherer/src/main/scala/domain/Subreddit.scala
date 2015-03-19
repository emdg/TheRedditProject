package com.emdg.thegatherer.domain

import play.api.libs.json._
import play.api.libs.functional.syntax._




case class SubReddit(_id: String, name: String, url: String, _rev: Option[String]) extends Model
case class SubRedditBunch(models: List[SubReddit]) extends Collection




object SubRedditProtocol{

	implicit val subredditReads: Reads[SubReddit] = (
			(__ \ "data" \ "id").read[String] and
			(__ \ "data" \ "display_name").read[String] and
			(__ \ "data" \ "url").read[String] and
			(__ \ "data" \ "rev").readNullable[String] 
		)(SubReddit.apply _)

}