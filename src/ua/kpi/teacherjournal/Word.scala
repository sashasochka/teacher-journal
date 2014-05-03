package ua.kpi.teacherjournal
import org.scaloid.common._
import android.content.Context

object Word {
  def chooseCorrectForm(count: Int, singular: String, paucal: String, plural: String): String = count match {
    case 0 => ""
    case _ if count % 10 == 1 => singular
    case _ if 1.to(4).contains(count % 10) && count / 10 % 10 != 1 => paucal
    case _ => plural
  }

  def correctForm(count: Int, singular: Int, paucal: Int, plural: Int)(implicit context: Context): String =
    chooseCorrectForm(count, singular.r2String, paucal.r2String, plural.r2String)
}
