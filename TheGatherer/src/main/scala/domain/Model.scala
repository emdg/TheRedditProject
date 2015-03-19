package com.emdg.thegatherer.domain
trait Model {
	val _id: String
}


trait Collection {
	val models: List[Model]
}