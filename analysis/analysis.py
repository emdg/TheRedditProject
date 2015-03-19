import couchdb
from nltk.stem.snowball import SnowballStemmer
import couchdb
import re
import codecs

couch = couchdb.Server("http://localhost:5984")
stemmer = SnowballStemmer("english")



def getLinks():
	db = couch["link"]

	map_fun = '''
		function(doc){
			if (doc.title)
				emit(doc)
		}
	'''

	return db.query(map_fun)


def getOccurrencesOfAuthors(db_name, threshold):
	db = couch[db_name]
	map_fun = '''function(doc) {
			if (doc.author)
				emit(doc.author)
			}'''

	return db.query(map_fun, reduce_fun = "_count", group=True)

def saveComment(comment):
	db = couch["comment"]
	db.save(comment.key)
	

def getComments():
	db = couch["comment"]
	map_fun = '''
		function(doc){
			if (doc.body)
				emit(doc)
		}'''
	return db.query(map_fun)

def getCommentWithId(string):
	db = couch["comment"]
	return db.get(string)



def getGoodComments():
	db = couch["goodcomments"]
	map_fun = '''
		function(doc){
			if (doc.body)
				emit(doc)
		}'''
	return db.query(map_fun)



def writeLinkTitlesToFile(file_name):
	f = open(file_name, 'w')
	links = getLinksWithTitles()
	for link in links:
		f.write(link.key["stemmed"] + "\n")


def writeCommentBodiesToFile(file_name):
	f = codecs.open(file_name, 'w+', 'utf-8')
	comments = getComments()
	for comment in comments:
		f.write(comment.key["stemmed"] + "\n")


def writeGoodCommentBodiesToFile(file_name):
	f = codecs.open(file_name, 'w+', 'utf-8')
	comments = getGoodComments()
	for comment in comments:
		f.write(comment.key["stemmed"] + "\n")

def stemSentence(text):
	return " ".join(map(lambda word: stemmer.stem(word), text.split(" ")))



def stemLinks():
	db = couch["link"]
	map_fun = '''function(doc){
	if (doc.title)
		emit(doc)
	}'''
	results = db.query(map_fun)
	for row in results:
		row.key["stemmed"] = stemSentence(row.key["title"])
		db.save(row.key)


def saveComment(comment):
	db = couch["comment"]
	db.save(comment.key)
	

def stemComments():
	db = couch["comment"]
	map_fun = '''function(doc){
	if (doc.body)
		emit(doc)
	}'''
	results = db.query(map_fun)
	i = 0
	for row in results:
		row.key["stemmed"] = stemSentence(row.key["body"])
		db.save(row.key)
		i+= 1

#stemComments()
#writeCommentBodiesToFile("data/raw/comment_texts")







