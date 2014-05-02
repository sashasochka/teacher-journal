package ua.kpi.teacherjournal

import android.graphics.Color._
import android.os.Bundle
import android.view.{Gravity, ViewGroup, LayoutInflater}
import org.scaloid.common._
import scala.language.postfixOps
import android.app.FragmentManager

object BottomBar {
  def setup(fragmentManager: FragmentManager) = {
    fragmentManager.beginTransaction()
      .replace(R.id.bottom_bar, new BottomBar)
      .commit()
  }
}

class BottomBar extends RichFragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    require(ctx != null)
    new SRelativeLayout {
      backgroundColor = rgb(0x1D, 0x2E, 0x35)
      val lastSync = STextView("Остання синхронізація: 11.10")
        .textSize(18 dip)
        .gravity(Gravity.CENTER_VERTICAL)
        .textColor(GRAY).padding(25 dip).<<.fill.>>

    }
  }
}
