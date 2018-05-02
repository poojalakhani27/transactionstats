# Restful APIs for transaction statistics
## Design approach
- In order to achieve constant time retrieval, the data should be pre-summarized in buckets of time frames. So that statistics retrieval is a constant operation of merge of  last 'N' bucket summaries.
- Since the store is in-memory, there should be an optimization to have *bounded store* for statistics. This helps a constant memory for storage of statistics.

## Architechture
- A variant of **LinkedHashMap** is implemented as **InMemoryKeyValueStore** so that the older keys are evicted to keep the size of the store bounded. 
	* **ConcurrentHashMap** is utilised for thread safe key-value lookup.
	* **PriorityQueue** is utilised as a "bounded buffer" to store keys so that the older keys are evicted on buffer full.
- To avoid race condition in concurrency, a **ReentranLock** is used for summarizing and storing the summary.
- The SummaryStore is a *Singleton scoped bean*.
- The timeframe granularity per bucket is configured as **properties**. Also the duration for which we want statistics (e.g. last 60 seconds)
- **Concurreny tests** are written to verify thread safety of the store.

## Future improvements
- Transaction creation should be event based notification for the summarizer. This means transaction creation and transacrition summarization should communicate by an ESB
