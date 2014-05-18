package ua.kpi.teacherjournal

import android.app._
import android.content.Context
import android.graphics.Color._
import android.os.Bundle
import android.text.format.DateUtils
import android.view._
import org.scaloid.common._
import RandData._
import scala.language.postfixOps
import ua.kpi.teacherjournal.Journal.HourMinute

object BottomBar {
  def setup(fragmentManager: FragmentManager) = {
    fragmentManager.beginTransaction()
      .replace(R.id.bottom_bar, new BottomBar)
      .commit()
  }
}

class BottomBar extends Fragment with RichFragment {
  def untilClassEndText(implicit ctx: Context) = timeUntilClassEnd match {
      case Some(HourMinute(hours, minutes)) =>
        R.string.until_class_end.r2String + " %01d:%02d".format(hours, minutes)
      case None => ""
    }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    assert(ctx != null)
    new SRelativeLayout {
      backgroundColor = rgb(0x1D, 0x2E, 0x35)
      val rightBlock = new SVerticalLayout {
        padding(0, 0, 25 dip, 0)
        gravity = Gravity.CENTER_VERTICAL
        val untilClassEnd = STextView()
          .textSize(18 dip)
          .padding(0, 0, 0, 5 dip)
          .textColor(WHITE)
          .gravity(Gravity.CENTER_VERTICAL)


        this += new SLinearLayout {
          val progressBarHeight = 3 dip
          val positive = STextView()
            .backgroundColor(rgb(0x78, 0x9c, 0x24)).<<(FILL_PARENT, progressBarHeight).Weight(1.0f).>>
          val negative = STextView()
            .backgroundColor(rgb(0xd1, 0x3d, 0x3d)).<<(FILL_PARENT, progressBarHeight).Weight(1.0f).>>

          def updateView(): Unit = {
              if (ctx != null) {
                untilClassEnd.text = untilClassEndText

                timeUntilClassEnd match {
                  case Some(HourMinute(h, m)) =>
                    positive.visibility(View.VISIBLE)
                    negative.visibility(View.VISIBLE)
                    val q = (h * 60 + m) / 95.0f
                    positive.<<(FILL_PARENT, progressBarHeight).Weight(q)
                    negative.<<(FILL_PARENT, progressBarHeight).Weight(1.0f - q)
                  case None =>
                    positive.visibility(View.INVISIBLE)
                    negative.visibility(View.INVISIBLE)
                }
                postDelayed(updateView(),
                  DateUtils.MINUTE_IN_MILLIS - System.currentTimeMillis % DateUtils.MINUTE_IN_MILLIS)
              }
          }
          updateView()
        }
      }
      this += rightBlock
        .<<(WRAP_CONTENT, FILL_PARENT)
        .alignParentRight
        .>>

      val lastSync = STextView(s"${R.string.last_sync.r2String}: $randomTime")
        .textSize(14 dip)
        .gravity(Gravity.CENTER_VERTICAL)
        .textColor(GRAY)
        .drawableLeft(R.drawable.ic_action_refresh)
        .compoundDrawablePadding(10 dip)
        .padding(25 dip, 0, 10 dip, 0)
        .<<(WRAP_CONTENT, MATCH_PARENT)
        .alignParentLeft
        .>>
    }
  }
}
