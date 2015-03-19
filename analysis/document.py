
from analysis import getLinks, getComments
import codecs
from os import listdir, remove
from lda import topicquery,similarityQuery
import couchdb
import json


def createDocs():
	i = 0
	for comment in getComments():
		link_id = comment.key["link_id"]
		if (len(comment.key["key_phrases"]) > 0):
			f = codecs.open("data/raw/documents/document_"+str(link_id), 'a', 'utf-8')
			f.write("%s\n" % " ".join(comment.key["key_phrases"]))
		print i
		i += 1


def removeDocumentsWithFewWords(threshold):
	path = "data/raw/documents/"
	for filename in listdir(path):
		f = open(path + filename)
		words = f.read().replace('\n', '').split(" ")
		if (len(words) < threshold):
			print filename
			remove(path + filename)


def addTopicDistribution():
	couch = couchdb.Server("http://localhost:5984")
	path = "data/raw/documents"
	db = couch["link"]
	endDb = couch["ns202links"]
	i = 0

	for f in listdir(path):
		link_id = f.split("_")[2]
		link = db.get(link_id)
		document = open("data/raw/documents/" + f).read().replace('\n', '')
		link["topic_dist"] = dict(topicquery(document))
		endDb.save(link)
		print i
		i+=1


def addEdges():
	couch = couchdb.Server("http://localhost:5984")
	path = "data/raw/documents"
	db = couch["ns202edges"]
	file_names = listdir(path)
	j = 0
	links = []
	for f in file_names:
		link_from = f.split("_")[2]
		document = open("data/raw/documents/" + f).read().replace('\n', '')
		sim = similarityQuery(document)
		for i in range(0, len(sim)):
			if (sim[i] > 0.70):
				dictionary = {}
				dictionary["from"] = link_from
				dictionary["to"] = file_names[i].split("_")[2]
				if ((dictionary["to"], dictionary["from"]) in links):
					continue
				if (dictionary["to"] == dictionary["from"]):
					continue
				dictionary["weight"] = float(sim[i])
				links.append((dictionary["from"], dictionary["to"]))
				db.save(dictionary)
		print j
		j += 1

def writeEdgesTXT():
	couch = couchdb.Server("http://localhost:5984")
	def createEdgeString(edge):
		return edge["to"] + " " + edge["from"] + " " + str(edge["weight"]) + "\n"
	f = open("data/dataset/edges.txt", 'w+')
	db = couch["ns202edges"]
	for edge_id in db:
		edge = db.get(edge_id)
		f.write(createEdgeString(edge))

def writeNodesTXT():
	couch = couchdb.Server("http://localhost:5984")
	def createEdgeString(node):
		return node["_id"] + " " + str(node["score"]) + " 10\n"
	f = open("data/dataset/nodes.txt", 'w+')
	db = couch["ns202links"]
	for link_id in db:
		link = db.get(link_id)
		f.write(createEdgeString(link))


def writeTopicEdgesToTXT():
	couch = couchdb.Server("http://localhost:5984")
	db = couch["ns202links"]
	f = open("data/dataset/edges.txt", 'w+')
	def createEdgeString(topic_num, sim, link_id):
		return "topic" + str(topic_num) + " " + link_id + " " + str(sim) + "\n"
	for link_id in db:
		link = db.get(link_id)
		topic_dist = link["topic_dist"]
		for key in topic_dist.keys():
			f.write(createEdgeString(key, topic_dist[key], link_id))


def writeTopicNodesToTXT(topics = 40):
	f = open("data/dataset/nodes.txt", 'a')
	for i in range(0, topics):
		string = "topic" + str(i) + " 300 500 \n"
		f.write(string)

writeNodesTXT()
writeTopicNodesToTXT()
writeTopicEdgesToTXT()




