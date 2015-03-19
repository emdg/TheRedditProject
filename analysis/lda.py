from gensim import corpora, models, similarities
from stop_words import get_stop_words
from gensim import corpora, models, similarities
from analysis import getLinks
from os import listdir
import numpy
class LDADictionary(object):
	def __init__(self, file_paths):
		self.file_paths = file_paths
		self.dictionary = None
		self.corpus = None

	def create(self, file_path):

		documents = []
		for f in listdir("data/raw/documents"):
			string = open("data/raw/documents/" + f).read().replace('\n', '')
			documents.append(string)	
		print "reading complete"
		# remove common words and tokenize
		#stoplist = set(get_stop_words('english'))
		texts = [[word for word in document.lower().split()]
		         for document in documents]

		# remove words that appear only once
		all_tokens = sum(texts, [])
		tokens_once = set(word for word in set(all_tokens) if all_tokens.count(word) == 1)
		texts = [[word for word in text if word not in tokens_once] for text in texts]
		dic = corpora.Dictionary(texts)
		corpus = [dic.doc2bow(text) for text in texts]
		corpora.MmCorpus.serialize('data/ldastuff/corpus.mm', corpus)
		dic.save("data/ldastuff/dictionary.dict")

		self.dictionary = dic
		self.corpus = corpus



	def load(self, file_path):
		dictionary = corpora.Dictionary.load('data/ldastuff/dictionary.dict')
		self.corpus = corpora.mmcorpus.MmCorpus('data/ldastuff/corpus.mm')
		self.dictionary = dictionary



class LDAModel(object):
	def __init__(self):
		self.model = None


	def create(self, file_path, LDA_dictionary, clusters = 40, passes = 2):
		lda = models.ldamodel.LdaModel(corpus=LDA_dictionary.corpus, id2word=LDA_dictionary.dictionary, num_topics=clusters, \
                               update_every=1, chunksize=10000, passes=passes, alpha = 'auto')
		self.model = lda
		raw_input("done")
		lda.save(file_path)

	
	def print_topics(self):
		print self.model.num_topics
		for i in range(0, self.model.num_topics-1):
			print self.model.print_topic(i)

	def load(self, file_path):
		self.model = models.LdaModel.load(file_path)


def train(dictionary = None):
	if (dictionary == None):
		dictionary = LDADictionary([""])
		dictionary.create("data/ldastuff/dictionary.dict")

	lda = LDAModel()
	lda.create("data/ldastuff/lda.model", dictionary)
	lda.print_topics()
	print "done training"


def createIndex():
	dictionary = corpora.Dictionary.load('data/ldastuff/dictionary.dict')
	corpus = corpora.MmCorpus("data/ldastuff/corpus.mm")
	lda = models.LdaModel.load("data/ldastuff/lda.model") 
	index = similarities.MatrixSimilarity(lda[corpus])
	index.save("data/ldastuff/simIndex.index")



def topicquery(document): 
	dictionary = LDADictionary("")
	dictionary.load("")
	lda = LDAModel()
	lda.load("data/ldastuff/lda.model")
	lda = lda.model
	query = dictionary.dictionary.doc2bow(document.split(" "))
	return lda[query]

def similarityQuery(document):
	index = similarities.MatrixSimilarity.load("data/ldastuff/simIndex.index")
	lda_vec = topicquery(document)
	return index[lda_vec]





# #createIndex()


# # dictionary = dictionary.dictionary

# #train(dictionary)
if __name__ == "__main__":
	#dictionary = LDADictionary("")
#dictionary.create("")
	#dictionary.load("")
	#train(dictionary)
	#createIndex()

	#doc = "Some guy named Mike leaves voice mails on my work phone. I don't know how he got my number. Here's one where he tells me of his big plans. NSFWX"
	#dist = similarityQuery(doc)
	#print numpy.argmax(dist)
	lda = LDAModel()
	lda.load("data/ldastuff/lda.model") 
	print lda.print_topics()



# lda = lda.model
# query = dictionary.dictionary.doc2bow("this simpl bitch...".split(" "))

# a  = sorted(lda[query], key= lambda x: x[1])
# print lda.print_topic(a[-1][0])
# print a







