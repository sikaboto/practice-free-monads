- Spark Applications are bundled in jars and are ran using `spark-submit`, but then how can we use that jar to query from? 

XML can be hosted on Kubernetes, MESOS or YARN clusters?
- Spark Application should connect to the XML
  - BL connects to the spark application to query for shit?

We can create a spark job that runs our ice debt job
  [basex] stream XML update file from FTP server into temporary IceGsmUpdate database
    - sparkContext.read()
  [basex] compute instrument diffs between IceGsm and IceGsmUpdate databases and write diffs to IceGsmUpdateLog
    - this might be a problem, let's look for a diff for xmls? probably intersect? at one point
    - transform the current RDD and the newly created RDD that's optimal for diffing, to create the diffs!
  [basex] merge IceGsm and IceGsmUpdate databases
    - 
  [mysql] get CUSIPs in IceCusipIssuers not yet in IceCusips <== Ceril's pull request
  [mysql] get CUSIPs already in IceCusips <== Sam's pull request
  [basex] query for instruments from steps d and e
    - using sparksql
  [cashflow API] make API requests for payments of new and updated CUSIPs
  [mysql] perform inserts/updates based on results from steps f and g

can't have fewer partitions than blocks? what does that mean?

- Inputs can be of Hadoop formats, but what are those formats?

- functions on an RDD[_] that return RDD[_] are lazily evaluated
  - map, for example, transforms RDDs
- functions on an RDD[_] that return some A, are eagerly evaluated
 - aggregate, gor example, returns the result of a computation throughout the RDD



useful article that compares rdds vs dataframes vs datasets
https://www.analyticsvidhya.com/blog/2020/11/what-is-the-difference-between-rdds-dataframes-and-datasets/


most important capability is persisting, in other words caching, a dataset to be readily available (across worker nodes ?)


potential pitfall/painpoint
- while it's nice that RDDs are fault tolerant, it seems like a black/magic box kinda behaviour when it fails.. but who wants to handle any faults?
- how often will we see a cluster fail?


RDD[<some_case_class>] ==> DataSet

we can use HIVE as a datastore? what is HIVE? pass on this one maybe

- get xml DONE
- SparkJob to transform the data and create a new DF ==> parquet DONE

- how long will the job take to stream from an EC2 instance?
- how long will it take to write parquet and read from parquet? DONE

lower hanging fruits:
- what's going on with instrument_id = 5618602, seems to have no instrument_xrefs.xrefs
- can we map values? Only with RDD, gotta go from DataFrame ==> RDD

TODO:
- DataFrame supposedly has less compile time guarantees, how can we transform to a dataset instead?
  - DataFrame is DataSet[Row]
- make a ReadMe
- learn to deploy note any changes in performance
- Try persist()? 
- what type of queries do we want to run that we have in basex?
- use spark sql the right way before showing a POC to the team (maybe by using RDDs so that we can see joins and shit easier)
- why doesn't the schema work?
- how will spark work with AWS?
- how to use StructType that we learned from scala?
- how well do joins perform? do we even need joins?
- use org.joda.time instead of java.time
  - use java.time.Encoder then transform
- integrate into bl codebase