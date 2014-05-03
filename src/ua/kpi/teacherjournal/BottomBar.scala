package ua.kpi.teacherjournal

import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.view.{Gravity, ViewGroup, LayoutInflater}
import org.scaloid.common._
import scala.language.postfixOps
import android.app.FragmentManager
import RandData._
import ua.kpi.teacherjournal.Journal.HourMinute
import Word.correctForm

object BottomBar {
  def setup(fragmentManager: FragmentManager) = {
    fragmentManager.beginTransaction()
      .replace(R.id.bottom_bar, new BottomBar)
      .commit()
  }
}

class BottomBar extends RichFragment {
  def untilClassEndText(implicit ctx: Context) = timeUntilClassEnd match {
      case Some(HourMinute(hours, minutes)) =>
        import R.{string => RS}
        val hrs = if (hours == 0) ""
          else s"$hours ${correctForm(hours, RS.hour, RS.hours_paucal, RS.hours)} "
        val mins = if (minutes == 0) ""
          else minutes + " " + correctForm(minutes, RS.minute, RS.minutes_paucal, RS.minutes)
        R.string.until_class_end.r2String + ": " + hrs + mins
      case None => ""
    }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    require(ctx != null)
    new SRelativeLayout {
      backgroundColor = rgb(0x1D, 0x2E, 0x35)
      val untilClassEnd = STextView()
        .textSize(25 dip)
        .textColor(WHITE)
        .gravity(Gravity.CENTER_VERTICAL)
        .padding(0, 0, 25 dip, 0)
        .<<(WRAP_CONTENT, MATCH_PARENT)
        .alignParentRight
        .>>
      val lastSync = STextView(s"${R.string.last_sync.r2String}: $randomTime")
        .textSize(18 dip)
        .gravity(Gravity.CENTER_VERTICAL)
        .textColor(GRAY)
        .drawableLeft(R.drawable.ic_action_refresh)
        .compoundDrawablePadding(10 dip)
        .padding(25 dip, 0, 10 dip, 0)
        .<<(WRAP_CONTENT, MATCH_PARENT)
        .alignParentLeft
        .>>
      def updateTime(): Unit = {
        if (ctx != null) {
          untilClassEnd.text(untilClassEndText)
          postDelayed(updateTime(), 1000)
        }
      }
      updateTime()
    }
  }
}
