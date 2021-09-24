/* SimpleApp.scala */
import org.apache.spark.sql.SparkSession

object SimpleApp {
  def main(args: Array[String]) {
    // val logFile = "src/main/resources/README.md" // Should be some file on your system
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    // val logData = spark.read.textFile(logFile).cache()
    // val numAs = logData.filter(line => line.contains("a")).count()
    // val numBs = logData.filter(line => line.contains("b")).count()
    // println(s"Lines with a: $numAs, Lines with b: $numBs")
    // spark.stop()
    // import com.databricks.spark.xml.util.XSDToSchema
    import com.databricks.spark.xml._
    import java.nio.file.Paths

    // val schema = XSDToSchema.read(Paths.get("/Users/cvenegas/Downloads/security_master_47.xsd"))
    val df = spark.read.option("rowTag", "instrument").xml("/Users/cvenegas/Downloads/gsm_update_muni_APBNDLNK_GSMF00I.145.1.20210922T1000-04.xml")
    // 4:22 -> 4:25, 3 minutes to read xml, with each row as an instrument
    df.createOrReplaceTempView("instruments")
    spark.sql("SELECT _id, master_information.instrument_xref.xref._VALUE[0] FROM instruments WHERE master_information.instrument_xref.xref._VALUE IS NOT NULL ").show()
    val dfDecades = spark.sql("SELECT _id, debt.muni_details.issue_key, master_information.instrument_master.primary_name, master_information.instrument_master.issue_date, master_information.instrument_xref.xref._VALUE[0] FROM instruments WHERE master_information.instrument_xref.xref._VALUE IS NOT NULL AND master_information.instrument_master.issue_date BETWEEN '2010-01-01' AND '2020-01-01' ").show()


    spark.sql("SELECT _id, debt.muni_details.issue_key, master_information.instrument_master.primary_name, master_information.instrument_master.issue_date, master_information.instrument_xref.xref._VALUE[0] FROM instruments WHERE master_information.instrument_xref.xref._VALUE IS NOT NULL AND master_information.instrument_master.issue_date BETWEEN '2010-01-01' AND '2020-01-01' ")
    .withColumnRenamed("master_information.instrument_xref.xref._VALUE AS `_VALUE`[0]", "cusip9").write.format("parquet").save("issued2010Decade.parquet")
    // this took 16:12:58 - 16:14:37 2 Minutes, and created 2M in total according to du -hs

    df.write.format("parquet").save("gsm_update_muni_APBNDLNK_GSMF00I.145.1.20210922T1000-04.parquet")
    //16:26:14 - 16:28:06 to write raw gsm.parquet 2 Minutes, and created ~100MB file
    // these are just notes at this point, i did did everything in the shell


    val gsmDF = spark.read.format("parquet").load("gsm_update_muni_APBNDLNK_GSMF00I.145.1.20210922T1000-04.parquet")

    // group by works!
    spark.sql("SELECT first(_id), count(*), debt.fixed_income.interest_payment_frequency FROM instruments group by debt.fixed_income.interest_payment_frequency").show()
    spark.stop()
  }
}