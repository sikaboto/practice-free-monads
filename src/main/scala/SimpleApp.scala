/* SimpleApp.scala */
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import com.databricks.spark.xml._
import java.nio.file.Paths
import org.apache.spark.sql
import org.apache.spark.sql.catalyst.encoders._
import org.apache.spark.sql.SaveMode

object SimpleApp {

    case class Instr(id: BigInt, issue_key: String, primary_name: String, issue_date: java.time.LocalDate, cusip9: String)

  def main(args: Array[String]) {
    // val logFile = "src/main/resources/README.md" // Should be some file on your system
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    import spark.implicits._

    def toInstrColumns(df: DataFrame) =
      df.select(
        $"_id",
        $"debt.muni_details.issue_key",
        $"master_information.instrument_master.primary_name",
        $"master_information.instrument_master.issue_date",
        $"master_information.instrument_xref.xref._VALUE"(0))
      .withColumnRenamed("master_information.instrument_xref.xref._VALUE AS `_VALUE`[0]", "cusip9")
      .withColumnRenamed("_id", "id")
      .where($"cusip9".isNotNull)


    val dfFromXml0922 = spark
      .read
      .option("rowTag", "instrument")
      .xml("/Users/cvenegas/Downloads/gsm_update_muni_APBNDLNK_GSMF00I.145.1.20210922T1000-04.xml")
    // 3 minutes to read xml, with each row as an instrument
    dfFromXml0922
      .write
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .save("parquets/gsm_update_muni_APBNDLNK_GSMF00I.145.1.20210922T1000-04.parquet")
    val ds0922Instrs = toInstrColumns(dfFromXml0922).as[Instr]
    ds0922Instrs.collect() // this is pretty expensive

    val dfFromXml0923 = spark
      .read
      .option("rowTag", "instrument")
      .xml("/Users/cvenegas/Downloads/gsm_update_muni_APBNDLNK_GSMF00I.146.1.20210923T1000-04.xml")

    dfFromXml0923
      .write
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .save("parquets/gsm_update_muni_APBNDLNK_GSMF00I.146.1.20210923T1000-04.parquet")
    val ds0923Instrs = toInstrColumns(dfFromXml0923).as[Instr]

    // compute the diffs started at 22 
    val diff09230922 = ds0922Instrs.unionAll(ds0923Instrs).except(ds0922Instrs.intersect(ds0923Instrs))
    diff09230922.write.format("parquet").save("parquets/09222021diff09232021")

    // this took 16:12:58 - 16:14:37 2 Minutes, and created 2M in total according to du -hs


    //16:26:14 - 16:28:06 to write raw gsm.parquet 2 Minutes, and created ~100MB file
    // these are just notes at this point, i did did everything in the shell

    // group by works!
    spark.stop()
  }
}
