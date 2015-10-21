class TextProcessor(object):

    def next_alpha(self, text, start):
        i = start
        while i < len(text) and not text[i].isalpha(): i += 1
        return i

    def next_non_alpha(self, text, start):
        i = start
        while i < len(text) and text[i].isalpha(): i += 1
        return i

class TextChunker(TextProcessor):
    """
    Class for counting occurrences of unique words in a text document. 
    """

    def __init__(self):
        self.max_chunks=1 

    def split_text(self, text):

        text_size = len(text)
        chunk_size = text_size/self.max_chunks
        chunk_start = 0

        for chunk_id in range(1, self.max_chunks):
            chunk_end = self.next_non_alpha(text, chunk_start + chunk_size)
            if chunk_end >= text_size:
                yield text[chunk_start:text_size]
                return
            else:
                yield text[chunk_start:chunk_end]
                chunk_start = chunk_end

        yield text[chunk_start:text_size]

    def split_text_exact_chunks(self, text):
        chunk_index = 1
        for text_chunk in self.split_text(text):
            yield text_chunk
            chunk_index += 1
        while chunk_index <= self.max_chunks:
            yield ''
            chunk_index += 1

    def split_text_with_counts(self, text):
        for chunk_id, text_chunk in enumerate(self.split_text_exact_chunks(text)):
            yield chunk_id+1, max_chunks, text_chunk

class WordCounter(TextProcessor):

    def count_words(self, text):

        count = {}
        start = 0
        text_size = len(text)
    
        while (start < text_size):
    
            word_start = self.next_alpha(text, start)
            if (word_start >= text_size): break
    
            word_end = self.next_non_alpha(text, word_start)
            if (word_end == text_size): word_end = text_size - 1
    
            word = text[word_start:word_end].lower()
    
            if (count.has_key(word)):
                count[word] += 1
            else:
                count[word] = 1
    
            start = word_end

        return count

class CountAccumulator(TextProcessor):

    def __init__(self):
        self.merged_counts={} 
        self.packet_count = 0
        
    def accumulate_word_counts(self, word_count_tuple):

        chunk_id, chunk_count, word_counts = word_count_tuple

        for word, count in word_counts.iteritems():
            if (self.merged_counts.has_key(word)):
                self.merged_counts[word] += count
            else:
                self.merged_counts[word] = count
        
        self.packet_count += 1

        if self.packet_count == chunk_count:
            return self.merged_counts

if __name__ == '__main__':

    tc = TextChunker()

    text = "= _#Every _ good_ boy _ _ _ _ does _,@@# fine.."

    for max_chunks in range (1, 20):
        tc.max_chunks = max_chunks
        chunks = list(tc.split_text_with_counts(text))
        print chunks

    text = "= _#Every, every _ good, good, good, boy _ _ _ _ does _,@@# fine.."

    wc = WordCounter()
    counts =  wc.count_words(text)
    print counts

    ca = CountAccumulator()
    print ca.accumulate_word_counts((1,3,counts))
    print ca.accumulate_word_counts((2,3,counts))
    print ca.accumulate_word_counts((3,3,counts))